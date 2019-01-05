package org.team3132.lib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.team3132.Constants;
import org.team3132.mock.MockLog;

public class RobotConfigurationTest {
	MockLog log = new MockLog(true);

	// Ensure all subsystems are enabled to their default values if the file is empty.
	@Test
	public void testMissingFile() throws IOException {
		Path tempDir = Files.createTempDirectory("robot");
		Path path = Paths.get(tempDir.toString(), "robot.config");
		RobotConfiguration config = new RobotConfiguration(path.toString(), 3132, log);
		
		assertThat(config.drivebaseIsPresent, is(equalTo(true)));
		assertThat(config.liftIsPresent, is(equalTo(true)));
		assertThat(config.drivebaseCanIdsLeftWithEncoders, is(equalTo(Constants.DRIVE_LEFT_TALON_WITH_ENCODERS_CAN_ID_LIST)));
	}
	
	// Ensure all subsystems are enabled to their default values if the file is empty.
	@Test
	public void testEmptyFile() throws IOException {
		File file = File.createTempFile("robot", ".config");
		String exampleFile = file.toString() + ".example";
		RobotConfiguration config = new RobotConfiguration(file.toString(), 3132, log);
		assertThat(file.exists(), is(equalTo(true)));
		
		assertThat(config.drivebaseIsPresent, is(equalTo(true)));
		assertThat(config.liftIsPresent, is(equalTo(true)));
		assertThat(config.drivebaseCanIdsLeftWithEncoders, is(equalTo(Constants.DRIVE_LEFT_TALON_WITH_ENCODERS_CAN_ID_LIST)));
		assertThat(exampleFile.toString().length(), greaterThan(20)); // make sure the example file isn't empty after first run of update
		
		// Load in the example file to ensure it's valid. Cook one of the lines
		// to ensure that it's actually read and not the default.
		Path path = Paths.get(exampleFile);
		String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		Files.write(path, content.replaceAll("3132", "9132").getBytes(StandardCharsets.UTF_8));
		config = new RobotConfiguration(exampleFile, 3132, log);
		assertThat(config.robotName, is(equalTo("9132")));
		assertThat(config.drivebaseIsPresent, is(equalTo(true)));
		assertThat(config.liftIsPresent, is(equalTo(true)));
		assertThat(config.drivebaseCanIdsLeftWithEncoders, is(equalTo(Constants.DRIVE_LEFT_TALON_WITH_ENCODERS_CAN_ID_LIST)));
	}

	@Test
	public void testGetValueType() throws IOException {
		File file = File.createTempFile("robot", ".config");
		assertThat(file.exists(), is(equalTo(true)));

		StringBuilder str = new StringBuilder();
		str.append("robot/name=\"firstValue\"\n");  // First value of a dup is ignored.
		str.append("robot/name=\"unitTest\"\n");
		str.append("  drivebase/present=false\n");  // Leading spaces.
		str.append("pcm/canID=210\n");
		str.append("drivebase/left/canIDs/withEncoders=7, 9,15\n");
		str.append("drivebase/left/canIDs/withoutEncoders=\n");
		Files.write(file.toPath(), str.toString().getBytes(StandardCharsets.UTF_8));

		RobotConfiguration config = new RobotConfiguration(file.toString(), 31332, log);

		assertThat(config.robotName, is(equalTo("unitTest")));
		assertThat(config.drivebaseIsPresent, is(equalTo(false)));
		assertThat(config.pcmCanId, is(equalTo(210)));
		assertThat(config.drivebaseCanIdsLeftWithEncoders, is(equalTo(new int[] { 7, 9, 15 })));
		assertThat(config.drivebaseCanIdsLeftWithoutEncoders, is(equalTo(new int[] {})));
	}
}
