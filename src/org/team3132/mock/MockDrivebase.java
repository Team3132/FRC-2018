package org.team3132.mock;

import org.team3132.driveRoutines.DriveRoutine;
import org.team3132.interfaces.DrivebaseInterface;
import org.team3132.interfaces.Log;

import com.ctre.phoenix.motorcontrol.ControlMode;

public class MockDrivebase implements DrivebaseInterface  {
	String name = "MockDrivebase";
	Log log;
	
	public MockDrivebase(Log log) {
		this.log = log;
	}

	@Override
	public void execute(long timeInMillis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
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
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DriveRoutine setDriveRoutine(DriveRoutine newDC, ControlMode mode) {
		// TODO Auto-generated method stub
		return newDC;
	}

}
