package org.strongback.mock;

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
 * This class implements a mock talon.
 * We have some simulation of the actual hardware and the sensors.
 * 
 * To achieve this the class is both the talon and the sensor collection.
 */
public class MockTalonSRX implements TalonSRX, TalonSensorCollection {
	private int	id;
	private FeedbackDevice currentFeedbackDevice;
	private int statusFramePeriod = 100;
	private ControlMode mode = ControlMode.Disabled;
	private double demand = 0;
	
	// sensor collection information
	private int	analogValue = 0;
	private double quadraturePosition = 0;
	private double quadratureSpeed = 0;
	private double scale = 1;
	private boolean fwdLimitSwitchClosed = false;
	private boolean revLimitSwitchClosed = false;
	private double sensorPosition = 123;
	private double sensorVelocity = 0;
	private double outputCurrent = 0;
	
	MockTalonSRX(int id) {
		this.id = id;
		currentFeedbackDevice = FeedbackDevice.None;
	}

	@Override
	public ErrorCode configSelectedFeedbackSensor(FeedbackDevice feedbackDevice, int pidIdx, int timeoutMs) {
		currentFeedbackDevice = feedbackDevice;
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode setStatusFramePeriod(StatusFrameEnhanced frame, int periodMs, int timeoutMs) {
		statusFramePeriod = periodMs;
		return ErrorCode.OK;
	}

	@Override
	public int getStatusFramePeriod(StatusFrameEnhanced frame, int timeoutMs) {
		return statusFramePeriod;
	}

	@Override
	public ErrorCode configVelocityMeasurementPeriod(VelocityMeasPeriod period, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configVelocityMeasurementWindow(int windowSize, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configForwardLimitSwitchSource(LimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configReverseLimitSwitchSource(LimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configPeakCurrentLimit(int amps, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configPeakCurrentDuration(int milliseconds, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configContinuousCurrentLimit(int amps, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public void enableCurrentLimit(boolean enable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void set(ControlMode mode, double demand) {
		this.mode = mode;
		this.demand = demand;
	}

	@Override
	public void set(ControlMode mode, double demand0, double demand1) {
		this.mode = mode;
		this.demand = demand0;
	}
	
	public ControlMode getLastControlMode() {
		return mode;
	}
	
	public double getLastDemand() {
		return demand;
	}

	@Override
	public void neutralOutput() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNeutralMode(NeutralMode neutralMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSensorPhase(boolean PhaseSensor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInverted(boolean invert) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getInverted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ErrorCode configOpenloopRamp(double secondsFromNeutralToFull, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configClosedloopRamp(double secondsFromNeutralToFull, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configPeakOutputForward(double percentOut, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configPeakOutputReverse(double percentOut, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configNominalOutputForward(double percentOut, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configNominalOutputReverse(double percentOut, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configNeutralDeadband(double percentDeadband, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configVoltageCompSaturation(double voltage, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configVoltageMeasurementFilter(int filterWindowSamples, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public void enableVoltageCompensation(boolean enable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getBusVoltage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMotorOutputPercent() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMotorOutputVoltage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getOutputCurrent() {
		return outputCurrent;
	}
	
	public MockTalonSRX setOutputCurrent(double current) {
		outputCurrent = current;
		return this;
	}

	@Override
	public double getTemperature() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ErrorCode configSelectedFeedbackSensor(RemoteFeedbackDevice feedbackDevice, int pidIdx, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configRemoteFeedbackFilter(int deviceID, RemoteSensorSource remoteSensorSource, int remoteOrdinal,
			int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configSensorTerm(SensorTerm sensorTerm, FeedbackDevice feedbackDevice, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public double getSelectedSensorPosition(int pidIdx) {
		return sensorPosition;
	}

	@Override
	public ErrorCode setSelectedSensorPosition(double sensorPos, int pidIdx, int timeoutMs) {
		this.sensorPosition = sensorPos;
		return ErrorCode.OK;
	}

	@Override
	public double getSelectedSensorVelocity(int pidIdx) {
		return sensorVelocity;
	}

	public void setSelectedSensorVelocity(double sensorVel) {
		this.sensorVelocity = sensorVel;
	}

	@Override
	public ErrorCode setControlFramePeriod(ControlFrame frame, int periodMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode setStatusFramePeriod(StatusFrame frame, int periodMs, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public int getStatusFramePeriod(StatusFrame frame, int timeoutMs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ErrorCode configForwardLimitSwitchSource(RemoteLimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int deviceID, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configReverseLimitSwitchSource(RemoteLimitSwitchSource type, LimitSwitchNormal normalOpenOrClose,
			int deviceID, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public void overrideLimitSwitchesEnable(boolean enable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ErrorCode configForwardSoftLimitThreshold(int forwardSensorLimit, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configReverseSoftLimitThreshold(int reverseSensorLimit, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configForwardSoftLimitEnable(boolean enable, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configReverseSoftLimitEnable(boolean enable, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public void overrideSoftLimitsEnable(boolean enable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ErrorCode config_kP(int slotIdx, double value, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode config_kI(int slotIdx, double value, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode config_kD(int slotIdx, double value, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode config_kF(int slotIdx, double value, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode config_IntegralZone(int slotIdx, int izone, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configAllowableClosedloopError(int slotIdx, int allowableCloseLoopError, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configMaxIntegralAccumulator(int slotIdx, double iaccum, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode setIntegralAccumulator(double iaccum, int pidIdx, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public int getClosedLoopError(int pidIdx) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getIntegralAccumulator(int pidIdx) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getErrorDerivative(int pidIdx) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void selectProfileSlot(int slotIdx, int pidIdx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getActiveTrajectoryPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getActiveTrajectoryVelocity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getActiveTrajectoryHeading() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ErrorCode configMotionCruiseVelocity(int sensorUnitsPer100ms, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configMotionAcceleration(int sensorUnitsPer100msPerSec, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode clearMotionProfileTrajectories() {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public int getMotionProfileTopLevelBufferCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ErrorCode pushMotionProfileTrajectory(TrajectoryPoint trajPt) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public boolean isMotionProfileTopLevelBufferFull() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void processMotionProfileBuffer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ErrorCode getMotionProfileStatus(MotionProfileStatus statusToFill) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode clearMotionProfileHasUnderrun(int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode changeMotionControlFramePeriod(int periodMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode getLastError() {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode getFaults(Faults toFill) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode getStickyFaults(StickyFaults toFill) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode clearStickyFaults(int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public int getFirmwareVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasResetOccurred() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ErrorCode configSetCustomParam(int newValue, int paramIndex, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public int configGetCustomParam(int paramIndex, int timoutMs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ErrorCode configSetParameter(ParamEnum param, double value, int subValue, int ordinal, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode configSetParameter(int param, double value, int subValue, int ordinal, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public double configGetParameter(ParamEnum paramEnum, int ordinal, int timeoutMs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double configGetParameter(int paramEnum, int ordinal, int timeoutMs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBaseID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDeviceID() {
		return id;
	}

	@Override
	public TalonSensorCollection getSensorCollection() {
		// TODO Auto-generated method stub
		return this;
	}
	
	public void update() {
		// update all the sensors depending on how we're moving...
	}
	
	
	
	/*
	 * Sensor Collection entries.
	 * 
	 * Because the sensors are intimately tied to the talon's functions
	 * We include these methods here. This allows the two classes to share data, such as motor values.
	 */
	
	@Override
	public IMotorController getHWTalon() {
		return null;
	}
	
	public TalonSRX follow(IMotorController master) {
		return this;
	}
	
	@Override
	public int getAnalogIn() {
		// TODO Auto-generated method stub
		return analogValue;
	}

	@Override
	public int getAnalogInRaw() {
		// TODO Auto-generated method stub
		return analogValue;
	}

	@Override
	public int getAnalogInVel() {
		// TODO Auto-generated method stub
		return analogValue;
	}

	@Override
	public boolean getPinStateQuadA() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getPinStateQuadB() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getPinStateQuadIdx() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getPulseWidthPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPulseWidthRiseToFallUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPulseWidthRiseToRiseUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPulseWidthVelocity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getQuadraturePosition() {
		// TODO Auto-generated method stub
		return quadraturePosition * scale;
	}

	@Override
	public int getQuadratureVelocity() {
		return (int) quadratureSpeed;
	}

	public void setQuadratureVelocity(double velocity) {
		this.quadratureSpeed = velocity;
	}

	@Override
	public boolean isFwdLimitSwitchClosed() {
		return fwdLimitSwitchClosed;
	}

	public MockTalonSRX setFwdLimitSwitchClosed(boolean closed) {
		fwdLimitSwitchClosed = closed;
		return this;
	}

	@Override
	public boolean isRevLimitSwitchClosed() {
		return revLimitSwitchClosed;
	}
	
	public MockTalonSRX setRevLimitSwitchClosed(boolean closed) {
		revLimitSwitchClosed = closed;
		return this;
	}

	@Override
	public ErrorCode setAnalogPosition(int newPosition, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode setPulseWidthPosition(int newPosition, int timeoutMs) {
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}

	@Override
	public ErrorCode setQuadraturePosition(double newPosition, int timeoutMs) {
		quadraturePosition = newPosition / scale;
		// TODO Auto-generated method stub
		return ErrorCode.OK;
	}
	
	@Override
	public TalonSRX setScale(double scale) {
		this.scale = scale;
		return this;
	}
}
