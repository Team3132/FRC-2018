package org.team3132;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.ctre.phoenix.CANifier;
import com.ctre.phoenix.CANifier.LEDChannel;

/**
 * These are constants used by the robot. They define physical things about the world, or the robot.
 * 
 * We collate them here to have all these stored in one place.
 */
public class Constants {

    private Constants() {
		throw new IllegalStateException();
	}

	/*
	 * Global - These things are immutable
	 */
	public static final double FULL_CIRCLE = 360.0;		// size of a full circle in internal units (degrees)
	public static final double HALF_CIRCLE = 180.0;		// size of a half circle in internal units (degrees)
	public static final double QUARTER_CIRCLE = 90.0;	// size of a quarter circle in internal units (degrees)
	public static final int NUM_JOYSTICK_BUTTONS = 32;	// maximum number of push buttons on a joystick
	public static final int NUM_JOYSTICK_DIRECTIONS = 10;
	public static final double INCHES_TO_METRES = 0.0254;

	public static final double JOYSTICK_DEADBAND_MINIMUM_VALUE = 0.05;	// below this we deadband the value away
	/*
	 * Location on the roborio of the configuration file.
	 */
	public static final String CONFIG_FILE_PATH= "/home/lvuser/config.txt";
	public static final long EXECUTOR_CYCLE_INTERVAL_MSEC = 20;  // 50Hz
	public static final double DASHBOARD_UPDATE_INTERVAL_SEC = 0.25;
	
	/*
	 * Current limits.
	 */
	public static final int DEFAULT_TALON_CONTINUOUS_CURRENT_LIMIT = 30;
	public static final int DEFAULT_TALON_PEAK_CURRENT_LIMIT = 40;

	/*
	 * Drivebase Constants
	 * 
	 * The robot has motors on each side. This is the information that defines these motors and their behaviour
	 */
	public static final double ROBOT_WIDTH_INCHES = 20;
	public static final int[] DRIVE_LEFT_TALON_WITH_ENCODERS_CAN_ID_LIST	= {3};
	public static final int[] DRIVE_LEFT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST	= {2, 1};
	public static final int[] DRIVE_RIGHT_TALON_WITH_ENCODERS_CAN_ID_LIST	= {7, 6};  // Dual encoders on the right side!
	public static final int[] DRIVE_RIGHT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST	= {5};
	public static final boolean DRIVE_BRAKE_MODE			= true;
	public static final double DRIVE_WHEEL_DIAMETER         = 4.17; // Calculated wheel diameter HAN on 18-01-31
	public static final int DRIVE_ENCODER_CODES_PER_REV		= 4 * 360;
	// distance the robot moves per revolution of the encoders. Gearing needs to be taken into account here.
	// at full speed in a static environment the encoders are producing 2000 count differences per 100ms
	public static final double DRIVE_DISTANCE_PER_REV = DRIVE_WHEEL_DIAMETER * Math.PI;
	public static final double DRIVE_MOTOR_POSITION_SCALE = DRIVE_ENCODER_CODES_PER_REV / DRIVE_DISTANCE_PER_REV;
	
	// This magic number is the "fastest" we want the motor to go. It is calculated
	// by running the motor at full speed and observing what the quad encoder
	// velocity returns.
	// This number is very suspect.
	public static final double DRIVE_COUNT_100ms = 13.0;
	// A more sensible number.
	public static final double DRIVE_MAX_SPEED = 4;
	public static final double DRIVE_MAX_ACCELERATION = 2; // Inches/sec/sec
	public static final double DRIVE_MAX_JERK = 1; // Inches/sec/sec/sec.
	public static final double DRIVE_P = 2.5;//1.0;
	public static final double DRIVE_I = 0.0;
	public static final double DRIVE_D = 0.0;//0.01;
	public static final double DRIVE_F = 0.7;//0.665;
	public static final double DRIVE_DEADBAND = 0.05;
	public static final double DRIVE_RAMP_RATE = 0.13125; //0.175; sluggish but smooth //0.1375; jittered	// seconds from neutral to full
	public static final String DRIVE_MODE_ARCADE = "arcade";
	public static final String DRIVE_MODE_CHEESY = "cheesy";	
	public static final String DRIVE_DEFAULT_MODE = DRIVE_MODE_ARCADE;  // Joystick teleop mode.
	public static final int DRIVE_CONT_CURRENT = 38;	// current limit to this value if...
	public static final int DRIVE_PEAK_CURRENT = 80;	// the current exceeds this value for 100ms
	public static final int DRIVE_SCALE_FACTOR = 128;


	// LED controller
	public static final byte I2C_ARDUINO_LED = (byte) 0x62;
	
	// Power distribution Panel (PDP)
	public static final int PDP_CAN_ID = 62;
	
	// Pneumatic Control Modules (PCM)
	public static final int PCM_CAN_ID = 61;
	
	// Canifier 
	public static final int LED_CANIFIER_CAN_ID = 21;
	public static final CANifier.GeneralPin CANIFIER_LIMITSWITCH_PIN = CANifier.GeneralPin.LIMF;
	// LED channels for the canifier.
	public static final CANifier.LEDChannel RED_LED_STRIP_CHANNEL = LEDChannel.LEDChannelB;
	public static final CANifier.LEDChannel GREEN_LED_STRIP_CHANNEL = LEDChannel.LEDChannelA;
	public static final CANifier.LEDChannel BLUE_LED_STRIP_CHANNEL = LEDChannel.LEDChannelC;
	
	
	// logging information constants
	public static final String WEB_BASE_PATH = "/media/sda1";		// where web server's data lives
	public static final String LOG_BASE_PATH = WEB_BASE_PATH;		// log files (has to be inside web server)
	public static final String LOG_DATA_EXTENSION = "data";
	public static final String LOG_DATE_EXTENSION = "date";
	public static final Path LOG_NUMBER_FILE = Paths.get(System.getProperty("user.home"), "lognumber.txt");
	public static final int	 WEB_PORT = 5800;			// first open port for graph/log web server
	public static final double LOG_GRAPH_PERIOD = 0.05;	// run the graph updater every 50ms
	
	// LocationHistory
	public static final int LOCATION_HISTORY_MEMORY_SECONDS = 5;
	public static final int LOCATION_HISTORY_CYCLE_SPEED = 100; // in hz
	
	/*
	 * Command timings
	 */
	public static final double TIME_COMMAND_RUN_PERIOD = (1.0/50.0);		// run the commands 50 times a second
	public static final double TIME_LOCATION_PERIOD = (1.0/(double)LOCATION_HISTORY_CYCLE_SPEED);	// update the location subsystem 100 times a second
	public static final double TIME_DRIVEBASE_PERIOD = (1.0/40.0);	// update the drivebase 40 times a second

	/*
	 * We list the vision constants here. These will be fed into the vision subsystem to say which camera we are looking
	 * through and which target we are looking for.
	 */
	public enum VisionTarget {
		NO_SEEKING,
	}
	/*
	 * Vision Server. This has to be personalised for the targets each year.
	 */
	public static final int VISION_PORT_NUMBER = 5801;
	public static final double VISION_MAX_DATA_AGE_SEC = 1;
	public static final double VISION_PERIOD = (1.0/20.0);		// check 20 times a second. This allows a decent framerate.
	public static final String NO_SEEKING_CAMERA_ID = "n";		// don't look for anything.
	public static final VisionTarget VISION_DEFAULT_CAMERA_MODE = VisionTarget.NO_SEEKING;
	
	/*
	 * Lift constants
	 */
	public static final int[] LIFT_MOTOR_TALON_CAN_ID_LIST = {10,11,12};
	public static final int LIFT_SHIFTER_SOLENOID_PCM_ID = 0;

	public static final double LIFT_LOW_GEAR_P = 10.0;
	public static final double LIFT_LOW_GEAR_I = 0.0;
	public static final double LIFT_LOW_GEAR_D = 80.0;
	public static final double LIFT_LOW_GEAR_F = 10.0;

	public static final double LIFT_HIGH_GEAR_UP_P = 10.0;
	public static final double LIFT_HIGH_GEAR_UP_I = 0.0;
	public static final double LIFT_HIGH_GEAR_UP_D = 80.0;
	public static final double LIFT_HIGH_GEAR_UP_F = 10.0;

	public static final double LIFT_HIGH_GEAR_DOWN_P = 10.0;
	public static final double LIFT_HIGH_GEAR_DOWN_I = 0; // 0.01;
	public static final double LIFT_HIGH_GEAR_DOWN_D = 0; // 800.0;
	public static final double LIFT_HIGH_GEAR_DOWN_F = 10.0;

	public static final double LIFT_BOTTOM_HEIGHT = 2.0;

	public static final int LIFT_MOTION_MAX = 20;  // was 100
	public static final int LIFT_MOTION_ACCEL = 5 * LIFT_MOTION_MAX;


	public static final double LIFT_DEFAULT_TOLERANCE = 1.5;
	public static final double LIFT_CLIMBING_CURRENT = 0.5;		// The current the lift usually draws if it is holding at least our robot // TODO get real values
	public static final double LIFT_SCALE = (775.0-36.0)/39.0;		// The scaling factor which converts talon units to inches
	public static final int LIFT_BOTTOM_RAW_VALUE = 36;
	public static final double LIFT_MICRO_ADJUST_HEIGHT = 0.05;	// The smallest height by which the operator can raise the lift
	public static final double LIFT_CALIBRATE_TIMEOUT = 10; 	// The longest time for which we should ever be calibrating
	public static final double LIFT_CALIBRATE_PERCENT_OUT = .2; // The power we use to move the lift down to calibrate
	public static final int LIFT_CONTINUOUS_CURRENT_LIMIT = 38;
	public static final int LIFT_PEAK_CURRENT_LIMIT = 43;
	public static final int LIFT_CURRENT_TIMEOUT_MS = 100;
	public static final int LIFT_FWD_SOFT_LIMIT = 775;
	public static final int LIFT_REV_SOFT_LIMIT = 30;

	// Position Constants
	public static final double LIFT_MAX_HEIGHT_WITH_HOOKS_DEPLOYED = 36;
	public static final double LIFT_PORTAL_HEIGHT = 7.75;
	public static final double LIFT_MAX_HEIGHT_WITH_INTAKE_STOWED = 0; // the max height the lift can go to when the intake is stowed
	public static final double LIFT_MIN_HEIGHT_WITH_INTAKE_STOWED = 8; // the min height the lift can go to when the intake is stowed
	public static final double LIFT_DEFAULT_MAX_HEIGHT = 42;		// maximum height to which we can ask the lift to move.
	public static final double LIFT_DEFAULT_MIN_HEIGHT = 0.0;
	public static final double LIFT_DEPLOY_THRESHOLD_HEIGHT = 8.0; // lift is above the lowest height at which we can safely deploy/stow the intake
	// heights of the scale in four positions
	public static final double LIFT_SCALE_HEIGHT_HIGHEST = 40.0; // low: 30  mid: 36 high: 42   // lift is high enough to score at the SCALE		// TODO get real values
	public static final double LIFT_SCALE_HEIGHT_HIGH = 39.0;
	public static final double LIFT_SCALE_HEIGHT_LEVEL = 36.0;
	public static final double LIFT_SCALE_HEIGHT_LOW = 30.0;
	public static final double LIFT_SWITCH_HEIGHT = 14.0;//was12 	// lift is high enough to score at the SWITCH		// TODO get real values
	public static final double LIFT_INTAKE_HEIGHT = 0.0; 		// lift is low enough to intake a cube				// TODO get real values
	public static final double LIFT_DEPLOY_HOOKS_HEIGHT = 24; // low enough that the carriage isn't in the way to deploy.
	public static final double LIFT_CLIMB_HEIGHT = 32;	//33 lift is high enough to engage with the RUNG		// TODO get real values
	public static final double LIFT_CLIMB_STOP_HEIGHT = 0;//LIFT_CLIMB_HEIGHT - 31; 	// lift has climbed enough that the robot has completed the climb	// TODO get real values
	public static final double LIFT_SHIFTING_THRESHOLD_HEIGHT = LIFT_CLIMB_HEIGHT - 2;
	public static final double LIFT_SAFE_OUTTAKE_HEIGHT = 2;
	public static final double LIFT_VARIABLE_TRAVEL = -6.0;	// travel using gamepad variable adjust (negative as upwards has to be positive
	public static final double[] LIFT_SETPOINTS = {
			LIFT_INTAKE_HEIGHT,
			LIFT_SWITCH_HEIGHT,
			LIFT_SCALE_HEIGHT_HIGHEST,
			LIFT_SCALE_HEIGHT_HIGHEST + 4, // Higher in case it's tilted.
			LIFT_CLIMB_HEIGHT,
			LIFT_CLIMB_HEIGHT + 4, // Extra height.
			};


	/*
	 * Intake constants
	 */
	public static final double INTAKE_DELAY_SEC = 2; 	// TODO get real values
	public static final double INTAKE_IN_MOTOR_POWER = 1; 	// Full power needed to drag the cube into the intake
	public static final double INTAKE_EJECT_MOTOR_POWER = -1;//-0.3; 	// Push it out slower, so we don't injure the person at the exchange
	public static final int INTAKE_MOTOR_TALON_CAN_ID_LEFT = 14;
	public static final int INTAKE_MOTOR_TALON_CAN_ID_RIGHT = 13;
	/*
	 * Intake 2 (intake with motors) constants
	 */
	public static final int INTAKE_POSITION_TALON_CAN_ID_LEFT = 40;
	public static final int INTAKE_POSITION_TALON_CAN_ID_RIGHT = 41;
	public static final int INTAKE_POT_TOLERANCE = 100;//13;
	public static final double INTAKE_POSITION_P = 3;//4
	public static final double INTAKE_POSITION_I = 0.0;
	public static final double INTAKE_POSITION_D = 150;
	public static final double INTAKE_POSITION_F = 0.0;
	
	// Changes to base position to move intake to desired position.
	// Should be the same on all robots for the same pot type.
	public static final int INTAKE_DELTA_TO_LIMIT = 80;
	public static final int INTAKE_DELTA_TO_STOWED = 105;
	public static final int INTAKE_DELTA_TO_WIDE = 1600;
	public static final int INTAKE_DELTA_TO_NARROW = 1935; //2165;
	public static final int INTAKE_DELTA_TO_SUPER_NARROW = 2165;
	
	// Measured positions of the intake. To measure, position a horizontal battery Anderson
	// between the lift and an intake wheel and read eg https://tinyurl.com/ybxracat
	// Each robot is different because the pot position will be physically different.
	public static final int INTAKE_LEFT_BASE_POS_3132 = 3940;  // Needs update!
	public static final int INTAKE_RIGHT_BASE_POS_3132 = -1891;
	public static final int INTAKE_LEFT_BASE_POS_9132 = 3464;
	public static final int INTAKE_RIGHT_BASE_POS_9132 = -2272;

	/*
	 * Outtake
	 */
	public static final double OUTTAKE_CLOSING_DELAY = 0.5;
	public static final double OUTTAKE_OPENING_DELAY = 0.5;
	public static final int[] OUTTAKE_MOTOR_TALON_CAN_ID_LIST = {15};
	public static final int OUTTAKE_CANIFIER_CAN_ID = 20;
	public static final double OUTTAKE_SCORE_POWER_LEFT = -0.5;	// percent output to be used for scoring by auto
	public static final double OUTTAKE_SCORE_POWER_RIGHT = 0.5;	// percent output to be used for scoring by auto
	public static final double OUTTAKE_SCORING_DELAY = 1;	// time in seconds we should wait after we no longer see a cube in the outtake while scoring	// TODO get real values
	public static final int OUTTAKE_CLAMPING_SOLENOID_PCM_ID = 3;
	public static final double OUTTAKE_TIME_DELAY = 1;		// the time for which we keep outtaking after hasHalfCube() becomes false
	public static final double OUTTAKE_CUBE_TIME_MICROS = 600000;

	/*
	 * End game constants
	 *
	 */
	public static final int ENDGAME_RAMP_SOLENOID_PCM_ID = 5;
	public static final double ENDGAME_PCM_SOLENOID_TIMEOUT = 0.5;
	public static final double 	ENDGAME_DEPLOY_SECONDS = 120.0;

	/*
	 * Camera server constants
	 * 
	 */
	public static final int CAMERA_RESOLUTION_WIDTH = 320;
	public static final int CAMERA_RESOULTION_HEIGHT = 180;
	public static final int CAMERA_FRAMES_PER_SECOND = 30;
	public static final int NUMBER_OF_CAMERAS_DEFUALT = 0;
}