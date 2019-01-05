package org.team3132.interfaces;

import org.strongback.Executable;
import org.team3132.driveRoutines.DriveRoutine;

import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * The Drivebase subsystem is responsible for dealing with the drivebase.
 * It will call the location subsystem when things on the drivebase change, and it
 * requests information from the DriveControl to tell it how to move.
 * 
 * The Drivebase is passed the motors and other devices it uses and implements the
 * control algorithms needed to co-ordinate actions on these devices.
 * 
 * We use two helper classes to pass information:
 * DriveThrottles: for the left and right "power" values
 * DriveEncoders: for the left and right encoder counts
 * 
 */
public abstract interface DrivebaseInterface extends Executable, SubsystemInterface, DashboardUpdater {
	
	/**
	 * Set the drive control for the drivebase. This allows us to specify the different driving
	 * style (auto, tank, joystick, gamepad, whatever) with any desired assists (turn controller etc)
	 * just be using a different class that implements the drive control interface.
	 * 
	 * @param newDC The drive control to use to control the robot. If null revert to the default driveControl.
	 * @param mode The new mode to operate at for this drive control
	 * @return the old drive control value.
	 */
	public DriveRoutine setDriveRoutine(DriveRoutine newDC, ControlMode mode);
}