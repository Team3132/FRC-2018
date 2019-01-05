package org.team3132.simulator;

import org.team3132.Constants;
import org.team3132.interfaces.LiftInterface;
import org.team3132.lib.MovementSimulator;

/**
 * Very basic lift simulator used for unit testing.
 * Does not do gravity etc.
 */
public class LiftSimulator implements LiftInterface {
	String name = "LiftSimulator";
	
	private final double kMaxSpeed = 20;  // inches/sec
	private final double kMaxAccel = 10;   // inches/sec/sec
	private final double kMinPos = 0;
	private final double kMaxPos = 5 * 12; // Five feet tall.
	private final double kTolerance = 0.5;
	private MovementSimulator calc = new MovementSimulator("lift", kMaxSpeed, kMaxAccel, kMinPos, kMaxPos, kTolerance);
	private boolean isLowGear = false;
	private long lastTimeMs = 0;
		
	public LiftSimulator() {
	}

	@Override
	public double getHeight() {
		return calc.getPos();
	}

	@Override
	public LiftInterface setHeight(double setpoint) {
		if (calc.getTargetPos() == setpoint) return this;
		System.out.printf("  Setting lift height to %.1f\n", setpoint);
		calc.setTargetPos(setpoint);
		return this;
	}
	
	/**
	 * Overrides the lift height ignoring time and simulator.
	 * @param height
	 * @return
	 */
	public LiftInterface setLiftHeightActual(double height) {
		calc.setPos(height);
		calc.setSpeed(0);  // Reset speed.
		return this;
	}

	@Override
	public double getDesiredHeight() {
		return calc.getTargetPos();
	}

	@Override
	public boolean isInPosition() {
		return Math.abs(calc.getPos() - calc.getTargetPos()) < Constants.LIFT_DEFAULT_TOLERANCE;
	}

	@Override
	public LiftInterface setLowGear() {
		isLowGear = true;
		return this;
	}

	@Override
	public LiftInterface setHighGear() {
		isLowGear = false;
		return this;
	}
	
	@Override
	public boolean isInLowGear() {
		return isLowGear;
	}

	@Override
	public boolean isAboveIntakeThreshold() {
		return getHeight() > Constants.LIFT_DEPLOY_THRESHOLD_HEIGHT - Constants.LIFT_DEFAULT_TOLERANCE;
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
		if (lastTimeMs == 0) {
			lastTimeMs = timeInMillis;
			return;
		}
		// Update the lift position.
		calc.step((timeInMillis - lastTimeMs) / 1000.);
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
	public String toString() {
		return calc.toString();
	}
}
