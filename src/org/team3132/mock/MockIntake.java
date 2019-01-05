package org.team3132.mock;

import org.team3132.interfaces.IntakeInterface;
import org.team3132.interfaces.Log;


/**
 * Mock subsystem responsible for the intake
 */
public class MockIntake implements IntakeInterface {
	private double output = 0;

	public MockIntake(Log log) {
	}

	@Override
	public IntakeInterface setIntakeMotorOutput(double output) {
		this.output = output;
		return this;
	}

	@Override
	public double getIntakeMotorOutput() {
		return output;
	}

	public double getIntakeOutput() {
		return output;
	}
	
	@Override
	public String getName() {
		return "MockIntake";
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

	@Override
	public IntakeInterface setConfiguration(IntakeConfiguration configuration) {
		return this;
	}

	public IntakeConfiguration getConfiguration() {
		return IntakeConfiguration.MOVING;
	}
	
	public boolean isStowed() {
		return getConfiguration() == IntakeConfiguration.STOWED;
	}

	@Override
	public boolean isInDesiredState() {
		return false;
	}
}