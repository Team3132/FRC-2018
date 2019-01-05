package org.team3132.mock;

import org.team3132.interfaces.LEDControllerInterface;

public class MockLEDController implements LEDControllerInterface {

	@Override
	public void setColour(double red, double green, double blue) {
	}


	@Override
	public void doIdleColours() {
	}

	@Override
	public String getName() {
		return "led";
	}

	@Override
	public void enable() {
	}

	@Override
	public void disable() {
	}

	@Override
	public void execute(long timeInMillis) {
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void cleanup() {
	}
}
