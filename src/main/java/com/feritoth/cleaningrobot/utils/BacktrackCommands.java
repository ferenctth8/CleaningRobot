package com.feritoth.cleaningrobot.utils;

import java.util.Arrays;
import java.util.List;

import com.feritoth.cleaningrobot.core.ValidCommands;

/**
 * The following interface contains all the possible backtrack commands stated by the problem requirements.
 * 
 * @author Ferenc Toth
 */
public interface BacktrackCommands {
	
	/* First backtrack sequence */
	static List<ValidCommands> FIRST_BACKTRACK_SEQUENCE = Arrays.asList(ValidCommands.TURN_RIGHT, ValidCommands.ADVANCE, ValidCommands.TURN_LEFT);
	/* Second backtrack sequence - must be executed twice according to the problem requirements */
	static List<ValidCommands> SECOND_BACKTRACK_SEQUENCE = Arrays.asList(ValidCommands.TURN_RIGHT, ValidCommands.ADVANCE, ValidCommands.TURN_RIGHT);
	/* Third backtrack sequence */
	static List<ValidCommands> THIRD_BACKTRACK_SEQUENCE = Arrays.asList(ValidCommands.TURN_RIGHT, ValidCommands.BACK, ValidCommands.TURN_RIGHT, ValidCommands.ADVANCE);
	/* Fourth backtrack sequence - after this sequence, the robot is considered to be stuck in case of reaching an obstacle */
	static List<ValidCommands> FOURTH_BACKTRACK_SEQUENCE = Arrays.asList(ValidCommands.TURN_LEFT, ValidCommands.TURN_LEFT, ValidCommands.ADVANCE);

}