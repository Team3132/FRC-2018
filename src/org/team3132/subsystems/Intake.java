package org.team3132.subsystems;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import org.strongback.Executable;
import org.strongback.components.TalonSRX;
import org.team3132.Constants;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.IntakeInterface;
import org.team3132.interfaces.Log;
import org.team3132.lib.Subsystem;

import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * Provides control for the intake arms. There are two intake arms, each in one
 * of the positions, stowed, wide, narrow or super narrow.
 * 
 * On each intake arm there are intake motors (spinning wheels) to suck the cube
 * in, as well as arm positioning motors with position feedback.
 */
public class Intake extends Subsystem implements IntakeInterface, Executable {

	private TalonSRX leftIntakeMotor, rightIntakeMotor;
	private IntakeConfiguration config;
	public SingleIntakeSide leftArm, rightArm;
	
	/**
	 * Left and right intake arm positions relative to the base position
	 * of each intake.
	 * The intakes need to be setup so that a positive movement makes
	 * both of them move from the stowed position towards the narrow
	 * position.
	 */
	private static class Position {
		public final int left;
		public final int right;
		public Position(int left, int right) {
			this.left = left;
			this.right = right;
		}
	}

	// Map the interfaces idea of positions to offsets that can be passed to the arms.
	static final Map<IntakeConfiguration, Position> mappings;
	static void addMapping(IntakeConfiguration cfg, int left, int right) {
		mappings.put(cfg, new Position(left, right));
		
	}

	/**
	 * Load in the static mappings from each IntakeConfiguration to the actual
	 * positions for the arms. Makes it simple to find the new positions when
	 * a new configuration is selected.
	 */
	static {
		mappings = new HashMap<IntakeConfiguration, Position>();
		addMapping(IntakeConfiguration.STOWED, Constants.INTAKE_DELTA_TO_STOWED, Constants.INTAKE_DELTA_TO_STOWED);
		addMapping(IntakeConfiguration.LEFT_WIDE_RIGHT_NARROW, Constants.INTAKE_DELTA_TO_WIDE, Constants.INTAKE_DELTA_TO_NARROW);
		addMapping(IntakeConfiguration.LEFT_NARROW_RIGHT_WIDE, Constants.INTAKE_DELTA_TO_NARROW, Constants.INTAKE_DELTA_TO_WIDE);
		addMapping(IntakeConfiguration.LEFT_WIDE_RIGHT_SUPER_NARROW, Constants.INTAKE_DELTA_TO_WIDE, Constants.INTAKE_DELTA_TO_SUPER_NARROW);
		addMapping(IntakeConfiguration.LEFT_SUPER_NARROW_RIGHT_WIDE, Constants.INTAKE_DELTA_TO_SUPER_NARROW, Constants.INTAKE_DELTA_TO_WIDE);
		addMapping(IntakeConfiguration.WIDE, Constants.INTAKE_DELTA_TO_WIDE, Constants.INTAKE_DELTA_TO_WIDE);
		addMapping(IntakeConfiguration.NARROW, Constants.INTAKE_DELTA_TO_NARROW, Constants.INTAKE_DELTA_TO_NARROW);
		addMapping(IntakeConfiguration.SUPER_NARROW, Constants.INTAKE_DELTA_TO_SUPER_NARROW, Constants.INTAKE_DELTA_TO_SUPER_NARROW);
		// Moving isn't a valid destination, but give it a valid place to go so it always works.
		addMapping(IntakeConfiguration.MOVING, Constants.INTAKE_DELTA_TO_STOWED, Constants.INTAKE_DELTA_TO_STOWED);
		// Check that no new values have been added that haven't been mapped above.
		assert (IntakeConfiguration.values().length == mappings.size());
	}
	
	private static Position getBasePositions(int teamNumber) {
		switch (teamNumber) {
		case 3132:
			return new Position(Constants.INTAKE_LEFT_BASE_POS_3132, Constants.INTAKE_RIGHT_BASE_POS_3132);
		case 9132:
			return new Position(Constants.INTAKE_LEFT_BASE_POS_9132, Constants.INTAKE_RIGHT_BASE_POS_9132);
		default:
			throw new InvalidParameterException(String.format("Invalid team %d for intake config", teamNumber));
		}
	}
		
	public Intake(int teamNumber, TalonSRX leftIntakeMotor, TalonSRX rightIntakeMotor,
			TalonSRX leftPositionMotor, TalonSRX rightPositionMotor, DashboardInterface dashboard,
			Log log) {
		super("Intake", dashboard, log);
		enabled = true;
		config = IntakeConfiguration.STOWED;
		log.sub("Intake using position config for team number " + teamNumber);
		Position base = getBasePositions(teamNumber);
		
		// Set limits on the motors for safety. These prevent the arms being driven out of range.
		leftPositionMotor.configForwardSoftLimitThreshold(base.left + Constants.INTAKE_DELTA_TO_SUPER_NARROW + Constants.INTAKE_DELTA_TO_LIMIT, 10);
		leftPositionMotor.configForwardSoftLimitEnable(false, 10);
		leftPositionMotor.configReverseSoftLimitThreshold(base.left - Constants.INTAKE_DELTA_TO_LIMIT, 10);
		leftPositionMotor.configReverseSoftLimitEnable(false, 10);

		rightPositionMotor.configForwardSoftLimitThreshold(base.right + Constants.INTAKE_DELTA_TO_SUPER_NARROW + Constants.INTAKE_DELTA_TO_LIMIT, 10);
		rightPositionMotor.configForwardSoftLimitEnable(false, 10);
		rightPositionMotor.configReverseSoftLimitThreshold(base.right - Constants.INTAKE_DELTA_TO_LIMIT, 10);
		rightPositionMotor.configReverseSoftLimitEnable(false, 10);

		leftArm = new SingleIntakeSide("LeftIntake", base.left, leftPositionMotor);
		rightArm = new SingleIntakeSide("RightIntake", base.right, rightPositionMotor);
		
		this.leftIntakeMotor = leftIntakeMotor;
		this.rightIntakeMotor = rightIntakeMotor;
	}

	/**
	 * Set the current position as the target position on enable.
	 */
	@Override
	public void enable() {
		super.enable();
		leftArm.enable();
		rightArm.enable();
	}

	/**
	 * Set the speed of the intake motors. We use percent output, so just provide a power.
	 * The motor configurations when created are responsible for having a positive value being intake, and a negative value being outtake.
	 */
	@Override
	public IntakeInterface setIntakeMotorOutput(double output) {
		leftIntakeMotor.set(ControlMode.PercentOutput, output);
		rightIntakeMotor.set(ControlMode.PercentOutput, output);
		return this;
	}

	@Override
	public double getIntakeMotorOutput() {
		return leftIntakeMotor.getMotorOutputPercent();
	}

	/**
	 * Set the desired intake arm configuration.
	 * We log each change, and invalid requests (currently only moving) cause an error message and do not change the configuration.
	 */
	@Override
	public IntakeInterface setConfiguration(IntakeConfiguration newConfig) {
		if (config == newConfig) {
			return this;	// still the same.
		}
		log.sub("%s: Changing intake state from %s to %s", name, config, newConfig);
		config = newConfig;
		if (!enabled) {
			return this;	// we're not enabled, so do nothing.
		}
		if (!mappings.containsKey(config)) {
			log.error("Invalid intake configuration %s", config);
			return this;
		}
		Position pos = mappings.get(config);	
		leftArm.moveTo(pos.left);
		rightArm.moveTo(pos.right);
		return this;
	}

	/**
	 * Return the current intake arm configuration, or MOVING if the arms are not in their final position.
	 */
	@Override
	public IntakeConfiguration getConfiguration() {
		if (leftArm.isMoving() || rightArm.isMoving()) {
			return IntakeConfiguration.MOVING;
		}
		return config;
	}

	/**
	 * Return true if the intake arms are in their desired configuration.
	 */
	@Override
	public boolean isInDesiredState() {
		return !leftArm.isMoving() && !rightArm.isMoving();
	}
	
	/**
	 * The single intake side holds the code and logic to control a single arm motor.
	 */
	protected class SingleIntakeSide {
		private final double base;
		private double target;
		private final TalonSRX motor;
		
		public SingleIntakeSide(String name, double base, TalonSRX motor) {
			this.base = base;
			this.motor = motor;
			this.target = motor.getSelectedSensorPosition(0);
			
			log.register(false, () -> motor.getSelectedSensorPosition(0), "%s/positionActual", name)
			   .register(true, () -> target, "%s/positionTarget", name)
			   .register(false, motor::getMotorOutputVoltage, "%s/outputVoltage", name)
			   .register(false, motor::getMotorOutputPercent, "%s/outputPercent", name)
			   .register(false, motor::getOutputCurrent, "%s/outputCurrent", name);
			
		}
		
		/**
		 * Update the target on enable in case the lift arms have been moved
		 * between starting and enabling, otherwise the controller will wait
		 * for the arms to move into place, but the arms won't know they need
		 * to move.
		 */
		public void enable() {
			target = motor.getSelectedSensorPosition(0);
			// Get the motor to enforce this or the arm may wander
			// and nothing will correct it.
			motor.set(ControlMode.Position, target);
		}
		
		public SingleIntakeSide moveTo(int position) {
			target = base + position;
			motor.set(ControlMode.Position, target);
			return this;	
		}

		/**
		 * Is the sample point within the specified tolerance of the mark point?
		 * @return true if the sample is within tolerance of the mark.
		 */
		private boolean inTolerance(double sample, double mark, double range) {
			return (Math.abs(mark - sample) <= range);
		}

		/**
		 * Returns if the arm is in the requested position.
		 */
		public boolean isMoving() {
			return !inTolerance(motor.getSelectedSensorPosition(0), target, Constants.INTAKE_POT_TOLERANCE);
		}
	}
	
	public void updateDashboard() {
		dashboard.putString("Intake confiuration", config.name());
	}
}
