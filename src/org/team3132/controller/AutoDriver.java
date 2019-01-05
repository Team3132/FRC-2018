package org.team3132.controller;

import org.team3132.Constants;
import org.team3132.controller.Controller.TrajectoryGenerator;
import org.team3132.driveRoutines.DriveRoutineTrajectory;
import org.team3132.interfaces.Log;
import org.team3132.lib.Position;
import org.team3132.lib.WaypointUtil;
import org.team3132.subsystems.Subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;

import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;

/**
 * Wrap driving code so that the final position can be printed with
 * the difference from where it was expected to be.
 */
public class AutoDriver {
	private final Waypoint[] waypoints;
	private final Subsystems subsystems;
	private DriveRoutineTrajectory driveRoutine;
	private final Log log;
	private boolean finished = false;
	private Position initialPos;
	private Waypoint finalWaypoint;

	public AutoDriver(Waypoint[] waypoints, boolean forward, boolean relative,
			TrajectoryGenerator generator, Subsystems subsystems) {
		this.waypoints = waypoints;
		this.subsystems = subsystems;
		this.log = subsystems.log;
		if (waypoints == null) {
			log.sub("No waypoints/driving for this state");
			finished = true;
			return;
		}
		initialPos = subsystems.location.getCurrentLocation();
		// If going in reverse, change the apparent direction of the robot so the
		// motor powers can be reversed.
		if (!forward) initialPos.heading += Constants.HALF_CIRCLE;
		if (!relative) {
			// Convert to relative waypoints.
			waypoints = WaypointUtil.subtract(waypoints, WaypointUtil.toWaypoint(initialPos));
		}
		finalWaypoint = waypoints[waypoints.length-1];
		Trajectory[] trajectories = generator.generate(waypoints);
		driveRoutine = new DriveRoutineTrajectory(trajectories, forward, subsystems.leftDriveDistance,
				subsystems.rightDriveDistance, subsystems.location, subsystems.clock, log);
	}
	
	public void start() {
		// Start driving if there are waypoints.
		if (waypoints == null) {
			log.sub("No waypoints/driving for this state");
			return;
		}
		subsystems.drivebase.setDriveRoutine(driveRoutine, ControlMode.PercentOutput);
	}
	
	public boolean isFinished() {
		if (!finished && driveRoutine.isFinished()) {
			// Go back to the default drive routine.
			subsystems.drivebase.setDriveRoutine(null, ControlMode.PercentOutput);
			logFinalPosition();
			finished = true;
		}
		return finished;
	}
	
	/**
	 * Print out the final position and the difference to where it is expected to be.
	 */
	private void logFinalPosition() {
		Position finalPos = subsystems.location.getCurrentLocation();
		Position expectedDiff = WaypointUtil.toPosition(finalWaypoint);
		Position actualDiff = finalPos.getRelativeToLocation(initialPos);
		Position zeroPos = new Position(0, 0, 0);
		double expectedDist = expectedDiff.distanceTo(zeroPos);
		double actualDist = finalPos.distanceTo(initialPos);
		double percentage = 100;
		if (expectedDist != 0) percentage = 100 * actualDist / expectedDist;
		log.info("Finished driving, expected to be at %s but got to %s", expectedDiff, actualDiff);
		log.info("Travelled %.1f%% of the expected %.1f inches", percentage, expectedDist);
	}
}
