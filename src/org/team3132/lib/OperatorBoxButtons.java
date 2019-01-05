package org.team3132.lib;

public class OperatorBoxButtons {
	/*
	 * Buttons 0 - 22 appear to be valid buttons, with special cases:
	 *  5  -  8 is a rotary selector
	 *  13 - 17 is a rotary selector
	 *  19 - 22 is a rotary selector
	 *  Leaving 23 - 12 = 11 buttons.
	 *  
	 *  The disable/manual/auto button switches are as follows:
	 *  23/24, 25/26, 27/28, 29/30, 31/32
	 *  
	 *  Unassigned buttons: 12,
	 *  
	 *  Note that this box also contains four pots for the axis.
	 */
	
	public static final int BUTTON0 = 0;
	public static final int BUTTON1 = 1;
	public static final int BUTTON2 = 2;
	public static final int BUTTON3 = 3;
	public static final int BUTTON4 = 4;
	
	public static final int BUTTON9 = 9; //???
	public static final int BUTTON10 = 10;
	public static final int BUTTON11 = 11;
	public static final int BUTTON12 = 12;

	public static final int BUTTON18 = 18;

	// Rotary buttons. Only one of these can be pressed at any one time.
	// Not clear how many positions there are, but there may be an
	// unbuttoned one that is implicit if none of the others is pressed.
	// Maybe these should have been done in binary??
	public static final int ROTARY0_1 = 5;
	public static final int ROTARY0_2 = 6;
	public static final int ROTARY0_3 = 7;
	public static final int ROTARY0_4 = 8;
	public static final int ROTARY0_5 = 9; //???

	public static final int ROTARY1_1 = 13;
	public static final int ROTARY1_2 = 14;
	public static final int ROTARY1_3 = 15;
	public static final int ROTARY1_4 = 16;
	public static final int ROTARY1_5 = 17;
	
	public static final int ROTARY2_1 = 19;
	public static final int ROTARY2_2 = 20;
	public static final int ROTARY2_3 = 21;
	public static final int ROTARY2_4 = 22;
	
	// Pot as joystick axis.
	public static final int POT0 = 0;
	public static final int POT1 = 1;
	public static final int POT2 = 2;
	public static final int POT3 = 3;

	// 3-way switches. If neither manual or auto is pressed, then
	// it is assumed to be in auto mode.
	public static final int SWITCH1_DISABLE = 23;
	public static final int SWITCH1_MANUAL = 24;
	public static final int SWITCH2_DISABLE = 25;
	public static final int SWITCH2_MANUAL = 26;
	public static final int SWITCH3_DISABLE = 27;
	public static final int SWITCH3_MANUAL = 28;
	public static final int SWITCH4_DISABLE = 29;
	public static final int SWITCH4_MANUAL = 30;
	public static final int SWITCH5_DISABLE = 31;
	public static final int SWITCH5_MANUAL = 32;

	
	// This years game-specific mappings
	
	// Intake
	public static final int INTAKE_DISABLE = SWITCH1_DISABLE;
	public static final int INTAKE_MANUAL = SWITCH1_MANUAL;
	// Manual overrides.
	public static final int INTAKE_STOWED = ROTARY0_1;
	public static final int INTAKE_WIDE = ROTARY0_2;	
	public static final int INTAKE_NARROW = ROTARY0_3;	
	public static final int INTAKE_SUPER_NARROW = ROTARY0_4;
	public static final int INTAKE_INWARDS = BUTTON0;
	public static final int INTAKE_OUTWARDS = BUTTON1;

	// Lift
	public static final int LIFT_DISABLE = SWITCH1_DISABLE;
	public static final int LIFT_MANUAL = SWITCH1_MANUAL;
	// Manual overrides.
	public static final int LIFT_ADD_HEIGHT = BUTTON2;
	public static final int LIFT_DECREASE_HEIGHT = BUTTON3;
	public static final int LIFT_SHIFTER = BUTTON2;

	// Outtake
	public static final int OUTTAKE_DISABLE = SWITCH2_DISABLE;
	public static final int OUTTAKE_MANUAL = SWITCH2_MANUAL;
	// Manual overrides
	public static final int OUTTAKE_OPEN = BUTTON9;
	public static final int OUTTAKE_CLOSE = BUTTON10;
	public static final int OUTTAKE_SCORE_LEFT = BUTTON11;
	public static final int OUTTAKE_SCORE_RIGHT = BUTTON12;

	// Climbing
	public static final int READY_CLIMB = BUTTON4;
	public static final int CLIMB = BUTTON3;
	
	// Endgame
	public static final int RAMP = BUTTON1;
		
	

	/**
	 * The old mappings corresponding with the left over stickers on the button box.
	 *
	
	public static final int EJECT = 1;
	public static final int AUTO_FIRE = 2;
	public static final int CLIMB = 3;
	public static final int RETRACT = 4;

	public static final int AUTO_PROG_BIT_0 = 5; // autonomous encoded here
	public static final int AUTO_PROG_BIT_1 = 6;
	public static final int AUTO_PROG_BIT_2 = 7;
	public static final int AUTO_PROG_BIT_3 = 8;

	public static final int INTAKE = 9;
	public static final int OUTTAKE = 10;
	public static final int FENDER_RIGHT = 11;
	public static final int FENDER_LEFT = 12;
	public static final int DEFENCE_5 = 13;
	public static final int DEFENCE_4 = 14;
	public static final int DEFENCE_3 = 15;
	public static final int DEFENCE_2 = 16;
	public static final int DEFENCE_1 = 17; // same as LOW_BAR
	public static final int LOW_BAR = 17;
	public static final int KICKER = 18;

	public static final int DRIVE_STYLE_BIT_0 = 19; // Drive style encoded here
	public static final int DRIVE_STYLE_BIT_1 = 20;
	public static final int DRIVE_STYLE_BIT_2 = 21;
	public static final int DRIVE_STYLE_BIT_3 = 22;

	public static final int INTAKE_DISABLE = 23;
	public static final int INTAKE_MANUAL = 24;
	public static final int TURRET_DISABLE = 25;
	public static final int TURRET_MANUAL = 26;
	public static final int HOOD_DISABLE = 27;
	public static final int HOOD_MANUAL = 28;
	public static final int SHOOTER_DISABLE = 29;
	public static final int SHOOTER_MANUAL = 30;
	public static final int BALL_IN_DISABLE = 31;
	public static final int BALL_IN_MANUAL = 32;

	public final static int INTAKE_SCALE = 3;
	public final static int TURRET_SCALE = 2;
	public final static int HOOD_SCALE = 1;
	public final static int SHOOTER_SCALE = 0;

	*/

}
