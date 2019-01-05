package org.strongback.hardware;

import org.strongback.components.TalonSRX;
import org.strongback.components.TalonSensorCollection;

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

/*
 * Package to wrap a CTRE talon SRX controller.
 * 
 * This is the hardware interface class that implements the interface we use.
 * 
 * We have a scale factor. This is useful in position and velocity close loop feedback modes.
 * 	For reading it we divide by the scale factor, when writing values we multiply by the scale factor.
 * 
 */
public class HardwareTalonSRX implements TalonSRX {
	private HardwareSensorCollection sensorCollection;
	private com.ctre.phoenix.motorcontrol.can.TalonSRX talon;
	private double scale = 1.0;
	private ControlMode lastMode = ControlMode.Disabled;
	
	private boolean scalable(ControlMode mode) {
		return ((mode == ControlMode.Velocity) || (mode == ControlMode.Position) || (mode == ControlMode.MotionMagic));
	}
	
	HardwareTalonSRX(com.ctre.phoenix.motorcontrol.can.TalonSRX talon) {
		this.talon = talon;
		sensorCollection = new HardwareSensorCollection(talon);
	}
	
	@Override
	public ErrorCode configSelectedFeedbackSensor(FeedbackDevice feedbackDevice, int pidIdx, int timeoutMs) {
		return talon.configSelectedFeedbackSensor(feedbackDevice, pidIdx, timeoutMs);
	}

	@Override
	public ErrorCode setStatusFramePeriod(StatusFrameEnhanced frame, int periodMs, int timeoutMs) {
		return talon.setStatusFramePeriod(frame, periodMs, timeoutMs);
	}

	@Override
	public int getStatusFramePeriod(StatusFrameEnhanced frame, int timeoutMs) {
		return talon.getStatusFramePeriod(frame, timeoutMs);
	}

	@Override
	public ErrorCode configVelocityMeasurementPeriod(VelocityMeasPeriod period, int timeoutMs) {
		return talon.configVelocityMeasurementPeriod(period, timeoutMs);
	}

	@Override
	public ErrorCode configVelocityMeasurementWindow(int windowSize, int timeoutMs) {
		return talon.configVelocityMeasurementWindow(windowSize, timeoutMs);
	}

	@Override
	public ErrorCode configForwardLimitSwitchSource(LimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int timeoutMs) {
		return talon.configForwardLimitSwitchSource(type, normalOpenOrClose, timeoutMs);
	}

	@Override
	public ErrorCode configReverseLimitSwitchSource(LimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int timeoutMs) {
		return talon.configReverseLimitSwitchSource(type, normalOpenOrClose, timeoutMs);
	}

	@Override
	public ErrorCode configPeakCurrentLimit(int amps, int timeoutMs) {
		return talon.configPeakCurrentLimit(amps, timeoutMs);
	}

	@Override
	public ErrorCode configPeakCurrentDuration(int milliseconds, int timeoutMs) {
		return talon.configPeakCurrentDuration(milliseconds, timeoutMs);
	}

	@Override
	public ErrorCode configContinuousCurrentLimit(int amps, int timeoutMs) {
		return talon.configContinuousCurrentLimit(amps, timeoutMs);
	}

	@Override
	public void enableCurrentLimit(boolean enable) {
		talon.enableCurrentLimit(enable);		
	}

	@Override
	public void set(ControlMode mode, double demand) {
		if (scalable(mode)) {
			demand *= scale;
		}
		lastMode = mode;
		talon.set(mode, demand);
	}

	@Override
	public void set(ControlMode mode, double demand0, double demand1) {
		if (scalable(mode)) {
			demand0 *= scale;
			demand1 *= scale;
		}
		lastMode = mode;
		talon.set(mode, demand0, demand1);
	}

	@Override
	public void neutralOutput() {
		talon.neutralOutput();
	}

	@Override
	public void setNeutralMode(NeutralMode neutralMode) {
		talon.setNeutralMode(neutralMode);
	}

	@Override
	public void setSensorPhase(boolean PhaseSensor) {
		talon.setSensorPhase(PhaseSensor);
	}

	@Override
	public void setInverted(boolean invert) {
		talon.setInverted(invert);
	}

	@Override
	public boolean getInverted() {
		return talon.getInverted();
	}

	@Override
	public ErrorCode configOpenloopRamp(double secondsFromNeutralToFull, int timeoutMs) {
		return talon.configOpenloopRamp(secondsFromNeutralToFull, timeoutMs);
	}

	@Override
	public ErrorCode configClosedloopRamp(double secondsFromNeutralToFull, int timeoutMs) {
		return talon.configClosedloopRamp(secondsFromNeutralToFull, timeoutMs);
	}

	@Override
	public ErrorCode configPeakOutputForward(double percentOut, int timeoutMs) {
		return talon.configPeakOutputForward(percentOut, timeoutMs);
	}

	@Override
	public ErrorCode configPeakOutputReverse(double percentOut, int timeoutMs) {
		return talon.configPeakOutputReverse(percentOut, timeoutMs);
	}

	@Override
	public ErrorCode configNominalOutputForward(double percentOut, int timeoutMs) {
		return talon.configNominalOutputForward(percentOut, timeoutMs);
	}

	@Override
	public ErrorCode configNominalOutputReverse(double percentOut, int timeoutMs) {
		return talon.configNominalOutputReverse(percentOut, timeoutMs);
	}

	@Override
	public ErrorCode configNeutralDeadband(double percentDeadband, int timeoutMs) {
		return talon.configNeutralDeadband(percentDeadband, timeoutMs);
	}

	@Override
	public ErrorCode configVoltageCompSaturation(double voltage, int timeoutMs) {
		return talon.configVoltageCompSaturation(voltage, timeoutMs);
	}

	@Override
	public ErrorCode configVoltageMeasurementFilter(int filterWindowSamples, int timeoutMs) {
		return talon.configVoltageMeasurementFilter(filterWindowSamples, timeoutMs);
	}

	@Override
	public void enableVoltageCompensation(boolean enable) {
		talon.enableVoltageCompensation(enable);
	}

	@Override
	public double getBusVoltage() {
		return talon.getBusVoltage();
	}

	@Override
	public double getMotorOutputPercent() {
		return talon.getMotorOutputPercent();
	}

	@Override
	public double getMotorOutputVoltage() {
		return talon.getMotorOutputVoltage();
	}

	@Override
	public double getOutputCurrent() {
		return talon.getOutputCurrent();
	}

	@Override
	public double getTemperature() {
		return talon.getTemperature();
	}

	@Override
	public ErrorCode configSelectedFeedbackSensor(RemoteFeedbackDevice feedbackDevice, int pidIdx, int timeoutMs) {
		return talon.configSelectedFeedbackSensor(feedbackDevice, pidIdx, timeoutMs);
	}

	@Override
	public ErrorCode configRemoteFeedbackFilter(int deviceID, RemoteSensorSource remoteSensorSource, int remoteOrdinal,
			int timeoutMs) {
		return talon.configRemoteFeedbackFilter(deviceID, remoteSensorSource, remoteOrdinal, timeoutMs);
	}

	@Override
	public ErrorCode configSensorTerm(SensorTerm sensorTerm, FeedbackDevice feedbackDevice, int timeoutMs) {
		return talon.configSensorTerm(sensorTerm, feedbackDevice, timeoutMs);
	}

	@Override
	public double getSelectedSensorPosition(int pidIdx) {
		return talon.getSelectedSensorPosition(pidIdx) / scale;
	}

	@Override
	public double getSelectedSensorVelocity(int pidIdx) {
		return talon.getSelectedSensorVelocity(pidIdx) / scale;
	}

	@Override
	public ErrorCode setSelectedSensorPosition(double sensorPos, int pidIdx, int timeoutMs) {
		if (scalable(lastMode)) {
			sensorPos = (int)(sensorPos * scale);
		}
		return talon.setSelectedSensorPosition((int) sensorPos, pidIdx, timeoutMs);
	}

	@Override
	public ErrorCode setControlFramePeriod(ControlFrame frame, int periodMs) {
		return talon.setControlFramePeriod(frame, periodMs);
	}

	@Override
	public ErrorCode setStatusFramePeriod(StatusFrame frame, int periodMs, int timeoutMs) {
		return talon.setStatusFramePeriod(frame, periodMs, timeoutMs);
	}

	@Override
	public int getStatusFramePeriod(StatusFrame frame, int timeoutMs) {
		return talon.getStatusFramePeriod(frame, timeoutMs);
	}

	@Override
	public ErrorCode configForwardLimitSwitchSource(RemoteLimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int deviceID, int timeoutMs) {
		return talon.configForwardLimitSwitchSource(type, normalOpenOrClose, deviceID, timeoutMs);
	}

	@Override
	public ErrorCode configReverseLimitSwitchSource(RemoteLimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int deviceID, int timeoutMs) {
		return talon.configReverseLimitSwitchSource(type, normalOpenOrClose, deviceID, timeoutMs);
	}

	@Override
	public void overrideLimitSwitchesEnable(boolean enable) {
		talon.overrideLimitSwitchesEnable(enable);
	}

	@Override
	public ErrorCode configForwardSoftLimitThreshold(int forwardSensorLimit, int timeoutMs) {
		return talon.configForwardSoftLimitThreshold(forwardSensorLimit, timeoutMs);
	}

	@Override
	public ErrorCode configReverseSoftLimitThreshold(int reverseSensorLimit, int timeoutMs) {
		return talon.configReverseSoftLimitThreshold(reverseSensorLimit, timeoutMs);
	}

	@Override
	public ErrorCode configForwardSoftLimitEnable(boolean enable, int timeoutMs) {
		return talon.configForwardSoftLimitEnable(enable, timeoutMs);
	}

	@Override
	public ErrorCode configReverseSoftLimitEnable(boolean enable, int timeoutMs) {
		return talon.configReverseSoftLimitEnable(enable, timeoutMs);
	}

	@Override
	public void overrideSoftLimitsEnable(boolean enable) {
		talon.overrideSoftLimitsEnable(enable);
	}

	@Override
	public ErrorCode config_kP(int slotIdx, double value, int timeoutMs) {
		return talon.config_kP(slotIdx, value, timeoutMs);
	}

	@Override
	public ErrorCode config_kI(int slotIdx, double value, int timeoutMs) {
		return talon.config_kI(slotIdx, value, timeoutMs);
	}

	@Override
	public ErrorCode config_kD(int slotIdx, double value, int timeoutMs) {
		return talon.config_kD(slotIdx, value, timeoutMs);
	}

	@Override
	public ErrorCode config_kF(int slotIdx, double value, int timeoutMs) {
		return talon.config_kF(slotIdx, value, timeoutMs);
	}

	@Override
	public ErrorCode config_IntegralZone(int slotIdx, int izone, int timeoutMs) {
		return talon.config_IntegralZone(slotIdx, izone, timeoutMs);
	}

	@Override
	public ErrorCode configAllowableClosedloopError(int slotIdx, int allowableCloseLoopError, int timeoutMs) {
		return talon.configAllowableClosedloopError(slotIdx, allowableCloseLoopError, timeoutMs);
	}

	@Override
	public ErrorCode configMaxIntegralAccumulator(int slotIdx, double iaccum, int timeoutMs) {
		return talon.configMaxIntegralAccumulator(slotIdx, iaccum, timeoutMs);
	}

	@Override
	public ErrorCode setIntegralAccumulator(double iaccum, int pidIdx, int timeoutMs) {
		return talon.setIntegralAccumulator(iaccum, pidIdx, timeoutMs);
	}

	@Override
	public int getClosedLoopError(int pidIdx) {
		int value = talon.getClosedLoopError(pidIdx);
		
		if (scalable(lastMode)) {
			value = (int)(value / scale);
		}
		return value;
	}

	@Override
	public double getIntegralAccumulator(int pidIdx) {
		return talon.getIntegralAccumulator(pidIdx);
	}

	@Override
	public double getErrorDerivative(int pidIdx) {
		return talon.getErrorDerivative(pidIdx);
	}

	@Override
	public void selectProfileSlot(int slotIdx, int pidIdx) {
		talon.selectProfileSlot(slotIdx, pidIdx);
	}

	@Override
	public int getActiveTrajectoryPosition() {
		return talon.getActiveTrajectoryPosition();
	}

	@Override
	public int getActiveTrajectoryVelocity() {
		return talon.getActiveTrajectoryVelocity();
	}

	@Override
	public double getActiveTrajectoryHeading() {
		return talon.getActiveTrajectoryHeading();
	}

	@Override
	public ErrorCode configMotionCruiseVelocity(int sensorUnitsPer100ms, int timeoutMs) {
		return talon.configMotionCruiseVelocity(sensorUnitsPer100ms, timeoutMs);
	}

	@Override
	public ErrorCode configMotionAcceleration(int sensorUnitsPer100msPerSec, int timeoutMs) {
		return talon.configMotionAcceleration(sensorUnitsPer100msPerSec, timeoutMs);
	}

	@Override
	public ErrorCode clearMotionProfileTrajectories() {
		return talon.clearMotionProfileTrajectories();
	}

	@Override
	public int getMotionProfileTopLevelBufferCount() {
		return talon.getMotionProfileTopLevelBufferCount();
	}

	@Override
	public ErrorCode pushMotionProfileTrajectory(TrajectoryPoint trajPt) {
		return talon.pushMotionProfileTrajectory(trajPt);
	}

	@Override
	public boolean isMotionProfileTopLevelBufferFull() {
		return talon.isMotionProfileTopLevelBufferFull();
	}

	@Override
	public void processMotionProfileBuffer() {
		talon.processMotionProfileBuffer();		
	}

	@Override
	public ErrorCode getMotionProfileStatus(MotionProfileStatus statusToFill) {
		return talon.getMotionProfileStatus(statusToFill);
	}

	@Override
	public ErrorCode clearMotionProfileHasUnderrun(int timeoutMs) {
		return talon.clearMotionProfileHasUnderrun(timeoutMs);
	}

	@Override
	public ErrorCode changeMotionControlFramePeriod(int periodMs) {
		return talon.changeMotionControlFramePeriod(periodMs);
	}

	@Override
	public ErrorCode getLastError() {
		return talon.getLastError();
	}

	@Override
	public ErrorCode getFaults(Faults toFill) {
		return talon.getFaults(toFill);
	}

	@Override
	public ErrorCode getStickyFaults(StickyFaults toFill) {
		return talon.getStickyFaults(toFill);
	}

	@Override
	public ErrorCode clearStickyFaults(int timeoutMs) {
		return talon.clearStickyFaults(timeoutMs);
	}

	@Override
	public int getFirmwareVersion() {
		return talon.getFirmwareVersion();
	}

	@Override
	public boolean hasResetOccurred() {
		return talon.hasResetOccurred();
	}

	@Override
	public ErrorCode configSetCustomParam(int newValue, int paramIndex, int timeoutMs) {
		return talon.configSetCustomParam(newValue, paramIndex, timeoutMs);
	}

	@Override
	public int configGetCustomParam(int paramIndex, int timoutMs) {
		return talon.configGetCustomParam(paramIndex, timoutMs);
	}

	@Override
	public ErrorCode configSetParameter(ParamEnum param, double value, int subValue, int ordinal, int timeoutMs) {
		return talon.configSetParameter(param, value, subValue, ordinal, timeoutMs);
	}

	@Override
	public ErrorCode configSetParameter(int param, double value, int subValue, int ordinal, int timeoutMs) {
		return talon.configSetParameter(param, value, subValue, ordinal, timeoutMs);
	}

	@Override
	public double configGetParameter(ParamEnum paramEnum, int ordinal, int timeoutMs) {
		return talon.configGetParameter(paramEnum, ordinal, timeoutMs);
	}

	@Override
	public double configGetParameter(int paramEnum, int ordinal, int timeoutMs) {
		return talon.configGetParameter(paramEnum, ordinal, timeoutMs);
	}

	@Override
	public int getBaseID() {
		return talon.getBaseID();
	}

	@Override
	public int getDeviceID() {
		return talon.getDeviceID();
	}

	@Override
	public TalonSensorCollection getSensorCollection() {
		return sensorCollection;
	}
	
	@Override
	public TalonSRX setScale(double scale) {
		this.scale = scale;
		sensorCollection.setScale(scale);
		return this;
	}
		
	public TalonSRX follow(IMotorController master) {
		talon.follow(master);
		return this;
	}

	@Override
	public IMotorController getHWTalon() {
		// TODO Auto-generated method stub
		return talon;
	}	
}
