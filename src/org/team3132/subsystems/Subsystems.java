package org.team3132.subsystems;

import java.util.function.DoubleSupplier;

import org.strongback.Executor.Priority;
import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.components.Gyroscope;
import org.strongback.components.PneumaticsModule;
import org.strongback.components.Solenoid;
import org.strongback.components.TalonSRX;
import org.strongback.hardware.Hardware;
import org.strongback.mock.Mock;
import org.team3132.Constants;
import org.team3132.driveRoutines.DriveRoutine;
import org.team3132.interfaces.LEDControllerInterface;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.DrivebaseInterface;
import org.team3132.interfaces.EndgameInterface;
import org.team3132.interfaces.IntakeInterface;
import org.team3132.interfaces.LiftInterface;
import org.team3132.interfaces.LocationInterface;
import org.team3132.interfaces.Log;
import org.team3132.interfaces.OuttakeInterface;
import org.team3132.lib.MotorFactory;
import org.team3132.lib.NavXGyroscope;
import org.team3132.lib.RobotConfiguration;
import org.team3132.mock.MockLEDController;
import org.team3132.mock.MockDrivebase;
import org.team3132.mock.MockEndgame;
import org.team3132.mock.MockIntake;
import org.team3132.mock.MockLift;
import org.team3132.mock.MockLocation;
import org.team3132.mock.MockOuttake;

import com.ctre.phoenix.CANifier;
import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * Contains the subsystems for the robot.
 * 
 * Makes it easy to pass all subsystems around.
 */
public class Subsystems {
	// Not really a subsystem, but used by all subsystems.
	public DashboardInterface dashboard;
	public RobotConfiguration config;
	public Clock clock;
	public Log log;

	public LocationInterface location;
	public DrivebaseInterface drivebase;
	public IntakeInterface intake;
	public LiftInterface lift;
	public OuttakeInterface outtake;
	public EndgameInterface endgame;
	public PneumaticsModule compressor;
	public LEDControllerInterface led;
	public DoubleSupplier leftDriveDistance;
	public DoubleSupplier rightDriveDistance;
	
	// Override interfaces used by the button box.
	public OverridableLift overridableLift;
	public OverridableIntake overridableIntake;

	
	public Subsystems(DashboardInterface dashboard, RobotConfiguration config, Clock clock, Log log) {
		this.dashboard = dashboard;
		this.config = config;
		this.clock = clock;
		this.log = log;
	}
	
	public void enable() {
		log.info("Enabling subsystems");
		// location is always enabled.
		drivebase.enable();
		intake.enable();
		lift.enable();
		outtake.enable();
		endgame.enable();
		led.enable();
	}
	
	public void updateDashboard() {
		location.updateDashboard();
		drivebase.updateDashboard();
		intake.updateDashboard();
		lift.updateDashboard();
		outtake.updateDashboard();
	}
	
	/**
     * Create the drivebase and location subsystems.
     * Also creates the motors and gyro as needed by both.
     */
    public void createDrivebaseLocation(DriveRoutine defaultDriveRoutine) {
    	if (!config.drivebaseIsPresent) {
			log.sub("Using mock drivebase");
    		drivebase = new MockDrivebase(log);
    		location = new MockLocation();
   	    	log.sub("Created a mock drivebase and location");
    		return;
    	}
    	// Redundant drive motors - automatic failover if the talon or the encoders fail.
		TalonSRX leftMotor = MotorFactory.getDriveMotor(config.drivebaseCanIdsLeftWithEncoders,
				config.drivebaseCanIdsLeftWithoutEncoders, true, config.drivebaseSensorPhase, config.drivebaseRampRate,
				config.drivebaseCurrentLimiting, config.drivebaseContCurrent, config.drivebasePeakCurrent, clock, log);
		TalonSRX rightMotor = MotorFactory.getDriveMotor(config.drivebaseCanIdsRightWithEncoders,
				config.drivebaseCanIdsRightWithoutEncoders, false, config.drivebaseSensorPhase,
				config.drivebaseRampRate, config.drivebaseCurrentLimiting, config.drivebaseContCurrent,
				config.drivebasePeakCurrent, clock, log);
		leftDriveDistance = () ->leftMotor.getSelectedSensorPosition(0);
		rightDriveDistance = () ->rightMotor.getSelectedSensorPosition(0);

		Gyroscope gyro = new NavXGyroscope("NavX", config.navxIsPresent, log);
    	gyro.zero();
		location = new Location(leftDriveDistance, rightDriveDistance,
				gyro, clock, dashboard, log); // Encoders must return inches.
		drivebase = new Drivebase(leftMotor, rightMotor, defaultDriveRoutine, ControlMode.PercentOutput, dashboard,
				log);
    	Strongback.executor().register(drivebase, Priority.HIGH);
		Strongback.executor().register(location, Priority.HIGH);
    }
    
    /**
     * Create the intake subsystem.
     * In this iteration of the robot we have two intake arms, each with a position motor
     * (and associated pot) as well as two intakes motors to suck in the cubes.
     * Also plumbs in the intake override, so the intake can be disconnected from the
     * main logic and controlled directly by the operator box.
     */
	public void createIntake() {
		if (!config.intakeIsPresent) {
			intake = new MockIntake(log);
			log.sub("Created a mock intake");
			return;
		}		
		TalonSRX leftMotor = MotorFactory.getIntakeMotor(config.intakeCanLeft, true, log);
		TalonSRX rightMotor = MotorFactory.getIntakeMotor(config.intakeCanRight, false, log);
		TalonSRX leftPositionMotor = MotorFactory.getIntakePositionMotor(config.intakeCanPositionLeft, true, log);
		TalonSRX rightPositionMotor = MotorFactory.getIntakePositionMotor(config.intakeCanPositionRight, false, log);
		
		intake = new Intake(config.teamNumber, leftMotor, rightMotor,
				leftPositionMotor, rightPositionMotor, dashboard, log);
		// Plumb in the overridable intake for the button box.
		intake = overridableIntake = new OverridableIntake(intake, log);
		Strongback.executor().register(intake, Priority.HIGH);
	}

    /**
     * Create the lift subsystem. The current gearbox has a shifter.
     * Also plumb in the lift override, so the lift can be disconnected from the
     * main logic and controlled direcly by the operator box.
     */
    public void createLift() {
    	if (!config.liftIsPresent) {
    		lift = new MockLift(clock, log);
   	    	log.sub("Created a mock lift");
    		return;
    	}
    	Solenoid shifter = Hardware.Solenoids.singleSolenoid(config.pcmCanId, Constants.LIFT_SHIFTER_SOLENOID_PCM_ID, 0.5, 0.5);
    	TalonSRX motor = MotorFactory.getLiftMotor(config.liftCanIds, log);
    	lift = new Lift(motor, shifter, clock, dashboard, log);
    	lift = overridableLift = new OverridableLift(lift, log);
		Strongback.executor().register(lift, Priority.HIGH);
    }

	/**
	 * Create the outtake subsystem.
	 * Now only a pneumatic cylinder and an ejection motor.
	 * The cube sensors have been removed due to mechanical and electrical concerns.
	 */
	public void createOuttake() {
		if (!config.outtakeIsPresent) {
			outtake = new MockOuttake(log);
   	    	log.sub("Created a mock outtake");
   	    	return;
		}
		
		TalonSRX motor = MotorFactory.getOuttakeMotor(config.outtakeCanIds, false, log);
		Solenoid clampSolenoid = Hardware.Solenoids.singleSolenoid(config.pcmCanId,
				Constants.OUTTAKE_CLAMPING_SOLENOID_PCM_ID, Constants.OUTTAKE_CLOSING_DELAY,
				Constants.OUTTAKE_OPENING_DELAY);
		outtake = new Outtake(motor, clampSolenoid, dashboard, log);
		Strongback.executor().register(outtake, Priority.HIGH);
	}

    /**
     * Create the Pneumatics Control Module (PCM) subsystem.
     */
    public void createPneumatics() {
    	if (!config.pcmIsPresent) {
    		compressor = Mock.pneumaticsModule(config.pcmCanId);
   	    	log.sub("Created a mock compressor");
    		return;
    	}
    	compressor = Hardware.pneumaticsModule(config.pcmCanId);
    }
        
    /**
     * Create the subsystem for the ramp.
     */
    public void createEndgame() {
    	if (!config.endgameIsPresent) {
    		endgame = new MockEndgame();
    		return;
    	}
    	Solenoid rampSolenoid = Hardware.Solenoids.singleSolenoid(
    			config.pcmCanId,
    			Constants.ENDGAME_RAMP_SOLENOID_PCM_ID,
    			Constants.ENDGAME_PCM_SOLENOID_TIMEOUT,
    			Constants.ENDGAME_PCM_SOLENOID_TIMEOUT
    			);
    	endgame = new Endgame(rampSolenoid, dashboard, log);   	
    }

	/**
	 * The LED strip that is cosmetic only.
	 */
	public void createLEDController() {
		if (!config.canifierIsPresent) {
			led = new MockLEDController();
			return;
		}
		CANifier canifier = new CANifier(Constants.LED_CANIFIER_CAN_ID);
		led = new LEDController(dashboard, canifier, dashboard, clock, log);
	}
}
