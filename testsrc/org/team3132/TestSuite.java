package org.team3132;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.strongback.command.TestCommandGroup;
import org.team3132.controller.TestController;
import org.team3132.lib.RobotConfigurationTest;
import org.team3132.lib.TestLogFileWriter;
import org.team3132.lib.TestMovementSimulator;
import org.team3132.lib.TestRedundantTalonSRX;
import org.team3132.subsystems.TestDrivebase;
import org.team3132.subsystems.TestLift;
import org.team3132.subsystems.TestLocation;
import org.team3132.subsystems.TestVision;

@RunWith(Suite.class)


@Suite.SuiteClasses({
		TestCommandGroup.class,
		TestController.class,
        RobotConfigurationTest.class,
        TestLogFileWriter.class,
        TestMovementSimulator.class,
        TestRedundantTalonSRX.class,
        TestDrivebase.class,
        TestLift.class,
        TestLocation.class,
        TestVision.class,
})

/**
 * Used to run all our tests via junit commandline/ant
 * See https://github.com/junit-team/junit4/wiki/aggregating-tests-in-suites
 */
public class TestSuite {
}