package org.team3132.lib;

import org.team3132.Constants;
import org.team3132.lib.MathUtil;


/*
 *  Distances are in inches. Angles are always in degrees.
 */
public class Position {
	public double x;		// +ve is to the right of the robot in inches.
	public double y;		// -ve is forwards in inches.
	public double heading;  // Angle in degrees from initial direction (can be positive or negative and multiple turns)
	                        // +ve angle is anticlockwise.
    public double speed;	// current speed (inches/second)
    public double timeSec;		// time of this location (ms after start)
            
    public Position(double x, double y) {
    	this(x, y, 0, 0, 0);
	}
    
    public Position(double x, double y, double headingDegrees) {
    	this(x, y, headingDegrees, 0, 0);
	}
    
    public Position(double x, double y, double headingDegrees, double speed) {
    	this(x, y, headingDegrees, speed, 0);
	}   
    
    public Position(double x, double y, double headingDegrees, double speed, double time) {
    	this.x = x;
    	this.y = y;
    	this.heading = headingDegrees;
    	this.speed = speed;
    	this.timeSec = time;
	}

	public Position(Position position) {
    	this.x = position.x;
    	this.y = position.y;
    	this.heading = position.heading;
    	this.speed = position.speed;
    	this.timeSec = position.timeSec;
	}

	// Take two field oriented positions and returns a location relative to the second location.
    public Position getRelativeToLocation(Position otherLocation) {
        // Subtract off the robots position from the position.
        double newX = x - otherLocation.x;
        double newY = y - otherLocation.y;
        // Rotate to match the robots orientation.
        double result[] = MathUtil.rotateVector(newX, newY, -otherLocation.heading);
        // Subtract off the robots angle from the original angle so the angle is also relative.
        Position loc = new Position(result[0], result[1], heading - otherLocation.heading, 0, 0);
        return loc;
    }
    
    // Add another positon on to the current position.
    public Position add(Position other) {
        // Rotate to match the robots orientation.
        double result[] = MathUtil.rotateVector(other.x, other.y, heading);
    	return new Position(x + result[0], y + result[1], heading + other.heading);
    }
    
    @Override
    public String toString() {
        return String.format("X(%.3f),Y(%.3f),H(%.3f),S(%3f)", x, y, heading, speed);
    }
    
    public String toCompactString() {
        return String.format("P(%.1f,%.1f@%.1f\u00B0@%.1f\"/s)", x, y, heading, speed);
    }
    
    public void copyFrom(Position other) {
        x = other.x;
        y = other.y;
        heading = other.heading;
        speed = other.speed;
        timeSec = other.timeSec;
    }
    
    public final Position addVector(double distance, double angle) {
    	/*
    	 * Change a location by the vector supplied.
    	 * The final heading is aligned with the vector
    	 * 
    	 * Angle is in degrees.
    	 */
    	double newX = distance * MathUtil.sin(angle);
    	double newY = distance * MathUtil.cos(angle);
    	return new Position(x + newX, y - newY, angle);
    }
    
    /**
     * Return the bearing from this position to the destination position. The bearing is from -180 to 180 degrees.
     * If the destination has the same X and Y co-ordinates the bearing is defined as the destination heading normalised.
     * @param dest
     * @return
     */
	public double bearingTo(Position dest) {
		double angle;
		double diffX = x - dest.x;
		double diffY = y - dest.y;
		if (diffY == 0) {
			// can't calculate atan on a vertical trajectory
			if (diffX > 0.0) {
				angle = Constants.QUARTER_CIRCLE;
			} else if (diffX < 0.0) {
				angle = -Constants.QUARTER_CIRCLE;
			} else {	// we aren't moving - return the normalised destination direction
				angle = MathUtil.normalise(dest.heading, Constants.FULL_CIRCLE);
			}
		} else {
			// Question: Should we be using atan2() instead?
			angle = -MathUtil.atan(diffX/diffY);
			// there are four quadrants, we have to treat each differently
			if (diffY <= 0) {
				if (diffX >= 0.0) {
					angle = angle - (Constants.HALF_CIRCLE);
				} else {
					angle = angle + (Constants.HALF_CIRCLE);
				}
			}
		}
		return angle;
	}

	public double distanceTo(Position dest) {
		double diffX = dest.x - x;
		double diffY = dest.y - y;
		// See, learning about Pythagoras was useful :)
		return Math.sqrt((diffX * diffX) + (diffY * diffY));
	}
	
	/**
	 * Calculate the angle between two positions
	 * @param dest the destination angle as the heading value
	 * @return the normalised angle between the two headings
	 */
	public double angleBetweenBearings(Position dest) {
		return Math.abs(MathUtil.normalise(dest.heading - heading, Constants.FULL_CIRCLE));
	}
	
	public String getDygraphHeader(String name) {
		return String.format("%1$s/X,%1$s/Y,%1$s/Heading,%1$s/Speed,", name);
	}
	
	public String getDygraphData() {
		return String.format("%f,%f,%f,%f", x, y, heading, speed);
	}
	
	private boolean doubleEqual(double a, double b) {
		return Math.abs(a - b) < 0.1;
	}
	
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Position)) {
			return false;
		}
		Position p = (Position)o;		
		return doubleEqual(x, p.x) && doubleEqual(y, p.y) && doubleEqual(heading, p.heading) && doubleEqual(speed, p.speed);  
	}
}
