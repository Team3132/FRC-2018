package org.team3132.lib;

import org.team3132.interfaces.DashboardUpdater;
import org.team3132.interfaces.Log;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
/**
 *	Class to monitor the Power Distribution Panel.
 * This allows us to track if motors are stalling, and to observe where the power is going on the robot.
 * 
 * Currently we don't use this information in interesting ways, we just log it for post match diagnosis.
 */
public class PowerMonitor implements DashboardUpdater {		// no interface, as this is a purely hardware class.
    
	/*
	 * REX: We should only sample the values that are "interesting". OR, we should sample slower, with another thread.
	 * I believe that sampling too fast is what is causing the CAN bus timeouts.
	 */
	
	PowerDistributionPanel pdp;
	
	public PowerMonitor (PowerDistributionPanel pdp, int[] channelsToMonitor, boolean enabled, Log log) {
		final String name = "Power";
		this.pdp = pdp;
		if (!enabled) {
			return;
		}
		log.register(false, (() -> { return pdp.getTotalEnergy(); } ),	"%s/totalEnergy", name)
			.register(false, (() -> { return pdp.getTotalPower(); } ),		"%s/totalPower", name)
			.register(false, (() -> { return pdp.getTotalCurrent(); } ),	"%s/totalCurrent", name)
			.register(false, (() -> { return pdp.getTemperature(); } ),	"%s/temperature", name)
			.register(false, (() -> { return pdp.getVoltage(); } ),		"%s/inputVoltage", name);
		for (int i = 0; i < channelsToMonitor.length; i++) {
			final int channel = channelsToMonitor[i];
			log.register(false, (() -> { return pdp.getCurrent(channel); } ), "%s/channelCurrent/%d", name, channel);
		}
	}

	@Override
	public void updateDashboard() {
		SmartDashboard.putNumber("PDP Voltage: ", pdp.getVoltage());
	}
}

