package org.strongback.hardware;

import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.components.Solenoid;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

/**
 * Wrapper for WPILib {@link DoubleSolenoid}.
 *
 * @see Solenoid
 * @see Hardware
 * @see edu.wpi.first.wpilibj.DoubleSolenoid
 */
final class HardwareDoubleSolenoid implements Solenoid {
    private final DoubleSolenoid solenoid;
    private Direction direction;
    private double endTime;			// time the solenoid will finish moving
	private Clock clock;
	private double timeOut;			// time in seconds to move the solenoid out
	private double timeIn;			// time in seconds to move the solenoid in

    HardwareDoubleSolenoid(DoubleSolenoid solenoid, double timeOut, double timeIn) {
        assert solenoid != null;
        this.solenoid = solenoid;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.clock = Strongback.timeSystem();
        this.endTime = clock.currentTime();
        switch (solenoid.get()) {
        case kForward:
        	direction = Direction.EXTENDED;
        	break;
        case kReverse:
        	direction = Direction.RETRACTED;
        	break;
        default:
        	direction = Direction.STOPPED;
        }
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
	public HardwareDoubleSolenoid extend() {
		if (direction != Direction.EXTENDED) {
			endTime = clock.currentTime() + timeOut;
			direction = Direction.EXTENDED;
			solenoid.set(Value.kForward);
		}
		return this;
	}

	@Override
	public HardwareDoubleSolenoid retract() {
		if (direction != Direction.RETRACTED) {
			endTime = clock.currentTime() + timeIn;
			direction = Direction.RETRACTED;
			solenoid.set(Value.kReverse);
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