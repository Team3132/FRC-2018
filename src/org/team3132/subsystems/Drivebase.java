package org.team3132.subsystems;

import org.strongback.Executable;
import org.strongback.components.TalonSRX;
import org.team3132.driveRoutines.DriveRoutine;
import org.team3132.driveRoutines.DriveRoutine.DriveMotion;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.DrivebaseInterface;
import org.team3132.interfaces.Log;
import org.team3132.lib.Subsystem;

import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * Subsystem responsible for the drivetrain
 * 
 * Normally there are multiple motors per side, but these have been set to follow one
 * motor per side and these are passed to the drivebase.
 * 
 * It periodically queries the joysticks (or the auto routine) for the speed/power for
 * each side of the drivebase.
 */
public class Drivebase extends Subsystem implements DrivebaseInterface, Executable {
	private DriveRoutine defaultRoutine;
	private DriveRoutine routine;	// Changes if in teleop or autonmous
	private ControlMode defaultMode;
	private ControlMode mode;
	private final TalonSRX left;
	private final TalonSRX right;
	private DriveMotion currentMotion;
	
	public Drivebase(TalonSRX left, TalonSRX right, DriveRoutine routine, ControlMode mode, DashboardInterface dashboard, Log log) {
		super("Drivebase", dashboard, log);
		this.routine = defaultRoutine = routine;
		this.mode = defaultMode = mode;
		this.left = left;
		this.right = right;
		currentMotion = new DriveMotion(0, 0);
		disable();					// disable until we are ready to use it.
		log.register(true, () -> currentMotion.left * 10, "%s/setpoint/Left", name) // talons work in units/100ms, *10 to convert to units/second
		   .register(true, () -> currentMotion.right * 10, "%s/setpoint/Right", name) // talons work in units/100ms, *10 to convert to units/second
		   .register(false, () -> left.getSelectedSensorPosition(0), "%s/position/Left", name)
		   .register(false, () -> right.getSelectedSensorPosition(0), "%s/position/Right", name)
		   .register(false, () -> left.getSelectedSensorVelocity(0) * 10, "%s/actual/Left", name) // talons work in units/100ms, *10 to convert to units/second
		   .register(false, () -> right.getSelectedSensorVelocity(0) * 10, "%s/actual/Right", name) // talons work in units/100ms, *10 to convert to units/second
		   .register(false, () -> left.getMotorOutputVoltage(), "%s/outputVoltage/Left", name)
		   .register(false, () -> right.getMotorOutputVoltage(), "%s/outputVoltage/Right", name)
		   .register(false, () -> left.getOutputCurrent(), "%s/outputCurrent/Left", name)
		   .register(false, () -> right.getOutputCurrent(), "%s/outputCurrent/Right", name)
		   .register(false, () -> left.getMotorOutputPercent(), "%s/outputPercentage/Left", name)
		   .register(false, () -> right.getMotorOutputPercent(), "%s/outputPercentage/Right", name)
		   .register(false, () -> left.getOutputCurrent(), "%s/outputCurrent/Left", name)
		   .register(false, () -> right.getOutputCurrent(), "%s/outputCurrent/Right", name);
	}
	
	@Override
	synchronized public void update() {
		// Query the joysticks or the auto routine for the desired wheel speed/power.
		DriveMotion motion = routine.getMotion();
		if (motion.equals(currentMotion)) return;  // No change.
		// The TalonSRX doesn't have a watchdog (unlike the WPI_ version), so no need to
		// updated it often.
		currentMotion = motion;  // Save it for the logging.
		left.set(mode, motion.left);
		right.set(mode, motion.right);
	}

	@Override
	synchronized public DriveRoutine setDriveRoutine(DriveRoutine newRoutine, ControlMode mode) {
		if (newRoutine == null) {
			// Reset to the default routine.
			newRoutine = defaultRoutine;
			mode = defaultMode;
		}
		if (newRoutine == routine) return routine;
		log.sub("%s: Setting Drive Routine: " + newRoutine.getName(), name);
		DriveRoutine oldRoutine = routine;
	    oldRoutine.deactivate();
		newRoutine.activate();
		routine = newRoutine;
		this.mode = mode;
		return oldRoutine;
	}
	
	@Override
	public void enable() {
		super.enable();
		routine.activate();
	}
	
	public void disable() {
		super.disable();
		routine.deactivate();
		left.set(mode, 0.0);
		right.set(mode, 0.0);
		currentMotion.left = 0;
		currentMotion.right = 0;
	}

	@Override
	public void updateDashboard() {
		dashboard.putNumber("Left drive motor", currentMotion.left);
		dashboard.putNumber("Right drive motor", currentMotion.right);
		dashboard.putString("Drive control", routine.getName());
	}
}
