package org.team3132.subsystems;

import org.team3132.interfaces.LiftInterface;
import org.team3132.interfaces.Log;
import org.team3132.simulator.LiftSimulator;

/**
 * A shim layer between the real lift and the robot so that the
 * behavior can be overridden with the button box.
 * 
 * It wraps the real interface and chooses what it should
 * pass down the the wrapped lift.
 * 
 * It uses a simulator when not using the real interface so that
 * it still behaves like a real lift.
 * 
 * The button box has a switch to select between:
 *  - automatic: (just pass through to the real interface)
 *  - manual: only listen to the pots/buttons on the button box
 *  - off: completely disable the lift.
 *  
 *  Note: Using anything other than automatic risks damage to the robot.
 *  
 * USE AT YOUR OWN RISK!!
 *  
 * Example:
 *   	OverrideableLift overridableLift = new OverridableLift(lift);
 *   	lift = overridableLift;  // So downstream uses the correct layer.
 *      overridableLift.execute(...);  // Called in the main loop.
 * And in OI:
 *		onTriggered(buttonBoxJoystick.getButton(ButtonBoxMap.LIFT_ADD_HEIGHT),
 *				() -> overridableLift.overrideLiftHeightByDelta(2));
 *		onTriggered(buttonBoxJoystick.getButton(ButtonBoxMap.LIFT_DECREASE_HEIGHT),
 *				() -> overridableLift.overrideLiftHeightByDelta(-2));
 *		onTriggered(buttonBoxJoystick.getButton(ButtonBoxMap.SHIFTER_BUTTON),
 *				() -> overridableLift.overrideGear(overridableLift.isInHighGear()));
 *      // TODO: Change override mode with the switch.
 */
public class OverridableLift implements LiftInterface {

	// The real and simulator lifts that commands are sent to depending on the mode.
	private LiftInterface real, simulator;
	private Log log;
	
	private enum OverrideMode {
		AUTOMATIC,
		MANUAL,
		OFF;
	}
	private OverrideMode mode = OverrideMode.AUTOMATIC;
	
	public OverridableLift(LiftInterface real, Log log) {
		this.real = real;
		this.log = log;
		simulator = new LiftSimulator();  // Does nothing.
	}
	
	/**
	 * Determine which lift (real or simulator) is used on the normal
	 * lift interface. In automatic mode, this is the real interface.
	 */
	private LiftInterface getNormalInterface() {
		if (isAuto()) {
			return real;
		}
		return simulator;
	}

	/**
	 * Determine which lift (real or simulator) is used on the override
	 * lift interface. In automatic and off modes, this is the simulator
	 * interface so that the button box overrides are ignored.
	 */
	private LiftInterface getOverrideInterface() {
		if (isManual()) {
			return real;
		}
		return simulator;
	}

	// Mode change methods.
	public LiftInterface setAutomaticMode() {
		// This may need to be more clever to carry over state.
		log.sub("Lift switched to normal/automatic mode");
		mode = OverrideMode.AUTOMATIC;
		return this;
	}
	
	public LiftInterface setManualMode() {
		// This may need to be more clever to carry over state.
		log.sub("Lift switched manual mode");
		mode = OverrideMode.MANUAL;
		return this;
	}

	public LiftInterface turnOff() {
		// This may need to be more clever to carry over state.
		log.sub("Lift switched off");
		mode = OverrideMode.OFF;
		return this;
	}

	private boolean isManual() {
		return mode == OverrideMode.MANUAL;
	}
	
	private boolean isAuto() {
		return mode == OverrideMode.AUTOMATIC;
	}

	/**
	 * If in manual mode, send commands to the real lift.
	 */
	public LiftInterface overrideGear(boolean lowGear) {
		if (lowGear) {
			getOverrideInterface().setLowGear();
		} else {
			getOverrideInterface().setHighGear();
		}
		return this;
	}

	public LiftInterface overrideLiftHeightByDelta(double delta) {
		getOverrideInterface().setHeight(getOverrideInterface().getDesiredHeight() + delta);
		return this;
	}

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
	public boolean isEnabled() {
		return getNormalInterface().isEnabled();
	}

	@Override
	public void cleanup() {
		getNormalInterface().cleanup();
	}

	@Override
	public void execute(long timeInMillis) {
		if (isAuto() || isManual()) {
			real.execute(timeInMillis);
		}
		simulator.execute(timeInMillis);
	}
	
	/**
	 * Under automatic mode, send commands to the real interface
	 * otherwise ignore them by sending them to the simulator.
	 */
	@Override
	public LiftInterface setHeight(double height) {
		getNormalInterface().setHeight(height);
		return this;
	}

	@Override
	public double getDesiredHeight() {
		return getNormalInterface().getDesiredHeight();
	}

	@Override
	public double getHeight() {
		return getNormalInterface().getHeight();
	}

	@Override
	public boolean isInPosition() {
		return getNormalInterface().isInPosition();
	}

	@Override
	public LiftInterface setLowGear() {
		return getNormalInterface().setLowGear();
	}

	@Override
	public LiftInterface setHighGear() {
		return getNormalInterface().setHighGear();
	}

	@Override
	public boolean isInLowGear() {
		return getNormalInterface().isInLowGear();
	}

	@Override
	public boolean isAboveIntakeThreshold() {
		return getNormalInterface().isAboveIntakeThreshold();
	}

	@Override
	public boolean isSafeToShift() {
		return getNormalInterface().isSafeToShift();
	}
}
