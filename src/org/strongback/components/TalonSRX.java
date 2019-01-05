package org.strongback.components;

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
 * This interface wraps the CTRE CAN based Talon motor controller
 * 
 * We merge the talon's separate interfaces into one interface here, and expose the functions we use.
 */
public interface TalonSRX { //extends IMotorControllerEnhanced, IMotorController {

	public ErrorCode configSelectedFeedbackSensor(FeedbackDevice feedbackDevice, int pidIdx, int timeoutMs);

	public ErrorCode setStatusFramePeriod(StatusFrameEnhanced frame, int periodMs, int timeoutMs);
	
	public int getStatusFramePeriod(StatusFrameEnhanced frame, int timeoutMs);
	
	public ErrorCode configVelocityMeasurementPeriod(VelocityMeasPeriod period, int timeoutMs);
	
	public ErrorCode configVelocityMeasurementWindow(int windowSize, int timeoutMs);
	
	public ErrorCode configForwardLimitSwitchSource(LimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int timeoutMs);
	
	public ErrorCode configReverseLimitSwitchSource(LimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int timeoutMs);
	
	public ErrorCode configPeakCurrentLimit(int amps, int timeoutMs);
	
	public ErrorCode configPeakCurrentDuration(int milliseconds, int timeoutMs);
	
	public ErrorCode configContinuousCurrentLimit(int amps, int timeoutMs);
	
	public void enableCurrentLimit(boolean enable);
	
	public void set(ControlMode Mode, double demand);
	
	public void set(ControlMode Mode, double demand0, double demand1);
	
	public void neutralOutput();
	
	public void setNeutralMode(NeutralMode neutralMode);
	
	public void setSensorPhase(boolean PhaseSensor);
	
	public void setInverted(boolean invert);
	
	public boolean getInverted();
	
	public ErrorCode configOpenloopRamp(double secondsFromNeutralToFull, int timeoutMs);
	
	public ErrorCode configClosedloopRamp(double secondsFromNeutralToFull, int timeoutMs);
	
	public ErrorCode configPeakOutputForward(double percentOut, int timeoutMs);
	
	public ErrorCode configPeakOutputReverse(double percentOut, int timeoutMs);
	
	public ErrorCode configNominalOutputForward(double percentOut, int timeoutMs);
	
	public ErrorCode configNominalOutputReverse(double percentOut, int timeoutMs);
	
	public ErrorCode configNeutralDeadband(double percentDeadband, int timeoutMs);
	
	public ErrorCode configVoltageCompSaturation(double voltage, int timeoutMs);
	
	public ErrorCode configVoltageMeasurementFilter(int filterWindowSamples, int timeoutMs);
	
	public void enableVoltageCompensation(boolean enable);
	
	public double getBusVoltage();
	
	public double getMotorOutputPercent();
	
	public double getMotorOutputVoltage();
	
	public double getOutputCurrent();
	
	public double getTemperature();
	
	public ErrorCode configSelectedFeedbackSensor(RemoteFeedbackDevice feedbackDevice, int pidIdx, int timeoutMs);
	
	public ErrorCode configRemoteFeedbackFilter(int deviceID, RemoteSensorSource remoteSensorSource, int remoteOrdinal,
			int timeoutMs);
	
	public ErrorCode configSensorTerm(SensorTerm sensorTerm, FeedbackDevice feedbackDevice, int timeoutMs);
	
	public double getSelectedSensorPosition(int pidIdx);
	
	public double getSelectedSensorVelocity(int pidIdx);
	
	public ErrorCode setSelectedSensorPosition(double sensorPos, int pidIdx, int timeoutMs);
	
	public ErrorCode setControlFramePeriod(ControlFrame frame, int periodMs);
	
	public ErrorCode setStatusFramePeriod(StatusFrame frame, int periodMs, int timeoutMs);
	
	public int getStatusFramePeriod(StatusFrame frame, int timeoutMs);
	
	public ErrorCode configForwardLimitSwitchSource(RemoteLimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int deviceID, int timeoutMs);
	
	public ErrorCode configReverseLimitSwitchSource(RemoteLimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int deviceID, int timeoutMs);
	
	public void overrideLimitSwitchesEnable(boolean enable);
	
	public ErrorCode configForwardSoftLimitThreshold(int forwardSensorLimit, int timeoutMs);
	
	public ErrorCode configReverseSoftLimitThreshold(int reverseSensorLimit, int timeoutMs);
	
	public ErrorCode configForwardSoftLimitEnable(boolean enable, int timeoutMs);
	
	public ErrorCode configReverseSoftLimitEnable(boolean enable, int timeoutMs);
	
	public void overrideSoftLimitsEnable(boolean enable);
	
	public ErrorCode config_kP(int slotIdx, double value, int timeoutMs);
	
	public ErrorCode config_kI(int slotIdx, double value, int timeoutMs);
	
	public ErrorCode config_kD(int slotIdx, double value, int timeoutMs);
	
	public ErrorCode config_kF(int slotIdx, double value, int timeoutMs);
	
	public ErrorCode config_IntegralZone(int slotIdx, int izone, int timeoutMs);
	
	public ErrorCode configAllowableClosedloopError(int slotIdx, int allowableCloseLoopError, int timeoutMs);
	
	public ErrorCode configMaxIntegralAccumulator(int slotIdx, double iaccum, int timeoutMs);
	
	public ErrorCode setIntegralAccumulator(double iaccum, int pidIdx, int timeoutMs);
	
	public int getClosedLoopError(int pidIdx);
	
	public double getIntegralAccumulator(int pidIdx);
	
	public double getErrorDerivative(int pidIdx);
	
	public void selectProfileSlot(int slotIdx, int pidIdx);
	
	public int getActiveTrajectoryPosition();
	
	public int getActiveTrajectoryVelocity();
	
	public double getActiveTrajectoryHeading();
	
	public ErrorCode configMotionCruiseVelocity(int sensorUnitsPer100ms, int timeoutMs);
	
	public ErrorCode configMotionAcceleration(int sensorUnitsPer100msPerSec, int timeoutMs);
	
	public ErrorCode clearMotionProfileTrajectories();
	
	public int getMotionProfileTopLevelBufferCount();
	
	public ErrorCode pushMotionProfileTrajectory(TrajectoryPoint trajPt);
	
	public boolean isMotionProfileTopLevelBufferFull();
	
	public void processMotionProfileBuffer();
	
	public ErrorCode getMotionProfileStatus(MotionProfileStatus statusToFill);
	
	public ErrorCode clearMotionProfileHasUnderrun(int timeoutMs);
	
	public ErrorCode changeMotionControlFramePeriod(int periodMs);
	
	public ErrorCode getLastError();

	public ErrorCode getFaults(Faults toFill);

	public ErrorCode getStickyFaults(StickyFaults toFill);

	public ErrorCode clearStickyFaults(int timeoutMs);

	public int getFirmwareVersion();

	public boolean hasResetOccurred();

	public ErrorCode configSetCustomParam(int newValue, int paramIndex, int timeoutMs);

	public int configGetCustomParam(int paramIndex, int timoutMs);

	public ErrorCode configSetParameter(ParamEnum param, double value, int subValue, int ordinal, int timeoutMs);

	public ErrorCode configSetParameter(int param, double value, int subValue, int ordinal, int timeoutMs);

	public double configGetParameter(ParamEnum paramEnum, int ordinal, int timeoutMs);

	public double configGetParameter(int paramEnum, int ordinal, int timeoutMs);

	public int getBaseID();

	public int getDeviceID();
	
	public TalonSensorCollection getSensorCollection();
	
	/**
	 * Scale methods that give results in ticks by multiplying by this factor
	 * and divide for methods that take ticks.
	 * 
	 * @param scale multiple by this to convert from ticks eg 1.0/4096.
	 * @return
	 */
	TalonSRX setScale(double scale);
	
	public IMotorController getHWTalon();
}
