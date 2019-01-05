package org.team3132.lib;

import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;

public class WaypointUtil {

	/**
	 * Subtract two Waypoints.
	 * @return a - b
	 */
	public static Waypoint subtract(Waypoint a, Waypoint b) {
		return new Waypoint(a.x - b.x, a.y - b.y, a.angle - b.angle);
	}
	
	/**
	 * Add two Waypoints.
	 * @return a + b
	 */
	public static Waypoint add(Waypoint a, Waypoint b) {
		return new Waypoint(a.x + b.x, a.y + b.y, a.angle + b.angle);
	}
	
	/**
	 * Subtract a Waypoint from a list of Waypoints.
	 * @return l[] - b
	 */
	public static Waypoint[] subtract(Waypoint[] l, Waypoint b) {
		Waypoint[] result = new Waypoint[l.length];
		for (int i = 0; i < l.length; i++) {
			result[i] = subtract(l[i], b);
		}
		return result;
	}

	public static String toString(Waypoint w) {
		return String.format("(%.1f,%.1f,%.1f)", w.x, w.y, w.angle);
	}

	public static String toString(Waypoint[] l) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Waypoint w : l) {
			if (first) first = false;
			else builder.append(",");
			builder.append(toString(w));
		}
		return builder.toString();
	}
	
	// Conversion functions.
	// This needs to be standardized so that it's not confusing to debug.
	//
	//          | Forward | Left | Angle +ve dir    | Angle unit
	// ---------+---------+------+------------------+------------
	// Waypoint |   +x    |  +y  | counterclockwise | Radians
	// Position |   -y    |  -x  | counterclockwise | Degrees

	public static Waypoint toWaypoint(Position p) {
		return new Waypoint(-p.y, -p.x, AngleUtil.degreesToRadians(p.heading));
	}

	public static Position toPosition(Waypoint w) {
		return new Position(-w.y, -w.x, AngleUtil.radiansToDegrees(w.angle));
	}
	
	public static Position toPosition(Trajectory.Segment l, Trajectory.Segment r) {
		double x = (l.x + r.x) / 2;
		double y = (l.y + r.y) / 2;
		double velocity = (l.velocity + r.velocity) / 2;
		return new Position(x, y, l.heading, velocity);
	}
}
