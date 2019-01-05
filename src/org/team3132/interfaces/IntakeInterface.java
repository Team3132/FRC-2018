package org.team3132.interfaces;

/*
 * This mechanism allows a cube to be obtained across the width of the robot.
 * Cubes can be delivered out either the front, or out the left or right hand side.
 * 
 * The cubes can be moved across the mechanism from left to right side or vis-versa.
 * 
 * There used to be sensors on the mechanism to allow us to determine when the cube was contained,
 * as well as where the cube was within the mechanism. Both have been removed due to
 * mechanical/electrical unreliability.
 */
import org.strongback.Executable;

public interface IntakeInterface extends SubsystemInterface, Executable, DashboardUpdater {

	public enum IntakeConfiguration {
		STOWED,
		LEFT_WIDE_RIGHT_NARROW,
		LEFT_NARROW_RIGHT_WIDE,
		LEFT_WIDE_RIGHT_SUPER_NARROW,
		LEFT_SUPER_NARROW_RIGHT_WIDE,
		WIDE,
		NARROW,
		SUPER_NARROW,
		MOVING
	}
	
	/**
	 * Sets percentOutput for the intake motors
	 * @return this
	 */
	public IntakeInterface setIntakeMotorOutput(double output);

	public double getIntakeMotorOutput();
	
	/**
	 * Sets the desired Intake Configuration
	 * @param configuration
	 * @return
	 */
	public IntakeInterface setConfiguration(IntakeConfiguration configuration);
	
	/**
	 * @return the current configuration of the intake arms.
	 */
	public IntakeConfiguration getConfiguration();

	/**
	 * Has the intake moved to the desired state.
	 * @return
	 */
	boolean isInDesiredState();
}
