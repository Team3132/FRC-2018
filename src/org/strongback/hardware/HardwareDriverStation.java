package org.strongback.hardware;

import org.strongback.components.DriverStation;

/**
 * The reason we do all this, rather than just extend WPILib, is because DriverStation is a singleton with a private constructor.
 * As such, it is not possible to extend it.
 */
public class HardwareDriverStation implements DriverStation {
    private edu.wpi.first.wpilibj.DriverStation driverStation;

    public HardwareDriverStation() {
        driverStation = edu.wpi.first.wpilibj.DriverStation.getInstance();
    }

    @Override
    public String getGameSpecificMessage() {
        return driverStation.getGameSpecificMessage();
    }

    @Override
    public Alliance getAlliance() {
        switch (driverStation.getAlliance()) {
            case Red:
                return Alliance.Red;
            case Blue:
                return Alliance.Blue;
            case Invalid:
                return Alliance.Invalid;
        }
        return Alliance.Invalid;
    }

    @Override
    public String getEventName() {
        return driverStation.getEventName();
    }

    @Override
    public int getLocation() {
        return driverStation.getLocation();
    }

    @Override
    public int getMatchNumber() {
        return driverStation.getMatchNumber();
    }

    @Override
    public MatchType getMatchType() {
        switch (driverStation.getMatchType()) {
            case None:
                return MatchType.None;
            case Practice:
                return MatchType.Practice;
            case Qualification:
                return MatchType.Qualification;
            case Elimination:
                return MatchType.Elimination;
        }
        return MatchType.None;
    }

    @Override
    public int getReplayNumber() {
        return driverStation.getReplayNumber();
    }

    @Override
    public boolean isAutonomous() {
        return driverStation.isAutonomous();
    }

    @Override
    public boolean isDisabled() {
        return driverStation.isDisabled();
    }

    @Override
    public boolean isEnabled() {
        return driverStation.isEnabled();
    }

    @Override
    public boolean isFMSAttached() {
        return driverStation.isFMSAttached();
    }

    @Override
    public boolean isNewControlData() {
        return driverStation.isNewControlData();
    }

    @Override
    public boolean isDSAttached() {
        return driverStation.isDSAttached();
    }

    @Override
    public boolean isTest() {
        return driverStation.isTest();
    }

    @Override
    public double getMatchTime() {
        return driverStation.getMatchTime();
    }

    @Override
    public boolean isOperatorControl() {
        return driverStation.isOperatorControl();
    }
}
