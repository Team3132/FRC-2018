package org.team3132.driveRoutines;

import org.strongback.components.ui.ContinuousRange;
import org.team3132.interfaces.Log;

public class DriveRoutineArcade implements DriveRoutine {
	private String name = "ArcadeDrive";
	
	private ContinuousRange move;
	private boolean squaredInputs;
	private Log log;
	private ContinuousRange turn;
	
	public DriveRoutineArcade(String name, ContinuousRange move, ContinuousRange turn, Log log) {
		this(name, move, turn, true, log);
	}

	public DriveRoutineArcade(String name, ContinuousRange move, ContinuousRange turn, boolean squaredInputs, Log log) {
		this.name = name;
		this.move = move;
		this.turn = turn;
		this.squaredInputs = squaredInputs;
		this.log = log;
		log.register(false, () -> move.read(), "UI/%s/Move", name)
		   .register(false, () -> turn.read(), "UI/%s/Turn", name);
	}
	
	@Override
	public DriveMotion getMotion() {
		double m = move.read();
		double t = turn.read();
//		System.out.printf("%s: Move: %f, Turn: %f\n", name, m, t);
		return arcadeToTank(m, t, squaredInputs);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void activate() {
		log.info("Activating %s DriveControl", name);
	}
	
	@Override
	public void deactivate() {
		log.info("Deactivating %s DriveControl", name);
	}
}
