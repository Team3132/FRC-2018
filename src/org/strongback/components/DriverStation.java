package org.strongback.components;

/**
 * Provide access to the network communication data to / from the Driver Station.
 */
public interface DriverStation {
    public enum Alliance {
        Red,
        Blue,
        Invalid,
    }

    public enum MatchType {
        None,
        Practice,
        Qualification,
        Elimination
    }

    public String getGameSpecificMessage();
    public Alliance getAlliance();
    public String getEventName();
    public int getLocation();
    public int getMatchNumber();
    public MatchType getMatchType();
    public int getReplayNumber();
    public boolean isAutonomous();
    public boolean isDisabled();
    public boolean isEnabled();
    public boolean isFMSAttached();
    public boolean isNewControlData();
    public boolean isDSAttached();
    public boolean isTest();
    public double getMatchTime();
    public boolean isOperatorControl();
}
