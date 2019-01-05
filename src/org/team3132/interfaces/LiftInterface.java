package org.team3132.interfaces;

/*
 * The 2018 robot has a dual purpose lift.
 * In Lift mode its purpose is to raise and lower the intake/outtake device to allow us to pick up and deliver cubes.
 * in climb mode it allows us to raise the robot up the scale.
 * 
 * There are various sensors on the lift mechanism, a top and bottom stop indicator,
 * as well there is a sensor that will tell us how high the lift is currently.
 * 
 * The lift subsystems runs a state machine to raise and lower the lift autonomously.
 */
import org.strongback.Executable;
import org.team3132.Constants;

public interface LiftInterface extends SubsystemInterface, Executable, DashboardUpdater {
	
	/**
	 * Used to set positions on the lift.
	 * These are predetermined positions for ease of coding
	 */
	public enum LiftPosition {
		/**
		 * To be changed to three positions high, level and low
		 */
		SCALE_POSITION (Constants.LIFT_SCALE_HEIGHT_HIGHEST),
		/**
		 * Position just above the height of the fence around the switch
		 */
		SWITCH_POSITION (Constants.LIFT_SWITCH_HEIGHT),
		/**
		 * Position for intaking cubes
		 */
		INTAKE_POSITION (Constants.LIFT_INTAKE_HEIGHT),
		/**
		 * Position just above the height of the rung used before we actually climb
		 */
		CLIMB_POSITION (Constants.LIFT_CLIMB_HEIGHT),
		/**
		 * Position we move to climb our ~14 inches
		 */
		CLIMB_STOP_POSITION (Constants.LIFT_CLIMB_STOP_HEIGHT),
		/**
		 * Lowest height at which the intake can be safely deployed/stowed
		 */
		DEPLOY_THRESHOLD_HEIGHT (Constants.LIFT_DEPLOY_THRESHOLD_HEIGHT);
		
		public final double value;
		
		private LiftPosition(double value) {
			this.value = value;
		}
	}
	
	/**
	 * Moves the lift to a height
	 * @param height The requested height for the lift (inches)
	 */
	public LiftInterface setHeight(double height);
	
	/**
	 * Return the lift's desired height in inches
	 * @return desired height of the carriage from the base in inches
	 */
	public double getDesiredHeight();
	
	/**
	 * Returns the lift's current height in inches
	 * @return current height of the carriage from the base in inches
	 */
	public double getHeight();
	
	/**
	 * @return true if the lift is within tolerance of its setpoint
	 */
	public boolean isInPosition();
	
	/**
	 * Tells the lift to shift for low gear if safe to do so
	 */
	public LiftInterface setLowGear();

	/**
	 * Tells the lift to shift for high gear if safe to do so
	 */
	public LiftInterface setHighGear();
	
	/**
	 * @return true if in low (climbing) gear.
	 */
	public boolean isInLowGear();
	
	/**
	 * @return true if the lift is above the lowest height at which it is safe to deploy the intake
	 */
	public boolean isAboveIntakeThreshold();
	
	/**
	 * @return true if the lift is above height of the rung
	 */
	public boolean isSafeToShift();
}
