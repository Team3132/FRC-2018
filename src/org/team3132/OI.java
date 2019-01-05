package org.team3132;

import org.strongback.Strongback;
import org.strongback.SwitchReactor;
import org.strongback.components.Switch;
import org.strongback.components.ui.ContinuousRange;
import org.strongback.components.ui.InputDevice;
import org.team3132.interfaces.IntakeInterface.IntakeConfiguration;
import org.team3132.controller.Controller;
import org.team3132.controller.Sequence;
import org.team3132.controller.Sequences;
import org.team3132.interfaces.Log;
import org.team3132.interfaces.OIInterface;
import org.team3132.lib.GamepadButtonsX;
import org.team3132.lib.MathUtil;
import org.team3132.lib.OperatorBoxButtons;
import org.team3132.subsystems.OverridableIntake;
import org.team3132.subsystems.OverridableLift;

public class OI implements OIInterface {

	private SwitchReactor reactor = Strongback.switchReactor();
	private Controller exec;
	private OverridableIntake overridableIntake;
	private OverridableLift overridableLift;
	private Log log;
		
	public OI(Controller controller,
			OverridableIntake overridableIntake, OverridableLift overridableLift, Log log) {
		this.exec = controller;
		this.log = log;
		this.overridableIntake = overridableIntake;
		this.overridableLift = overridableLift;
	}
    
	/*
	 * Gamepad button mappings
	 * =======================
	 * NB all of the cube detection sensors were removed, so the operator has to release
	 * the intaking and scoring buttons to stop them.
	 * 
	 * Logitech controller looks like:
	 *   (left bumper)                (right bumper)
	 *   (left trigger)               (right trigger)
	 * 
	 *       ^        (back)   (start)      (Y)
	 *     < hat >                       (X)   (B)
	 *       \/       (mode)                (A)
	 *       
	 *      (left thumbstick)   (right thumbstick)
	 *      
	 * The hat vertical controls the lift height. Repeatedly pushing makes it go to the next height.
	 * The hat horizontal controls the sideways scoring.
	 * Pushing (A) starts intaking. Releasing it, stops intaking.
	 * Pushing (B) does an intake eject. Releasing it, stops ejecting.
	 * Pushing (left bumper) does a front eject (ie the outtake opens for the cube to fall out).
	 * Left thumbstick is the driving speed.
	 * Right thumbstick is the turning rate.
	 * Pushing both triggers deploys the ramp (safety)
	 * Pushing (Y) gets the robot ready for climbing.
	 * While (X) is held, the robot will climb. 
	 * (start) resets the robot lift at the bottom and intake stowed.
	 * 
	 * The following buttons are unused:
	 *  (back)(mode)(left bumper).
	 */
	public void configureJoysticks(InputDevice driver, InputDevice operator) {
		// Both joysticks have the same mapping, plus the driver joystick
		// can also drive.
    	configureJoystick(driver, "driver");
    	configureJoystick(operator, "operator");
    }
	
	@SuppressWarnings("unused")  // log code is unused.
	public void configureJoystick(InputDevice stick, String name) {
    	// Reset robot: intake stowed and lift at bottom.
		onTriggered(stick.getButton(GamepadButtonsX.START_BUTTON), Sequences.getStartSequence());

    	// Intaking. Stops on button release.
		onTriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.getStartIntakingSequence());
		onUntriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.getStopIntakingSequence());

		// Intake eject. Stops on button release
		onTriggered(stick.getButton(GamepadButtonsX.B_BUTTON), Sequences.getIntakeEjectSequence());
		onUntriggered(stick.getButton(GamepadButtonsX.B_BUTTON), Sequences.getResetSequence());

		// Lift movement. Multiple presses move up through configured stops.
		// TODO: Fix this!
		onTriggered(() -> stick.getDPad(0).getDirection() == GamepadButtonsX.DPAD_NORTH, Sequences.getLiftSetpointUpSequence());
		onTriggered(() -> stick.getDPad(0).getDirection() == GamepadButtonsX.DPAD_SOUTH, Sequences.getLiftSetpointDownSequence());

		// Sidewayscoring. Stops on button release.
		onTriggered(() -> stick.getDPad(0).getDirection() == GamepadButtonsX.DPAD_WEST, Sequences.getScoringLeftSequence());
		onUntriggered(() -> stick.getDPad(0).getDirection() == GamepadButtonsX.DPAD_WEST, Sequences.getResetSequence());
		onTriggered(() -> stick.getDPad(0).getDirection() == GamepadButtonsX.DPAD_EAST, Sequences.getScoringRightSequence());
		onUntriggered(() -> stick.getDPad(0).getDirection() == GamepadButtonsX.DPAD_EAST, Sequences.getResetSequence());
		// Front scoring. Stops on button release.
		onTriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.getFrontScoreSequence());
		onUntriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.getResetSequence());
		
		// Ready climb - too quick to stop.
		onTriggered(stick.getButton(GamepadButtonsX.Y_BUTTON), Sequences.getReadyClimbSequence());
		// Do climb, while held.
		onTriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.getClimbingSequence());
		onUntriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.getEmptySequence());
		
		// Ramp deploy. Can only be done once. Two buttons needed for safety.
		// Note the triggers are really axis, not buttons.
		onTriggered(Switch.and(
				triggerToButton(stick, GamepadButtonsX.LEFT_TRIGGER_AXIS),
				triggerToButton(stick, GamepadButtonsX.RIGHT_TRIGGER_AXIS)), Sequences.getDeployRampSequence());
		onUntriggered(Switch.and(
				triggerToButton(stick, GamepadButtonsX.LEFT_TRIGGER_AXIS),
				triggerToButton(stick, GamepadButtonsX.RIGHT_TRIGGER_AXIS)), Sequences.getRetractRampSequence());
		
		if (false) { // Performance concerns.
			/*
			 * Configure specific logging for the User Interface. This allows us to only
			 * record information that is important for our robot.
			 */
			log.register(false, stick.getDPad(0), "UI/%s/%s", name, "DPad")
					.register(false, stick.getButton(GamepadButtonsX.A_BUTTON), "UI/%s/%s", name, "A")
					.register(false, stick.getButton(GamepadButtonsX.B_BUTTON), "UI/%s/%s", name, "B")
					.register(false, stick.getButton(GamepadButtonsX.X_BUTTON), "UI/%s/%s", name, "X")
					.register(false, stick.getButton(GamepadButtonsX.Y_BUTTON), "UI/%s/%s", name, "Y")
					.register(false, stick.getButton(GamepadButtonsX.START_BUTTON), "UI/%s/%s", name, "Start")
					.register(false, stick.getButton(GamepadButtonsX.BACK_BUTTON), "UI/%s/%s", name, "Back")
					.register(false, stick.getButton(GamepadButtonsX.LEFT_BUMPER), "UI/%s/%s", name, "Left_Bumper")
					.register(false, stick.getButton(GamepadButtonsX.RIGHT_BUMPER), "UI/%s/%s", name, "Right_Bumper")
					.register(false, stick.getButton(GamepadButtonsX.LEFT_THUMBSTICK_CLICK), "UI/%s/%s", name, "Left_Click")
					.register(false, stick.getButton(GamepadButtonsX.RIGHT_THUMBSTICK_CLICK), "UI/%s/%s", name,	"Right_Click")
					.register(false, stick.getButton(GamepadButtonsX.DPAD_WEST), "UI/%s/%s", name, "DPad_West")
					.register(false, stick.getButton(GamepadButtonsX.DPAD_EAST), "UI/%s/%s", name, "DPad_East")
					.register(false, stick.getButton(GamepadButtonsX.DPAD_NORTH), "UI/%s/%s", name, "DPad_North")
					.register(false, stick.getButton(GamepadButtonsX.DPAD_SOUTH), "UI/%s/%s", name, "DPad_South")
					.register(false, () -> {
						return stick.getAxis(GamepadButtonsX.LEFT_TRIGGER_AXIS).read();
					}, "UI/%s/%s", name, "Left_Trigger_Axis").register(false, () -> {
						return stick.getAxis(GamepadButtonsX.RIGHT_TRIGGER_AXIS).read();
					}, "UI/%s/%s", name, "Right_Trigger_Axis").register(false, () -> {
						return stick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read();
					}, "UI/%s/%s", name, "Left_X_Axis").register(false, () -> {
						return stick.getAxis(GamepadButtonsX.LEFT_Y_AXIS).read();
					}, "UI/%s/%s", name, "Left_Y_Axis").register(false, () -> {
						return stick.getAxis(GamepadButtonsX.RIGHT_X_AXIS).read();
					}, "UI/%s/%s", name, "Right_X_Axis").register(false, () -> {
						return stick.getAxis(GamepadButtonsX.RIGHT_Y_AXIS).read();
					}, "UI/%s/%s", name, "Right_Y_Axis");
		}
	}

    @Override
	public void configureOperatorBox(InputDevice box) {

		// Intake overrides
		onTriggered(box.getButton(OperatorBoxButtons.INTAKE_DISABLE), () -> overridableIntake.turnOff());
		onTriggered(box.getButton(OperatorBoxButtons.INTAKE_MANUAL), () -> overridableIntake.setManualMode());
		onUntriggered(
				Switch.or(box.getButton(OperatorBoxButtons.INTAKE_MANUAL),
						box.getButton(OperatorBoxButtons.INTAKE_DISABLE)),
				() -> overridableIntake.setAutomaticMode());

		// Setup the callback for the button box to know the pot values.
		overridableIntake.setIntakeSpeedSupplier(() -> box.getAxis(GamepadButtonsX.LEFT_X_AXIS).read());
		// Intake deploy buttons.
		onTriggered(box.getButton(OperatorBoxButtons.INTAKE_STOWED),
				() -> overridableIntake.overrideIntakeConfiguration(IntakeConfiguration.STOWED));
		onTriggered(box.getButton(OperatorBoxButtons.INTAKE_WIDE),
				() -> overridableIntake.overrideIntakeConfiguration(IntakeConfiguration.WIDE));
		onTriggered(box.getButton(OperatorBoxButtons.INTAKE_NARROW),
				() -> overridableIntake.overrideIntakeConfiguration(IntakeConfiguration.NARROW));
		onTriggered(box.getButton(OperatorBoxButtons.INTAKE_SUPER_NARROW),
				() -> overridableIntake.overrideIntakeConfiguration(IntakeConfiguration.SUPER_NARROW));

		// Lift overrides
		onTriggered(box.getButton(OperatorBoxButtons.LIFT_DISABLE), () -> overridableLift.turnOff());
		onTriggered(box.getButton(OperatorBoxButtons.LIFT_MANUAL), () -> overridableLift.setManualMode());
		onUntriggered(
				Switch.or(box.getButton(OperatorBoxButtons.LIFT_MANUAL),
						box.getButton(OperatorBoxButtons.LIFT_DISABLE)),
				() -> overridableLift.setAutomaticMode());
		onTriggered(box.getButton(OperatorBoxButtons.LIFT_ADD_HEIGHT),
				() -> overridableLift.overrideLiftHeightByDelta(2));
		onTriggered(box.getButton(OperatorBoxButtons.LIFT_DECREASE_HEIGHT),
				() -> overridableLift.overrideLiftHeightByDelta(-2));
		onTriggered(box.getButton(OperatorBoxButtons.LIFT_SHIFTER),
				() -> overridableLift.overrideGear(!overridableLift.isInLowGear()));


		// Lift movement. Multiple presses move up through configured stops.
		onTriggered(box.getButton(OperatorBoxButtons.LIFT_ADD_HEIGHT), Sequences.getPositioningLiftScaleSequence());
		onTriggered(box.getButton(OperatorBoxButtons.LIFT_DECREASE_HEIGHT), Sequences.getPositioningLiftSwitchSequence());

		// Sideway scoring. Stops on button release.
		onTriggered(box.getButton(OperatorBoxButtons.OUTTAKE_SCORE_LEFT), Sequences.getScoringLeftSequence());
		onUntriggered(box.getButton(OperatorBoxButtons.OUTTAKE_SCORE_LEFT), Sequences.getResetSequence());
		onTriggered(box.getButton(OperatorBoxButtons.OUTTAKE_SCORE_RIGHT), Sequences.getScoringRightSequence());
		onUntriggered(box.getButton(OperatorBoxButtons.OUTTAKE_SCORE_RIGHT), Sequences.getResetSequence());
		// No front scoring button??
		
		// Ready climb - too quick to stop.
		onTriggered(box.getButton(OperatorBoxButtons.READY_CLIMB), Sequences.getReadyClimbSequence());
		// Do climb, while held.
		whileTriggered(box.getButton(OperatorBoxButtons.CLIMB), Sequences.getClimbingSequence());
		
		// Ramp deploy.
		onTriggered(box.getButton(OperatorBoxButtons.RAMP), Sequences.getDeployRampSequence());
		onUntriggered(box.getButton(OperatorBoxButtons.RAMP), Sequences.getRetractRampSequence());
	}
    
	/**
	 * Configure the rules for the user interfaces
	 */
	
	@SuppressWarnings("unused")
	private void onTriggered(Switch swtch, Sequence seq) {
		reactor.onTriggered(swtch, () -> exec.doSequence(seq));
	}
	
	@SuppressWarnings("unused")
	private void onTriggered(Switch swtch, Runnable func) {
		reactor.onTriggered(swtch, func);
	}

	@SuppressWarnings("unused")
	private void onUntriggered(Switch swtch, Sequence seq) {
		reactor.onUntriggered(swtch, () -> exec.doSequence(seq));
	}

	@SuppressWarnings("unused")
	private void onUntriggered(Switch swtch, Runnable func) {
		reactor.onUntriggered(swtch, func);
    }
	
	@SuppressWarnings("unused")
	private void whileTriggered(Switch swtch, Runnable func) {
		reactor.whileTriggered(swtch, func);
    }
	
	@SuppressWarnings("unused")
	private void whileTriggered(Switch swtch, Sequence seq) {
		reactor.onUntriggered(swtch, () -> exec.doSequence(seq));
    }
	
	@SuppressWarnings("unused")
	private Switch axisAsSwitch(ContinuousRange range, double min, double max) {
		return () -> { return MathUtil.scale(range.read(), min, max, 0, 1) > GamepadButtonsX.TRIGGER_THRESHOLD; };
	}
	
	// FIXME: Make the InputDevice do this instead.
	private Switch triggerToButton(InputDevice joystick, int axis) {
		return () -> Math.abs(joystick.getAxis(axis).read()) > 0.1;
	}
}