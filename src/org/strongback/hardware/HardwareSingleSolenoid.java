package org.strongback.hardware;

import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.components.Solenoid;

/**
 * Wrapper for WPILib {@link Solenoid}.
 *
 * @author Rex di Bona
 * @see Solenoid
 * @see Hardware
 * @see edu.wpi.first.wpilibj.Solenoid
 */
final class HardwareSingleSolenoid implements Solenoid {
    private final edu.wpi.first.wpilibj.Solenoid solenoid;
    private Direction direction;
    private double endTime;			// time the solenoid will finish moving
	private Clock clock;
	private double timeOut;			// time in seconds to move the solenoid out
	private double timeIn;			// time in seconds to move the solenoid in

    HardwareSingleSolenoid(edu.wpi.first.wpilibj.Solenoid solenoid, double timeIn, double timeOut) {
        assert solenoid != null;
        this.solenoid = solenoid;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.clock = Strongback.timeSystem();
        this.endTime = clock.currentTime();
        direction = (solenoid.get()?Direction.EXTENDED:Direction.RETRACTED);
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
	public HardwareSingleSolenoid extend() {
		if (direction != Direction.EXTENDED) {
			endTime = clock.currentTime() + timeOut;
			direction = Direction.EXTENDED;
			solenoid.set(true);
		}
		return this;
	}

	@Override
	public HardwareSingleSolenoid retract() {
		if (direction != Direction.RETRACTED) {
			endTime = clock.currentTime() + timeIn;
			direction = Direction.RETRACTED;
			solenoid.set(false);
		}
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