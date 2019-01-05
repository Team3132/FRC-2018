package org.team3132.subsystems;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockTalonSRX;
import org.team3132.driveRoutines.DriveRoutine;
import org.team3132.interfaces.DrivebaseInterface;
import org.team3132.mock.MockDashboard;
import org.team3132.mock.MockLog;

import com.ctre.phoenix.motorcontrol.ControlMode;

public class TestDrivebase {

	class MockDriveRoutine implements DriveRoutine {
		public int callCount = 0;
		public double leftSpeed = 0;
		public double rightSpeed = 0;
		
		@Override
		public DriveMotion getMotion() {
			callCount++;
			return new DriveMotion(leftSpeed, rightSpeed);
		}

		@Override
		public void activate() {
		}

		@Override
		public void deactivate() {
		}
	}
	
    @Test
    public void testDrivedriveRoutine() {
    	MockTalonSRX leftMotor = Mock.TalonSRXs.talonSRX(0);
    	MockTalonSRX rightMotor = Mock.TalonSRXs.talonSRX(0);
    	MockDriveRoutine driveRoutine = new MockDriveRoutine();
        DrivebaseInterface drive = new Drivebase(leftMotor, rightMotor, driveRoutine, ControlMode.PercentOutput, new MockDashboard(), new MockLog());
        int expectedCallCount = 0;

        // Subsystems should start disabled, so shouldn't be calling the DrivedriveRoutine.
        assertEquals(expectedCallCount, driveRoutine.callCount);
        drive.execute(0);
        assertEquals(expectedCallCount, driveRoutine.callCount);
        assertEquals(0, leftMotor.getLastDemand(), 0.01);
        assertEquals(0, rightMotor.getLastDemand(), 0.01); 

        // Enable the drivebase
        driveRoutine.leftSpeed = 0.5;
        driveRoutine.rightSpeed = 0.75;
        drive.enable();
        drive.execute(0); // Should call getMotion() on driveRoutine.
        assertEquals(++expectedCallCount, driveRoutine.callCount);
        // Check that the motors now have power.
        assertEquals(driveRoutine.leftSpeed, leftMotor.getLastDemand(), 0.01);
        assertEquals(driveRoutine.rightSpeed, rightMotor.getLastDemand(), 0.01);

        // Update the speed and see if the motors change.
        driveRoutine.leftSpeed = -0.1;
        driveRoutine.rightSpeed = 1;
        drive.execute(0); // Should call getMotion() on driveRoutine.
        assertEquals(++expectedCallCount, driveRoutine.callCount);
        // Check that the motors now have power.
        assertEquals(driveRoutine.leftSpeed, leftMotor.getLastDemand(), 0.01);
        assertEquals(driveRoutine.rightSpeed, rightMotor.getLastDemand(), 0.01);
        
        // Change driveRoutine and see if the outputs are different
    	MockDriveRoutine differentDriveRoutine = new MockDriveRoutine();
    	differentDriveRoutine.leftSpeed = 1;
    	differentDriveRoutine.rightSpeed = -1;
    	drive.setDriveRoutine(differentDriveRoutine, ControlMode.PercentOutput);
    	drive.execute(0);
        assertEquals(1, differentDriveRoutine.callCount); // first time running this driveRoutine
        assertEquals(differentDriveRoutine.leftSpeed, leftMotor.getLastDemand(), 0.01);
        assertEquals(differentDriveRoutine.rightSpeed, rightMotor.getLastDemand(), 0.01);

        // Disable and confirm that the driveRoutine isn't called and the motors are stopped
        drive.disable();
        drive.execute(0); // Should no call getMotion() on driveRoutine.
        assertEquals(expectedCallCount, driveRoutine.callCount);
        assertEquals(0, leftMotor.getLastDemand(), 0.01);
        assertEquals(0, rightMotor.getLastDemand(), 0.01);        
    }

}
