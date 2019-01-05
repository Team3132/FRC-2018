package org.team3132.interfaces;

import org.strongback.components.ui.InputDevice;

public interface OIInterface {
    
    /**
     * Configure the standard Operator Interface
	 * @param driverJoystick the drivers joystick
	 * @param operatorJoystick the operators joystick
     */
	public void configureJoysticks(InputDevice driverJoystick, InputDevice operatorJoystick);
    
    /**
	 * Configure the button box as a test interface device. This interface allows us
	 * a high degree of manual control over the robot
	 * 
	 * @param buttonBoxJoystick a hardware box that pretends to be a joystick
	 */
	public void configureOperatorBox(InputDevice buttonBoxJoystick);
}