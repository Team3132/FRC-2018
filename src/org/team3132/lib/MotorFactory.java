package org.team3132.lib;

import java.util.ArrayList;

import org.strongback.components.Clock;
import org.strongback.components.TalonSRX;
import org.strongback.hardware.Hardware;
import org.team3132.Constants;
import org.team3132.interfaces.Log;

import com.ctre.phoenix.ParamEnum;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;

public class MotorFactory {

	public static TalonSRX getDriveMotor(int[] canIDsWithEncoders, int[] canIDsWithoutEncoders, boolean leftMotor,
			boolean sensorPhase, double rampRate, boolean doCurrentLimiting, int contCurrent, int peakCurrent,
			Clock clock, Log log) {
		TalonSRX motor = getTalon(canIDsWithEncoders, canIDsWithoutEncoders, !leftMotor, NeutralMode.Brake, clock, log)		// don't invert output
				.setScale(Constants.DRIVE_MOTOR_POSITION_SCALE);									// number of ticks per inch of travel.
		motor.config_kF(0, Constants.DRIVE_F, 10);
		motor.config_kP(0, Constants.DRIVE_P, 10);
		motor.config_kI(0, Constants.DRIVE_I, 10);
		motor.config_kD(0, Constants.DRIVE_D, 10);
		motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		motor.setSensorPhase(sensorPhase);
		motor.configClosedloopRamp(rampRate, 10);
		motor.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 10, 10);
		/*
		 * Setup Current Limiting
		 */
		if (doCurrentLimiting) {
			motor.configContinuousCurrentLimit(contCurrent, 0);		// limit to 35 Amps when current exceeds 40 amps for 100ms
			motor.configPeakCurrentLimit(peakCurrent, 0);
			motor.configPeakCurrentDuration(100, 0);
			motor.enableCurrentLimit(true);
		}
		return motor;
	}
	
	public static TalonSRX getLiftMotor(int[] canIDs, Log log) {
    	TalonSRX motor = getTalon(canIDs, true, NeutralMode.Brake, log)
    			.setScale(Constants.LIFT_SCALE);
		motor.configSelectedFeedbackSensor(FeedbackDevice.Analog, 0, 10);
		motor.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10);	// true when carriage is at top
		motor.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10);	// true when carriage is at base
		motor.configContinuousCurrentLimit(Constants.LIFT_CONTINUOUS_CURRENT_LIMIT, Constants.LIFT_CURRENT_TIMEOUT_MS);
		motor.configPeakCurrentLimit(Constants.LIFT_PEAK_CURRENT_LIMIT, Constants.LIFT_CURRENT_TIMEOUT_MS);
		motor.setSensorPhase(true);
		motor.config_kF(0, Constants.LIFT_HIGH_GEAR_UP_F, 10);
		motor.config_kP(0, Constants.LIFT_HIGH_GEAR_UP_P, 10);
		motor.config_kI(0, Constants.LIFT_HIGH_GEAR_UP_I, 10);
		motor.config_kD(0, Constants.LIFT_HIGH_GEAR_UP_D, 10);
		motor.configClosedloopRamp(0, 10);
		// Set the deadband to zero.
		motor.configAllowableClosedloopError(0, 0, 10);  // 1" = 20
		motor.configAllowableClosedloopError(1, 0, 10);
		motor.configSetParameter(ParamEnum.eClearPositionOnLimitR, 0, 0x00, 0x00, 10); //this makes the lift set its height to 0 when it reaches the soft stop (hall effect)
		motor.configSetParameter(ParamEnum.eClearPositionOnLimitF, 0, 0x00, 0x00, 10);
		motor.configForwardSoftLimitThreshold(Constants.LIFT_FWD_SOFT_LIMIT, 10);
		motor.configReverseSoftLimitThreshold(Constants.LIFT_REV_SOFT_LIMIT, 10);
		motor.configForwardSoftLimitEnable(true, 10);
		motor.configReverseSoftLimitEnable(true, 10);
		motor.configMotionAcceleration(Constants.LIFT_MOTION_ACCEL, 10);
		motor.configMotionCruiseVelocity(Constants.LIFT_MOTION_MAX, 10);

		motor.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, 10);
		motor.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, 10);
		return motor;
	}
	
	public static TalonSRX getIntakeMotor(int canID, boolean invert, Log log) {
		TalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.configClosedloopRamp(.25, 10);
		motor.configReverseSoftLimitEnable(false, 10);
		motor.configReverseLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.NormallyClosed, 10);
		motor.configVoltageCompSaturation(8, 10);
		motor.enableVoltageCompensation(true);
		return motor;
	}

	public static TalonSRX getIntakePositionMotor(int canID, boolean invert, Log log) {
		TalonSRX motor = getTalon(canID, invert, NeutralMode.Brake, log);
		motor.selectProfileSlot(0, 0);
		motor.setSensorPhase(true);
		motor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 10);
		motor.config_kF(0, Constants.INTAKE_POSITION_F, 10);
		motor.config_kP(0, Constants.INTAKE_POSITION_P, 10);
		motor.config_kI(0, Constants.INTAKE_POSITION_I, 10);
		motor.config_kD(0, Constants.INTAKE_POSITION_D, 10);
		motor.configAllowableClosedloopError(0, 10, 10);
		motor.configNominalOutputForward(0, 10);
		motor.configNominalOutputReverse(0, 10);
		motor.configPeakOutputForward(1, 0);
		motor.configPeakOutputReverse(-1, 0);
		return motor;
	}
	
	public static TalonSRX getOuttakeMotor(int[] canIDs, boolean invert, Log log) {
		TalonSRX motor = getTalon(canIDs, invert, NeutralMode.Brake, log);
		motor.configClosedloopRamp(.25, 10);
		motor.configReverseSoftLimitEnable(false, 10);
		motor.configForwardSoftLimitEnable(false, 10);
		return motor;
	}
	
    /**
     * Code to allow us to log output current per talon using redundant talons so if a talon or encoder
     * fails, it will automatically log and switch to the next one.
     * @param canIDsWithEncoders list of talons that can be the leader due to having an encoder.
     * @param canIDsWithoutEncoders list of talons without encoders that can never be the leader.
     * @param invert reverse the direction of the output.
     * @param log logger.
     * @return
     */
    private static TalonSRX getTalon(int[] canIDsWithEncoders, int[] canIDsWithoutEncoders, boolean invert, NeutralMode mode, Clock clock, Log log) {
    	ArrayList<TalonSRX> potentialLeaders = getTalonList(canIDsWithEncoders, invert, mode, log);
    	ArrayList<TalonSRX> followers = getTalonList(canIDsWithoutEncoders, invert, mode, log);
    	return new RedundantTalonSRX(potentialLeaders, followers, clock, log);
	}
	
	private static ArrayList<TalonSRX> getTalonList(int[] canIDs, boolean invert, NeutralMode mode, Log log) {
		ArrayList<TalonSRX> list = new ArrayList<>();
		for (int i = 0; i < canIDs.length; i++) {
			TalonSRX talon = Hardware.TalonSRXs.talonSRX(canIDs[i], invert, mode);
			talon.configContinuousCurrentLimit(Constants.DEFAULT_TALON_CONTINUOUS_CURRENT_LIMIT, 10);
			talon.configPeakCurrentLimit(Constants.DEFAULT_TALON_PEAK_CURRENT_LIMIT, 10);
			list.add(talon);
		}
		return list;
	}
    		
    /**
     * Code to allow us to log output current per talon.
     * @param canIDs
     * @param invert
     * @param log
     * @return
     */
    private static TalonSRX getTalon(int[] canIDs, boolean invert, NeutralMode mode, Log log) {

    	TalonSRX leader = Hardware.TalonSRXs.talonSRX(canIDs[0], invert, mode);
		log.register(false, () -> leader.getOutputCurrent(), "Talons/%d/Current", canIDs[0]);
		leader.configContinuousCurrentLimit(Constants.DEFAULT_TALON_CONTINUOUS_CURRENT_LIMIT, 10);
		leader.configPeakCurrentLimit(Constants.DEFAULT_TALON_PEAK_CURRENT_LIMIT, 10);

    	for (int i = 1; i < canIDs.length; i++) {
    		TalonSRX follower = Hardware.TalonSRXs.talonSRX(canIDs[i], invert, mode);
			follower.getHWTalon().follow(leader.getHWTalon());
			log.register(false, () -> follower.getOutputCurrent(), "Talons/%d/Current", canIDs[i]);
		}
		return leader;
	}
        /**
     * Code to allow us to log output current for a single talon.
     * @param canID
     * @param invert
     * @param log
     * @return
     */
    private static TalonSRX getTalon(int canID, boolean invert, NeutralMode mode, Log log) {
		log.sub("%s: " + canID, "talon");
		int[] canIDs = new int[1];
		canIDs[0] = canID;
    	return getTalon(canIDs, invert, mode, log);
    }
}
