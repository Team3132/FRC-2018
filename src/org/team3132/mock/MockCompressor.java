package org.team3132.mock;

import org.team3132.interfaces.CompressorInterface;

public class MockCompressor implements CompressorInterface {

	private boolean enabled = true;
	@Override
	public void turnOn() {
		enabled = true;
	}

	@Override
	public void turnOff() {
		enabled = false;
	}

	@Override
	public boolean isOn() {
		return enabled;
	}
}