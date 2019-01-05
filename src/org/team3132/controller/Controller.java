package org.team3132.controller;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BooleanSupplier;

import org.strongback.components.Clock;
import org.team3132.Constants;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.DashboardUpdater;
import org.team3132.interfaces.IntakeInterface.IntakeConfiguration;
import org.team3132.interfaces.Log;
import org.team3132.lib.Position;
import org.team3132.subsystems.Subsystems;

import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;

/**
 * The controller of State Sequences while ensuring the robot is safe at every step.
 * 
 * Allows higher level code to specify just the states that the robot needs
 * to pass through but it doesn't need to care how it gets there - this code
 * will ensure it gets there safely.
 * 
 * This is very similar to commands, with the differences to a command-based
 * approach being:
 *  - Unlike commands, the activation logic is concentrated in one place, making
 *    it much safer to add new functionality.
 *  - Every state doesn't need to be aware of every other state (much simpler).
 *  - Creating strings of sequences is much simpler and shorter than commands.
 *  - Arbitrary combinations of parallel and sequential commands aren't supported,
 *    only a series of parallel operations.
 * 
 * This could be made faster, but we need to be careful it doesn't make it unsafe.
 */
public class Controller implements Runnable, DashboardUpdater {
	private final Subsystems subsystems;
	private final TrajectoryGenerator trajGenerator;
	private final Clock clock;
	private final DashboardInterface dashboard;
	private final Log log;
	private Sequence sequence = new Sequence("idle");  // Current sequence we are working through.
	private boolean sequenceHasChanged = true;
	private boolean sequenceHasFinished = false;
	private final double[] liftSetpoints;

	/**
	 * The Pathfinder library can't be run on x86 without recompiling, which makes it
	 * hard to unit test. Instead it's abstracted out.
	 */
	public interface TrajectoryGenerator {
		Trajectory[] generate(Waypoint[] waypoints);
	}

	static final TrajectoryGenerator NULL_GENERATOR = (Waypoint[] waypoint) -> new Trajectory[] {new Trajectory(0), new Trajectory(0)};
	public Controller(Subsystems subsystems, double[] liftSetpoints) {
		// If not TrajectoryGenerator supplied, use one that creates two empty trajectories.
		this(subsystems, liftSetpoints, NULL_GENERATOR);
	}

	public Controller(Subsystems subsystems, double[] liftSetpoints, TrajectoryGenerator generator) {
		Arrays.sort(liftSetpoints);
		this.subsystems = subsystems;
		this.liftSetpoints = liftSetpoints;
		this.trajGenerator = generator;
		this.clock = subsystems.clock;
		this.dashboard = subsystems.dashboard;
		this.log = subsystems.log;
		(new Thread(this)).start();
	}
	
	synchronized public void doSequence(Sequence sequence) {
		if (this.sequence == sequence) {
			// Exactly the same same sequence. Only start it again if it has
			// finished. Used in the whileTriggered(...) case.
			// Intentionally using == instead of .equalTo().
			if (!sequenceHasFinished) return;
		}
		this.sequence = sequence;
		sequenceHasChanged = true;
		logSub("Sequence has changed to %s sequence", sequence.getName());
		notifyAll();  // Tell the run() method that there is a new sequence.
	}

	/**
	 * Main entry point which applies state.
	 * 
	 * Usually run in a thread.
	 */
	@Override
	public void run() {
		try {
			Iterator<State> iterator = null;
			while (true) {
				State desiredState = null;
				synchronized (this) {
					if (sequenceHasChanged || iterator == null) {
						logSub("State sequence has changed, now executing %s sequence", sequence.getName());
						iterator = sequence.iterator();
						sequenceHasChanged = false;
						sequenceHasFinished = false;
					}
					if (!iterator.hasNext()) {
						logSub("Sequence %s is complete", sequence.getName());
						sequenceHasFinished = true;
						try {
							logSub("Controller waiting for a new sequence to run");
							wait();
							// logSub("Have a new sequence to run");
						} catch (InterruptedException e) {
							logSub("Waiting interrupted %s", e);
						}
						continue; // Restart from the beginning.
					}
					desiredState = iterator.next();
				}
				applyState(desiredState);
			}
		} catch (Exception e) {
			// The controller is dying, write the exception to the logs.
			log.exception("Controller caught an unhandled exception", e);
		}
	}
	
	/**
	 * Does the simple, dumb and most importantly, safe thing.
	 * 
	 * See the design doc before changing this.
	 * 
	 * Steps through:
	 *  - Wait for all subsystems to finish moving.
     *  - If necessary, deploy the ramp.
     *  - If the lift needs to move, move the intake to a safe configuration,
     *    possibly the target configuration if it's safe.
	 *  - Move the lift to it's final position, where that position isn't half
	 *    way through the intake or where the hooks will hit it if the hooks are
	 *    deployed or will be deployed.
     *  - Deploy the hooks as the lift is now out of the way.
	 *  - If still necessary, move the intake.
	 *  - Open or close the outtake if necessary.
	 *  
	 * Note if the step asks for something which will cause harm to the robot, the
	 * request will be ignored. For example if the lift was moved into a position
	 * the intake could hit it and then the intake was moved into the lift, the
	 * intake move would be ignored.
	 *  
	 * @param desiredState The state to leave the robot in.
	 */
	private void applyState(State desiredState) {
		logSub("Applying requested state: %s", desiredState);
		//logSub("Waiting subsystems to finish moving before applying state");
		waitForLift();
		waitForIntake();
		waitForOuttake();
		
		// Get the current state of the subsystems.
		State currentState = calculateState(subsystems);
		logSub("Current state: %s", currentState);
		// Fill in the blanks in the desired state.
		desiredState = updateDesiredState(desiredState, currentState, clock);
		logSub("Calculated new 'safe' state: %s", desiredState);

		// Calculate which subsystems need an update.
		boolean needsLiftUpdate = calcNeedsUpdate("lift", currentState.liftHeight, desiredState.liftHeight, Constants.LIFT_DEFAULT_TOLERANCE);
		boolean needsIntakeUpdate = calcNeedsUpdate("intakeConfig", currentState.intakeConfig, desiredState.intakeConfig);
		calcNeedsUpdate("intakeMotor", currentState.intakeMotorOutput, desiredState.intakeMotorOutput);
		calcNeedsUpdate("outtakeOpen", currentState.outtakeOpen, desiredState.outtakeOpen);
		boolean needsRampUpdate = calcNeedsUpdate("rampDeployed", currentState.rampRelease, desiredState.rampRelease);
		boolean needsGearChange = calcNeedsUpdate("lowGear", currentState.lowGear, desiredState.lowGear);
		
		maybeResetPosition(desiredState.resetPosition, subsystems);
		
		// Start driving if there are waypoints.
		AutoDriver driver = new AutoDriver(desiredState.waypoints, desiredState.forward, desiredState.relative_waypoints, trajGenerator, subsystems);
		driver.start();

		// Update the subsystems that don't intersect with others.
		if (needsRampUpdate) {
			if (desiredState.rampRelease) {
				subsystems.endgame.deployRamp(); // Can only deploy the ramp.
			} else {
				subsystems.endgame.retractRamp();
			}
		}
		// Always set the output motor output power.
		subsystems.outtake.setOuttakeMotorOutput(desiredState.outtakeMotorOutput);
		// If the lift needs to move, ensure that the intake is in a safe configuration.
		if (needsLiftUpdate) {
			if (!isSafeIntakeConfiguration(currentState.intakeConfig)) {
				logSub("Lift needs to move, moving intake to a safe configuration");
				IntakeConfiguration safeIntakeConfig = IntakeConfiguration.STOWED;
				if (isSafeIntakeConfiguration(desiredState.intakeConfig)) {
					// Use the desired intake config if it's a safe one.
					safeIntakeConfig = desiredState.intakeConfig;
				}
				assert(isSafeIntakeConfiguration(safeIntakeConfig));
				subsystems.intake.setConfiguration(safeIntakeConfig);
				waitForIntake();
			}
		}
		// Move the lift to it's final (possibly adjusted) position now that the
		// intake is out of the way.
		//logSub("Moving lift");
		subsystems.lift.setHeight(desiredState.liftHeight);
		maybeWaitForLift();  // This be aborted, so the intake needs to be wary below.
		// Do the next steps in parallel as they don't mechanically conflict with each other.
		if (desiredState.outtakeOpen) {
			subsystems.outtake.openOuttake();
		} else {
			subsystems.outtake.closeOuttake();
		}

		// Set the intake to the final configuration, but only if the lift.
		// is in a intake-friendly position.
		// Note the intake may already be in this config.
		if (needsIntakeUpdate) {
			if (isSafeToMoveIntake(subsystems.lift.getHeight())) {
				subsystems.intake.setConfiguration(desiredState.intakeConfig);
				waitForIntake();
			} else {
				logErr("Would move intake, but not safe to do so, fix the sequence!");
			}
		}
		waitForOuttake();

		// Have we been asked to change gear and is it safe to do?
		// It's only safe to change at the very top of the lift.
		if (needsGearChange) {
			if (subsystems.lift.isSafeToShift()) {
				if (desiredState.lowGear) {
					subsystems.lift.setLowGear();
				} else {
					subsystems.lift.setHighGear();
				}
			} else {
				logSub("Would change gear but not safe to");
			}
		}
		subsystems.intake.setIntakeMotorOutput(desiredState.intakeMotorOutput);

		// Wait for driving to finish if needed.
		waitUntil(() -> driver.isFinished(), "auto driving");
		
		// Last thing: wait for the delay time if it's set.
		waitForTime(desiredState.delayUntilTime);
	}

	private static State calculateState(Subsystems subsystems) {
		State state = new State();
		// Populate the current state.
		state.setLiftHeight(subsystems.lift.getHeight());
		state.setIntakeConfig(subsystems.intake.getConfiguration());
		state.setIntakeMotorOutput(subsystems.intake.getIntakeMotorOutput());
		state.setOuttakeOpen(!subsystems.outtake.isClosed());  // Note there are three states for the outtake, open, closed, and moving.
		state.setOuttakeMotorOutput(subsystems.outtake.getOuttakeMotorOutput());
		state.setRampRelease(subsystems.endgame.isRampExtended());
		state.setLowGear(subsystems.lift.isInLowGear());
		return state;
	}
	
	private static <T> T getNotNullValue(T a, T default_value) {
		if (a == null) return default_value;
		return a;
	}
	
	/**
	 * Fill in the nulls in a new desired state with the current values.
	 * 
	 * Makes is slightly easier to work with because all values have been set.
	 * Note that it doesn't modify desiredState because then the new values would stick
	 * in the sequence.
	 */
	private State updateDesiredState(State desiredState, State currentState, Clock clock) {
		State updatedState = new State();
		// Don't set both liftHeight and liftHeightDelta.
		assert(desiredState.delayUntilTime == null || desiredState.delayDeltaSec == null);
		updatedState.setDelayUntilTime(getNotNullValue(desiredState.delayUntilTime,
				clock.currentTime() + getNotNullValue(desiredState.delayDeltaSec, 0.0)));
		// Only set one of liftHeight, liftHeightDelta, liftStep.
		assert(desiredState.liftHeight == null || desiredState.liftHeightDelta == null);
		assert(desiredState.liftHeightDelta == null || desiredState.liftSetpointAction == null);
		assert(desiredState.liftHeight == null || desiredState.liftSetpointAction == null);
		if (desiredState.liftSetpointAction != null) {
			updatedState.setLiftHeight(getNextLiftSetpoint(currentState.liftHeight, desiredState.liftSetpointAction));
		} else {
			updatedState.setLiftHeight(getNotNullValue(desiredState.liftHeight,
					currentState.liftHeight + getNotNullValue(desiredState.liftHeightDelta, 0.0)));
		}
		updatedState.setIntakeConfig(getNotNullValue(desiredState.intakeConfig, currentState.intakeConfig));
		updatedState.setIntakeMotorOutput(getNotNullValue(desiredState.intakeMotorOutput, currentState.intakeMotorOutput));
		updatedState.setOuttakeOpen(getNotNullValue(desiredState.outtakeOpen, currentState.outtakeOpen));
		updatedState.setOuttakeMotorOutput(getNotNullValue(desiredState.outtakeMotorOutput, currentState.outtakeMotorOutput));
		updatedState.setRampRelease(getNotNullValue(desiredState.rampRelease, currentState.rampRelease));
		updatedState.setLowGear(getNotNullValue(desiredState.lowGear, currentState.lowGear));

		// Sanity check that the lift heights used are strictly increasing.
		assert (Constants.LIFT_INTAKE_HEIGHT < Constants.LIFT_DEPLOY_THRESHOLD_HEIGHT);
		assert (Constants.LIFT_DEPLOY_THRESHOLD_HEIGHT < Constants.LIFT_DEPLOY_HOOKS_HEIGHT);
		// Ensure the lift doesn't end up in an unsafe position for the intake.
		if (updatedState.liftHeight <= Constants.LIFT_INTAKE_HEIGHT + Constants.LIFT_DEFAULT_TOLERANCE) {
			updatedState.liftHeight = Constants.LIFT_INTAKE_HEIGHT;
		} else {
			// Otherwise push the lift up out of the intake zone.
			updatedState.liftHeight = Math.max(updatedState.liftHeight, Constants.LIFT_DEPLOY_THRESHOLD_HEIGHT);
		}
		updatedState.waypoints = desiredState.waypoints;
		updatedState.forward = desiredState.forward;
		updatedState.relative_waypoints = desiredState.relative_waypoints;
		return updatedState;
	}

	/**
	 * If not null, reset the current location in the Location subsystem to be position.
	 * Useful when starting autonomous.
	 * @param position
	 * @param subsystems
	 */
	private void maybeResetPosition(Waypoint position, Subsystems subsystems) {
		if (position == null) return;
		subsystems.location.setCurrentLocation(new Position(position.x, position.y, position.angle));
	}

	/**
	 * Jump between pre-configured lift setpoints.
	 * Depending on the current height and the requested action, move to one of the other
	 * setpoints.
	 * @param currentHeight The current lift height.
	 * @param action What direction to move.
	 * @return New target height for the lift.
	 */
	private double getNextLiftSetpoint(double currentHeight, State.LiftSetpointAction action) {
		// First see if it's on an existing setpoint.
		double lastSetpoint = -1;
		final int numSetpoints = liftSetpoints.length;
		for (int i = 0; i < numSetpoints; i++) {
			final double setpoint = liftSetpoints[i];
			assert(lastSetpoint < setpoint);  // Check setpoints only increase.
			lastSetpoint = setpoint;
			if (Math.abs(setpoint - currentHeight) > Constants.LIFT_DEFAULT_TOLERANCE) continue;
			// It's close to this setpoint.
			switch (action) {
			default:
				// Stay here.
				return liftSetpoints[i];
			case NEXT_UP:
				return liftSetpoints[Math.min(i + 1, numSetpoints - 1)];
			case NEXT_DOWN:
				return liftSetpoints[Math.max(0, i - 1)];
			}
		}
		// Must be between setpoints. Figure out which.
		for (int i = 0; i < numSetpoints; i++) {
			final double setpoint = liftSetpoints[i];
			if (currentHeight < setpoint) continue; // Still below setpoint.
			switch (action) {
			default:
				// Stay here.
				return currentHeight;
			case NEXT_UP:
				// Use the next setpoint, but be careful not to go off the end.
				return liftSetpoints[Math.min(i + 1, numSetpoints - 1)];
			case NEXT_DOWN:
				// Use the last seen setpoint.
				return setpoint;
			}
		}
		// Must be higher than the highest setpoint, return current height.
		return currentHeight;
	}

	/**
	 * Return true if it's a safe configuration to move the lift.
	 */
	public static boolean isSafeIntakeConfiguration(IntakeConfiguration config) {
		if (config == IntakeConfiguration.STOWED) return true;
		if (config == IntakeConfiguration.WIDE) return true;
		return false;
	}
	
	/**
	 * Return if it's safe to move the intake where the lift is.
	 * @param liftHeight
	 * @return if it's safe to move the intake.
	 */
	public static boolean isSafeToMoveIntake(double liftHeight) {
		return liftHeight > Constants.LIFT_DEPLOY_THRESHOLD_HEIGHT - Constants.LIFT_DEFAULT_TOLERANCE ||
				liftHeight < Constants.LIFT_INTAKE_HEIGHT + Constants.LIFT_DEFAULT_TOLERANCE;
	}

	/**
	 * Return true if this dimension needs an update. eg the lift height should change.
	 * @param name textual name for logging only.
	 * @param currentState current value of the this dimension.
	 * @param desiredState null if no update needed.
	 * @return true if an update is needed.
	 */
	private <T> boolean calcNeedsUpdate(String name, T currentState, T desiredState) {
		if (desiredState == null) return false;  // No update needed.
		assert(currentState != null);
		if (currentState.equals(desiredState)) return false;
		logSub(name + " needs update from " + currentState + " => " + desiredState);
		return true;
	}
	
	/**
	 * Return true if this dimension needs an update. eg the lift height should change.
	 * @param name textual name for logging only.
	 * @param currentState current value of the this dimension.
	 * @param desiredState what it is targeting.
	 * @param tolerance how close the values can be..
	 * @return true if an update is needed.
	 */
	private boolean calcNeedsUpdate(String name, double currentState, double desiredState, double tolerance) {
		if (Math.abs(currentState - desiredState) < tolerance) return false;
		logSub(name + " needs update from " + currentState + " => " + desiredState);
		return true;
	}
	
	/**
	 * Blocks waiting till the lift is in position.
	 * If the sequence changes it will stop the lift.
	 */
	private void maybeWaitForLift() {
		try {
			waitUntilOrAbort(() -> subsystems.lift.isInPosition());
		} catch (SequenceChangedException e) {
			logSub("Sequence changed while moving lift, stopping lift");
			// The sequence has changed, grab the current position
			// and set that as the target so the lift quickly stops.
			double height = subsystems.lift.getHeight();
			subsystems.lift.setHeight(height);
			logSub("Resetting lift target height to " + height);
			// Give it a chance to stop moving.
			clock.sleepSeconds(0.5);
		}
	}

	/**
	 * Blocks waiting till the lift is in position.
	 */
	private void waitForLift() {
		waitUntil(() -> subsystems.lift.isInPosition(), String.format("lift to move to %.0f", subsystems.lift.getDesiredHeight()));
	}

	/**
	 * Blocks waiting till the intake is in position.
	 */
	private void waitForIntake() {
		waitUntil(() -> subsystems.intake.isInDesiredState(), "intake to move to " + subsystems.intake.getConfiguration());
	}

	/**
	 * Blocks waiting till the outtake is in position.
	 */
	private void waitForOuttake() {
		waitUntil(() -> subsystems.outtake.isClosed() || subsystems.outtake.isOpen(), "outtake");
	}

	/**
	 * Blocks waiting until endtime has passed.
	 */
	private void waitForTime(double endTimeSec) {
		if (clock.currentTime() < endTimeSec) {
			//logSub("Waiting for %.1f seconds", endTimeSec - clock.currentTime());
		}
		waitUntil(() -> clock.currentTime() > endTimeSec, "time");
	}

	/**
	 * Waits for func to return true or the sequence has changed.
	 * @param func returns when this function returns true.
	 * @throws SequenceChangedException if the sequence has changed.
	 */
	private void waitUntilOrAbort(BooleanSupplier func) throws SequenceChangedException {
		// Wait until func returns true or the desired state changed.
		while (!func.getAsBoolean()) {
			synchronized (this) {
				if (sequenceHasChanged) {
					throw new SequenceChangedException();
				}
			}
			clock.sleepMilliseconds(10);
		}
		//logSub("Done waiting");
	}
	
	/**
	 * Waits for func to return true.
	 * @param func returns when this function returns true.
	 */
	private void waitUntil(BooleanSupplier func, String name) {
		double startTimeSec = clock.currentTime();
		double waitDurationSec = 1;
		double nextLogTimeSec = startTimeSec + waitDurationSec;
		// Keep waiting until func returns true
		while (!func.getAsBoolean()) {
			double now = clock.currentTime();
			if (now > nextLogTimeSec) {
				logSub("Controller waiting on %s, has waited %fs so far", name, now - startTimeSec);
				waitDurationSec *= 2;
				nextLogTimeSec = now + waitDurationSec;
			}
			clock.sleepMilliseconds(10);
		}
		if (clock.currentTime() - nextLogTimeSec > 1) {
			// Print a final message.
			logSub("Controller done waiting on %s", name);
		}
	}

	private class SequenceChangedException extends Exception {
		private static final long serialVersionUID = 1L;
		public SequenceChangedException() {
			super("SequenceChanged");
		}
	}
	
	private void logSub(String message, Object... args) {
		String time_str = String.format("%.3f controller: ", clock.currentTime());
		log.sub(time_str + message, args);
	}

	private void logErr(String message, Object... args) {
		String time_str = String.format("%.3f controller: ", clock.currentTime());
		log.error(time_str + message, args);
	}

	public synchronized void updateDashboard() {
		String name = "None";
		if (sequence != null) {
			name = sequence.getName();
		}
		dashboard.putString("Controller: Current Sequence", name);
		dashboard.putBoolean("Controller: Sequence finished", sequenceHasFinished);
	}
}
