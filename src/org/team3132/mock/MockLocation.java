package org.team3132.mock;

import org.team3132.Constants;
import org.team3132.interfaces.LocationInterface;
import org.team3132.lib.MathUtil;
import org.team3132.lib.Position;

public class MockLocation implements LocationInterface {

	Position here = new Position(0, 0, 0);
	double heading = 0;
	
	@Override
	public void execute(long timeInMillis) {
	}

	@Override
	public void setCurrentLocation(Position current) {
		here = current;
	}

	@Override
	public Position getCurrentLocation() {
		return here;
	}

	@Override
	public void setDesiredLocation(Position location) {
	}

	@Override
	public Position getHistoricalLocation(double timeSec) {
		// Build the position based on the time.
		return new Position(10*timeSec, 100*timeSec, timeSec % 360);
	}

	@Override
	public void update() {
	}

	@Override
	public double getHeading() {
		return heading;
	}

	@Override
	public double getBearing() {
		return MathUtil.normalise(heading, Constants.FULL_CIRCLE);
	}

	@Override
	public double getSpeed() {
		return 0;
	}

	@Override
	public void resetHeading() {
		heading = 0;
	}

	@Override
	public void enable() {
	}

	@Override
	public void disable() {
	}
	
	public void setHeading(double heading) {
		this.heading = heading;
	}
}
