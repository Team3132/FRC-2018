package org.team3132.subsystems;

import org.strongback.components.Solenoid;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.EndgameInterface;
import org.team3132.interfaces.Log;
import org.team3132.lib.Subsystem;

public class Endgame extends Subsystem implements EndgameInterface {

	Solenoid rampSolenoid;
	
	public Endgame(Solenoid rampSolenoid, DashboardInterface dashboard, Log log) {
		super("Endgame", dashboard, log);
		this.rampSolenoid = rampSolenoid;
		
		log.register(false, (this::isRampExtended), "%s/rampExtended", name);				
	}

	@Override
	public void deployRamp() {
		if (!enabled) {
			log.sub("%s: Trying to deploy ramp when subsystem is disabled\n", name);
			return;
		}
		log.sub("%s: Deploying ramp\n", name);
		rampSolenoid.extend();
	}

	/**
	 * Available for testing, but doesn't actually retract
	 */
	@Override
	public void retractRamp() {
		if (!enabled) {
			log.sub("%s: Trying to retract ramp when subsystem is disabled\n", name);
			return;
		}
		log.sub("%s: Retracting ramp\n", name);
		rampSolenoid.retract();
	}

	@Override
	public boolean isRampExtended() {
		return rampSolenoid.isExtended();
	}

	@Override
	public void execute(long timeInMillis) {	
	}
}
