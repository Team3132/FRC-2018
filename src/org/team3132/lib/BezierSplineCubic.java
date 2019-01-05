package org.team3132.lib;

/*
 * Class to calculate the position (and heading) of a bezier spline.
 * We are given the four enclosing points and the "distance" through the spline.
 * We return the point and heading
 */
public class BezierSplineCubic {
	
	private static Position splitLine(Position start, Position end, double distance) {
		double newX, newY;
		
		distance = MathUtil.clamp(distance, 0.0, 1.0);		// ensure within the interval.
		newX = ((end.x - start.x) * distance) + start.x;
		newY = ((end.y - start.y) * distance) + start.y;
		return new Position(newX, newY);
	}
	
	public static Position findBezierSplineCubic(Position start, Position control1, Position control2, Position end, double distance) {
		/*
		 *  return the Position (X,Y) and heading of a point that is distance (0.0 to 1.0) through the
		 *  Cubic Bezier Spline as specified.
		 */
		Position Q0, Q1, Q2;
		Position R0, R1;
		Position P;
		
		Q0 = splitLine(start, control1, distance);
		Q1 = splitLine(control1, control2, distance);
		Q2 = splitLine(control2, end, distance);
		R0 = splitLine(Q0, Q1, distance);
		R1 = splitLine(Q1, Q2, distance);
		P = splitLine(R0, R1, distance);
		P.heading = R0.bearingTo(R1);	// and calculate the heading between the two points
		return P;
	}
}
