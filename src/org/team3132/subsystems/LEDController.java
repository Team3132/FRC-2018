package org.team3132.subsystems;

import org.strongback.components.Clock;
import org.team3132.Constants;
import org.team3132.interfaces.LEDControllerInterface;
import org.team3132.interfaces.DashboardInterface;
import org.team3132.interfaces.Log;
import org.team3132.lib.MathUtil;
import org.team3132.lib.Subsystem;

import com.ctre.phoenix.CANifier;

public class LEDController extends Subsystem implements LEDControllerInterface {

	private final double GREEN_R = 0.0;
	private final double GREEN_G = 0.5;	// Not too bright please.
	private final double GREEN_B = 0.0;
	private final double GOLD_R = 1.0;
	private final double GOLD_G = 0.843;
	private final double GOLD_B = 0.0;
	
	CANifier canifier;
	Clock clock;
	Mode mode = Mode.IDLE_COLOUR_CYCLE;

	private enum Mode {
		STATIC_COLOUR,
		IDLE_COLOUR_CYCLE;
	}
	
	public LEDController(DashboardInterface dashboardx, CANifier canifier, DashboardInterface dashboard, Clock clock, Log log) {
		super("led", dashboard, log);
		this.clock = clock;
		this.canifier = canifier;
	}
	
	/**
	 * Set a static colour.
	 * @param red
	 * @param green
	 * @param blue
	 */
	@Override
	public void setColour(double red, double green, double blue) {
		mode = Mode.STATIC_COLOUR;
		setColourInternal(red, green, blue);		
	}

	/**
	 * Cycle between green and gold (Australian colours).
	 */
	@Override
	public void doIdleColours() {
		mode = Mode.IDLE_COLOUR_CYCLE;
	}
	
	/**
	 * Called by strongback's background scheduler.
	 */
	@Override
	protected void update() {
		if (mode != Mode.IDLE_COLOUR_CYCLE) return;  // Leave leds alone.

		// Breathe between green (0, 0.5, 0) and gold (1, .843, 0) while idle.
		double now = clock.currentTime();  // Grab the current time.
		double alpha = now % 2;  // Make it so numbers cycle between 0 and 2.
		if (alpha > 1) alpha = Math.abs(alpha - 2);  // Have it go 0->1->0 again.
		double r = (GREEN_R - GOLD_R) * alpha + GOLD_R;
		double g = (GREEN_G - GOLD_G) * alpha + GOLD_G;
		double b = (GREEN_B - GOLD_B) * alpha + GOLD_B;
		setColourInternal(r, g, b);		
	}

	public void setColourInternal(double red, double green, double blue) {
		canifier.setLEDOutput(clamp(red), Constants.RED_LED_STRIP_CHANNEL);
		canifier.setLEDOutput(clamp(green), Constants.GREEN_LED_STRIP_CHANNEL);
		canifier.setLEDOutput(clamp(blue), Constants.BLUE_LED_STRIP_CHANNEL);		
	}

	private double clamp(double input) {
		return MathUtil.clamp(input, 0, 1);
	}
}
