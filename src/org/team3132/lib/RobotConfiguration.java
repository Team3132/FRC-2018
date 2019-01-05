package org.team3132.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.team3132.Constants;
import org.team3132.interfaces.Log;

/**
 * Class responsible for updating values which are dependent on robot hardware.
 * (e.g. if subsystems are present or not) It reads from a text file
 * Currently the supported types are String, int, double, boolean and int array.
 * 
 * Example lines:
 * drivebase/present = true
 * drivebase/rampRate = 0.13125
 * drivebase/right/canIDs/withEncoders = 7,6
 * drivebase/right/canIDs/withoutEncoders = 5
 * 
 * TODO: Add support for comments and freeform parameters.
 * @author carlin
 */
public class RobotConfiguration {
	private String name = "RobotConfig";
	
	private Log log;
	private Map<String, String> lines;
	private Map<String, String> ignoredEntries;  // Lines/entries not used in the config file.
	private Map<String, String> nonDefaultParameters;  // Non-default values used from the config file.
	private ArrayList<String> exampleText = new ArrayList<String>();

	// These are variables which will be updated
	public String robotName = "3132";
	
	public double robotLengthWithBumpers = 0;
	public double robotWidthWithBumpers = 0;
	public double cameraFromFrontWithBumpers = 0;
	public double cameraFromLeftWithBumpers = 0;

	public int teamNumber = 3132;
	public boolean drivebaseIsPresent = true;
	public int[] drivebaseCanIdsLeftWithEncoders = Constants.DRIVE_LEFT_TALON_WITH_ENCODERS_CAN_ID_LIST;
	public int[] drivebaseCanIdsLeftWithoutEncoders = Constants.DRIVE_LEFT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST;
	public int[] drivebaseCanIdsRightWithEncoders = Constants.DRIVE_RIGHT_TALON_WITH_ENCODERS_CAN_ID_LIST;
	public int[] drivebaseCanIdsRightWithoutEncoders = Constants.DRIVE_RIGHT_TALON_WITHOUT_ENCODERS_CAN_ID_LIST;
	public boolean drivebaseCurrentLimiting = true;
	public int drivebaseContCurrent = Constants.DRIVE_CONT_CURRENT;
	public int drivebasePeakCurrent = Constants.DRIVE_PEAK_CURRENT;
	public double drivebaseRampRate = Constants.DRIVE_RAMP_RATE;
	public String drivebaseMode = Constants.DRIVE_DEFAULT_MODE;


	public boolean drivebaseSensorPhase = true;
	public double drivebaseCount =  Constants.DRIVE_COUNT_100ms;


	public boolean liftIsPresent = true;
	public int[] liftCanIds = Constants.LIFT_MOTOR_TALON_CAN_ID_LIST;
	
	public boolean intakeIsPresent = true;
	public int intakeCanLeft = Constants.INTAKE_MOTOR_TALON_CAN_ID_LEFT;
	public int intakeCanRight = Constants.INTAKE_MOTOR_TALON_CAN_ID_RIGHT;
	public int intakeCanPositionLeft = Constants.INTAKE_POSITION_TALON_CAN_ID_LEFT;
	public int intakeCanPositionRight = Constants.INTAKE_POSITION_TALON_CAN_ID_RIGHT;

	public boolean outtakeIsPresent = true;
	public int[] outtakeCanIds = Constants.OUTTAKE_MOTOR_TALON_CAN_ID_LIST;

	public boolean pdpIsPresent = true;
	public int pdpCanId = Constants.PDP_CAN_ID;
	public boolean pdpMonitor = false;  // by default we do NOT monitor the PDP
	public int[] pdpChannelsToMonitor = new int[0];  // by default we do NOT monitor any channels

	public boolean pcmIsPresent = true;
	public int pcmCanId = Constants.PCM_CAN_ID;
	
	public boolean canifierIsPresent = true;
	public int canifierCanId;

	public boolean canifierGlow = false;

	public boolean navxIsPresent = true;

	public boolean endgameIsPresent = true;

	public boolean cameraIsPresent = true;
	public int numberOfCameras = Constants.NUMBER_OF_CAMERAS_DEFUALT;

	public boolean dsPresent = true;

	public double endgameDeploySeconds = Constants.ENDGAME_DEPLOY_SECONDS;

	// Logging default is to not log anything to the graph, and to only log local information when we turn it on.
	// These are the safest defaults.
	public boolean doLogging = false;
	public boolean onlyLocal = true;

	public RobotConfiguration(String filePath, Log log) {
		this(filePath, TeamNumber.get(), log);
	}

	public RobotConfiguration(String filePath, int teamNumber, Log log) {
		this.teamNumber = teamNumber;
		this.log = log;
		robotName = Integer.toString(teamNumber);
		
		readLines(Paths.get(filePath));
		readParameters();  // Creates example contents.
		Collections.sort(exampleText);
		writeExampleFile(filePath, String.join("\n", exampleText));
	}
	
	private void writeExampleFile(String filePath, String contents) {
		Path exampleFile = Paths.get(filePath + ".example");
		try {
			BufferedWriter writer;
			writer = Files.newBufferedWriter(exampleFile, StandardOpenOption.CREATE);
			writer.write(contents + "\n");
			writer.close();
			log.info("Wrote example config file " + exampleFile.toString());
		} catch (IOException e) {
			log.exception("Unable to write example config file " + exampleFile.toString(), e);
		}
	}

	private void readLines(Path path) {
		log.info("Reading config file " + path);
		lines = new HashMap<String, String>();
		ignoredEntries = new TreeMap<String, String>();
		nonDefaultParameters = new TreeMap<String, String>();
		try (BufferedReader reader = Files.newBufferedReader(path)) {
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		    	String[] parts = line.split("\\s*=\\s*", -1); // Keep empty values
		    	if (parts.length < 2) {
		    		log.error("Bad config line " + line);
		    		continue;
		    	}
		    	String tag = parts[0].trim();
		    	String value = parts[1].trim();
		    	if (lines.containsKey(tag)) {
		    		log.error("ERROR: Duplicate tag %s in configuration file, last value will be used.", tag);
		    	}
		    	lines.put(tag, value);
		    	ignoredEntries.put(parts[0].trim(), line);
		    }
		} catch (NoSuchFileException e) {
			log.error("Config file %s not found, attempting to create it", path);
			// Touch the file so at least it's there next time.
			try {
				Files.createFile(path);
			} catch (IOException e1) {}
		} catch (IOException e) {
			log.exception("Error loading configuration file " + path + ", using defaults", e);
		}
	}
	
	private void readParameters() {	
		drivebaseIsPresent = getAsBoolean("drivebase/present", drivebaseIsPresent);
		drivebaseCanIdsLeftWithEncoders = getAsIntArray("drivebase/left/canIDs/withEncoders", drivebaseCanIdsLeftWithEncoders);
		drivebaseCanIdsLeftWithoutEncoders = getAsIntArray("drivebase/left/canIDs/withoutEncoders", drivebaseCanIdsLeftWithoutEncoders);
		drivebaseCanIdsRightWithEncoders = getAsIntArray("drivebase/right/canIDs/withEncoders", drivebaseCanIdsRightWithEncoders);
		drivebaseCanIdsRightWithoutEncoders = getAsIntArray("drivebase/right/canIDs/withoutEncoders", drivebaseCanIdsRightWithoutEncoders);
		drivebaseCurrentLimiting = getAsBoolean("drivebase/currentLimiting", drivebaseCurrentLimiting);
		drivebaseContCurrent = getAsInt("drivebase/maxCurrent", drivebaseContCurrent);
		drivebasePeakCurrent = getAsInt("drivebase/peakCurrent", drivebasePeakCurrent);
		drivebaseRampRate = getAsDouble("drivebase/rampRate", drivebaseRampRate);
		drivebaseMode = getAsString("drivebase/mode", drivebaseMode);
		drivebaseSensorPhase = getAsBoolean("drivebase/sensor/phase", drivebaseSensorPhase);
		drivebaseCount = getAsDouble("drivebase/count100ms", drivebaseCount);

		liftIsPresent = getAsBoolean("lift/present", true);
		liftCanIds = getAsIntArray("lift/canIDs", Constants.LIFT_MOTOR_TALON_CAN_ID_LIST);
		
		intakeIsPresent = getAsBoolean("intake/present", true);
		intakeCanLeft = getAsInt("intake/left/canID", Constants.INTAKE_MOTOR_TALON_CAN_ID_LEFT);
		intakeCanRight = getAsInt("intake/right/canID", Constants.INTAKE_MOTOR_TALON_CAN_ID_RIGHT);
		intakeCanPositionLeft = getAsInt("intake/left/position/canID", Constants.INTAKE_POSITION_TALON_CAN_ID_LEFT);
		intakeCanPositionRight = getAsInt("intake/right/position/canID", Constants.INTAKE_POSITION_TALON_CAN_ID_RIGHT);

		outtakeIsPresent = getAsBoolean("outtake/present", true);
		outtakeCanIds = getAsIntArray("outtake/canIDs", Constants.OUTTAKE_MOTOR_TALON_CAN_ID_LIST);
		
		pdpIsPresent = getAsBoolean("pdp/present", true);
		pdpCanId = getAsInt("pdp/canID", Constants.PDP_CAN_ID);
		pdpMonitor = getAsBoolean("pdp/monitor", false);		// by default we do NOT monitor the PDP
		pdpChannelsToMonitor = getAsIntArray("pdp/channels", new int[0]);	// by default we do NOT monitor and channels

		pcmIsPresent = getAsBoolean("pcm/present", true);
		pcmCanId = getAsInt("pcm/canID", Constants.PCM_CAN_ID);
		
		cameraIsPresent = getAsBoolean("cameras/present", false);
		numberOfCameras = getAsInt("cameras/number", Constants.NUMBER_OF_CAMERAS_DEFUALT);

		navxIsPresent = getAsBoolean("navx/present", true);

		dsPresent = getAsBoolean("ds/present", true);

		robotLengthWithBumpers = getAsDouble("dimensions/robot/lengthWithBumpers", 0.0);
		robotWidthWithBumpers = getAsDouble("dimensions/robot/widthWithBumpers", 0.0);
		cameraFromFrontWithBumpers = getAsDouble("dimensions/cameraFromFrontWithBumpers", 0.0);
		cameraFromLeftWithBumpers = getAsDouble("dimensions/cameraFromLeftWithBumpers", 0.0);

		// logging default is to not log anything to the graph, and to only log local information when we turn it on.
		// These are the safest defaults.
		doLogging = getAsBoolean("logging/enabled", true);
		onlyLocal = getAsBoolean("logging/onlyLocal", true);

		canifierIsPresent = getAsBoolean("canifier/present", true);
		canifierCanId = getAsInt("canifier/canID", Constants.LED_CANIFIER_CAN_ID);
		canifierGlow = getAsBoolean("canifier/glow", false);

		endgameIsPresent = getAsBoolean("endgame/present", true);
		endgameDeploySeconds = getAsDouble("endgame/deploySeconds", Constants.ENDGAME_DEPLOY_SECONDS);
		
		robotName = getAsString("robot/name", robotName );

		if (!ignoredEntries.isEmpty()) {
			log.warning("WARNING: These config file lines weren't used:");
			for (String entry : ignoredEntries.values()) {
				log.warning("  %s", entry);
			}
		}
		if (!nonDefaultParameters.isEmpty()) {
			log.warning("WARNING: These parameters have non-default values:");
			for (Entry<String, String> entry : nonDefaultParameters.entrySet()) {
				log.warning("  %s = %s", entry.getKey(), entry.getValue());
			}
		}
		log.info("RobotConfig finished loading parameters\n");
	}
	

	private <T> void appendExample(String key, T defaultValue) {
		exampleText.add(key + " = " + defaultValue);
	}
	
	private int getAsInt(String key, int defaultValue) {
		appendExample(key, defaultValue);
		try {
			if (lines.containsKey(key)) {
				int value = Integer.valueOf(lines.get(key));
				ignoredEntries.remove(key);  // Used this line.
				log.debug("%s: %s -> %d", name, key, value);
				if (value != defaultValue) {
					nonDefaultParameters.put(key, lines.get(key));
				}
				return value;
			}
		} catch (Exception e) {
			log.exception("Error reading key: " + key + " using default", e);
		}
		return defaultValue;
	}
	
	private double getAsDouble(String key, double defaultValue) {
		appendExample(key, defaultValue);
		try {
			if (lines.containsKey(key)) {
				double value = Double.valueOf(lines.get(key));
				ignoredEntries.remove(key);  // Used this line.
				log.debug("%s: %s -> %f", name, key, value);
				if (value != defaultValue) {
					nonDefaultParameters.put(key, lines.get(key));
				}
				return value;
			}
		} catch (Exception e) {
			log.exception("Error reading key: " + key + " using default", e);
		}
		return defaultValue;
	}
	
	private boolean getAsBoolean(String key, boolean defaultValue) {
		appendExample(key, defaultValue);
		try {
			if (lines.containsKey(key)) {
				boolean value = Boolean.valueOf(lines.get(key));
				ignoredEntries.remove(key);  // Used this line.
				log.debug("%s: %s -> %s", name, key, value);
				if (value != defaultValue) {
					nonDefaultParameters.put(key, lines.get(key));
				}
				return value;
			}
		} catch (Exception e) {
			log.exception("Error reading key: " + key + " using default", e);
		}
		return defaultValue;
	}
	
	private String getAsString(String key, String defaultValue) {
		appendExample(key, "\"" + defaultValue + "\"");
		try {
			if (lines.containsKey(key)) {
				// Get the value between the quotes.
				String[] parts = lines.get(key).split("\"", -1);
				if (parts.length < 3) {
					log.error("Bad string value for %s, needs to be in double quotes, not: %s", key, lines.get(key));
					return defaultValue;
				}
				String value = parts[1];
				ignoredEntries.remove(key);  // Used this line.
				log.debug("%s: %s -> %s", name, key, value);
				if (!value.equals(defaultValue)) {
					nonDefaultParameters.put(key, lines.get(key));
				}
				return value;
			}
		} catch (Exception e) {
			log.exception("Error reading key: " + key + " using default", e);
		}
		return defaultValue;
	}

	private int[] getAsIntArray(String key, int[] defaultValue) {
		// Joining primitive arrays seems to be painful under Java.
		appendExample(key, joinIntArray(defaultValue));
		try {
			if (lines.containsKey(key)) {
				String value = lines.get(key);
				int[] values;
				if (value.equals("")) {
					// No values.
					values = new int[0];
				} else {
					// One or more values.
					String[] parts = value.split("\\s*,\\s*");
					values = new int[parts.length];
					for (int i = 0; i < parts.length; i++) {
						values[i] = Integer.valueOf(parts[i]);
					}
				}
				ignoredEntries.remove(key);  // Used this line.
				log.debug("%s: %s -> %s", name, key, joinIntArray(values));
				if (!java.util.Arrays.equals(values, defaultValue)) {
					nonDefaultParameters.put(key, lines.get(key));
				}
				return values;
			}
		} catch (Exception e) {
			log.exception("Error reading key: " + key + " using default", e);
		}
		return defaultValue;
	}

	private String joinIntArray(int[] values) {
		return Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","));
	}
}