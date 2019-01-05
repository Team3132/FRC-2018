package org.team3132.lib;

/*
 *  Angles are 'always' in DEGREES.
 *  
 *  There should be no angles expressed in radians within the codebase.
 *  Please always convert to degrees.
 *  
 *  This class provides degree versions of the needed trig functions.
 *  Add others as necessary.
 */
public class AngleUtil {
    /**
     * Rotate a vector in Cartesian space.
     * Angle is in degrees.
     * Cheerfully stolen from RobotDrive by WPI.
	 * @param x input X value of the vector
	 * @param y input Y value of the vector
	 * @param angle angle to rotate the vector (counter clockwise)
	 * @return
	 */
    public static double[] rotateVector(double x, double y, double angle) {
        double cosA = AngleUtil.cos(angle);
        double sinA = AngleUtil.sin(angle);
        double out[] = new double[2];
        out[0] = x * cosA - y * sinA;
        out[1] = x * sinA + y * cosA;
        return out;
    }

    /**
     * Normalise a value to within a range specified.
     * A helper function to perform double modulo arithmetic.
	 * Given a value and a range (-range/2 .. range/2) will quickly bring the value to within that range.
     * @param value input value to normalise
     * @param range bring the value into the set of numbers in -range/2 .. range/2
     * @return value brought into the specified range
     */
	public static final double normalise(double value, double range) {
		int quot;
		int	sign;
		double result;
		double range2;
		
		range2 = range/2.0;
		if (value >= -range2 && value <= range2) {
			return value;		// already in range
		}
		if (value < 0.0) {
			sign = -1;
			value = -value;
		} else {
			sign = 1;
		}
		quot = (int) (value/(range));
		result = value - (quot * range);
		if (result > (range2)) {
			result -= range;
		}
		result *= sign;
		if (result <= (-range2)) {
			result += range;
		}
		if (result == -0.0) {
			result = 0.0;
		}
		return result;
	}

	public static final double degreesToRadians(double d) {
		d = normalise(d, 360.0);
		return d * (Math.PI/180.0);
	}
	
	public static final double radiansToDegrees(double r) {
		r = normalise(r, Math.PI * 2);
		return r * (180.0/Math.PI);
	}
	
	public static double tan(double a) {
		return (Math.tan(degreesToRadians(a)));
	}
	
	
	public static double atan(double i) {
		return (radiansToDegrees(Math.atan(i)));
	}
	
	public static double cos(double a) {
		return (Math.cos(degreesToRadians(a)));
	}
	
	public static double sin(double a) {
		return (Math.sin(degreesToRadians(a)));
	}
}
