package org.team3132.interfaces;

import org.team3132.lib.Position;

/**
 * The Vision Subsystem - handles all interaction with the roboRio and the external processor for vision
 * 
 * The vision subsystem has listens for messages indicating when and where a vision target was last seen.
 */
public interface VisionInterface {

	/**
	 * Information about the last target seen.
	 */
	public static class TargetDetails {
		public boolean targetFound = false;  // Was a target seen.
		public double seenAtSec; // What time this target was seen at in seconds since boot.
		public Position location;  // Co-ordinates relative to the location subsystem.
		public double height;  // How high the target is.
		
		@Override
		public String toString() {
			return String.format("found: %s, at %f, location %s", targetFound, seenAtSec, location);
		}
	}
	
	/**
	 * Returns a TargetDetails if the vision processor has seen a target in the last frame.
	 * 
	 * Note the robot has likely moved since this was seen, so it's important to
	 * use the calculated location.
	 * If there was more than one seen, then return the closest one.
	 * 
	 * @return A TargetDetails.
	 */
	public TargetDetails getTargetDetails();
	
	/**
	 * @return hasConnection returns the current status of the connection to the external vision processor
	 */
	public boolean isConnected();	
}
