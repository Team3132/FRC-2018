package org.team3132.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import org.junit.Before;
import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockSolenoid;
import org.strongback.mock.MockTalonSRX;
import org.team3132.Constants;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.LiftInterface;
import org.team3132.interfaces.Log;
import org.team3132.mock.MockDashboard;
import org.team3132.mock.MockLog;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test the core Lift functionality.
 * End goal is to be able to make changes to the Lift without needing a robot and have confidence in them 
 * because all the unit tests pass.
 */
public class TestLift {

	protected DashboardInterface dashboard = new MockDashboard();
	protected Log log = new MockLog(true);

	protected MockTalonSRX motor;
	protected MockSolenoid shifter;
	protected MockClock clock;
	protected LiftInterface lift;

	/**
	 * Setup fields used by this test.
	 */
	@Before
	public void setUp() {
    	motor = Mock.TalonSRXs.talonSRX(0);
    	motor.setSelectedSensorPosition(0, 0, 10);
    	shifter = Mock.Solenoids.singleSolenoid(0);
    	clock = Mock.clock(); // Reset the clock.
    	lift = new Lift(motor, shifter, clock, dashboard, log);
    	lift.enable();
    	motor.setFwdLimitSwitchClosed(false);
    	motor.setRevLimitSwitchClosed(false);
	}
	
	/**
	 * Increment the time and poke the finite state machine.
	 */
	protected void runExecute() {
    	clock.incrementByMilliseconds(20);
    	lift.execute(0);	
	}
	
	protected boolean inHighGear() {
		return shifter.isExtended();
	}

	protected boolean inLowGear() {
		return !inHighGear();
	}
    
    /**
	 * Check that isSafeToShift() won't allow shifting when it's climbing and won't
	 * let users shift into low gear when too low to start climbing.
	 */
    @Test
    public void climbingDoesntShiftToHighGear() {
    	System.out.println("TestLift::climbingDoesntShiftToHighGear()");

    	// Set a low current draw.
    	motor.setOutputCurrent(0.1);
        // We're in high gear and down low, so not 'safe' to shift to low gear.
    	assertThat("In high gear", inHighGear(), is(equalTo(true)));
    	lift.setHeight(0);
    	assertThat(lift.isSafeToShift(), is(equalTo(false)));
    	
    	// Tell it to go to above the lift shifting threshold.
    	final double aboveShiftingThreshold = Constants.LIFT_SHIFTING_THRESHOLD_HEIGHT + 1; 
    	lift.setHeight(aboveShiftingThreshold);
    	runExecute();
    	assertThat("Motor should have a position", motor.getLastDemand(), is(equalTo(aboveShiftingThreshold + Constants.LIFT_BOTTOM_HEIGHT)));
    	assertThat(motor.getLastControlMode(), is(equalTo(ControlMode.MotionMagic)));
    	assertThat("In high gear", inHighGear(), is(equalTo(true)));
    	assertThat(lift.isSafeToShift(), is(equalTo(false)));  // Not above threshold yet.
   
    	// Update the lift position to be the target height.
    	motor.setSelectedSensorPosition(aboveShiftingThreshold + Constants.LIFT_BOTTOM_HEIGHT, 0, 0);
    	runExecute();
    	assertThat("In high gear", inHighGear(), is(equalTo(true)));
    	// Now safe to shift.
    	assertThat(lift.isSafeToShift(), is(equalTo(true)));
    	// Ask it to switch to low gear as if it was going to climb.
    	lift.setLowGear();
    	runExecute();
    	// The solenoid will take time to shift, so fast forward time.
      	clock.incrementByMilliseconds(5000);
    	runExecute();
    	assertThat("In low gear", inLowGear(), is(equalTo(true)));
    	// Can switch back and forward at this height.
    	assertThat(lift.isSafeToShift(), is(equalTo(true)));
    	
    	// Move below shifting threshold, while claiming that the motor is
    	// drawing a lot of current.
    	motor.setOutputCurrent(15);
    	final double belowShiftingThreshold = 10;
    	assertThat(belowShiftingThreshold, is(lessThan(Constants.LIFT_SHIFTING_THRESHOLD_HEIGHT))); 
    	lift.setHeight(belowShiftingThreshold);
    	runExecute();
    	assertThat("In low gear", inLowGear(), is(equalTo(true)));

    	// Update the lift position to be the target height.
    	motor.setSelectedSensorPosition(belowShiftingThreshold, 0, 0);
    	runExecute();
    	assertThat("In low gear", inLowGear(), is(equalTo(true)));

    	// Check that isSafeToShift() (which is called by the business rules
    	// thinks it's unsafe to shift.
    	assertThat(lift.isSafeToShift(), is(equalTo(false)));
  }
}
