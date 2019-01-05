package org.team3132.mock;

import org.strongback.components.Clock;
import org.team3132.Constants;
import org.team3132.interfaces.LiftInterface;
import org.team3132.interfaces.Log;

/**
 * Used to test things that depend on the Lift.
 * Also used when the robot's lift has been disabled and the robot shouldn't try talking
 * to the lift hardware.
 */
public class MockLift implements LiftInterface {
	String name = "MockLift";
	
	private double height = 0;
	private double setpoint = 0;
	private boolean isLowGear = false;
	public MockLift(Clock clock, Log log) {
	}

	public double getHeight() {
		return height;
	}

	@Override
	public LiftInterface setHeight(double setpoint) {
		height = setpoint;
		return null;
	}
	
	/**
	 * sets the lift height ignoring time
	 * @param newHeight
	 * @return
	 */
	public LiftInterface setLiftHeightActual(double newHeight) {
		this.setpoint = newHeight;
		height = newHeight;
		return this;
	}

	@Override
	public double getDesiredHeight() {
		return setpoint;
	}

	@Override
	public boolean isInPosition() {
		return Math.abs(height - setpoint) < Constants.LIFT_DEFAULT_TOLERANCE;
	}

	@Override
	public LiftInterface setLowGear() {
		isLowGear = true;
		return null;
	}

	@Override
	public LiftInterface setHighGear() {
		isLowGear = false;
		return null;
	}
	
	@Override
	public boolean isInLowGear() {
		return isLowGear;
	}

	@Override
	public boolean isAboveIntakeThreshold() {
		return getHeight() > Constants.LIFT_DEPLOY_THRESHOLD_HEIGHT;
	}
	
	@Override
	public boolean isSafeToShift() {
		return getHeight() > Constants.LIFT_SHIFTING_THRESHOLD_HEIGHT;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void enable() {
	}

	@Override
	public void disable() {
	}

	@Override
	public void execute(long timeInMillis) {
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void cleanup() {
	}
}
