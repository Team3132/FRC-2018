package org.team3132.mock;

import org.team3132.interfaces.Log;
import org.team3132.interfaces.OuttakeInterface;

/**
 * Mock subsystem responsible for the for the outtake
 */
public class MockOuttake implements OuttakeInterface {
	private String name = "MockOuttake";

	public MockOuttake(Log log) {
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void enable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(long timeInMillis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isClosed() {
		return false;
	}
	
	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public void setOuttakeMotorOutput(double power) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public double getOuttakeMotorOutput() {
		return 0;
	}

	@Override
	public boolean closeOuttake() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean openOuttake() {
		// TODO Auto-generated method stub
		return false;
	}
}
