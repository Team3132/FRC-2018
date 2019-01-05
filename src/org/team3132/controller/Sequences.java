/**
 * Sequences for doing most actions on the robot.
 * 
 * If you add a new sequence, add it to allSequences at the end of this file.
 * 
 * Design doc:
 *   https://docs.google.com/document/d/1IBAw5dKG8hiahRkd8FzU75j3yF6No33oQXtNEKi3LXc/edit#
 */
package org.team3132.controller;

import org.team3132.Constants;
import org.team3132.controller.State.LiftSetpointAction;
import org.team3132.interfaces.IntakeInterface.IntakeConfiguration;
import org.team3132.interfaces.LiftInterface;
import org.team3132.lib.WaypointUtil;

import jaci.pathfinder.Waypoint;

/**
 * Control sequences for most robot operations.
 */
public class Sequences {
	
	/**
	 * Do nothing sequence.
	 */
	public static Sequence getEmptySequence() {
		if (emptySeq == null) {
			emptySeq = new Sequence("empty");
		}
		return emptySeq;
	}
	private static Sequence emptySeq = null;

	/**
	 * The first sequence run in the autonomous period.
	 */
	public static Sequence getStartSequence() {
		if (startSeq == null) {
			startSeq = new Sequence("start");
			startSeq.add().setLowGear(false).setLiftHeight(Constants.LIFT_INTAKE_HEIGHT);
		}
		return startSeq;
	}
	private static Sequence startSeq = null;

	/**
	 * Returns the sequence to reset the robot. Used to stop ejecting etc.
	 * The lift is at intake height, the intake is stowed, all motors are off.
	 * @return
	 */
	public static Sequence getResetSequence() {
		if (resetSeq == null) {
			resetSeq = new Sequence("reset");
			resetSeq.add().setLowGear(false); // Set high gear first if possible.
			resetSeq.add().setLiftHeight(Constants.LIFT_INTAKE_HEIGHT).setIntakeConfig(IntakeConfiguration.STOWED)
					.setOuttakeOpen(false).setOuttakeMotorOutput(0).setIntakeMotorOutput(0);
		}
		return resetSeq;
	}
	private static Sequence resetSeq = null;

	/**
	 * Start intaking from the ground.
	 */
	public static Sequence getStartIntakingSequence() {
		if (startIntakingSeq == null) {
			startIntakingSeq = new Sequence("start intaking");
			startIntakingSeq.add().setLowGear(false);
			startIntakingSeq.add().setLiftHeight(Constants.LIFT_INTAKE_HEIGHT)
					.setIntakeConfig(IntakeConfiguration.NARROW).setOuttakeOpen(true);
			startIntakingSeq.add().setIntakeMotorOutput(1);
		}
		return startIntakingSeq;
	}
	private static Sequence startIntakingSeq = null;

	/**
	 * Stop intaking at ground level.
	 */
	public static Sequence getStopIntakingSequence() {
		if (stopIntakingSeq == null) {
			stopIntakingSeq = new Sequence("stop intaking");
			stopIntakingSeq.add().setOuttakeOpen(false);
			stopIntakingSeq.add().setIntakeMotorOutput(0).setIntakeConfig(IntakeConfiguration.STOWED);
		}
		return stopIntakingSeq;
	}
	private static Sequence stopIntakingSeq = null;
	
	/**
	 * Intake at portal height.
	 * @return
	 */
	public static Sequence getIntakingPortalSequence() {
		// Only differs from the intaking sequence by the portal height and not using intake. Could be refactored, but
		// left like this for clarity.
		if (intakingPortalSeq == null) {
			intakingPortalSeq = new Sequence("portal intaking");
			intakingPortalSeq.add().setLowGear(false).setIntakeMotorOutput(0).setIntakeConfig(IntakeConfiguration.STOWED);
			intakingPortalSeq.add().setLiftHeight(Constants.LIFT_PORTAL_HEIGHT).setOuttakeOpen(true);
		}
		return intakingPortalSeq;
	}
	private static Sequence intakingPortalSeq = null;
	
	/**
	 * Move up to the next lift setpoint.
	 */
	public static Sequence getLiftSetpointUpSequence() {
		return liftSetpointUpSeq;
	}
	
	/**
	 * Move down to the next lift setpoint.
	 */
	public static Sequence getLiftSetpointDownSequence() {
		return liftSetpointDownSeq;
	}
	private static Sequence liftSetpointUpSeq = getLiftSetpointSequence(LiftSetpointAction.NEXT_UP);
	private static Sequence liftSetpointDownSeq = getLiftSetpointSequence(LiftSetpointAction.NEXT_DOWN);
	private static Sequence getLiftSetpointSequence(LiftSetpointAction action) {
		Sequence seq = new Sequence("lift setpoint " + action.name());
		seq.add().setIntakeConfig(IntakeConfiguration.STOWED);
		seq.add().setLiftHeightSetpoint(action);
		return seq;		
	}

	/**
	 * Move the lift to the scale height.
	 */
	public static Sequence getPositioningLiftScaleSequence() {
		return positioningLiftScaleSeq;
	}
	/**
	 * Move the lift to the switch height.
	 */
	public static Sequence getPositioningLiftSwitchSequence() {
		return positioningLiftSwitchSeq;
	}
	private static Sequence positioningLiftScaleSeq = getPositioningLiftSequence(LiftInterface.LiftPosition.SCALE_POSITION.value);
	private static Sequence positioningLiftSwitchSeq = getPositioningLiftSequence(LiftInterface.LiftPosition.SWITCH_POSITION.value);
	private static Sequence getPositioningLiftSequence(double liftHeight) {
		Sequence seq = new Sequence(String.format("positioning lift to %f", liftHeight));
		seq.add().setLowGear(false);
		seq.add().setIntakeConfig(IntakeConfiguration.STOWED).setOuttakeOpen(false).setIntakeMotorOutput(0)
				.setOuttakeOpen(false).setLiftHeight(liftHeight);
		return seq;		
	}

	/**
	 * Score out of the left side of the robot. Does not move the lift.
	 * The reset sequence should be run after it to stop the outtake motor.
	 */
	public static Sequence getScoringLeftSequence() {
		return scoreLeftSeq;
	}
	/**
	 * Score out of the right side of the robot. Does not move the lift.
	 * The reset sequence should be run after it to stop the outtake motor.
	 */
	public static Sequence getScoringRightSequence() {
		return scoreRightSeq;
	}
	private static Sequence scoreLeftSeq = getScoringSequence(1);
	private static Sequence scoreRightSeq = getScoringSequence(-1);
	private static Sequence getScoringSequence(double motorPower) {
		Sequence seq = new Sequence("score");
		seq.add().setIntakeMotorOutput(0).setIntakeConfig(IntakeConfiguration.STOWED).setOuttakeOpen(false);
		seq.add().setOuttakeMotorOutput(motorPower);  // Push the cube out.
		return seq;
	}

	/**
	 * Score out the front of the outtake.
	 */
	public static Sequence getFrontScoreSequence() {
		if (frontScoreSeq == null) {
			frontScoreSeq = new Sequence("front score");
			frontScoreSeq.add().setOuttakeMotorOutput(0); // Not using the outtake motor.
			frontScoreSeq.add().setOuttakeOpen(true); // Let the cube fall out.
		}
		return frontScoreSeq;
	}
	private static Sequence frontScoreSeq = null;

	/**
	 * Eject the cube out the front of the intake by letting it fall out the
	 * outtake and push it out the intake.
	 */
	public static Sequence getIntakeEjectSequence() {
		if (intakeEjectSeq == null) {
			intakeEjectSeq = new Sequence("intake eject");
			intakeEjectSeq.add().setIntakeConfig(IntakeConfiguration.NARROW);
			intakeEjectSeq.add().setOuttakeMotorOutput(0).setIntakeMotorOutput(-1); // Not using the outtake motor.
			intakeEjectSeq.add().setOuttakeOpen(true); // Let the cube fall out.
			// Leave it ejecting until another sequence is run.
		}
		return intakeEjectSeq;
	}
	private static Sequence intakeEjectSeq = null;

	/**
	 * Stow the intake and prepare for climing.
	 */
	public static Sequence getReadyClimbSequence() {
		if (readyClimbSeq == null) {
			readyClimbSeq = new Sequence("ready climb");
			// Attempt to use high gear, but the lift may not be able
			// to shift if it's part way through a climb.
			readyClimbSeq.add().setIntakeConfig(IntakeConfiguration.STOWED).setOuttakeMotorOutput(0)
					.setOuttakeOpen(false).setIntakeMotorOutput(0).setLowGear(false);
			readyClimbSeq.add().setLiftHeight(Constants.LIFT_CLIMB_HEIGHT);
			// The next step would normally be to put it into low gear,
			// but that might not be true. Leave it in high gear.
			readyClimbSeq.add().setLowGear(false);
		}
		return readyClimbSeq;
	}
	private static Sequence readyClimbSeq = null;

	/**
	 * Returns the climbing sequence which includes changing to low gear
	 * and a large move down. This can be interrupted if the button is released.
	 * @return
	 */
	public static Sequence getClimbingSequence() {
		// ReadyClimb needs to be called first.
		if (climbingSeq == null) {
			climbingSeq = new Sequence("climbing");
			// Basic setup in case this is not called after ready climb.
			climbingSeq.add().setIntakeConfig(IntakeConfiguration.STOWED).setOuttakeMotorOutput(0).setOuttakeOpen(false)
					.setIntakeMotorOutput(0);
			climbingSeq.add().setLowGear(true);
			climbingSeq.add().setLiftHeight(Constants.LIFT_CLIMB_STOP_HEIGHT);
		}
		return climbingSeq;
	}
	private static Sequence climbingSeq = null;

	/**
	 * Micro adjust lift up.
	 */
	public static Sequence getMicroAdjustUpSequence() {
		return microAdjustUpSeq;
	}
	/**
	 * Micro adjust lift down.
	 */
	public static Sequence getMicroAdjustDownSequence() {
		return microAdjustDownSeq;
	}
	private static Sequence microAdjustUpSeq = getMicroAdjustSequence(Constants.LIFT_MICRO_ADJUST_HEIGHT);
	private static Sequence microAdjustDownSeq = getMicroAdjustSequence(-Constants.LIFT_MICRO_ADJUST_HEIGHT);
	static public Sequence getMicroAdjustSequence(double delta) {
		Sequence seq = new Sequence("micro adjust");
		seq.add().setLiftHeightDelta(delta);
		return seq;
	}

	/**
	 * Trigger the ramp by releasing the solenoid.
	 */
	public static Sequence getDeployRampSequence() {
		if (deployRampSeq == null) {
			deployRampSeq = new Sequence("deploy ramp");
			deployRampSeq.add().setRampRelease(true);
		}
		return deployRampSeq;
	}	
	private static Sequence deployRampSeq = null;
	
	/**
	 * Retract the solenoid that normally holds the lift.
	 */
	public static Sequence getRetractRampSequence() {
		if (retractRampSeq == null) {
			retractRampSeq = new Sequence("retract ramp");
			retractRampSeq.add().setRampRelease(false);
		}
		return retractRampSeq;
	}	
	private static Sequence retractRampSeq = null;

	/**
	 * Drive to a point on the field, relative to the starting point.
	 */
	public static Sequence getDriveToWaypointSequence(double x, double y, double angle) {
		if (driveToWaypointSeq == null) {
			Waypoint waypoint = new Waypoint(x, y, angle);
			driveToWaypointSeq = new Sequence(String.format("drive to %s", WaypointUtil.toString(waypoint)));
			driveToWaypointSeq.add().setRelativeWaypoints(new Waypoint[]{waypoint}, true);
		}
		return driveToWaypointSeq;
	}	
	private static Sequence driveToWaypointSeq = null;

	// For testing. Needs to be at the end of the file.
	public static Sequence[] allSequences = new Sequence[] { getEmptySequence(), getStartSequence(), getResetSequence(),
			getStartIntakingSequence(), getStopIntakingSequence(), getIntakingPortalSequence(),
			getPositioningLiftScaleSequence(), getPositioningLiftSwitchSequence(), getScoringLeftSequence(),
			getScoringRightSequence(), getFrontScoreSequence(), getIntakeEjectSequence(), getReadyClimbSequence(),
			getClimbingSequence(), getMicroAdjustUpSequence(), getMicroAdjustDownSequence(), getDeployRampSequence(),
			getRetractRampSequence(), getDriveToWaypointSequence(0, 12, 0) };
	
}
