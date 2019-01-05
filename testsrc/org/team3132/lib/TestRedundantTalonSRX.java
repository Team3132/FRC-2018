package org.team3132.lib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.strongback.components.TalonSRX;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockTalonSRX;
import org.team3132.interfaces.Log;
import org.team3132.mock.MockLog;

import com.ctre.phoenix.motorcontrol.ControlMode;

public class TestRedundantTalonSRX {
	private Log log = new MockLog(true);
	private ArrayList<MockTalonSRX> potentialLeaders;
	private ArrayList<MockTalonSRX> followers;
	private ArrayList<MockTalonSRX> allTalons;
	private MockClock clock;
	private RedundantTalonSRX redundant;
	
	@Before
	public void setUp() {
		potentialLeaders = createMockTalons(new int[]{1,2,3});
		followers = createMockTalons(new int[]{4, 5});
		allTalons = new ArrayList<>();
		allTalons.addAll(potentialLeaders);
		allTalons.addAll(followers);
		clock = Mock.clock();
		redundant = new RedundantTalonSRX(toTalonSRX(potentialLeaders), toTalonSRX(followers), clock, log);
		RedundantTalonSRX.clearFailures();
	}
	
	private ArrayList<MockTalonSRX> createMockTalons(int[] ids) {
		ArrayList<MockTalonSRX> talons = new ArrayList<>();
		for (int id : ids) {
			talons.add(Mock.TalonSRXs.talonSRX(id));
		}
		return talons;
	}
	
	private static ArrayList<TalonSRX> toTalonSRX(ArrayList<MockTalonSRX> talons) {
		ArrayList<TalonSRX> result = new ArrayList<>();
		talons.forEach((talon) -> result.add(talon));
		return result;
	}

	private MockTalonSRX getTalonByID(int id) throws Exception {
		for (MockTalonSRX talon : allTalons) {
			if (talon.getDeviceID() == id) return talon;
		}
		throw new Exception(String.format("Talon %d not found", id));
	}
	
	private void setCurrentForAll(double current) {
		allTalons.forEach((talon) -> talon.setOutputCurrent(current));
	}
	
	private void setCurrentForID(int id, double current) throws Exception {
		getTalonByID(id).setOutputCurrent(current);
	}
	
	private void setVelocityForAll(double velocity) {
		allTalons.forEach((talon) -> talon.setSelectedSensorVelocity(velocity));
	}
	
	private void setVelocityForID(int id, double velocity) throws Exception {
		getTalonByID(id).setSelectedSensorVelocity(velocity);
	}
	
	private void assertLeader(int leaderID, ControlMode expectedMode, double demand) {
		boolean leaderFound = false;
		for (MockTalonSRX talon : allTalons) {
			if (talon.getDeviceID() == leaderID) {
				assertFalse(leaderFound);
				leaderFound = true;
				assertThat(talon.getLastControlMode(), is(equalTo(expectedMode)));
				assertThat(talon.getLastDemand(), is(equalTo(demand)));
				continue;
			}
			// Check to see if it's listed as a bad talon.
			for (TalonSRX bad : RedundantTalonSRX.getBadTalons()) {
				if (talon.getDeviceID() == bad.getDeviceID()) {
					// It's bad, expect it to be disabled.
					assertThat(talon.getLastControlMode(), is(equalTo(ControlMode.Disabled)));
					continue;
				} else {
					// Not bad, not the leader, check that it's following the leader.
					assertThat(talon.getLastControlMode(), is(equalTo(ControlMode.Follower)));
					assertThat(talon.getLastDemand(), is(equalTo((double)leaderID)));
				}
			}
		}
		assertThat(leaderFound, is(equalTo(true)));
	}
	
	/**
	 * Normal cases, no failures or leadership switches.
	 */
	@Test
	public void normalRunning() {
		assertThat(RedundantTalonSRX.getBadTalons().size(), is(equalTo(0)));
		assertThat(RedundantTalonSRX.getBadEncoders().size(), is(equalTo(0)));
		redundant.set(ControlMode.PercentOutput, 0.5);
		// First potential leader should be the leader.
		assertLeader(1, ControlMode.PercentOutput, 0.5);
		// Update the current for all.
		setCurrentForAll(30);
		setVelocityForAll(1000);
		// poll the encoders.
		redundant.execute(clock.currentTimeInMillis());
		clock.incrementBySeconds((long)RedundantTalonSRX.kSpeedOutlierDisableTimeSec + 1);
		redundant.execute(clock.currentTimeInMillis());
		redundant.execute(clock.currentTimeInMillis());
		assertLeader(1, ControlMode.PercentOutput, 0.5);
		// All encoders are consistent, same current, so there shouldn"t be any bad encoders/talons.
		assertThat(RedundantTalonSRX.getBadTalons().size(), is(equalTo(0)));
		assertThat(RedundantTalonSRX.getBadEncoders().size(), is(equalTo(0)));
	}

	/**
	 * Check that on a bad talon that leadership fails over.
	 * @throws Exception 
	 */
	@Test
	public void failingTalon() throws Exception {
		assertThat(RedundantTalonSRX.getBadTalons().size(), is(equalTo(0)));
		assertThat(RedundantTalonSRX.getBadEncoders().size(), is(equalTo(0)));
		redundant.set(ControlMode.PercentOutput, 0.5);
		// First potential leader should be the leader.
		assertLeader(1, ControlMode.PercentOutput, 0.5);
		// Update the current for all.
		setCurrentForAll(30);
		setCurrentForID(1, 40);
		setVelocityForAll(1000);
		// poll the encoders.
		redundant.execute(clock.currentTimeInMillis());
		clock.incrementBySeconds((long)RedundantTalonSRX.kCurrentOutlierDisableTimeSec + 1);
		redundant.execute(clock.currentTimeInMillis());
		redundant.execute(clock.currentTimeInMillis());
		assertLeader(2, ControlMode.PercentOutput, 0.5);
		// All encoders are consistent, same current, so there shouldn"t be any bad encoders/talons.
		assertThat(RedundantTalonSRX.getBadTalons().size(), is(equalTo(1)));
		assertThat(RedundantTalonSRX.getBadEncoders().size(), is(equalTo(0)));
	}

	/**
	 * Check that on a bad encoder that leadership fails over.
	 * @throws Exception 
	 */
	@Test
	public void failingEncoder() throws Exception {
		assertThat(RedundantTalonSRX.getBadTalons().size(), is(equalTo(0)));
		assertThat(RedundantTalonSRX.getBadEncoders().size(), is(equalTo(0)));
		redundant.set(ControlMode.PercentOutput, 0.5);
		// First potential leader should be the leader.
		assertLeader(1, ControlMode.PercentOutput, 0.5);
		// Update the current for all.
		setCurrentForAll(30);
		setVelocityForAll(1000);
		setVelocityForID(1, 500);
		// poll the encoders.
		redundant.execute(clock.currentTimeInMillis());
		clock.incrementBySeconds((long)RedundantTalonSRX.kCurrentOutlierDisableTimeSec + 1);
		redundant.execute(clock.currentTimeInMillis());
		redundant.execute(clock.currentTimeInMillis());
		assertLeader(2, ControlMode.PercentOutput, 0.5);
		// All encoders are consistent, same current, so there shouldn"t be any bad encoders/talons.
		assertThat(RedundantTalonSRX.getBadTalons().size(), is(equalTo(0)));
		assertThat(RedundantTalonSRX.getBadEncoders().size(), is(equalTo(1)));
	}
}
