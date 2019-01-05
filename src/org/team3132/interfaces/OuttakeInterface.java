package org.team3132.interfaces;

import org.strongback.Executable;

public interface OuttakeInterface extends SubsystemInterface, Executable, DashboardUpdater  {
		
	/**
	 * Closes the outtake.
	 * @return true if closed.
	 */	
	public boolean closeOuttake();
	
	/**
	 * Open the outtake.
	 * @return true if open.
	 */
	public boolean openOuttake();
		
	/**
	 * @return true if the outtake is closed 
	 */
	public boolean isClosed();
	
	/**
	 * @return true if the outtake is open
	 */
	public boolean isOpen();

	/**
	 * Set outtake power
	 * @param power power to apply to outtake motor
	 */
	public void setOuttakeMotorOutput(double power);

	/**
	 * Get outtake power
	 * @return power to applied to outtake motor
	 */
	public double getOuttakeMotorOutput();
}
