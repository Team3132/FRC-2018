package org.team3132.subsystems;

import org.strongback.components.Clock;
import org.strongback.components.Solenoid;
import org.strongback.components.TalonSRX;
import org.team3132.Constants;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.LiftInterface;
import org.team3132.interfaces.Log;
import org.team3132.lib.MathUtil;
import org.team3132.lib.Subsystem;

import com.ctre.phoenix.motorcontrol.ControlMode;

/*
 * The lift subsystem.
 * 
 * The lift is a two stage lift with two "soft stops" at either end.
 * There is a pot that is used to measure how far along the lift the carriage is currently.
 */

public class Lift extends Subsystem implements LiftInterface {
	private final int UP_CLIMB_PID_SLOT = 0;
	private final int DOWN_PID_SLOT = 1;

	private double desiredHeight = 0;
	private double maxHeight = Constants.LIFT_DEFAULT_MAX_HEIGHT;
	private double minHeight = Constants.LIFT_DEFAULT_MIN_HEIGHT;
	private TalonSRX liftMotor;
	private Solenoid shifter;
	private Clock clock;
	
	public Lift(TalonSRX liftMotor, Solenoid shifter, Clock clock, DashboardInterface dashboard, Log log) {
		super("Lift", dashboard, log);
		this.liftMotor = liftMotor;
		this.shifter = shifter;
		this.clock = clock;
		
		log.register(false, this::getHeight, "%s/Actual", name)
		   .register(true, () -> minHeight, "%s/Min", name)
		   .register(true, () -> maxHeight, "%s/Max", name)
		   .register(true, () -> desiredHeight, "%s/Desired", name)
		   .register(false, () -> shifter.isExtended()?1.0:0.0, "%s/Shifter", name)
		   .register(false, liftMotor::getOutputCurrent, "%s/Current", name)
		   .register(false, liftMotor::getMotorOutputVoltage, "%s/Voltage", name)
		   .register(false, liftMotor::getMotorOutputPercent, "%s/Percent", name);
		// PID values for up and low gear are the same, put them in slot 0.
		liftMotor.config_kP(UP_CLIMB_PID_SLOT, Constants.LIFT_HIGH_GEAR_UP_P, 10);
		liftMotor.config_kI(UP_CLIMB_PID_SLOT, Constants.LIFT_HIGH_GEAR_UP_I, 10);
		liftMotor.config_kD(UP_CLIMB_PID_SLOT, Constants.LIFT_HIGH_GEAR_UP_D, 10);
		liftMotor.config_kF(UP_CLIMB_PID_SLOT, Constants.LIFT_HIGH_GEAR_UP_F, 10);
		// Downwards PID values.
		liftMotor.config_kP(DOWN_PID_SLOT, Constants.LIFT_HIGH_GEAR_DOWN_P, 10);
		liftMotor.config_kI(DOWN_PID_SLOT, Constants.LIFT_HIGH_GEAR_DOWN_I, 10);
		liftMotor.config_kD(DOWN_PID_SLOT, Constants.LIFT_HIGH_GEAR_DOWN_D, 10);
		liftMotor.config_kF(DOWN_PID_SLOT, Constants.LIFT_HIGH_GEAR_DOWN_F, 10);
		// Use slot 0 (up).
		liftMotor.selectProfileSlot(UP_CLIMB_PID_SLOT, 0);
		// Tell the lift that the current height is where we want to be.
		// The robot should start with the lift at the bottom, but in case it
		// doesn't leave the lift in the starting position for a sequence to change.
		setHeight(getHeight());
		// Start in high gear.
		setHighGear();
	}
	
	/**
	 * Set the current height as the target height on enable.
	 */
	@Override
	public void enable() {
		super.enable();
		log.sub("Setting height target to " + getHeight());
		setHeight(getHeight());
	}

	@Override
	public LiftInterface setHeight(double height) {
		if (height == desiredHeight) return this;
		
		desiredHeight = height;
		if (isInLowGear()) {
			liftMotor.selectProfileSlot(UP_CLIMB_PID_SLOT, 0);
		} else if (height > getHeight()) { // going up
			liftMotor.selectProfileSlot(UP_CLIMB_PID_SLOT, 0);
		} else {
			liftMotor.selectProfileSlot(DOWN_PID_SLOT, 0);
		}
		liftMotor.set(ControlMode.MotionMagic,
				MathUtil.clamp(desiredHeight + Constants.LIFT_BOTTOM_HEIGHT, minHeight, maxHeight));
		log.sub("%s: set lift height %f", name, height);
		return this;
	}

	private double lastD = Constants.LIFT_HIGH_GEAR_DOWN_D;
	private double lastTimeOutOfRange = -1;
	/**
	 * Override the value of kD when the lift is close to the setpoint.
	 * A high value of kD is needed when the lift is going down so that it
	 * doesn't overshoot, but with a high value of kD the lift will jitter
	 * around the setpoint.
	 * Update: The lift has been slowed down so much that this hack is no
	 * longer needed.
	 */
	protected void update() {
		double desiredD = Constants.LIFT_HIGH_GEAR_DOWN_D;
		final double now = clock.currentTime();
		if (Math.abs(getDesiredHeight() - getHeight()) > 1) {
			lastTimeOutOfRange = now;
		}
		if (now - lastTimeOutOfRange > 0.1) {
			// Have been within range for more than a 1/10th second, turn off d.
			desiredD = 0;
		}
		if (desiredD != lastD) {
			lastD = desiredD;
			// Lift now goes so slow that this is no longer needed.
			//log.error("Overriding kD to %f",  desiredD);
			// Reset kD if the lift is close to the setpoint. Only has an effect
			// if we are using the high gear / down PID slot.
			//liftMotor.config_kD(DOWN_PID_SLOT, desiredD, 10);
		}
	}

	@Override
	public double getDesiredHeight() {
		return desiredHeight;
	}

	@Override
	public double getHeight() {
		return liftMotor.getSelectedSensorPosition(0) - Constants.LIFT_BOTTOM_HEIGHT;
	}
	
	@Override
	public boolean isInPosition() {
		return Math.abs(getDesiredHeight() - getHeight()) < Constants.LIFT_DEFAULT_TOLERANCE;
	}
	
	@Override
	public LiftInterface setLowGear() {
		liftMotor.set(ControlMode.PercentOutput, 0.0);
		shifter.retract();
		// Use the PID values for climbing.
		liftMotor.selectProfileSlot(UP_CLIMB_PID_SLOT, 0);
		return this;
	}

	@Override
	public LiftInterface setHighGear() {
		liftMotor.set(ControlMode.PercentOutput, 0.0);
		shifter.extend();
		if (getDesiredHeight() > getHeight()) {
			liftMotor.selectProfileSlot(UP_CLIMB_PID_SLOT, 0);
		} else {
			liftMotor.selectProfileSlot(DOWN_PID_SLOT, 0);
		}
		return this;
	}
	
	@Override
	public boolean isInLowGear() {
		return shifter.isRetracted();
	}
	
	@Override
	public boolean isAboveIntakeThreshold() {
		return getHeight() > Constants.LIFT_DEPLOY_THRESHOLD_HEIGHT - Constants.LIFT_DEFAULT_TOLERANCE;
	}
	
	/**
	 * Function which determines if it is safe to shift.
	 * If we are in high gear we should only shift above the rung
	 * If we are in low gear we should only shift if we are not holding the weight
	 * of the robot (i.e. the motors aren't stalling)
	 */
	@Override
	public boolean isSafeToShift() {
		System.out.printf("is safe to shift, lift height = %f\n", getHeight());
		if (isInLowGear()) {
			return liftMotor.getOutputCurrent() < Constants.LIFT_CLIMBING_CURRENT;
		}
		// High gear.
		return getHeight() > Constants.LIFT_CLIMB_HEIGHT - 2;
	}

	@Override
	public void updateDashboard() {
		dashboard.putNumber("Lift height", getHeight());		
		dashboard.putNumber("Lift setpoint", getDesiredHeight());
		dashboard.putString("Lift status", isInLowGear()?"Low gear":"High gear");
	}
}
