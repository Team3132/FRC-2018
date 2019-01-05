package org.team3132.interfaces;

import org.strongback.Executable;

public interface LEDControllerInterface extends SubsystemInterface, Executable {
	
	public void setColour(double red, double green, double blue);
	
	public void doIdleColours();
}
