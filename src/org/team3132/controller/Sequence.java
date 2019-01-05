package org.team3132.controller;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A list of State instances that the robot should go through.
 * Can be aborted at any time.
 * 
 * Every add() call adds a new state that the robot should achieve
 * before moving to the next state. The final state is where the robot
 * will remain once it has done all states.
 * 
 * Any value not set will cause it to not be changed. eg if we don't set the
 * lift height, the height will be unchanged.
 * 
 * Example usage for intaking a cube
 *   Sequence seq = new Sequence("Intake cube");
 *   seq.add().setLiftHeight(0).setIntakeConfig(NARROW).setOuttakeOpen(true);
 *   seq.add().setIntakeMotorOutput(1).setOuttakeHasCube(true);
 *   seq.add().setOuttakeOpen(false);
 *   seq.add().setIntakeMotorOutput(0).setIntakeConfig(STOWED);
 *   br.setStates(seq);
 */
public class Sequence implements Iterable<State> {
	private final String name;
	private ArrayList<State> states = new ArrayList<State>();
	
	public Sequence(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Adds a new state at the end of the sequence.
	 * @return new state
	 */
	public State add() {
		states.add(new State());
		return states.get(states.size() - 1);
	}
	
	public Iterator<State> iterator() {
		return states.iterator();
	}
	
	/**
	 * Add another sequence to the end of this one.
	 * @param other the sequence to add.
	 */
	public void appendSequence(Sequence other) {
		states.addAll(other.states);
	}
}
