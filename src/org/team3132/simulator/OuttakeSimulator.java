package org.team3132.simulator;

import org.team3132.interfaces.OuttakeInterface;
import org.team3132.lib.MovementSimulator;

/**
 * Very basic outtake simulator used for unit testing.
 * Does not do gravity etc.
 */
public class OuttakeSimulator implements OuttakeInterface {
	String name = "OuttakeSimulator";
	
	private final double kMaxSpeed = 20;  // inches/sec
	private final double kMaxAccel = 20;   // inches/sec/sec
	private final double kMinPos = 0;
	private final double kMaxPos = 12; // One foot tall
	private final double kOpenedPos = kMaxPos;
	private final double kClosedPos = kMinPos;
	private final double kTolerance = 0.5;
	private MovementSimulator calc = new MovementSimulator("outtake", kMaxSpeed, kMaxAccel, kMinPos, kMaxPos, kTolerance);
	private long lastTimeMs = 0;
	private boolean hasCube = false;
	private double outtakePower = 1;
	
	public OuttakeSimulator() {
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
		// Update the intake position.
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

	public OuttakeSimulator setHasCube(boolean hasCube) {
		this.hasCube = hasCube;
		return this;
	}

	@Override
	public boolean closeOuttake() {
		if (calc.getTargetPos() != kClosedPos) {
			System.out.println("  Closing outtake");
			calc.setTargetPos(kClosedPos);
		}
		return calc.isInPosition();
	}

	@Override
	public boolean openOuttake() {
		if (calc.getTargetPos() != kOpenedPos) {
			System.out.println("  Opening outtake");
			calc.setTargetPos(kOpenedPos);
		}
		return calc.isInPosition();
	}

	@Override
	public boolean isClosed() {
		return calc.getTargetPos() == kClosedPos && calc.isInPosition();
	}
	
	@Override
	public boolean isOpen() {
		return calc.getTargetPos() == kOpenedPos && calc.isInPosition();
	}

	@Override
	public void setOuttakeMotorOutput(double power) {
		this.outtakePower = power;
	}

	@Override
	public double getOuttakeMotorOutput() {
		return outtakePower;
	}
	
	@Override
	public String toString() {
		return String.format(calc.toString());
	}
}
