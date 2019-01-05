package org.team3132.mock;

import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.Log;
import org.team3132.interfaces.VisionInterface;
import org.team3132.lib.Subsystem;

public class MockVision extends Subsystem implements VisionInterface {

	public MockVision(DashboardInterface dashboard, Log log) {
		super("MockVision", dashboard, log);
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public TargetDetails getTargetDetails() {
		// TODO Auto-generated method stub
		return null;
	}

}
