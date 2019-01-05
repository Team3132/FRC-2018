package org.team3132.driveRoutines;

public class DriveRoutineDeadband implements DriveRoutine {
	private DriveRoutine child;
	
	public DriveRoutineDeadband(DriveRoutine child) {
		this.child = child;
	}
	
	@Override
	public DriveMotion getMotion() {
		DriveMotion dm = child.getMotion();
		
		if (Math.abs(dm.left) < 0.02) {
			dm.left = 0;
		}
		if (Math.abs(dm.right) < 0.02) {
			dm.right = 0;
		}
		return dm;
	}

}
