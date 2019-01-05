package org.team3132.driveRoutines;

import java.util.function.DoubleSupplier;

import org.strongback.components.Clock;
import org.team3132.Constants;
import org.team3132.interfaces.LocationInterface;
import org.team3132.interfaces.Log;
import org.team3132.lib.Position;
import org.team3132.lib.WaypointUtil;

import jaci.pathfinder.Trajectory;
import jaci.pathfinder.followers.EncoderFollower;

/**
 * Walks the drivebase through a pair of Trajectories, one for the left
 * and one for the right side of the drivebase.
 */
public class DriveRoutineTrajectory implements DriveRoutine {
    static private double ENCODER_SCALING_FACTOR = 100;

    private final double scale;
    private Clock clock;
	private final Log log;
	
	private DoubleSupplier leftDistance, rightDistance;
	private final LocationInterface location;
    private EncoderFollower leftFollower, rightFollower;
    private Position initialPosition;
    static private boolean enabled = true;
    private final int numSegments;
    private int segmentNum = 0;
	private double nextUpdateSec = 0;
	private final double updatePeriodSec = 0.5;		
    
	public DriveRoutineTrajectory(Trajectory[] trajectories, boolean forward,
			DoubleSupplier leftDistance, DoubleSupplier rightDistance,
			LocationInterface location, Clock clock, Log log) {
		scale = forward ? 1 : -1;
		this.leftDistance = leftDistance;
		this.rightDistance = rightDistance;
		this.location = location;
		this.clock = clock;
		this.log = log;
		this.initialPosition = location.getCurrentLocation();
		log.info("Starting to drive trajectory");
		
        // Run the trajectories, one per side, using the encoders as feedback.
        leftFollower = createFollower(trajectories[0], leftDistance.getAsDouble());
        rightFollower = createFollower(trajectories[1], rightDistance.getAsDouble());
        numSegments = trajectories[0].length();
        
        // Allow it to run.
        enabled = true;
	}
	
	private EncoderFollower createFollower(Trajectory traj, double initialPosition) {
		EncoderFollower follower = new EncoderFollower(traj);
		// The encoders have been configured to return inches, which doesn't play
		// well with the EncoderFollower. Convert the values to 1/100ths of an inch
		// so that they can be pretended to be inches.
		int initial_position = distanceToFakeTicks(initialPosition);
		int ticks_per_revolution = (int) ENCODER_SCALING_FACTOR;
		double wheel_diameter = 1 / Math.PI;  // One turn moves 1 inch.
		follower.configureEncoder(initial_position, ticks_per_revolution, wheel_diameter);
		double kp = 0.08;  // Needs tuning.
		double ki = 0;  // Unused.
		double kd = 0;
		double kv = 1 / Constants.DRIVE_MAX_SPEED;
		double ka = 0.1;  // Needs tuning.
		follower.configurePIDVA(kp, ki, kd, kv, ka);
		return follower;
	}
	
	private static int distanceToFakeTicks(double distance) {
		return (int) (distance * ENCODER_SCALING_FACTOR);
	}
	
	static public void disable() {
		// Can only be disabled.
		enabled = false;
	}

	@Override
	public DriveMotion getMotion() {
		segmentNum++;
		updateLocationSubsystem();
		// WARNING: The follower routine in use doesn't measure how much time has passed
		// between steps - it assumes that everything it running perfectly to time.
		// This will cause inaccuracies and weird errors.
		// It would ideally be re-written to check the time between calls.
		if (!enabled || leftFollower.isFinished()) {
			// It's done, return zero.
			//log.sub("auto driving done, enabled = %s, isFinished = %s", enabled, leftFollower.isFinished());
			logProgress();
			return new DriveMotion(0, 0);
		}
		maybeLogProgress();
		// Calculate the new speeds for both left and right motors.
		double leftPower = leftFollower.calculate(distanceToFakeTicks(leftDistance.getAsDouble()));
		double rightPower = rightFollower.calculate(distanceToFakeTicks(rightDistance.getAsDouble()));
		if (leftFollower.isFinished()) {
			log.info("Finished driving trajectory");
		}
		//log.sub("drive power = %.1f %.1f", leftPower, rightPower);
		return new DriveMotion(scale * leftPower, scale * rightPower);
	}
	
	public boolean isFinished() {
		return leftFollower.isFinished();
	}
	
	/**
	 * Tell the location subsystem where we should be so it can be recorded
	 * for plotting against the actual position.
	 */
	private void updateLocationSubsystem() {
		final Position desired = WaypointUtil.toPosition(leftFollower.getSegment(), rightFollower.getSegment());
		//log.sub("desired postion %.1f,%.1f - %.1f,%.1f", leftFollower.getSegment().x, leftFollower.getSegment().y, rightFollower.getSegment().x, rightFollower.getSegment().y);
		location.setDesiredLocation(initialPosition.add(desired));
	}
	
	/**
	 * Periodically log the progress on the console.
	 */
	private void maybeLogProgress() {
		double now = clock.currentTime();
		if (now < nextUpdateSec) return;
		nextUpdateSec = now + updatePeriodSec;
		logProgress();
	}

	private void logProgress() {
		final int PROGRESS_LENGTH = 50;
		// Print a progress indicator like:
		// |=======================>      |
		int progress = (PROGRESS_LENGTH - 2) * segmentNum / numSegments;
		StringBuilder b = new StringBuilder(PROGRESS_LENGTH);
		b.append('|');
		while (progress-- > 0) b.append('=');
		if (b.length() < PROGRESS_LENGTH -1) b.append('>');
		while (b.length() < PROGRESS_LENGTH - 1) b.append(' ');
		b.append('|');
		log.info(b.toString());
	}
}
