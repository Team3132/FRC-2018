package org.team3132.lib;

import org.strongback.components.Clock;
import org.team3132.Constants;

/**
 * This class provides a history of where the robot was. It is used by the vision system to accurately determine where the robot was,
 * so that the exact field position of a target can be determined. We keep the history for a fixed number of seconds, being the maximum
 * time that we may be asked for the previous location.
 * 
 * The history is stored in a circular buffer, so older values are overwritten with newer values.
 */
public class LocationHistory {
	private final Clock clock;
	private final Position[] historyArray = new Position[HISTORY_LENGTH];
	private int nextWriteIndex;
	private static final int HISTORY_LENGTH = Constants.LOCATION_HISTORY_MEMORY_SECONDS * Constants.LOCATION_HISTORY_CYCLE_SPEED;
	
	public LocationHistory(Clock clock) {
		this.clock = clock;
		setInitial(new Position(0,0,0,0,0));
	}
	
	/**
	 * Get location as a position at the specified time
	 * @param timeSec time that we wish to know position
	 * @return position at specified time accurate to the nearest 5ms or a position with 0 values if there is no existing position yet
	 */
	public Position getLocation(double timeSec) {
		double now = clock.currentTime();
		if (timeSec < now - Constants.LOCATION_HISTORY_MEMORY_SECONDS) {
			// too long ago. 
			return historyArray[nextWriteIndex];	// oldest value we have!
		}
		timeSec = Math.min(timeSec, clock.currentTime()); // Make sure the time isn't in the future.
		return historyArray[secondsToIndex(timeSec)];
	}
	
	/**
	 * Set the initial values for the history array. We do this to ensure that all previous times are set to the initial value.
	 * @param filler the location to fill the array with
	 */
	public void setInitial(Position filler) {
		for (int i = 0; i < HISTORY_LENGTH; i++){
			historyArray[i] = filler;
		}
		nextWriteIndex = secondsToIndex(clock.currentTime());
	}
	
	/**
	 * Add a location to the history. We should include the averaging of the two values as we move between them.
	 * That's better than just the new value for all points. I.e. a movement rather than a jump.
	 * @param position position of the robot, including the time that the robot was at that position.
	 */
	public void addLocation(Position position) {
		// Make a deep copy in case the original one is overwritten.
		Position p = new Position(position.x, position.y, position.heading, position.speed, position.timeSec);
		int index = secondsToIndex(p.timeSec);
		while (nextWriteIndex != (index + 1) % HISTORY_LENGTH) {
			historyArray[nextWriteIndex] = p;
			nextWriteIndex++;
			nextWriteIndex %= HISTORY_LENGTH;
		}
	}
	
	/**
	 * Converts a time in milliseconds into an index 
	 * @param timeSec desired time in milliseconds
	 * @return index to be called in historyArray
	 */
	private int secondsToIndex(double timeSec){
		return ((int)(Math.round(timeSec*Constants.LOCATION_HISTORY_CYCLE_SPEED))) % HISTORY_LENGTH;
	}
}