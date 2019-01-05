package org.strongback.mock;

import org.strongback.components.DriverStation;

public class MockDriverStation implements DriverStation {
    private String gameSpecificMessage = "LLL";
    private Alliance alliance = Alliance.Red;
    private String eventName = "Sydney";
    private int location = 1;
    private int matchNumber = 1;
    private MatchType matchType = MatchType.None;
    private int replayNumber = 0;
    private boolean autonomous = false;
    private boolean enabled = false;
    private boolean fmsAttached = false;
    private boolean test = false;
    private boolean dsAttached = false;
    private boolean newControlData = false;
    private double matchTime = 0;
    private boolean operatorControl = false;

    public MockDriverStation() {

    }

    @Override
    public String getGameSpecificMessage() {
        return gameSpecificMessage;
    }

    public MockDriverStation setGameSpecificMessage(String gameSpecificMessage) {
        this.gameSpecificMessage = gameSpecificMessage;
        return this;
    }

    @Override
    public Alliance getAlliance() {
        return alliance;
    }

    public MockDriverStation setAlliance(Alliance alliance) {
        this.alliance = alliance;
        return this;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    public MockDriverStation setEventName(String eventName) {
        this.eventName = eventName;
        return this;
    }

    @Override
    public int getLocation() {
        return location;
    }

    public MockDriverStation setLocation(int location) {
        this.location = location;
        return this;
    }

    @Override
    public int getMatchNumber() {
        return matchNumber;
    }

    public MockDriverStation setMatchNumber(int matchNumber) {
        this.matchNumber = matchNumber;
        return this;
    }

    @Override
    public MatchType getMatchType() {
        return matchType;
    }

    public MockDriverStation setMatchType(MatchType matchType) {
        this.matchType = matchType;
        return this;
    }

    @Override
    public int getReplayNumber() {
        return replayNumber;
    }

    public MockDriverStation setReplayNumber(int replayNumber) {
        this.replayNumber = replayNumber;
        return this;
    }

    @Override
    public boolean isAutonomous() {
        return autonomous;
    }

    public MockDriverStation setAutonomous(boolean autonomous) {
        this.autonomous = autonomous;
        return this;
    }

    @Override
    public boolean isDisabled() {
        return !enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public MockDriverStation setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }


    @Override
    public boolean isFMSAttached() {
        return fmsAttached;
    }

    public MockDriverStation setFMSAttached(boolean fmsAttached) {
        this.fmsAttached = fmsAttached;
        return this;
    }

    @Override
    public boolean isNewControlData() {
        return newControlData;
    }

    public MockDriverStation setNewControlData(boolean newControlData) {
        this.newControlData = newControlData;
        return this;
    }

    @Override
    public boolean isDSAttached() {
        return dsAttached;
    }

    public MockDriverStation setDSAttached(boolean attached) {
        this.dsAttached = attached;
        return this;
    }

    @Override
    public boolean isTest() {
        return test;
    }


    public MockDriverStation setTest(boolean test) {
        this.test = test;
        return this;
    }

    @Override
    public double getMatchTime() {
        return matchTime;
    }

    public MockDriverStation setMatchTime(double matchTime) {
        this.matchTime = matchTime;
        return this;
    }

    @Override
    public boolean isOperatorControl() {
        return operatorControl;
    }

    public MockDriverStation setOperatorControl(boolean operatorControl) {
        this.operatorControl = operatorControl;
        return this;
    }
}
