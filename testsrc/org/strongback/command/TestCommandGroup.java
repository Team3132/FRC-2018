package org.strongback.command;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;

/**
 * Test to find out if strongback sequential and parallel command groups work
 * as written on the tin.
 * 
 * In 2018 it was determined that they didn't work, so new ones were reimplemented.
 * 
 * These tests pass, showing correct behaviour from strongback.
 */
public class TestCommandGroup {
	
	private MockClock clock;
	private Scheduler scheduler;
	
	private class SleepCommand extends Command {
		private final String name;
		private final double sleepTimeSecs;
		private final Clock clock;
		private final ArrayList<String> output;
		private double endTimeSecs;
		public SleepCommand(String name, double secs, Clock clock, ArrayList<String> output) {
			this.name = name;
			this.sleepTimeSecs = secs;
			this.clock = clock;
			this.output = output;
		}

		public void initialize() {
			output.add(String.format("%s starting", name));
			endTimeSecs = clock.currentTime() + sleepTimeSecs;
	    }	

		public boolean execute() {
			return clock.currentTime() > endTimeSecs;
		}
		
		public void end() {
			output.add(String.format("%s finished", name));
		}

	}	
	
	@Before
	public void setUp() {
		clock = Mock.clock();
		scheduler = new Scheduler(Strongback.logger());
	}

	private void stepTillDone() {
		final long stepDurationMs = 500;
		while(!scheduler.isEmpty()) {
			scheduler.execute(clock.currentTimeInMillis());
			clock.incrementByMilliseconds(stepDurationMs);
		}
	}
	
	private <T> void printList(ArrayList<T> list) {
		System.out.println("Output:");
		for (T item : list) {
			System.out.println(item);
		}		
	}
	
	@Test
	public void parallelCommand() {
		System.out.println("parallelCommand() test");
		ArrayList<String> actual = new ArrayList<String>();
		CommandGroup parallel = CommandGroup.runSimultaneously(
				new SleepCommand("P1", 2, clock, actual),
				new SleepCommand("P2", 2, clock, actual),
				new SleepCommand("P3", 2, clock, actual));
		scheduler.submit(parallel);
		
		stepTillDone();
		// Command has now finished.
		printList(actual);
		ArrayList<String> expected = new ArrayList<String>(Arrays.asList(
				"P1 starting",
				"P2 starting",
				"P3 starting",
				"P1 finished",
				"P2 finished",
				"P3 finished"
				));
		Assert.assertArrayEquals(expected.toArray(), actual.toArray());
	}

	@Test
	public void sequentialCommand() {
		System.out.println("sequentialCommand() test");
		ArrayList<String> actual = new ArrayList<String>();
		CommandGroup sequential = CommandGroup.runSequentially(
				new SleepCommand("S1", 2, clock, actual),
				new SleepCommand("S2", 2, clock, actual),
				new SleepCommand("S3", 2, clock, actual));	
		scheduler.submit(sequential);
		
		stepTillDone();
		// Command has now finished.
		printList(actual);
		ArrayList<String> expected = new ArrayList<String>(Arrays.asList(
				"S1 starting",
				"S1 finished",
				"S2 starting",
				"S2 finished",
				"S3 starting",
				"S3 finished"
				));
		Assert.assertArrayEquals(expected.toArray(), actual.toArray());
	}

	@Test
	public void sequentialParallelCommand() {
		System.out.println("sequentialParallelCommand() test");
		ArrayList<String> actual = new ArrayList<String>();
		CommandGroup parallel = CommandGroup.runSimultaneously(
				new SleepCommand("P1", 2, clock, actual),
				new SleepCommand("P2", 2, clock, actual),
				new SleepCommand("P3", 2, clock, actual));
		CommandGroup sequential = CommandGroup.runSequentially(
				new SleepCommand("S1", 2, clock, actual),
				parallel,
				new SleepCommand("S2", 2, clock, actual));	
		scheduler.submit(sequential);
		
		stepTillDone();
		// Command has now finished.
		printList(actual);
		ArrayList<String> expected = new ArrayList<String>(Arrays.asList(
				"S1 starting",
				"S1 finished",
				
				"P1 starting",
				"P2 starting",
				"P3 starting",
				"P1 finished",
				"P2 finished",
				"P3 finished",
				
				"S2 starting",
				"S2 finished"
				));
		Assert.assertArrayEquals(expected.toArray(), actual.toArray());
	}

	@Test
	public void parallelSequentialCommand() {
		System.out.println("parallelSequentialCommand() test");
		ArrayList<String> actual = new ArrayList<String>();
		CommandGroup sequential = CommandGroup.runSequentially(
				new SleepCommand("S1", 2, clock, actual),
				new SleepCommand("S2", 2, clock, actual),
				new SleepCommand("S3", 2, clock, actual));	

		CommandGroup parallel = CommandGroup.runSimultaneously(
				new SleepCommand("P1", 2, clock, actual),
				sequential,
				new SleepCommand("P3", 2, clock, actual));
		scheduler.submit(parallel);
		
		stepTillDone();
		// Command has now finished.
		printList(actual);
		ArrayList<String> expected = new ArrayList<String>(Arrays.asList(
				"P1 starting",
				"S1 starting",
				"P3 starting",
				
				"P1 finished",
				"S1 finished",
				"P3 finished",

				"S2 starting",
				"S2 finished",

				"S3 starting",
				"S3 finished"
				));
		Assert.assertArrayEquals(expected.toArray(), actual.toArray());
	}
}
