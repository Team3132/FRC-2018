package org.team3132.controller;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockPneumaticsModule;
import org.team3132.Constants;
import org.team3132.controller.Controller;
import org.team3132.controller.Sequence;
import org.team3132.controller.Sequences;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.IntakeInterface.IntakeConfiguration;
import org.team3132.interfaces.LiftInterface;
import org.team3132.interfaces.LiftInterface.LiftPosition;
import org.team3132.interfaces.Log;
import org.team3132.mock.MockDashboard;
import org.team3132.mock.MockDrivebase;
import org.team3132.mock.MockEndgame;
import org.team3132.mock.MockLEDController;
import org.team3132.mock.MockLog;
import org.team3132.simulator.IntakeSimulator;
import org.team3132.simulator.LiftSimulator;
import org.team3132.simulator.OuttakeSimulator;
import org.team3132.subsystems.Subsystems;

/**
 * Test cases for the Controller and the Sequences
 * 
 * Mocks out almost everything so that no hardware is needed.
 */
public class TestController {
	// Use 10 ms steps between executions.
	private final long ktestStepMs = 10;
	private final long kRandomSeed = 123456;
	private final double kMaxWaitTimeSeconds = 4;
	protected DashboardInterface dashboard = new MockDashboard();
	protected Log log = new MockLog(true);
	private MockClock clock;
	private Subsystems subsystems;
	// Store direct access to the simulators so the simulator-only
	// methods can be called.
	private IntakeSimulator intake;
	private LiftSimulator lift;
	private TestHelper test;
	// The bit that is being tested under test.
	private Controller exec;
		
	/**
	 * Setup fields used by this test.
	 */
	@Before
	public void setUp() {
		System.out.println("\n******************************");
		clock = new MockClock();
		subsystems = new Subsystems(new MockDashboard(), null, clock, log);

		subsystems.intake = intake = new IntakeSimulator();
		subsystems.outtake = new OuttakeSimulator();
		subsystems.lift = lift = new LiftSimulator();
		subsystems.endgame = new MockEndgame();  // Change to simulator.
		subsystems.compressor = new MockPneumaticsModule(); 
		subsystems.drivebase = new MockDrivebase(log);
		subsystems.led = new MockLEDController();
		subsystems.leftDriveDistance = () -> 0;
		subsystems.rightDriveDistance = () -> 0;
		
		exec = new Controller(subsystems, Constants.LIFT_SETPOINTS);

		test = new TestHelper(() -> {
			clock.incrementByMilliseconds(ktestStepMs);
			//long now = clock.currentTimeInMillis();
			//System.out.printf("==== Cycle starting at time %.03fms ====\n", now/1000.);
			subsystems.intake.execute(clock.currentTimeInMillis());
			subsystems.outtake.execute(clock.currentTimeInMillis());
			subsystems.lift.execute(clock.currentTimeInMillis());
			subsystems.endgame.execute(clock.currentTimeInMillis());
			return clock.currentTime();
		},() -> {
			System.out.println(subsystems.intake.toString());
			System.out.println(subsystems.outtake.toString());
			System.out.println(subsystems.lift.toString());
			System.out.println(subsystems.endgame.toString());
		});
		// Add safety functions to be called every step off the way.
		test.registerSafetyFunc(() -> checkIntakeVsLift());
	}
	
	/**
	 * Example test.
	 * 
	 * Use this as a template when designing new tests.
	 */
	/*
	@Test
	public void testExampleForCopying() {
		// Update the println statement with your test name.
		System.out.println("testExampleForCopying");
		
		// Setup initial subsystem state. Lift starts at the bottom and the intake stowed. 
		// Set what  you need for the test here. Once the subsystems have been set up,
		// then the test will move on to the next thenSet(), thenAssert() or thenWait()
		// statement.
		test.thenAssert(intakeStowed(), outtakeOpen(true), intakeMotorPower(0), liftHeight(LiftPosition.INTAKE_POSITION));

		// Run the intaking sequence.
		test.thenSet(sequence(Sequences.getStartIntakingSequence()));
		
		// Then assert that the robot subsystems have eventually been put into the expected states.
		// Having them in one .thenAssert() means that they will all have to be true at once.
		// In this case the intake should be in the narrow configuration, the outtake should be open,
		// the intake motor should have full power forward and the lift should be in the intake position. 
		test.thenAssert(intakeNarrow(), outtakeOpen(true), intakeMotorPower(1), liftHeight(LiftPosition.INTAKE_POSITION));
		
		// The test can then be told to do nothing for some number of seconds.
		test.thenWait(0.5);
		
		// Then you can tell the robot that a cube was detected in the outtake, which
		// may make the sequence move on to the next state.
		// NB, this sensor has been removed, so hasCube() is no longer an option.
		test.thenSet(hasCube(true));
		
		// And then make the test check that the subsystems are updated to be in the correct state.
		test.thenAssert(intakeStowed(), outtakeOpen(false), intakeMotorPower(0), liftHeight(LiftPosition.INTAKE_POSITION));
		
		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		// This line executes the steps set up above. Note adding println statements
		// will print out the statements when the test is setup, not as it moves through
		// the states.
		assert(test.run());
	}
    */
	
	/**
	 * Test the reset sequence. This should turn everything off, drop the lift to
	 * the bottom and stow the intake. Used on some button releases to stop doing
	 * something, eg scoring.
	 */
	@Test
	public void testReset() {
		System.out.println("testReset");
		// Setup initial state.
		test.thenSet(intakeNarrow(), outtakeOpen(true), intakeMotorPower(1), liftHeight(LiftInterface.LiftPosition.SCALE_POSITION));
		
		test.thenSet(sequence(Sequences.getResetSequence()));
				
		// Outtake should close, intake stow and intake motor turn off.
		test.thenAssert(intakeStowed(), outtakeOpen(false), intakeMotorPower(0), liftHeight(LiftInterface.LiftPosition.INTAKE_POSITION));
		
		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		assert(test.run());
	}
	
	/**
	 * Test the start intaking sequence.
	 * Normally started by the operator.
	 */
	@Test
	public void testStartIntaking() {
		System.out.println("testStartIntaking");
		// Setup initial state to be completely different to the intaking sequence.
		test.thenSet(intakeWide(), outtakeOpen(false), intakeMotorPower(0), liftHeight(LiftInterface.LiftPosition.SCALE_POSITION));

		// Begin the intaking sequence
		test.thenSet(sequence(Sequences.getStartIntakingSequence()));
		
		// Expect the Controller to match all of these at once.
		test.thenAssert(intakeNarrow(), outtakeOpen(true), intakeMotorPower(1), liftHeight(LiftPosition.INTAKE_POSITION));
		
		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		assert(test.run());
	}
	
	/**
	 * Test the stop intaking sequence.
	 * Normally started by the operator once a cube has been ingested.
	 */
	@Test
	public void testStopIntaking() {
		System.out.println("testStopIntaking");
		// Setup initial state to be like it was intaking.
		test.thenSet(intakeNarrow(), outtakeOpen(true), intakeMotorPower(1), liftHeight(LiftPosition.INTAKE_POSITION));

		// Begin the intaking sequence
		test.thenSet(sequence(Sequences.getStopIntakingSequence()));
		
		// Outtake should close, intake stow and intake motor turn off.
		test.thenAssert(intakeStowed(), outtakeOpen(false), intakeMotorPower(0), liftHeight(LiftPosition.INTAKE_POSITION));
		
		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		assert(test.run());
	}
	
	/**
	 * Test that positioning the lift at the scale height works.
	 */
	@Test
	public void testPositioningLiftScale() {
		System.out.println("testPositioningLiftScale");
		// Setup initial state.
		test.thenSet(intakeWide(), liftHeight(LiftPosition.INTAKE_POSITION));
		
		// Trigger BR to start moving lift.
		test.thenSet(sequence(Sequences.getPositioningLiftScaleSequence()));
		
		// Expect the lift at the top and the rest to be stowed etc.
		test.thenAssert(liftHeight(LiftPosition.SCALE_POSITION), intakeStowed(), outtakeOpen(false), intakeMotorPower(0));
		
		assert(test.run());
	}
	
	/**
	 * Test that positioning the lift at the switch height works.
	 */
	@Test
	public void testPositioningLiftSwitch() {
		System.out.println("testPositioningLiftSwitch");
		// Setup initial state.
		test.thenSet(intakeWide(), liftHeight(LiftPosition.INTAKE_POSITION));
		
		// Trigger BR to start moving lift.
		test.thenSet(sequence(Sequences.getPositioningLiftSwitchSequence()));
		
		// Expect the lift at the top and the rest to be stowed etc.
		test.thenAssert(liftHeight(LiftPosition.SWITCH_POSITION), intakeStowed(), outtakeOpen(false), intakeMotorPower(0));
		
		assert(test.run());
	}
	
	/**
	 * Test ready climb brings the lift up high ready to attach to the scale.
	 */
	@Test
	public void testReadyClimb() {
		System.out.println("testReadyClimb");
		// Setup initial state.
		test.thenSet(liftHeight(LiftPosition.SWITCH_POSITION), intakeWide(), outtakeOpen(true));
		
		// Trigger BR to move to ready climb state
		test.thenSet(sequence(Sequences.getReadyClimbSequence()));
		
		// Expect the lift at the top and the rest to be stowed etc.
		test.thenAssert(liftHeight(LiftPosition.CLIMB_POSITION), intakeStowed(), outtakeOpen(false), intakeMotorPower(0));
		
		assert(test.run());
	}

	/**
	 * Test climbing pulls the robot up by lowering the lift in low gear.
	 * The operator should have put the lift to the very top with ready_climb.
	 * The BR should take care of stowing everything but not change the initial
	 * height, assuming that the lift is already at the correct height.
	 */
	@Test
	public void testClimbing() {
		System.out.println("testClimbing");
		// Setup initial state.
		test.thenSet(liftHeight(LiftPosition.CLIMB_POSITION), intakeNarrow(), outtakeOpen(false), lowGear(false));

		// Single call of the climbing sequence should put it into
		// low gear and start moving down.
		test.thenSet(sequence(Sequences.getClimbingSequence()));
		
		// Expect the lift at the top briefly and put into low gear ready for the climb.
		test.thenAssert(liftHeight(LiftPosition.CLIMB_POSITION), intakeStowed(), outtakeOpen(false), intakeMotorPower(0), lowGear(true));

		// Start the climbing sequence.
		test.thenSet(sequence(Sequences.getClimbingSequence()));

		// Expect the lift at the bottom and in low gear
		test.thenAssert(liftHeight(LiftPosition.CLIMB_STOP_POSITION), intakeStowed(), outtakeOpen(false), intakeMotorPower(0), lowGear(true));
		
		assert(test.run());
	}

	/**
	 * Test it won't shift to high gear while the lift isn't at the top.
	 * Pretend to do a climb, then go back to the top.
	 */
	@Test
	public void testNoUnsafeLiftShift() {
		System.out.println("testNoUnsafeLiftShift");
		// Setup initial state.
		test.thenSet(liftHeight(LiftPosition.CLIMB_POSITION), intakeNarrow(), outtakeOpen(false), lowGear(false));

		// Single call of the climbing sequence should put it into
		// low gear and start moving down.
		test.thenSet(sequence(Sequences.getClimbingSequence()));
		
		// Expect the lift at the top and put into low gear ready for the climb.
		test.thenAssert(liftHeight(LiftPosition.CLIMB_POSITION), intakeStowed(), outtakeOpen(false),
				intakeMotorPower(0), lowGear(true));

		// Start climbing.
		test.thenSet(sequence(Sequences.getClimbingSequence()));

		// Expect the lift at the bottom and in low gear
		test.thenAssert(liftHeight(LiftPosition.CLIMB_STOP_POSITION), intakeStowed(), outtakeOpen(false),
				intakeMotorPower(0), lowGear(true));
	
		// The robot is now in low gear. Tell it to do a ready climb
		// and check that on the way up that it hasn't changed to high
		// gear at any point.
		test.thenSet(sequence(Sequences.getReadyClimbSequence()));
		
		// Part the way back to the top, check that it's still in low gear.
		// The height isn't important, only that it's between the end of the
		// climb and the ready climb position.
		test.thenAssert(liftHeight(LiftPosition.SWITCH_POSITION), lowGear(true));
		
		// Expect the lift at the top and the rest to be stowed etc and back in high gear.
		test.thenAssert(liftHeight(LiftPosition.CLIMB_POSITION), intakeStowed(), outtakeOpen(false), intakeMotorPower(0), lowGear(false));
		
		assert(test.run());
	}

	/**
	 */
	@Test
	public void testMicroAdjust() {
		System.out.println("testMicroAdjust");
		// Setup initial state.
		test.thenSet(liftHeight(LiftPosition.SWITCH_POSITION));

		// Micro adjust the lift up in one go from the switch position to
		// the scale position.
		final double kAdjustBy = LiftPosition.SCALE_POSITION.value - LiftPosition.SWITCH_POSITION.value;
		test.thenSet(sequence(Sequences.getMicroAdjustSequence(kAdjustBy)));

		// The lift should now be in the scale position.
		test.thenAssert(liftHeight(LiftPosition.SCALE_POSITION));
	
		assert(test.run());
	}

	/**
	 * Test the scoring sequence out the side.
	 */
	@Test
	public void testScoring() {
		System.out.println("testScoring");
		// Setup initial state with a cube and ready to score.
		test.thenSet(liftHeight(LiftPosition.SCALE_POSITION));
		
		// Run scoring left sequence.
		test.thenSet(sequence(Sequences.getScoringLeftSequence()));
		
		// Wait for the outtake to be trying to eject the cube.
		test.thenAssert(intakeStowed(), outtakeOpen(false), liftHeight(LiftPosition.SCALE_POSITION),
				outtakeMotorPower(1));
				
		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		assert(test.run());
	}

	/**
	 * Test the intake eject sequence where the cube falls out the front.
	 */
	@Test
	public void testIntakeEject() {
		System.out.println("testIntakeEject");
		// Setup initial state.
		test.thenSet(liftHeight(LiftPosition.INTAKE_POSITION), intakeStowed(), outtakeOpen(false));
		
		// Run the intake eject sequence.
		test.thenSet(sequence(Sequences.getIntakeEjectSequence()));
		
		// Wait for the outtake to be trying to eject the cube.
		test.thenAssert(intakeNarrow(), outtakeOpen(true), liftHeight(LiftPosition.INTAKE_POSITION),
				intakeMotorPower(-1), outtakeMotorPower(0));
		
		// The operator would normally wait for the cube to fall out and then release the 
		// button that triggered this state, causing the robot to reset, closing everything.
		
		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		assert(test.run());
	}

	/**
	 * Test the lift setpoints.
	 * 
	 * Setpoints are fixed points that can be jumped between.
	 */
	@Test
	public void testLiftSetpoints() {
		System.out.println("testLiftSetpoints");
		// Setup initial state, starting on a setpoint.
		test.thenSet(liftHeight(LiftPosition.INTAKE_POSITION));
		
		// Tell it to move up to the next setpoint.
		test.thenSet(sequence(Sequences.getLiftSetpointUpSequence()));
		
		// Should now be at switch height.
		test.thenAssert(liftHeight(LiftPosition.SWITCH_POSITION));
		
		// Wait for the sequence to finish before running it again.
		test.thenWait(1);

		// Tell it to move up to the next setpoint.
		test.thenSet(sequence(Sequences.getLiftSetpointUpSequence()));
		
		// Should now be at scale height.
		test.thenAssert(liftHeight(LiftPosition.CLIMB_POSITION));
		
		// Wait for the sequence to finish before running it again.
		test.thenWait(1);

		// Drop it down more than the lift tolerance and tell it to go back
		// up to the same setpoint.
		test.thenSet(sequence(Sequences.getMicroAdjustSequence(-4)));
		test.thenSet(sequence(Sequences.getLiftSetpointUpSequence()));
		
		// Should now be back at the scale height.
		test.thenAssert(liftHeight(LiftPosition.CLIMB_POSITION));
		
		// Wait for the sequence to finish before running it again.
		test.thenWait(1);

		// Move down a setpoint.
		test.thenSet(sequence(Sequences.getLiftSetpointDownSequence()));

		// Should now be at switch height.
		test.thenAssert(liftHeight(LiftPosition.SWITCH_POSITION));

		// Lift it up more than the lift tolerance and tell it to go back
		// down to the same setpoint.
		test.thenSet(sequence(Sequences.getMicroAdjustSequence(4)));
		test.thenSet(sequence(Sequences.getLiftSetpointDownSequence()));

		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		assert(test.run());
	}

	/**
	 * Test intaking and then scoring.
	 */
	@Test
	public void testIntakingAndScoring() {
		System.out.println("testIntakingAndScoring");
		// Setup initial state.
		test.thenSet(liftHeight(LiftPosition.SCALE_POSITION));
		
		// Trigger BR to start intaking
		test.thenSet(sequence(Sequences.getStartIntakingSequence()));
		
		// Check that it's setup to intake.
		test.thenAssert(intakeNarrow(), outtakeOpen(true), intakeMotorPower(1), liftHeight(LiftPosition.INTAKE_POSITION));
		
		// Wait for a cube to be sucked in. Note the cube sensors were removed.
		test.thenWait(0.5);
		
		// Now the operator releases the intaking button which should run the stop intaking sequence.
		test.thenSet(sequence(Sequences.getStopIntakingSequence()));
		
		// Outtake should close, intake stow and intake motor turn off.
		test.thenAssert(intakeStowed(), outtakeOpen(false), intakeMotorPower(0), liftHeight(LiftPosition.INTAKE_POSITION));
		
		// Move the lift to the height of the scale.
		test.thenSet(sequence(Sequences.getPositioningLiftScaleSequence()));
		
		// Expect the lift at the top and the rest to be stowed etc.
		test.thenAssert(liftHeight(LiftPosition.SCALE_POSITION), intakeStowed(), outtakeOpen(false), intakeMotorPower(0));
		
		// Then score the cube out of the right side.
		test.thenSet(sequence(Sequences.getScoringRightSequence()));
		
		// Wait for the outtake to be trying to eject the cube.
		test.thenAssert(intakeStowed(), outtakeOpen(false), liftHeight(LiftPosition.SCALE_POSITION),
				outtakeMotorPower(-1));
		
		// Wait for half a second to pretend it's taken time to push out a cube.
		test.thenWait(0.5);
		
		// Operator has spotted the cube leaving the outtake and releases the button.
		test.thenSet(sequence(Sequences.getResetSequence()));
		
		// The business rules should keep the outtake motor running briefly
		// before cleaning up.
		test.thenAssert(intakeStowed(), outtakeOpen(false), liftHeight(LiftPosition.INTAKE_POSITION),
				outtakeMotorPower(0));
				
		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		assert(test.run());
	}

	/**
	 * Pretends to be a crazy operator that keeps changing their mind.
	 * 
	 * This allows it to check that the robot is always in a safe configuration
	 * as the safety checker is checking the state of the robot every time.
	 * It sleeps for a random amount of time between desired state changes to
	 * allow the robot to get either fully into the new state, or part way.
	 * 
	 * There is no checking that the robot is actually doing anything useful
	 * here, only that it doesn't hurt itself.
	 */
	@Test
	public void testCrazyOperatorFuzzTest() {
		System.out.println("testCrazyOperatorFuzzTest");
		// Seed the random number generator so that the same
		// random numbers are generated every time.
		Random generator = new Random(kRandomSeed);
		
		// Build a large number random steps.
		for (int i=0; i<100; i++) {
			// Ask for a random desired state.
			test.thenSet(sequence(getRandomDesiredSequence(generator)));
			test.thenWait(generator.nextDouble() * kMaxWaitTimeSeconds);				
		}

		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		assert(test.run());
	}
	

	// Helpers only from this point onwards.

	private Sequence getRandomDesiredSequence(Random generator) {
		return Sequences.allSequences[generator.nextInt(Sequences.allSequences.length)];
	}

	/**
	 * Either sets the outtake to be open or closed, OR asserts it's open or
	 * closed depending if it's in a thenSet() or a thenAssert().
	 * @param open which is the desired state.
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter outtakeOpen(boolean open) {
		return new StateSetterOrAsserter() {
			@Override
			public String name() {
				return String.format("OuttakeOpen(%s)", open);
			}
			@Override
			public void setState() {
				if (open) {
					subsystems.outtake.openOuttake();
				} else {
					subsystems.outtake.closeOuttake();
				}
			}
			@Override
			public void assertState() throws AssertionError {
				boolean isOpen = subsystems.outtake.isClosed();
				if (isOpen == open) {
					if (open) {
						throw new AssertionError("Expected outtake to be open, but it's closed.");
					} else {
						throw new AssertionError("Expected outtake to be closed, but it's open");
					}
				}
			}
		};
	}

	/**
	 * Either sets the outtake motor power, OR asserts the power the motor has
	 * been set to, depending if it's in a thenSet() or a thenAssert().
	 * @param power to set/expect to/from the motor.
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter outtakeMotorPower(double power) {
		return new StateSetterOrAsserter() {
			@Override
			public String name() {
				return String.format("OuttakeMotorPower(%.1f)", power);
			}
			@Override
			public void setState() {
				subsystems.outtake.setOuttakeMotorOutput(power);
			}
			@Override
			public void assertState() throws AssertionError {
				if (Math.abs(subsystems.outtake.getOuttakeMotorOutput() - power) > 0.1) {
					throw new AssertionError("Expected outtake motor to have power " + power + " but it is "
							+ subsystems.outtake.getOuttakeMotorOutput());
				}
			}
		};
	}

	/**
	 * Helper function. Use one of the ones below.
	 * Either sets the intake to a configuration, OR asserts it's in the
	 * supplied configuration in a thenSet() or a thenAssert().
	 * @param config which is the desired configuration.
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter intakeConfig(IntakeConfiguration config) {
		return new StateSetterOrAsserter() {
			@Override
			public String name() {
				return String.format("IntakeConfig(%s)", config.name());
			}
			@Override
			public void setState() {
				// Override the intake positions.
				intake.setIntakePositionsActual(config);
			}
			@Override
			public void assertState() throws AssertionError {
				if (subsystems.intake.getConfiguration() != config) {
					throw new AssertionError("Expected to intake to be in " + config.name()
							+ " configuration but is in " + subsystems.intake.getConfiguration().name()
							+ "\n" + subsystems.intake.toString());
				}
			}
		};
	}

	/**
	 * Either set intake to move to the stowed state or assert that it's in the
	 * stowed state depending if it's in a thenSet() or a thenAssert().
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter intakeStowed() {
		return intakeConfig(IntakeConfiguration.STOWED);
	}

	/**
	 * Either set intake to move to the wide state or assert that it's in the
	 * wide state depending if it's in a thenSet() or a thenAssert().
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	@SuppressWarnings("unused")
	private StateSetterOrAsserter intakeWide() {
		return intakeConfig(IntakeConfiguration.WIDE);
	}

	/**
	 * Either set intake to move to the narrow state or assert that it's in the
	 * narrow state depending if it's in a thenSet() or a thenAssert().
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter intakeNarrow() {
		return intakeConfig(IntakeConfiguration.NARROW);
	}

	/**
	 * Either sets the intakes motor power, OR asserts the power the motor has
	 * been set to, depending if it's in a thenSet() or a thenAssert().
	 * @param power to set/expect to/from the motor.
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter intakeMotorPower(double power) {
		return new StateSetterOrAsserter() {
			@Override
			public String name() {
				return String.format("IntakeMotorPower(%.1f)", power);
			}
			@Override
			public void setState() {
				subsystems.intake.setIntakeMotorOutput(power);
			}
			@Override
			public void assertState() throws AssertionError {
				if (Math.abs(subsystems.intake.getIntakeMotorOutput() - power) > 0.1) {
					throw new AssertionError("Expected intake motor to have power " + power + " but it is "
							+ subsystems.intake.getIntakeMotorOutput());
				}
			}
		};
	}

	/**
	 * Either sets the lift height, OR asserts the lift height has
	 * been set to, depending if it's in a thenSet() or a thenAssert().
	 * @param pos the position of the lift.
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter liftHeight(LiftInterface.LiftPosition pos) {
		return new StateSetterOrAsserter() {
			@Override
			public String name() {
				return String.format("LiftHeight(%s)", pos.toString());
			}
			@Override
			public void setState() {
				// Use the override.
				lift.setLiftHeightActual(pos.value);
			}
			@Override
			public void assertState() throws AssertionError {
				if (Math.abs(subsystems.lift.getHeight() - pos.value) > Constants.LIFT_DEFAULT_TOLERANCE) {
					//System.out.println("Expected lift to be at position " + pos.toString() + "(" + pos.value
					//		+ ") but it is " + subsystems.lift.getLiftHeight());
					throw new AssertionError("Expected lift to be at position " + pos.toString() + "(" + pos.value
							+ ") but it is " + subsystems.lift.getHeight());
				}
			}
		};
	}

	/**
	 * Either sets the gear, OR asserts the lift is in the correct gear,
	 * depending if it's in a thenSet() or a thenAssert().
	 * @param enabled.
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter lowGear(boolean enabled) {
		return new StateSetterOrAsserter() {
			@Override
			public String name() {
				return String.format("LowGear(%s)", enabled);
			}
			@Override
			public void setState() {
				// Use the override.
				if (enabled) {
					lift.setHighGear();
				} else {
					lift.setLowGear();
				}
			}
			@Override
			public void assertState() throws AssertionError {
				if (subsystems.lift.isInLowGear() != enabled) {
					if (enabled) {
						throw new AssertionError("Expected lift to be in low gear, but it is in high gear");
					} else {
						throw new AssertionError("Expected lift to be in high gear, but it is in low gear");
					}
				}
			}
		};
	}

	/**
	 * Tells the Controller to run the desired sequence. Only makes sense in a
	 * thenSet(), not a thenAssert().
	 * @param sequence the sequence to execute.
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter sequence(Sequence sequence) {
		return new StateSetterOrAsserter() {
			@Override
			public String name() {
				return String.format("Sequence(%s)", sequence.getName());
			}
			@Override
			public void setState() {
				exec.doSequence(sequence);
			}
			@Override
			public void assertState() throws AssertionError {
				throw new AssertionError("Invalid usage of sequence() in thenAssert()");
			}
		};
	}

	/**
	 * Safety function.
	 * Check that the lift and the intake aren't colliding.
	 * If the lift is low, then the intake needs to be either in
	 * the stowed or the wide configuration. Anything else will
	 * mean that any cube will catch on the intake as the cube is
	 * lifted.
	 */
	private void checkIntakeVsLift() throws AssertionError {
		if (subsystems.lift.isAboveIntakeThreshold()) return;
		if (subsystems.lift.getDesiredHeight() < Constants.LIFT_INTAKE_HEIGHT + Constants.LIFT_DEFAULT_TOLERANCE) return;
		// Lift is below intake threshold.
		IntakeConfiguration config = subsystems.intake.getConfiguration();
		if (config == IntakeConfiguration.STOWED) return;
		if (config == IntakeConfiguration.WIDE) return;
		// Intake isn't in stowed or wide, this is a problem.
		throw new AssertionError("Lift (" + subsystems.lift.getHeight() + ") is below intake threshold (" + subsystems.lift.getHeight() + ") and intake is in configuration " + config);
	}
}
