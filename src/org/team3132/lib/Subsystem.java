package org.team3132.lib;

import org.strongback.Executable;
import org.strongback.command.Requirable;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.DashboardUpdater;
import org.team3132.interfaces.Log;

import edu.wpi.first.wpilibj.Notifier;

/**
 * The subsystem wrapper class is wrapped around a subsystem.
 * It ensures all subsystems are self contained, thread based, independent entities.
 * 
 * Each subsystem will have a routine 'update()' which will be called every 'period' seconds.
 * 
 * Each subsystem will have a routine 'cleanup()' which is called when the subsystem has to shutdown.
 * 
 * Each subsystem should provide logging information.
 * The log file is available automatically using the 'log' super type.
 */
public abstract class Subsystem implements Requirable, Executable, DashboardUpdater {
	protected String name;
	protected Notifier notifier;
	protected boolean enabled;
	protected DashboardInterface dashboard;
	protected Log log;
	
	public Subsystem(String name, DashboardInterface dashboard, Log log) {
		this.name = name;
		this.dashboard = dashboard;
		this.log = log;
		enabled = false;	// we start out disabled
	}
	
	/**
	 * Return the name of the subsystem
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Enable the subsystem. This allows for subsystems to be enabled and disabled on the fly.
	 * A disabled subsystem will not have its update() method called whilst it is disabled
	 */
	public void enable() {
		enabled = true;
	}
	
	/**
	 * Disable the subsystem. This allows for subsystems to be enabled and disabled on the fly.
	 * A disabled subsystem will not have its update() method called whilst it is disabled
	 */
	public void disable() {
		enabled = false;
	}
	
	/**
	 * The execute() method is called to execute the subsystem when it should be run.
	 * Run checks that the subsystem is present and enabled before calling update().
	 */
	@Override
	public void execute(long timeInMillis) {
		if (!enabled) return;
		update();
	}
	
	/**
	 * @return True is the subsystem is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * The update() method is called to allow the subsystem to move towards its goal.
	 * This method is called depending on the period that the subsystem was originally configured for.
	 * This method should only be called from execute
	 */
	protected void update() {
		// update the subsystem based on things you have been told
	}
	
	/**
	 * If a subsystem is being stopped or disabled
	 */
	public void cleanup() {
		// the subsystem is being stopped. Shut things down. Default is to do nothing.
	}
}
