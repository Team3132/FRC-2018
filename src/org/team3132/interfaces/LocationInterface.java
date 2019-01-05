package org.team3132.interfaces;

import org.strongback.Executable;
import org.team3132.lib.Position;

/**
 * The location system is responsible for tracking the location of the robot on the field.
 * It does this through reading the encoders and the gyro and plotting where the robot has moved.
 * This is guaranteed lossy, as we cannot update often enough to track every slight movement, and
 * undetected movements may occur, such as slippage of the wheels.
 * 
 * The location subsystem works in headings and bearings. When the robot starts we call the forward direction heading/bearing 0.
 * 
 * A heading is the angle that the robot has moved from the initial angle.
 * It can range from -infinity to infinity, so full circles change the heading by 360 degrees each time.
 * 
 * A bearing is the relative angle from the initial angle. In nautical terms it is the absolute bearing.
 * A bearing is constrained to the range -180 to 180 degrees.
 * 
 * The location subsystem is also responsible for keeping track of where the robot was on the field.
 * It can be asked for the historical location of the robot. We do not have to keep history from the start of the match,
 * only the last number of seconds.
 * The history parameters are defined in the Constants file as both a period and number of samples per second.
 * 
 * The Location system is an independent entity within the drive code. It is implemented to obtain information from sources,
 * possibly including gyros, the drivebase, other sensors, maybe even video and keep track of the robot's position at all times.
 * It also need to be able to return where the robot was at some time in the past.
 * This allows the code to work out relative angles to external objects based on current and historical positions (and through projection, future positions).
 */
public abstract interface LocationInterface extends Executable, DashboardUpdater  {
    
    /**
     * Set the current location. This allows a subsystem to override the location and force the location to a particular point.
     * In particular the start location should be set as accurately as possible, so the robot knows where it starts on the field
     * @param current The current location.
     */
    public void setCurrentLocation(Position location);
    
    /**
     * Return the location on the field at the current time.
     * @return the current location
     */
    public Position getCurrentLocation();

    /**
     * Set the desired location.
     * Usually used for the automatic driving to log where the robot should be.
     * @param position The current location.
     */
    public void setDesiredLocation(Position location);
    
    /**
     * Return the location on the field at the specified time
     * @param timeSec The time (in seconds) for which we wish to obtain the location
     * @return The location at the specified time
     */
	public Position getHistoricalLocation(double timeSec);
    
	/**
	 * Update our location on the field.
	 *
	 * We will need to get the movement of the robot since the last update
	 * and the heading of that movement and calculate the new position of the robot.
	 * 
	 * We also add this new value to the LocationHistory class which holds the history of where the robot has been on the field.
	 *
	 * The field orientation has the X axis across the driver's station and the Y axis between the alliance's ends.
	 * Positive for X is from left to right, and positive for Y is from the opposite alliance's end towards the driver's station.
	 *  
	 * X is along the "horizontal line", and Y is the "vertical" line.
	 * 
	 *                        ^  Y -ve
	 *                        |
	 *                        |
	 *       X -ve   <--------+-------->   X +ve
	 *                        |
	 *                        |
	 *                        v  Y +ve
	 * 
	 * 
	 * Heading angles:
	 *                        ^  0 degrees 
	 *                        |
	 *                        |
	 *   -90 degrees <--------+-------->   90 degrees
	 *                        |
	 *                        |
	 *                        v  -180/180 degrees
	 * 
	 * 
	 * y -= distance * cos(Heading)
	 * x += distance * sin(Heading)
	 * 
	 * If the encoder deltas are different then we have been moving in an arc.
	 * Assume that the arc is smooth and has straight entry and exit segments to calculate the new X and Y locations.
	 * The gyro is assumed correct, and is used to update the Heading.
	 * 
	 * First cut. assume we have moved the average distance of both fieldConfig at
	 * the average Heading between the start and the end Heading.
	 * This is not exact, but since we sample fast enough it is close enough for tracking how the robot is moving.
	 */
	public void update();

	/**
	 * Return the heading of the robot
	 * @return Returns the cumulative heading of the robot. This is a non discontinuous heading,
	 * so it moves from 360 to 361 degrees.
	 */
	public double getHeading();
	
	/**
	 * Return the robot heading restricted to -180 to 180 degrees
	 * @return the current bearing of the robot
	 */
	public double getBearing();
	
	/**
	 * Return the distance travelled over the last interval/time of the last interval.
	 * This has problems if we are sampling more often than the talon is updating, as the
	 * distance drops to zero (and so the speed drops to zero).
	 * 
	 * This should be a decaying moving average over the last few samples, with instantaneous zeros ignored to
	 * solve the sampling problem.
	 * @return speed for the last interval.
	 */
	public double getSpeed();
	
	/**
	 * Reset the heading of the gyro. AFter this method the getHeading() method will return 0.0
	 */
	public void resetHeading();
    

	/**
	 * enable the subsystem. Unless overridden this is handled by the Subsystem class
	 */
	public void enable();

	/**
	 * disable the subsystem. Unless overridden this is handled by the Subsystem class
	 */
	public void disable();
}

