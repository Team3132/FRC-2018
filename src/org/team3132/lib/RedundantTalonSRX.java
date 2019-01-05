package org.team3132.lib;

import java.util.ArrayList;
import java.util.HashMap;

import org.strongback.Executable;
import org.strongback.Strongback;
import org.strongback.Executor.Priority;
import org.strongback.components.Clock;
import org.strongback.components.TalonSRX;
import org.strongback.components.TalonSensorCollection;
import org.team3132.interfaces.Log;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.ParamEnum;
import com.ctre.phoenix.motion.MotionProfileStatus;
import com.ctre.phoenix.motion.TrajectoryPoint;
import com.ctre.phoenix.motorcontrol.ControlFrame;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.RemoteFeedbackDevice;
import com.ctre.phoenix.motorcontrol.RemoteLimitSwitchSource;
import com.ctre.phoenix.motorcontrol.RemoteSensorSource;
import com.ctre.phoenix.motorcontrol.SensorTerm;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.StickyFaults;
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod;

/**
 * Creates what appears to be a single TalonSRX, but is really a list of
 * them, with some of them being able to lead (ie has encoder), and some
 * only able to follow.
 * 
 * Encoder speed and current are monitored for failure.
 * 
 * Logs when leadership changes or talons are disabled due to high current.
 * 
 * Most of this file is plumbing the TalonSRX methods through to one or more
 * of the underlying talons.
 */
public class RedundantTalonSRX implements TalonSRX, TalonSensorCollection, Executable {

	private ArrayList<TalonSRX> potentialLeaders;  // All leaders.
	private ArrayList<TalonSRX> followers;
	private TalonSRX leader;
	private TalonSensorCollection sensorCollection;
	private ArrayList<TalonSRX> otherLeaders;  // All possible leaders that aren't currently leading.
	private ArrayList<TalonSRX> activeTalons; // All possible leaders and the followers.
	private static ArrayList<TalonSRX> badTalons = new ArrayList<>();  // Talons that have drawn so much current.
	private static ArrayList<TalonSRX> badEncoders = new ArrayList<>(); // Talons that have bad encoders.
	private Clock clock;
	private static Log log;
	
	// Current draw.
	// Record how long each talon has been an outlier for current draw. If they are long enough,
	// Then they get disabled for safety.
	private HashMap<TalonSRX, Double> currentOutlierSinceSec = new HashMap<TalonSRX, Double>();
	public static final double kCurrentOutlierThresholdAmps = 5;
	public static  final double kCurrentOutlierDisableTimeSec = 1;
	
	// Encoder check thresholds
	private HashMap<TalonSRX, Double> speedOutlierSinceSec = new HashMap<TalonSRX, Double>();
	public static  final double kSpeedOutlierAbsoluteThresholdTicks = 100;
	public static  final double kSpeedOutlierMinimumRatio = 0.9;  // Encoders returning less than 90% of ticks are bad.
	public static  final double kSpeedOutlierDisableTimeSec = 1;
	
	private final double kMinLoggingIntervalSec = 4;  // Only log message once every n seconds.
	
	// Remember the last values from set(...) so that when the leadership is changed
	// the new leader can be told what to do until set() is called again.
	ControlMode lastMode = ControlMode.Velocity;
	double lastDemand0 = 0;
	double lastDemand1 = 0;
	
	public RedundantTalonSRX(ArrayList<TalonSRX> potentialLeaders, ArrayList<TalonSRX> followers, Clock clock, Log log) {
		this.potentialLeaders = potentialLeaders;
		this.followers = followers;
		this.clock = clock;
		RedundantTalonSRX.log = log;
		activeTalons = new ArrayList<TalonSRX>();
		activeTalons.addAll(potentialLeaders);
		activeTalons.addAll(followers);
		otherLeaders = new ArrayList<TalonSRX>();
		changeLeader(0);
		for(TalonSRX talon : activeTalons) {
			log.register(false, () -> talon.getOutputCurrent(), "Talons/%d/Current", talon.getDeviceID());
		}
		log.register(false, () -> (double)badEncoders.size(), "RedundantTalons/numBadEncoders");
		log.register(false, () -> (double)badTalons.size(), "RedundantTalons/numBadTalons");
		// Ensure execute gets called to check the talons/encoders.
    	Strongback.executor().register(this, Priority.MEDIUM);
	}	
	
	public static ArrayList<TalonSRX> getBadEncoders() {
		return badEncoders;
	}

	public static ArrayList<TalonSRX> getBadTalons() {
		return badTalons;
	}
	
	public static void clearFailures() {
		badEncoders.clear();
		badTalons.clear();
	}

	@Override
	public void execute(long timeInMillis) {
		if (activeTalons.size() < 2) return;
		checkCurrentDraw();
		checkEncoders();
	}
	
	/**
	 * Check the current draw of each talon against the average of all the
	 * talons and disable it if is too different.
	 * @param clock for the current time.
	 */
	public void checkCurrentDraw() {
		final double nowSec = clock.currentTime();
		// Walk through all talons and if there is an outlier, then flag it
		// as being broken so it can be disabled.
		HashMap<TalonSRX, Double> currentHash = new HashMap<TalonSRX, Double>();
		double sum = 0;
		int count = 0;
		for (TalonSRX talon : activeTalons) {
			double current = Math.abs(talon.getOutputCurrent());
			currentHash.put(talon, current);
			sum += current;
			count++;
		}
		if (count < 2) return;  // Nothing to do here.
		double average = sum/count;
		// Look for any talon that is consistently drawing more than 5A than the rest.
		// Iterate over the currentHash instead of activeTalons so activeTalons
		// can be mutated.
		for (TalonSRX talon: currentHash.keySet()) {
			double difference = currentHash.get(talon) - average;
			if (difference < kCurrentOutlierThresholdAmps) {
				// Not an outlier, remove it if it has been flagged in the past.
				currentOutlierSinceSec.remove(talon);
				continue;
			}
			// Now an outlier, add it to the outlier list if it's not already there.
			if (!currentOutlierSinceSec.containsKey(talon)) {
				// New entry.
				currentOutlierSinceSec.put(talon, nowSec);
				continue;
			}
			// Existing entry, check it's age.
			double ageSec = nowSec - currentOutlierSinceSec.get(talon);
			if (ageSec > kCurrentOutlierDisableTimeSec) {
				if (talon == leader && otherLeaders.size() == 0) {
					limitedError("current: only leader",
							"TalonSRX with CAN ID %d is drawing %f amps more than the average draw but is the only leader so it can't be disabled",
							talon.getDeviceID(), difference);
					currentOutlierSinceSec.remove(talon);  // Remove it so that there is only one log message per interval.
					continue;
				}
				limitedError(String.format("cur: %d", talon.getDeviceID()),
						"TalonSRX with CAN ID %d is drawing %f amps more than the average draw",
						talon.getDeviceID(), difference);
				disableTalon(talon);
			}
		}
	}
	
	/**
	 * Check the current draw of each talon against the average of all the
	 * talons and disable it if is too different.
	 * @param clock for the current time.
	 */
	public void checkEncoders() {
		final double nowSec = clock.currentTime();
		// Walk through all potential leaders and if there is an outlier,
		// then flag it as being broken so that the leadership can be changed..
		HashMap<TalonSRX, Double> speedHash = new HashMap<TalonSRX, Double>();
		double sum = 0;
		int count = 0;
		for (TalonSRX talon : potentialLeaders) {
			double speed = Math.abs(talon.getSelectedSensorVelocity(0));
			speedHash.put(talon, speed);
			sum += speed;
			count++;
		}
		if (count < 2) return;  // Nothing to do here.
		double average = sum/count;
		// Ignore values if the average is too low.
		if (average < kSpeedOutlierAbsoluteThresholdTicks) return; // Going too slow.
		// Look for any encoder that is returning .
		// Iterate over the currentHash instead of activeTalons so activeTalons
		// can be mutated.
		for (TalonSRX talon: speedHash.keySet()) {
			double ratio = speedHash.get(talon) / average;
			if (ratio > kSpeedOutlierMinimumRatio) {
				// Returning at least ~90% of the ticks.
				// Not an outlier, remove it if it has been.
				speedOutlierSinceSec.remove(talon);
				continue;
			}
			// Now an outlier, add it to the outlier list if it's not already there.
			if (!speedOutlierSinceSec.containsKey(talon)) {
				// New entry.
				speedOutlierSinceSec.put(talon, nowSec);
				continue;
			}
			// Existing entry, check it's age.
			double ageSec = nowSec - speedOutlierSinceSec.get(talon);
			if (ageSec > kSpeedOutlierDisableTimeSec) {
				if (talon == leader && otherLeaders.size() == 0) {
					limitedError("encoder: only leader",
							"The encoder on TalonSRX with CAN ID %d is reporting %.1f%% of the average value, but is the only leader so it can't be disabled",
							talon.getDeviceID(), 100 * ratio);
					speedOutlierSinceSec.remove(talon);  // Remove it so that there is only one log message per interval.
					continue;
				}
				limitedError(String.format("enc: %d", talon.getDeviceID()),
						"The encoder on TalonSRX with CAN ID %d is reporting %.1f%% of the average speed - that's bad",
						talon.getDeviceID(), 100 * ratio);
				disableEncoder(talon);
			}
		}
	}

	/**
	 * Log error message if it hasn't been logged in the last kMinLoggingIntervalSec.
	 * The error message is the same if it has the same key.
	 * @param key uniquely identifies this error message.
	 * @param msg message to put in the log.
	 */
	private void limitedError(String key, String msg, Object... args) {
		double now = clock.currentTime();
		// Has it been long enough since the last log message?
		if (now - lastErrorTime.getOrDefault(key, 0.) < kMinLoggingIntervalSec) return;
		// It has. Log now.
		log.error(msg, args);
		// Update the last log time.
		lastErrorTime.put(key, now);		
	}
	private HashMap<String, Double> lastErrorTime = new HashMap<String, Double>();
		
	public void disableTalon(TalonSRX talon) {
		badTalons.add(talon);
		potentialLeaders.remove(talon);
		activeTalons.remove(talon);
		otherLeaders.remove(talon);
		followers.remove(talon);
		// If it was the leader, then find a new leader.
		if (leader == talon) {
			changeLeader(0);
		}
		// Disable it now that it's not the leader.
		talon.set(ControlMode.Disabled, 0);
		log.error("Disabled TalonSRX with CAN ID %d", talon.getDeviceID());
	}
	
	/**
	 * Remove this talon/encoder from the list of potential leaders and
	 * switch leadership away if it's the current leader.
	 * @param talon the talon to ignore the encoder for.
	 */
	public void disableEncoder(TalonSRX talon) {
		badEncoders.add(talon);
		potentialLeaders.remove(talon);
		otherLeaders.remove(talon);
		followers.add(talon);  // It can now only follow.
		// If it was the leader, then find a new leader.
		if (leader == talon) {
			changeLeader(0);
			log.error("Bad encoder on leader TalonSRX %d, changed leadership", talon.getDeviceID());
		} else {
			log.error("Converted potential leader TalonSRX %d with bad encoder to a follower only", talon.getDeviceID());

		}
		// It will have been setup as a follower now.
	}
	
	public void changeLeader(int index) {
		assert(index < potentialLeaders.size());
		otherLeaders.clear();
		for (int i=0; i<potentialLeaders.size(); i++) {
			if (i == index) {
				leader = potentialLeaders.get(i);
				sensorCollection = leader.getSensorCollection();
				continue;
			}
			otherLeaders.add(potentialLeaders.get(i));
		}
		// Tell the new leader not to follow anyone by giving it a non-follow
		// instruction.
		leader.set(lastMode, lastDemand0, lastDemand1);
		// Tell the rest to follow the new leader.
		otherLeaders.forEach((talon) -> talon.set(ControlMode.Follower, leader.getDeviceID()));
		followers.forEach((talon) -> talon.set(ControlMode.Follower, leader.getDeviceID()));
	}
	
	/**
	 * Print out any issues seen across all RedundantTalonSRX's.
	 */
	public static void printStatus() {
		if (!badEncoders.isEmpty()) {
			log.error("The following talons have a bad encoder:");
			for (TalonSRX talon : badEncoders) {
				log.error("  Talon SRX %d", talon.getDeviceID());
			}
		}
		if (!badTalons.isEmpty()) {
			log.error("The following talons or motors have drawn a large amount of current:");
			for (TalonSRX talon : badTalons) {
				log.error("  Talon SRX %d", talon.getDeviceID());
			}
		}
		if (badTalons.isEmpty() && badEncoders.isEmpty()) {
			log.info("No bad talons or encoders found");
		}
	}
	
	// Main entry point for talon.
	
	@Override
	public void set(ControlMode mode, double demand) {
		lastMode = mode;
		lastDemand0 = demand;
		lastDemand1 = 0;
		// Master only.
		leader.set(mode, demand);
	}

	@Override
	public void set(ControlMode mode, double demand0, double demand1) {
		lastMode = mode;
		lastDemand0 = demand0;
		lastDemand1 = demand1;
		// Master only.
		leader.set(mode, demand0, demand1);
	}

	// Boring pass-thru for each of the methods follows. //////////////////////
	
	@Override
	public ErrorCode configSelectedFeedbackSensor(FeedbackDevice feedbackDevice, int pidIdx, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configSelectedFeedbackSensor(feedbackDevice, pidIdx, timeoutMs));
		return leader.configSelectedFeedbackSensor(feedbackDevice, pidIdx, timeoutMs);
	}

	@Override
	public ErrorCode setStatusFramePeriod(StatusFrameEnhanced frame, int periodMs, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.setStatusFramePeriod(frame, periodMs, timeoutMs));
		return leader.setStatusFramePeriod(frame, periodMs, timeoutMs);
	}

	@Override
	public int getStatusFramePeriod(StatusFrameEnhanced frame, int timeoutMs) {
		return leader.getStatusFramePeriod(frame, timeoutMs);
	}

	@Override
	public ErrorCode configVelocityMeasurementPeriod(VelocityMeasPeriod period, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configVelocityMeasurementPeriod(period, timeoutMs));
		return leader.configVelocityMeasurementPeriod(period, timeoutMs);
	}

	@Override
	public ErrorCode configVelocityMeasurementWindow(int windowSize, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configVelocityMeasurementWindow(windowSize, timeoutMs));
		return leader.configVelocityMeasurementWindow(windowSize, timeoutMs);
	}

	@Override
	public ErrorCode configForwardLimitSwitchSource(LimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configForwardLimitSwitchSource(type, normalOpenOrClose, timeoutMs));
		return leader.configForwardLimitSwitchSource(type, normalOpenOrClose, timeoutMs);
	}

	@Override
	public ErrorCode configReverseLimitSwitchSource(LimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configReverseLimitSwitchSource(type, normalOpenOrClose, timeoutMs));
		return leader.configReverseLimitSwitchSource(type, normalOpenOrClose, timeoutMs);
	}

	@Override
	public ErrorCode configPeakCurrentLimit(int amps, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configPeakCurrentLimit(amps, timeoutMs));
		followers.forEach((talon) -> talon.configPeakCurrentLimit(amps, timeoutMs));
		return leader.configPeakCurrentLimit(amps, timeoutMs);
	}

	@Override
	public ErrorCode configPeakCurrentDuration(int milliseconds, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configPeakCurrentDuration(milliseconds, timeoutMs));
		followers.forEach((talon) -> talon.configPeakCurrentDuration(milliseconds, timeoutMs));
		return leader.configPeakCurrentDuration(milliseconds, timeoutMs);
	}

	@Override
	public ErrorCode configContinuousCurrentLimit(int amps, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configContinuousCurrentLimit(amps, timeoutMs));
		followers.forEach((talon) -> talon.configContinuousCurrentLimit(amps, timeoutMs));
		return leader.configContinuousCurrentLimit(amps, timeoutMs);
	}

	@Override
	public void enableCurrentLimit(boolean enable) {
		otherLeaders.forEach((talon) -> talon.enableCurrentLimit(enable));
		followers.forEach((talon) -> talon.enableCurrentLimit(enable));
		leader.enableCurrentLimit(enable);
	}

	@Override
	public void neutralOutput() {
		// Master only.
		leader.neutralOutput();
	}

	@Override
	public void setNeutralMode(NeutralMode neutralMode) {
		otherLeaders.forEach((talon) -> talon.setNeutralMode(neutralMode));
		leader.setNeutralMode(neutralMode);
	}

	@Override
	public void setSensorPhase(boolean phaseSensor) {
		otherLeaders.forEach((talon) -> talon.setSensorPhase(phaseSensor));
		leader.setSensorPhase(phaseSensor);
	}

	@Override
	public void setInverted(boolean invert) {
		otherLeaders.forEach((talon) -> talon.setInverted(invert));
		leader.setInverted(invert);
	}

	@Override
	public boolean getInverted() {
		return leader.getInverted();
	}

	@Override
	public ErrorCode configOpenloopRamp(double secondsFromNeutralToFull, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configOpenloopRamp(secondsFromNeutralToFull, timeoutMs));
		return leader.configOpenloopRamp(secondsFromNeutralToFull, timeoutMs);
	}

	@Override
	public ErrorCode configClosedloopRamp(double secondsFromNeutralToFull, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configClosedloopRamp(secondsFromNeutralToFull, timeoutMs));
		return leader.configClosedloopRamp(secondsFromNeutralToFull, timeoutMs);
	}

	@Override
	public ErrorCode configPeakOutputForward(double percentOut, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configPeakOutputForward(percentOut, timeoutMs));
		return leader.configPeakOutputForward(percentOut, timeoutMs);
	}

	@Override
	public ErrorCode configPeakOutputReverse(double percentOut, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configPeakOutputReverse(percentOut, timeoutMs));
		return leader.configPeakOutputReverse(percentOut, timeoutMs);
	}

	@Override
	public ErrorCode configNominalOutputForward(double percentOut, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configNominalOutputForward(percentOut, timeoutMs));
		return leader.configNominalOutputForward(percentOut, timeoutMs);
	}

	@Override
	public ErrorCode configNominalOutputReverse(double percentOut, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configNominalOutputReverse(percentOut, timeoutMs));
		return leader.configNominalOutputReverse(percentOut, timeoutMs);
	}

	@Override
	public ErrorCode configNeutralDeadband(double percentDeadband, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configNeutralDeadband(percentDeadband, timeoutMs));
		return leader.configNeutralDeadband(percentDeadband, timeoutMs);
	}

	@Override
	public ErrorCode configVoltageCompSaturation(double voltage, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configVoltageCompSaturation(voltage, timeoutMs));
		return leader.configVoltageCompSaturation(voltage, timeoutMs);
	}

	@Override
	public ErrorCode configVoltageMeasurementFilter(int filterWindowSamples, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configVoltageMeasurementFilter(filterWindowSamples, timeoutMs));
		return leader.configVoltageMeasurementFilter(filterWindowSamples, timeoutMs);
	}

	@Override
	public void enableVoltageCompensation(boolean enable) {
		otherLeaders.forEach((talon) -> talon.enableVoltageCompensation(enable));
		leader.enableVoltageCompensation(enable);
	}

	@Override
	public double getBusVoltage() {
		return leader.getBusVoltage();
	}

	@Override
	public double getMotorOutputPercent() {
		return leader.getMotorOutputPercent();
	}

	@Override
	public double getMotorOutputVoltage() {
		return leader.getMotorOutputVoltage();
	}

	@Override
	public double getOutputCurrent() {
		return leader.getOutputCurrent();
	}

	@Override
	public double getTemperature() {
		return leader.getTemperature();
	}

	@Override
	public ErrorCode configSelectedFeedbackSensor(RemoteFeedbackDevice feedbackDevice, int pidIdx, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configSelectedFeedbackSensor(feedbackDevice, pidIdx, timeoutMs));
		return leader.configSelectedFeedbackSensor(feedbackDevice, pidIdx, timeoutMs);
	}

	@Override
	public ErrorCode configRemoteFeedbackFilter(int deviceID, RemoteSensorSource remoteSensorSource, int remoteOrdinal,
			int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configRemoteFeedbackFilter(deviceID, remoteSensorSource, remoteOrdinal, timeoutMs));
		return leader.configRemoteFeedbackFilter(deviceID, remoteSensorSource, remoteOrdinal, timeoutMs);
	}

	@Override
	public ErrorCode configSensorTerm(SensorTerm sensorTerm, FeedbackDevice feedbackDevice, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configSensorTerm(sensorTerm, feedbackDevice, timeoutMs));
		return leader.configSensorTerm(sensorTerm, feedbackDevice, timeoutMs);
	}

	@Override
	public double getSelectedSensorPosition(int pidIdx) {
		return leader.getSelectedSensorPosition(pidIdx);
	}

	@Override
	public double getSelectedSensorVelocity(int pidIdx) {
		return leader.getSelectedSensorVelocity(pidIdx);
	}

	@Override
	public ErrorCode setSelectedSensorPosition(double sensorPos, int pidIdx, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.setSelectedSensorPosition(sensorPos, pidIdx, timeoutMs));
		return leader.setSelectedSensorPosition(sensorPos, pidIdx, timeoutMs);
	}

	@Override
	public ErrorCode setControlFramePeriod(ControlFrame frame, int periodMs) {
		otherLeaders.forEach((talon) -> talon.setControlFramePeriod(frame, periodMs));
		return leader.setControlFramePeriod(frame, periodMs);
	}

	@Override
	public ErrorCode setStatusFramePeriod(StatusFrame frame, int periodMs, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.setStatusFramePeriod(frame, periodMs, timeoutMs));
		return leader.setStatusFramePeriod(frame, periodMs, timeoutMs);
	}

	@Override
	public int getStatusFramePeriod(StatusFrame frame, int timeoutMs) {
		return leader.getStatusFramePeriod(frame, timeoutMs);
	}

	@Override
	public ErrorCode configForwardLimitSwitchSource(RemoteLimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int deviceID, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configForwardLimitSwitchSource(type, normalOpenOrClose, deviceID, timeoutMs));
		return leader.configForwardLimitSwitchSource(type, normalOpenOrClose, deviceID, timeoutMs);
	}

	@Override
	public ErrorCode configReverseLimitSwitchSource(RemoteLimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int deviceID, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configReverseLimitSwitchSource(type, normalOpenOrClose, deviceID, timeoutMs));
		return leader.configReverseLimitSwitchSource(type, normalOpenOrClose, deviceID, timeoutMs);
	}

	@Override
	public void overrideLimitSwitchesEnable(boolean enable) {
		otherLeaders.forEach((talon) -> talon.overrideLimitSwitchesEnable(enable));
		leader.overrideLimitSwitchesEnable(enable);
	}

	@Override
	public ErrorCode configForwardSoftLimitThreshold(int forwardSensorLimit, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configForwardSoftLimitThreshold(forwardSensorLimit, timeoutMs));
		return leader.configForwardSoftLimitThreshold(forwardSensorLimit, timeoutMs);
	}

	@Override
	public ErrorCode configReverseSoftLimitThreshold(int reverseSensorLimit, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configReverseSoftLimitThreshold(reverseSensorLimit, timeoutMs));
		return leader.configReverseSoftLimitThreshold(reverseSensorLimit, timeoutMs);
	}

	@Override
	public ErrorCode configForwardSoftLimitEnable(boolean enable, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configForwardSoftLimitEnable(enable, timeoutMs));
		return leader.configForwardSoftLimitEnable(enable, timeoutMs);
	}

	@Override
	public ErrorCode configReverseSoftLimitEnable(boolean enable, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configReverseSoftLimitEnable(enable, timeoutMs));
		return leader.configReverseSoftLimitEnable(enable, timeoutMs);
	}

	@Override
	public void overrideSoftLimitsEnable(boolean enable) {
		otherLeaders.forEach((talon) -> talon.overrideSoftLimitsEnable(enable));
		leader.overrideSoftLimitsEnable(enable);
	}

	@Override
	public ErrorCode config_kP(int slotIdx, double value, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.config_kP(slotIdx, value, timeoutMs));
		return leader.config_kP(slotIdx, value, timeoutMs);
	}

	@Override
	public ErrorCode config_kI(int slotIdx, double value, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.config_kI(slotIdx, value, timeoutMs));
		return leader.config_kI(slotIdx, value, timeoutMs);
	}

	@Override
	public ErrorCode config_kD(int slotIdx, double value, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.config_kD(slotIdx, value, timeoutMs));
		return leader.config_kD(slotIdx, value, timeoutMs);
	}

	@Override
	public ErrorCode config_kF(int slotIdx, double value, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.config_kF(slotIdx, value, timeoutMs));
		return leader.config_kF(slotIdx, value, timeoutMs);
	}

	@Override
	public ErrorCode config_IntegralZone(int slotIdx, int izone, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.config_IntegralZone(slotIdx, izone, timeoutMs));
		return leader.config_IntegralZone(slotIdx, izone, timeoutMs);
	}

	@Override
	public ErrorCode configAllowableClosedloopError(int slotIdx, int allowableCloseLoopError, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configAllowableClosedloopError(slotIdx, allowableCloseLoopError, timeoutMs));
		return leader.configAllowableClosedloopError(slotIdx, allowableCloseLoopError, timeoutMs);
	}

	@Override
	public ErrorCode configMaxIntegralAccumulator(int slotIdx, double iaccum, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configMaxIntegralAccumulator(slotIdx, iaccum, timeoutMs));
		return leader.configMaxIntegralAccumulator(slotIdx, iaccum, timeoutMs);
	}

	@Override
	public ErrorCode setIntegralAccumulator(double iaccum, int pidIdx, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.setIntegralAccumulator(iaccum, pidIdx, timeoutMs));
		return leader.setIntegralAccumulator(iaccum, pidIdx, timeoutMs);
	}

	@Override
	public int getClosedLoopError(int pidIdx) {
		return leader.getClosedLoopError(pidIdx);
	}

	@Override
	public double getIntegralAccumulator(int pidIdx) {
		return leader.getIntegralAccumulator(pidIdx);
	}

	@Override
	public double getErrorDerivative(int pidIdx) {
		return leader.getErrorDerivative(pidIdx);
	}

	@Override
	public void selectProfileSlot(int slotIdx, int pidIdx) {
		otherLeaders.forEach((talon) -> talon.selectProfileSlot(slotIdx, pidIdx));
		leader.selectProfileSlot(slotIdx, pidIdx);
	}

	@Override
	public int getActiveTrajectoryPosition() {
		return leader.getActiveTrajectoryPosition();
	}

	@Override
	public int getActiveTrajectoryVelocity() {
		return leader.getActiveTrajectoryVelocity();
	}

	@Override
	public double getActiveTrajectoryHeading() {
		return leader.getActiveTrajectoryHeading();
	}

	@Override
	public ErrorCode configMotionCruiseVelocity(int sensorUnitsPer100ms, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configMotionCruiseVelocity(sensorUnitsPer100ms, timeoutMs));
		return leader.configMotionCruiseVelocity(sensorUnitsPer100ms, timeoutMs);
	}

	@Override
	public ErrorCode configMotionAcceleration(int sensorUnitsPer100msPerSec, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configMotionAcceleration(sensorUnitsPer100msPerSec, timeoutMs));
		return leader.configMotionAcceleration(sensorUnitsPer100msPerSec, timeoutMs);
	}

	@Override
	public ErrorCode clearMotionProfileTrajectories() {
		otherLeaders.forEach((talon) -> talon.clearMotionProfileTrajectories());
		return leader.clearMotionProfileTrajectories();
	}

	@Override
	public int getMotionProfileTopLevelBufferCount() {
		return leader.getMotionProfileTopLevelBufferCount();
	}

	@Override
	public ErrorCode pushMotionProfileTrajectory(TrajectoryPoint trajPt) {
		// Leader only.
		return leader.pushMotionProfileTrajectory(trajPt);
	}

	@Override
	public boolean isMotionProfileTopLevelBufferFull() {
		// Leader only.
		return leader.isMotionProfileTopLevelBufferFull();
	}

	@Override
	public void processMotionProfileBuffer() {
		// Leader only.
		leader.processMotionProfileBuffer();
	}

	@Override
	public ErrorCode getMotionProfileStatus(MotionProfileStatus statusToFill) {
		return leader.getMotionProfileStatus(statusToFill);
	}

	@Override
	public ErrorCode clearMotionProfileHasUnderrun(int timeoutMs) {
		return leader.clearMotionProfileHasUnderrun(timeoutMs);
	}

	@Override
	public ErrorCode changeMotionControlFramePeriod(int periodMs) {
		otherLeaders.forEach((talon) -> talon.changeMotionControlFramePeriod(periodMs));
		return leader.changeMotionControlFramePeriod(periodMs);
	}

	@Override
	public ErrorCode getLastError() {
		return leader.getLastError();
	}

	@Override
	public ErrorCode getFaults(Faults toFill) {
		return leader.getFaults(toFill);
	}

	@Override
	public ErrorCode getStickyFaults(StickyFaults toFill) {
		return leader.getStickyFaults(toFill);
	}

	@Override
	public ErrorCode clearStickyFaults(int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.clearStickyFaults(timeoutMs));
		followers.forEach((talon) -> talon.clearStickyFaults(timeoutMs));
		return leader.clearStickyFaults(timeoutMs);
	}

	@Override
	public int getFirmwareVersion() {
		return leader.getFirmwareVersion();
	}

	@Override
	public boolean hasResetOccurred() {
		return leader.hasResetOccurred();
	}

	@Override
	public ErrorCode configSetCustomParam(int newValue, int paramIndex, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configSetCustomParam(newValue, paramIndex, timeoutMs));
		return leader.configSetCustomParam(newValue, paramIndex, timeoutMs);
	}

	@Override
	public int configGetCustomParam(int paramIndex, int timoutMs) {
		return leader.configGetCustomParam(paramIndex, timoutMs);
	}

	@Override
	public ErrorCode configSetParameter(ParamEnum param, double value, int subValue, int ordinal, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configSetParameter(param, value, subValue, ordinal, timeoutMs));
		return leader.configSetParameter(param, value, subValue, ordinal, timeoutMs);
	}

	@Override
	public ErrorCode configSetParameter(int param, double value, int subValue, int ordinal, int timeoutMs) {
		otherLeaders.forEach((talon) -> talon.configSetParameter(param, value, subValue, ordinal, timeoutMs));
		return leader.configSetParameter(param, value, subValue, ordinal, timeoutMs);
	}

	@Override
	public double configGetParameter(ParamEnum paramEnum, int ordinal, int timeoutMs) {
		return leader.configGetParameter(paramEnum, ordinal, timeoutMs);
	}

	@Override
	public double configGetParameter(int paramEnum, int ordinal, int timeoutMs) {
		return leader.configGetParameter(paramEnum, ordinal, timeoutMs);
	}

	@Override
	public int getBaseID() {
		return leader.getBaseID();
	}

	@Override
	public int getDeviceID() {
		return leader.getDeviceID();
	}

	@Override
	public TalonSensorCollection getSensorCollection() {
		// FIXME: split this up so that it can be switched.
		return sensorCollection;
	}

	@Override
	public TalonSRX setScale(double scale) {
		otherLeaders.forEach((talon) -> talon.setScale(scale));
		return leader.setScale(scale);
	}

	@Override
	public IMotorController getHWTalon() {
		return leader.getHWTalon();
	}

	///// TalonSensorCollection methods from here.
	// Note that this is done in case someone stores the sensor collection of the leader then
	// and the leadership changes.
	
	@Override
	public int getAnalogIn() {
		return sensorCollection.getAnalogIn();
	}

	@Override
	public int getAnalogInRaw() {
		return sensorCollection.getAnalogInRaw();
	}

	@Override
	public int getAnalogInVel() {
		return sensorCollection.getAnalogInVel();
	}

	@Override
	public boolean getPinStateQuadA() {
		return sensorCollection.getPinStateQuadA();
	}

	@Override
	public boolean getPinStateQuadB() {
		return sensorCollection.getPinStateQuadB();
	}

	@Override
	public boolean getPinStateQuadIdx() {
		return sensorCollection.getPinStateQuadIdx();
	}

	@Override
	public int getPulseWidthPosition() {
		return sensorCollection.getPulseWidthPosition();
	}

	@Override
	public int getPulseWidthRiseToFallUs() {
		return sensorCollection.getPulseWidthRiseToFallUs();
	}

	@Override
	public int getPulseWidthRiseToRiseUs() {
		return sensorCollection.getPulseWidthRiseToRiseUs();
	}

	@Override
	public int getPulseWidthVelocity() {
		return sensorCollection.getPulseWidthVelocity();
	}

	@Override
	public double getQuadraturePosition() {
		return sensorCollection.getQuadraturePosition();
	}

	@Override
	public int getQuadratureVelocity() {
		return sensorCollection.getQuadratureVelocity();
	}

	@Override
	public boolean isFwdLimitSwitchClosed() {
		return sensorCollection.isFwdLimitSwitchClosed();
	}

	@Override
	public boolean isRevLimitSwitchClosed() {
		return sensorCollection.isRevLimitSwitchClosed();
	}

	@Override
	public ErrorCode setAnalogPosition(int newPosition, int timeoutMs) {
		return sensorCollection.setAnalogPosition(newPosition, timeoutMs);
	}

	@Override
	public ErrorCode setPulseWidthPosition(int newPosition, int timeoutMs) {
		return sensorCollection.setPulseWidthPosition(newPosition, timeoutMs);
	}

	@Override
	public ErrorCode setQuadraturePosition(double newPosition, int timeoutMs) {
		return sensorCollection.setQuadraturePosition(newPosition, timeoutMs);
	}
}
