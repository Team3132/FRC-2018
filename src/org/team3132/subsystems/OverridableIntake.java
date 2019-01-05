package org.team3132.subsystems;

import java.util.function.DoubleSupplier;

import org.team3132.interfaces.IntakeInterface;
import org.team3132.interfaces.Log;
import org.team3132.simulator.IntakeSimulator;

/**
 * A shim layer between the real intake and the robot so that the
 * behavior can be overridden with the button box.
 * 
 * It wraps the real interface and chooses what it should
 * pass down the the wrapped intake.
 * 
 * It uses a simulator when not using the real interface so that
 * it still behaves like a real intake.
 * 
 * The button box has a switch to select between:
 *  - automatic: (just pass through to the real interface)
 *  - manual: only listen to the pots/buttons on the button box
 *  - off: completely disable the intake.
 *  
 *  Note: Using anything other than automatic risks damage to the robot.
 *  
 * USE AT YOUR OWN RISK!!
 *  
 * Example:
 *   	OverrideableIntake overridableIntake = new OverridableIntake(intake);
 *   	intake = overridableIntake;  // So downstream uses the correct layer.
 *      overridableIntake.execute(...);  // Called in the main loop.
 * And in OI:
 *	    overridableIntake.setIntakeSpeedSupplier(() -> buttonBoxJoystick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read());
 *		onTriggered(buttonBoxJoystick.getButton(ButtonBoxMap.INTAKE_BUTTON),
 *				() -> overridableIntake.overrideIntakeConfiguration(IntakeConfiguration.LEFT_WIDE_RIGHT_NARROW));
 *		onTriggered(buttonBoxJoystick.getButton(ButtonBoxMap.OUTTAKE_BUTTON),
 *				() -> overridableIntake.overrideIntakeConfiguration(IntakeConfiguration.LEFT_NARROW_RIGHT_WIDE));
 *		onTriggered(buttonBoxJoystick.getButton(ButtonBoxMap.INTAKE_RETRACT),
 *				() -> overridableIntake.overrideIntakeConfiguration(IntakeConfiguration.STOWED));
 *		onTriggered(buttonBoxJoystick.getButton(ButtonBoxMap.INTAKE_OPEN),
 *				() -> overridableIntake.overrideIntakeConfiguration(IntakeConfiguration.WIDE));
 *		onTriggered(buttonBoxJoystick.getButton(ButtonBoxMap.INTAKE_CLOSE),
 *				() -> overridableIntake.overrideIntakeConfiguration(IntakeConfiguration.NARROW));
 *      // TODO: Change modes with the switch.
 */
public class OverridableIntake implements IntakeInterface {

	// The real and simulator intakes that commands are sent to depending on the mode.
	private IntakeInterface real, simulator;
	private Log log;
	
	private enum OverrideMode {
		AUTOMATIC,
		MANUAL,
		OFF;
	}
	private OverrideMode mode = OverrideMode.AUTOMATIC;
	private DoubleSupplier intakeMotorSpeedSupplier = null;
	
	public OverridableIntake(IntakeInterface real, Log log) {
		this.real = real;
		this.log = log;
		simulator = new IntakeSimulator();  // Does nothing.
	}
	
	/**
	 * Determine which intake (real or simulator) is used on the normal
	 * intake interface. In automatic mode, this is the real interface.
	 */
	private IntakeInterface getNormalInterface() {
		if (isAuto()) {
			return real;
		}
		return simulator;
	}

	/**
	 * Determine which intake (real or simulator) is used on the override
	 * intake interface. In automatic and off modes, this is the simulator
	 * interface so that the button box overrides are ignored.
	 */
	private IntakeInterface getOverrideInterface() {
		if (isManual()) {
			return real;
		}
		return simulator;
	}

	// Mode change methods.
	public IntakeInterface setAutomaticMode() {
		// This may need to be more clever to carry over state.
		log.sub("Intake switched to normal/automatic mode");
		mode = OverrideMode.AUTOMATIC;
		return this;
	}
	
	public IntakeInterface setManualMode() {
		// This may need to be more clever to carry over state.
		log.sub("Intake switched manual mode");
		mode = OverrideMode.MANUAL;
		return this;
	}

	public IntakeInterface turnOff() {
		// This may need to be more clever to carry over state.
		log.sub("Intake turned off");
		mode = OverrideMode.OFF;
		real.setIntakeMotorOutput(0);  // Only do this once per off command.
		return this;
	}
	
	private boolean isManual() {
		return mode == OverrideMode.MANUAL;
	}
	
	private boolean isAuto() {
		return mode == OverrideMode.AUTOMATIC;
	}

	// Override methods.
	public OverridableIntake setIntakeSpeedSupplier(DoubleSupplier supplier) {
		this.intakeMotorSpeedSupplier = supplier;
		return this;
	}

	/**
	 * If in manual mode, send override commands to the real intake.
	 */
	public IntakeInterface overrideIntakeConfiguration(IntakeConfiguration configuration) {
		getOverrideInterface().setConfiguration(configuration);
		return this;
	}

	// Normal methods from here down.
	
	@Override
	public String getName() {
		return getNormalInterface().getName();
	}

	@Override
	public void enable() {
		getNormalInterface().enable();
	}

	@Override
	public void disable() {
		getNormalInterface().disable();
	}

	@Override
	public IntakeInterface setConfiguration(IntakeConfiguration configuration) {
		getNormalInterface().setConfiguration(configuration);
		return this;
	}

	@Override
	public IntakeConfiguration getConfiguration() {
		return getNormalInterface().getConfiguration();
	}

	@Override
	public boolean isInDesiredState() {
		return getNormalInterface().isInDesiredState();
	}

	@Override
	public boolean isEnabled() {
		return getNormalInterface().isEnabled();
	}

	@Override
	public void cleanup() {
		getNormalInterface().cleanup();
	}

	@Override
	public IntakeInterface setIntakeMotorOutput(double output) {
		getNormalInterface().setIntakeMotorOutput(output);
		return this;
	}

	@Override
	public double getIntakeMotorOutput() {
		return getNormalInterface().getIntakeMotorOutput();
	}

	@Override
	public void execute(long timeInMillis) {
		if (isAuto() || isManual()) {
			real.execute(timeInMillis);
		}
		simulator.execute(timeInMillis);
		if (isManual()) {
			pushManualValues();
		}
		// Do nothing for off mode.
	}
	
	private void pushManualValues() {
		// Speed is set here. Buttons are only as they are pressed using
		// the overrideXX() methods.
		real.setIntakeMotorOutput(intakeMotorSpeedSupplier.getAsDouble());
	}
}
