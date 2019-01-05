package org.team3132.simulator;

import org.strongback.Executable;
import org.team3132.interfaces.IntakeInterface;
import org.team3132.lib.MovementSimulator;

/**
 * Very basic intake simulator used for unit testing.
 * Does not do gravity/friction etc.
 */
public class IntakeSimulator implements IntakeInterface, Executable {
	private final double kMaxSpeed = 180;  // degrees/sec
	private final double kMaxAccel = 200;   // degrees/sec/sec
	private final double kMinAngle = IntakeAngle.STOWED.angle;
	private final double kMaxAngle = IntakeAngle.SUPER_NARROW.angle;
	private final double kMovementTolerance = 1;  // How close before it's classed as being in position.
	private MovementSimulator left = new MovementSimulator("left intake", kMaxSpeed, kMaxAccel, kMinAngle, kMaxAngle, kMovementTolerance);
	private MovementSimulator right = new MovementSimulator("right intake", kMaxSpeed, kMaxAccel, kMinAngle, kMaxAngle, kMovementTolerance);
	private long lastTimeMs = 0;
	private IntakeConfiguration configuration = IntakeConfiguration.STOWED;
	private double outputPower;
	
	private enum IntakeAngle {
		STOWED(0),				// folded back
		WIDE(135),				// full width intake
		NARROW(225),     		// holding normally
		SUPER_NARROW(235);
		
		public final double angle;  // ~degrees.
		
		private IntakeAngle(double angle) {
			this.angle = angle;
		}
	}
	
	public IntakeSimulator() {
	}

	/**
	 * Overrides the intake position ignoring time and simulators.
	 * @param config intake configuration
	 * @return
	 */
	public IntakeInterface setIntakePositionsActual(IntakeConfiguration configuration) {
		if (this.configuration == configuration) return this;
		System.out.println("  Setting intake to " + configuration);
		this.configuration = configuration;
		switch (configuration) {
		case STOWED:
			left.setPos(IntakeAngle.STOWED.angle);
			right.setPos(IntakeAngle.STOWED.angle);
			break;
		case LEFT_WIDE_RIGHT_NARROW:
			left.setPos(IntakeAngle.WIDE.angle);
			right.setPos(IntakeAngle.NARROW.angle);
			break;
		case LEFT_NARROW_RIGHT_WIDE:
			left.setPos(IntakeAngle.NARROW.angle);
			right.setPos(IntakeAngle.WIDE.angle);
			break;
		case LEFT_WIDE_RIGHT_SUPER_NARROW:
			left.setPos(IntakeAngle.WIDE.angle);
			right.setPos(IntakeAngle.SUPER_NARROW.angle);
			break;
		case LEFT_SUPER_NARROW_RIGHT_WIDE:
			left.setPos(IntakeAngle.SUPER_NARROW.angle);
			right.setPos(IntakeAngle.WIDE.angle);
			break;
		case WIDE:
			left.setPos(IntakeAngle.WIDE.angle);
			right.setPos(IntakeAngle.WIDE.angle);
			break;
		case NARROW:
			left.setPos(IntakeAngle.NARROW.angle);
			right.setPos(IntakeAngle.NARROW.angle);
			break;
		case SUPER_NARROW:
			left.setPos(IntakeAngle.SUPER_NARROW.angle);
			right.setPos(IntakeAngle.SUPER_NARROW.angle);
			break;
		case MOVING:
			// This is invalid, use stowed instead.
			left.setPos(IntakeAngle.STOWED.angle);
			right.setPos(IntakeAngle.STOWED.angle);
			break;
		}
		left.setSpeed(0);  // Reset speed.
		right.setSpeed(0);
		return this;
	}

	@Override
	public String getName() {
		return "IntakeSimulator";
	}

	@Override
	public void enable() {
	}

	@Override
	public void disable() {
	}

	@Override
	public void execute(long timeInMillis) {
		if (lastTimeMs == 0) {
			lastTimeMs = timeInMillis;
			return;
		}
		// Update the lift position.
		left.step((timeInMillis - lastTimeMs) / 1000.);
		right.step((timeInMillis - lastTimeMs) / 1000.);
		lastTimeMs = timeInMillis;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void cleanup() {
	}

	@Override
	public IntakeInterface setIntakeMotorOutput(double output) {
		this.outputPower = output;
		return this;
	}

	@Override
	public double getIntakeMotorOutput() {
		return outputPower;
	}

	@Override
	public IntakeInterface setConfiguration(IntakeConfiguration configuration) {
		if (this.configuration == configuration) return this;
		System.out.println("  Setting intake to " + configuration);
		this.configuration = configuration;
		switch (configuration) {
		case STOWED:
			left.setTargetPos(IntakeAngle.STOWED.angle);
			right.setTargetPos(IntakeAngle.STOWED.angle);
			break;
		case LEFT_WIDE_RIGHT_NARROW:
			left.setTargetPos(IntakeAngle.WIDE.angle);
			right.setTargetPos(IntakeAngle.NARROW.angle);
			break;
		case LEFT_NARROW_RIGHT_WIDE:
			left.setTargetPos(IntakeAngle.NARROW.angle);
			right.setTargetPos(IntakeAngle.WIDE.angle);
			break;
		case LEFT_WIDE_RIGHT_SUPER_NARROW:
			left.setTargetPos(IntakeAngle.WIDE.angle);
			right.setTargetPos(IntakeAngle.SUPER_NARROW.angle);
			break;
		case LEFT_SUPER_NARROW_RIGHT_WIDE:
			left.setTargetPos(IntakeAngle.SUPER_NARROW.angle);
			right.setTargetPos(IntakeAngle.WIDE.angle);
			break;
		case WIDE:
			left.setTargetPos(IntakeAngle.WIDE.angle);
			right.setTargetPos(IntakeAngle.WIDE.angle);
			break;
		case NARROW:
			left.setTargetPos(IntakeAngle.NARROW.angle);
			right.setTargetPos(IntakeAngle.NARROW.angle);
			break;
		case SUPER_NARROW:
			left.setTargetPos(IntakeAngle.SUPER_NARROW.angle);
			right.setTargetPos(IntakeAngle.SUPER_NARROW.angle);
			break;
		case MOVING:
			// This is invalid, use stowed instead.
			left.setTargetPos(IntakeAngle.STOWED.angle);
			right.setTargetPos(IntakeAngle.STOWED.angle);
			break;
		}
		return this;
	}

	@Override
	public IntakeConfiguration getConfiguration() {
		if (!left.isInPosition() || !right.isInPosition()) {
			return IntakeConfiguration.MOVING;
		}
		
		return configuration;
	}

	@Override
	public boolean isInDesiredState() {
		return left.isInPosition() && right.isInPosition();
	}

	@Override
	public String toString() {
		return left.toString() + " " + right.toString();
	}
}
