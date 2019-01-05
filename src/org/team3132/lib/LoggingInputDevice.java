package org.team3132.lib;

import org.team3132.interfaces.Log;
import org.strongback.components.ui.InputDevice;

/**
 * Add generic logging to a Human Interface device (an InputDevice)
 * 
 * This could be customised for a particular joystick later.
 */
public class LoggingInputDevice {
	
	public static void AddLog(InputDevice input, String name, Log log) {
		for (int i = 0; i < input.getAxisCount(); i++) {
			final int axis = i;
			log.register(false, () -> input.getAxis(axis).read(), "%s/Axis/%d", name, i);
		}

		for (int i = 0; i < input.getButtonCount(); i++) {
			final int button = i+1;
			log.register(false, input.getButton(button), "%s/Button/%d", name, i);
		}
		
		for (int i = 0; i < input.getPOVCount(); i++) {
			final int axis = i;
			log.register(false, input.getDPad(axis), "%s/DPad/%d", name, i);
		}
	}
}
