package org.team3132;

import org.team3132.controller.Controller;
import org.team3132.controller.Sequence;
import org.team3132.controller.Sequences;
import org.team3132.interfaces.Log;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import jaci.pathfinder.Waypoint;

/**
 * Handles auto routine selection.
 * 
 * Auto routines should be defined in Sequences.java
 */
public class Auto {
	private final Log log;
	private SendableChooser<Sequence> autoProgram = new SendableChooser<Sequence>();
	
	public Auto(Log log) {
		this.log = log;
		addAutoSequences();
		addChooser();
	}

	public void executedSelectedSequence(Controller controller) {
		Sequence seq = autoProgram.getSelected();
		log.info("Starting selected auto program %s", seq.getName());
		controller.doSequence(seq);
	}

	private void addAutoSequences() {
		autoProgram.addDefault("Nothing", Sequences.getEmptySequence());
		autoProgram.addObject("Drive forward 10in", Sequences.getDriveToWaypointSequence(10, 0, 0));
		addDriveTestSequence();
	}
	
	private void addDriveTestSequence() {
		Sequence seq = new Sequence("Drive test");
		// Go forward 10"
		Waypoint[] waypoints1 = new Waypoint[] {
				new Waypoint(0, 0, 0), new Waypoint(5, 0, 0), new Waypoint(10, 0, 0)};
		seq.add().setRelativeWaypoints(waypoints1, true);
		// Go backwards 10"
		Waypoint[] waypoints2 = new Waypoint[] {
				new Waypoint(0, 0, 0), new Waypoint(-5, 0, 0), new Waypoint(-10, 0, 0)};
		seq.add().setRelativeWaypoints(waypoints2, false);
		autoProgram.addObject("Drive test", seq);
	}
	
	private void addChooser() {
		SmartDashboard.putData("Auto program", autoProgram);
	}	
}
