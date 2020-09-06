package com.feritoth.cleaningrobot.utils;

import com.feritoth.cleaningrobot.core.CleaningRobot;

/**
 * The current part will mark a possibility to symbolize that the robot is stuck in the map
 * - exception to be thrown in case of all back-off sequences failing.
 * 
 * @author Ferenc Toth
 *
 */
public class RobotStuckException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final CleaningRobot finalRobot;
	
	public RobotStuckException(CleaningRobot finalRobot) {
		super();
		this.finalRobot = finalRobot;
	}

	public CleaningRobot getFinalRobot() {
		return finalRobot;
	}
	
}