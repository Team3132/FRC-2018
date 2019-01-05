package org.strongback.mock;

import org.strongback.Strongback;
import org.strongback.hardware.Hardware;
import org.strongback.components.Clock;
import org.strongback.components.Solenoid;

/**
 * Mock version of a single solenoid.
 * Everything but the solenoid itself.
 *
 * @see Solenoid
 * @see Hardware
 * @see edu.wpi.first.wpilibj.DoubleSolenoid
 */
final class MockDoubleSolenoid implements MockSolenoid {
    private Direction direction;
    private double endTime;			// time the solenoid will finish moving
	private Clock clock;
	private double timeOut;			// time in seconds to move the solenoid out
	private double timeIn;			// time in seconds to move the solenoid in

    MockDoubleSolenoid(double timeOut, double timeIn) {
    	this.direction = Direction.STOPPED;	// unknown
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.clock = Strongback.timeSystem();
        this.endTime = clock.currentTime();
    }

    @Override
    public String toString() {
        return "position = " + direction;
    }

	@Override
	public Direction getDirection() {
		return direction;
	}

	@Override
	public MockDoubleSolenoid extend() {
		direction = Direction.EXTENDED;
		endTime = clock.currentTime() + timeOut;
		return this;
	}

	@Override
	public MockDoubleSolenoid retract() {
		direction = Direction.RETRACTED;
		endTime = clock.currentTime() + timeIn;
		return this;
	}

	@Override
	public boolean isExtended() {
		return ((direction == Direction.EXTENDED) && (clock.currentTime() >= endTime));
	}

	@Override
	public boolean isRetracted() {
		return ((direction == Direction.RETRACTED) && (clock.currentTime() >= endTime));
	}

	@Override
	public boolean isStopped() {
		return (clock.currentTime() >= endTime);
	}
}