package org.team3132.controller;

import java.util.ArrayList;

import org.team3132.interfaces.IntakeInterface.IntakeConfiguration;
import org.team3132.lib.WaypointUtil;

import jaci.pathfinder.Waypoint;

/**
 * Top level class to hold / specify some sort of current or target state.
 * 
 * This allows callers of the Controller to specify the target state.
 * These are held by 
 */
public class State {
	// Double and Boolean are used instead of double and boolean
	// so that null can be used to indicate that the state shouldn't
	// be changed and the current state be preserved.
	public Double liftHeight = null;  // Lift position.
	public Double liftHeightDelta = null;  // A delta to the current lift height.
	public LiftSetpointAction liftSetpointAction = null;  // Move up or down to the next defined lift position.
	public IntakeConfiguration intakeConfig = null;  // Stow, narrow, wide configurations.
	public Double intakeMotorOutput = null;  // How fast to spin the intake motors.
	public Boolean outtakeOpen = null;  // For holding the cube.
	public Double outtakeMotorOutput = null;  // How fast to spin the outtake motors.
	public Boolean rampRelease = null;  // Part of the endgame for lifting other robots.
	public Boolean lowGear = null;  // Lift uses low gear for climbing. This is a suggestion.
	public Double delayDeltaSec = null;  // How long to wait before moving on.
	public Double delayUntilTime = null;  // The time when the robot can move on.
	// Warning: Waypoints use a different coordinate system to Positions.
	// Waypoints have +x going forward and +y going left. They also use radians, with
	// more +ve numbers causing an anticlockwise turn.
	public Waypoint[] waypoints = null;  // Points that the robot should drive through.
	public boolean forward = true;  // Driving forward through the waypoints?
	public boolean relative_waypoints = false;  // Are the waypoints relative to current position or field?
	public Waypoint resetPosition = null;  // Reset where the location subsystem thinks the robot is.
	
	public enum LiftSetpointAction {
		NONE,
		NEXT_UP,  // Move to the next highest defined setpoint.
		NEXT_DOWN,  // Move down to the next lowest setpoint.
	};
	
	/** Set absolute lift height.
	 * Use only one of setLiftHeight(), setLiftHeightDelta(), setLiftHeightSetpoint().
	 * @param height in inches from the bottom of the lift.
	 */
	public State setLiftHeight(double height) {
		this.liftHeight = new Double(height);
		return this;
	}
	/**
	 * Adjust the current lift height by delta.
	 * Use only one of setLiftHeight(), setLiftHeightDelta(), setLiftHeightSetpoint().
	 * @param delta to apply to the current height.
	 */
	public State setLiftHeightDelta(double delta) {
		this.liftHeightDelta = new Double(delta);
		return this;
	}
	/**
	 * Adjust move the lift up to the next defined lift position.
	 * Use only one of setLiftHeight(), setLiftHeightDelta(), setLiftHeightSetpoint().
	 * @param delta to apply to the current height.
	 */
	public State setLiftHeightSetpoint(LiftSetpointAction action) {
		this.liftSetpointAction = action;
		return this;
	}
	public State setIntakeConfig(IntakeConfiguration config) {
		this.intakeConfig = config;
		return this;
	}
	public State setIntakeMotorOutput(double output) {
		this.intakeMotorOutput = new Double(output);
		return this;
	}
	public State setOuttakeOpen(boolean open) {
		this.outtakeOpen = new Boolean(open);
		return this;
	}
	public State setOuttakeMotorOutput(double output) {
		this.outtakeMotorOutput = new Double(output);
		return this;
	}
	public State setRampRelease(boolean release) {
		this.rampRelease = new Boolean(release);
		return this;
	}
	public State setLowGear(boolean lowGear) {
		this.lowGear = new Boolean(lowGear);
		return this;
	}
	/** Set absolute time that the robot has to wait until.
	 * Use this or setDelayDelta(), not both.
	 * @param time measured in seconds, eg time_t.
	 */
	public State setDelayUntilTime(double time) {
		this.delayUntilTime = new Double(time);
		return this;
	}
	/**
	 * Wait for delta seconds.
	 * Use this or setDelayUntilTime(), not both.
	 * @param delta to apply to the current time.
	 */
	public State setDelayDelta(double delta) {
		this.delayDeltaSec = new Double(delta);
		return this;
	}
	
	/**
	 * Add waypoints for the drivebase to drive through.
	 * Note: The robot will come to a complete halt after each list
	 * of Waypoints, so each State will cause the robot to drive and then
	 * halt ready for the next state. This should be improved.
	 * Waypoints are in field coordinates.
	 * @param waypoints list of Waypoints to drive through.
	 * @param forward drive forward through waypoints.
	 */
	public State setAbsoluteWaypoints(Waypoint[] waypoints, boolean forward) {
		this.waypoints = waypoints;
		this.forward = forward;
		this.relative_waypoints = false;
		return this;
	}
	
	/**
	 * Add waypoints for the drivebase to drive through.
	 * Note: The robot will come to a complete halt after each list
	 * of Waypoints, so each State will cause the robot to drive and then
	 * halt ready for the next state. This should be improved.
	 * Wayoints are relative to the robots position.
	 * @param waypoints list of Waypoints to drive through.
	 * @param forward drive forward through waypoints.
	 */
	public State setRelativeWaypoints(Waypoint[] waypoints, boolean forward) {
		this.waypoints = waypoints;
		this.forward = forward;
		this.relative_waypoints = true;
		return this;
	}
	
	/**
	 * Reset the position that the location subsystem thinks the robot is in.
	 * @param position The new position / orientation for the robot.
	 */
	public State setCurrentPosition(Waypoint position) {
		this.resetPosition = position;
		return this;
	}
	
	/**
	 * Append the description and value for this parameter if value is non null.
	 * @param name of the parameter.
	 * @param value of the parameter. May be null.
	 * @param result - StringBuilder to add to.
	 */
	private static <T> void maybeAdd(String name, T value, ArrayList<String> result) {
		if (value == null) return;  // Ignore this value.
		result.add(name + ":" + value);
	}
	
	/**
	 * Append the description and value for this parameter if value is non null.
	 * @param name of the parameter.
	 * @param value of the parameter. May be null.
	 * @param result - StringBuilder to add to.
	 */
	private static void maybeAdd(String name, Waypoint[] waypoints, boolean forward, ArrayList<String> result) {
		if (waypoints == null || waypoints.length == 0) return;  // Ignore this value.
		String direction = forward ? "forward" : "reverse";
		result.add(name + ": drive " + direction + " {" + WaypointUtil.toString(waypoints) + "}");
	}
	
	@Override
	public String toString() {
		ArrayList<String> result = new ArrayList<String>();
		maybeAdd("liftHeight", liftHeight, result);
		maybeAdd("liftHeightDelta", liftHeightDelta, result);
		maybeAdd("liftSetpoint", liftSetpointAction, result);
		maybeAdd("intakeConfig", intakeConfig, result);
		maybeAdd("intakeMotorOutput", intakeMotorOutput, result);
		maybeAdd("outtakeOpen", outtakeOpen, result);
		maybeAdd("outtakeMotorOutput", outtakeMotorOutput, result);
		maybeAdd("rampRelease", rampRelease, result);
		maybeAdd("lowGear", lowGear, result);
		maybeAdd("delayUntilTime", delayUntilTime, result);
		maybeAdd("delayDeltaSec", delayDeltaSec, result);
		maybeAdd("wayponts", waypoints, forward, result);
		return "[" + String.join(",", result) + "]";
	}
}
