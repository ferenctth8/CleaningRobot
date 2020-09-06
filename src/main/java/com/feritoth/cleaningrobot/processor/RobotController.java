package com.feritoth.cleaningrobot.processor;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feritoth.cleaningrobot.core.CleaningRobot;
import com.feritoth.cleaningrobot.core.Position;
import com.feritoth.cleaningrobot.core.Room;
import com.feritoth.cleaningrobot.core.ValidCommands;
import com.feritoth.cleaningrobot.core.ValidDirections;
import com.feritoth.cleaningrobot.utils.BacktrackCommands;
import com.feritoth.cleaningrobot.utils.RobotStuckException;

/**
 * The second class in the cleaning workflow chain - responsible for the concrete 
 * cleaning flow completion.
 * 
 * @author Ferenc Toth
 */
public class RobotController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RobotController.class);
	private final List<List<ValidCommands>> backOffCommandSequences;
	
	public RobotController() {
		super();
		backOffCommandSequences = Arrays.asList(BacktrackCommands.FIRST_BACKTRACK_SEQUENCE, BacktrackCommands.SECOND_BACKTRACK_SEQUENCE, BacktrackCommands.SECOND_BACKTRACK_SEQUENCE,
				                                BacktrackCommands.THIRD_BACKTRACK_SEQUENCE, BacktrackCommands.FOURTH_BACKTRACK_SEQUENCE);
	}

	/**
	 * The main method responsible for the triggering of the cleaning procedure once the robot and the other input parameters are ready
	 * 
	 * @param cleaningRobot - the cleaning robot
	 * @param room - the room to be cleaned
	 * @param allInputCommands - the list of commands to be executed
	 * @param outputFileLocation - the output file location
	 */
	public void activateCleaningProcedureForRobot(CleaningRobot cleaningRobot, Room room, List<ValidCommands> allInputCommands, String outputFileLocation) {
		//For empty battery, return immediately with an error message
		if (cleaningRobot.getBattery() <= 0L){
			LOGGER.warn("The initial battery capacity was 0 or below it, the program will thus exit...");
			return;
		}
		//Now go for the effective command processing
		//define an iterator for the traversal of the list in question
		int cmdIterator = 0;
		//define a boolean field for marking the stuck situation of the robot
		boolean robotStuck = false;
		//go inside a while loop to see how each of the commands needs to be executed
		cleaning_loop: while (cleaningRobot.getBattery() > 0 && !robotStuck && cmdIterator < allInputCommands.size()) {
			//pick the next command from the list
			ValidCommands nextCommand = allInputCommands.get(cmdIterator);
			ValidDirections currentDirection = cleaningRobot.getDirection();
			Long decrementAmount = 0L;
			Long currentBatteryLevel = cleaningRobot.getBattery();			
			//check and process the command accordingly
			switch(nextCommand){
			case TURN_LEFT:
				decrementAmount = 1L;
				currentDirection = cleaningRobot.getDirection();
				if (currentBatteryLevel - decrementAmount > 0) {
					//decrement the battery and set the new direction
					cleaningRobot.setBattery(currentBatteryLevel - decrementAmount);
					cleaningRobot.setDirection(turnLeft(currentDirection));
				} else {
					LOGGER.warn("Battery level insufficent for continuing, need to exit the current instruction sequence unfortunately...");
					break cleaning_loop;
				}
				break;
			case TURN_RIGHT:
				decrementAmount = 1L;
				currentDirection = cleaningRobot.getDirection();
				if (currentBatteryLevel - decrementAmount > 0) {
					//decrement the battery and set the new direction
					cleaningRobot.setBattery(currentBatteryLevel - decrementAmount);
					cleaningRobot.setDirection(turnRight(currentDirection));
				} else {
					LOGGER.warn("Battery level insufficent for continuing, need to exit the current instruction sequence unfortunately...");
					break cleaning_loop;
				}
				break;
			case ADVANCE:
				decrementAmount = 2L;
				if (currentBatteryLevel - decrementAmount < 0){
					LOGGER.warn("Battery level insufficent for continuing, need to exit the current instruction sequence unfortunately...");
					break cleaning_loop;
				} else {
					//decrement the battery with the corresponding amount and mark the current position as visited
					cleaningRobot.setBattery(currentBatteryLevel - decrementAmount);
					cleaningRobot.addNewVisitedPosition(cleaningRobot.getPosition());
				}
				//determine the new position for the robot
				try {
					determineNextRoomPosition(cleaningRobot, room, nextCommand);					
				} catch (RobotStuckException rse) {
					//for the robot being stuck, break the main loop and save the last recorded parameters for the robot in question
					robotStuck = true;
					if (robotStuck) {
						LOGGER.warn("Program termination iminent due to robot being stuck in position:" + rse.getFinalRobot().getPosition());
						cleaningRobot = rse.getFinalRobot();
						break cleaning_loop;
					}
				}
				break;
			case BACK:
				decrementAmount = 3L;
				if (currentBatteryLevel - decrementAmount < 0){
					LOGGER.warn("Battery level insufficent for continuing, need to exit the current instruction sequence unfortunately...");
					break cleaning_loop;
				} else {
					//decrement the battery with the corresponding amount and mark the current position as visited
					cleaningRobot.setBattery(currentBatteryLevel - decrementAmount);
					cleaningRobot.addNewVisitedPosition(cleaningRobot.getPosition());
				}
				//determine the new position for the robot
				try {
					determineNextRoomPosition(cleaningRobot, room, nextCommand);					
				} catch (RobotStuckException rse) {
					//for the robot being stuck, break the main loop
					robotStuck = true;
					if (robotStuck) {
						LOGGER.warn("Program termination iminent due to robot being stuck in position:" + rse.getFinalRobot().getPosition());
						cleaningRobot = rse.getFinalRobot();
						break cleaning_loop;
					}
				}
				break;
			case CLEAN:
				decrementAmount = 5L;
				if (currentBatteryLevel - decrementAmount > 0) {
					//decrement the battery with the corresponding amount and mark the current position as cleaned
					cleaningRobot.setBattery(currentBatteryLevel - decrementAmount);
					cleaningRobot.addNewCleanedPosition(cleaningRobot.getPosition());
				} else {
					LOGGER.warn("Battery level insufficent for continuing, need to exit the current instruction sequence unfortunately...");
					break cleaning_loop;
				}
				break;
			}
			//increment at the end the command iterator
			cmdIterator++;
		}
		//print out for verification the results after executing the whole command set
		LOGGER.info("The final robot parameters after cleaning are:" + cleaningRobot.toString());
		new JSONOutputFormatter().createCleaningProcedureOutput(cleaningRobot, outputFileLocation);
	}
	
	/**
	 * A utility method for operating the turn right command.
	 * 
	 * @param initialDirection the initial direction of the robot
	 * @return the new robot direction
	 */
	private ValidDirections turnRight(ValidDirections initialDirection){
		ValidDirections finalDirection = null;
		switch(initialDirection){
		case EAST:
			finalDirection = ValidDirections.SOUTH;
			break;
		case NORTH:
			finalDirection = ValidDirections.EAST;
			break;		
		case SOUTH:
			finalDirection = ValidDirections.WEST;
			break;
		case WEST:
			finalDirection = ValidDirections.NORTH;
			break;
		}
		return finalDirection;
	}
	
	/**
	 * A similar utility method for the turn left command.
	 * 
	 * @param initialDirection the initial direction of the robot
	 * @return the new robot direction
	 */
	private ValidDirections turnLeft(ValidDirections initialDirection){
		ValidDirections finalDirection = null;
		switch(initialDirection){
		case EAST:
			finalDirection = ValidDirections.NORTH;
			break;
		case NORTH:
			finalDirection = ValidDirections.WEST;
			break;
		case SOUTH:
			finalDirection = ValidDirections.EAST;
			break;
		case WEST:
			finalDirection = ValidDirections.SOUTH;
			break;
		}
		LOGGER.info("The new direction is:" + finalDirection + "(" + finalDirection.getDirection() + ")");
		return finalDirection;
	}
	
	/**
	 * The current method will determine the next position index for the cleaning robot - it may also signal
	 * if the robot got stuck inside a given cell.
	 * 
	 * @param cleaningRobot - the cleaning robot
	 * @param room - the map of the input room
	 * @param nextCommand - the motion command (may only be ADVANCE or BACK)
	 */
	private void determineNextRoomPosition(CleaningRobot cleaningRobot, Room room, ValidCommands nextCommand) {
		//get the current robot position and direction
		Position currentPosition = cleaningRobot.getPosition();
		ValidDirections currentDirection = cleaningRobot.getDirection();
		//determine the next position based on the motion command
		Position nextPosition = null;
		switch(nextCommand){
		case ADVANCE:
			nextPosition = advanceToNewPosition(currentDirection, currentPosition);
			break;
		case BACK:
			nextPosition = retreatToPreviousPosition(currentDirection, currentPosition);
			break;
		default:
			LOGGER.info("The rest of the commands do not currently generate any new positions...");
			break;
		}
		//now examine if the given position is suitable or not for the robot
		int rowNb = room.getNbOfRows();
		int columnNb = room.getNbOfColumns();
		boolean rowOK = (0L <= nextPosition.getyRow()) && (nextPosition.getyRow() < rowNb) ? true : false;
		boolean columnOK = (0L <= nextPosition.getxCol()) && (nextPosition.getxCol() < columnNb) ? true : false;
		if (rowOK && columnOK){
			//supplementary condition - look for any obstacles inside the new position
			LOGGER.debug("Indexes OK for position:" + nextPosition + ", now going for content analysis...");
			checkPositionContent(nextPosition, cleaningRobot, room);
		} else {
			LOGGER.warn("Indexes out of range detected for position:" + nextPosition + ". Backoff sequence initiated...");
			initiateBackOffStrategy(cleaningRobot, room);
		}
	}

	/**
	 * A method for examining if the robot is allowed to move to a given position - decision based on its content this time.
	 * 
	 * @param nextPosition - the new position to be moved to
	 * @param cleaningRobot - the cleaning robot
	 * @param room - the input room map
	 */
	private void checkPositionContent(Position nextPosition, CleaningRobot cleaningRobot, Room room) {
		String newCellContent = room.getMapElement(Long.valueOf(nextPosition.getxCol()).intValue(), Long.valueOf(nextPosition.getyRow()).intValue());
		LOGGER.info("The current content is:" + newCellContent);
		switch(newCellContent){
		case "C":
		case "null":
			LOGGER.warn("An obstacle was found on position:" + nextPosition.toString() + ". Backoff sequence initiated...");
			initiateBackOffStrategy(cleaningRobot, room);
			break;
		default:
			LOGGER.info("The current field has been declared as acceptable by the program, thus it will be used as the next position");
			cleaningRobot.setPosition(nextPosition);
			break;
		}
	}

	/**
	 * The current method will take care of the back-off strategy execution in case the robot will encounter any obstacles.
	 * May call itself recursively for with a different parameter set in case of any necessity.
	 *  
	 * @param cleaningRobot - the cleaning robot
	 * @param room - the room to be cleaned
	 */
	private void initiateBackOffStrategy(CleaningRobot cleaningRobot, Room room){
		LOGGER.info("Starting the back off strategy...");
		for (List<ValidCommands> currentBackOffSequence : backOffCommandSequences){
			//go through each back-off sequence and execute it - in case one such sequence ends successfully, the current loop can be broken out of
			boolean backOffSuccessful = executeSelectedBackOffSequence(cleaningRobot, room,	currentBackOffSequence);
			if (backOffSuccessful){
				LOGGER.info("Last invoked back-off strategy ended successfully, resuming original instruction sequence excution...");
				return;
			}
		}
		//for none of the previous back-offs succeeding, it means that the robot will be stuck unfortunately
		throw new RobotStuckException(cleaningRobot);
	}

	/**
	 * A method used for executing a given back-off sequence.
	 * TODO NOTE: As unfortunately I did not read anything else about any restrictions related to the scenario
	 * when during the back-off sequence the robot battery gets consumed - so I made an assumption that the 
	 * commands from the back-off sequence will get executed unconditionally during the scenario in question 
	 * (as previously this has been checked during the execution of the command which triggered the back-off 
	 * sequence in question).
	 * 
	 * @param cleaningRobot - the cleaning robot involved in the process
	 * @param room - the room to be cleaned
	 * @param currentBackOffSequence - the back-off sequence selected for execution
	 * @return a boolean flag indicating whether a given back-off sequence was or not successful
	 */
	private boolean executeSelectedBackOffSequence(CleaningRobot cleaningRobot, Room room, List<ValidCommands> currentBackOffSequence) {
		for (ValidCommands backOffCommand : currentBackOffSequence) {
			ValidDirections currentDirection = cleaningRobot.getDirection();
			Long currentBatteryLevel = cleaningRobot.getBattery();
			Position currentPosition = cleaningRobot.getPosition();
			switch(backOffCommand){
			case TURN_LEFT:
				Long decrementAmount = 1L;
				//decrement the battery and set the new direction
				cleaningRobot.setBattery(currentBatteryLevel - decrementAmount);
				cleaningRobot.setDirection(turnLeft(currentDirection));
				break;
			case TURN_RIGHT:
				decrementAmount = 1L;
				//decrement the battery and set the new direction
				cleaningRobot.setBattery(currentBatteryLevel - decrementAmount);
				cleaningRobot.setDirection(turnRight(currentDirection));
				break;
			case ADVANCE:
				decrementAmount = 2L;
				//decrement the battery with the corresponding amount and mark the current position as visited
				cleaningRobot.setBattery(currentBatteryLevel - decrementAmount);
				cleaningRobot.addNewVisitedPosition(cleaningRobot.getPosition());
				//determine the new position for the robot
				Position newPosition = advanceToNewPosition(currentDirection, currentPosition);
				int rowNb = room.getNbOfRows();
				int columnNb = room.getNbOfColumns();
				boolean rowOK = (0L <= newPosition.getyRow()) && (newPosition.getyRow() < rowNb) ? true : false;
				boolean columnOK = (0L <= newPosition.getxCol()) && (newPosition.getxCol() < columnNb) ? true : false;
				//if the position is OK, then pick the element from the room map and see if it is acceptable
				if (rowOK && columnOK) {
					String mapContent = room.getMapElement(Long.valueOf(newPosition.getxCol()).intValue(), Long.valueOf(newPosition.getyRow()).intValue());
					if (mapContent.equals("C") || mapContent.equals("null")) {
						LOGGER.debug("An obstacle has been found in the map on position:" + newPosition + ", moving on to the next back-off sequence...");
						return false;
					} else {
						cleaningRobot.setPosition(newPosition);
					}
				} else {
					LOGGER.debug("The current position is located outside the map unfortunately:" + newPosition + ", moving on to the next-back-off sequence");
					return false;
				}
				break;
			case BACK:
				decrementAmount = 3L;
				//decrement the battery with the corresponding amount and mark the current position as visited
				cleaningRobot.setBattery(currentBatteryLevel - decrementAmount);
				cleaningRobot.addNewVisitedPosition(cleaningRobot.getPosition());
				//determine the new position for the robot
				newPosition = advanceToNewPosition(currentDirection, currentPosition);
				rowNb = room.getNbOfRows();
				columnNb = room.getNbOfColumns();
				rowOK = (0L <= newPosition.getyRow()) && (newPosition.getyRow() < rowNb) ? true : false;
				columnOK = (0L <= newPosition.getxCol()) && (newPosition.getxCol() < columnNb) ? true : false;
				//if the position is OK, then pick the element from the room map and see if it is acceptable
				if (rowOK && columnOK) {
					String mapContent = room.getMapElement(Long.valueOf(newPosition.getxCol()).intValue(), Long.valueOf(newPosition.getyRow()).intValue());
					if (mapContent.equals("C") || mapContent.equals("null")) {
						LOGGER.debug("An obstacle has been found in the map on position:" + newPosition + ", moving on to the next back-off sequence...");
						return false;
					} else {
						cleaningRobot.setPosition(newPosition);
					}
				} else {
					LOGGER.debug("The current position is located outside the map unfortunately:" + newPosition + ", moving on to the next-back-off sequence");
					return false;
				}
				break;
			default:
				LOGGER.debug("The CLEAN command is not considered to be part of the back-off sequence for now...");
				break;
			}
		}
		return true;
	}
	
	/**
	 * An auxiliary method for the robot advance control.
	 * 
	 * @param currentDirection - the direction of movement
	 * @param currentPosition - the position from where to move
	 * @return the new position for the robot in question
	 */
	private Position advanceToNewPosition(ValidDirections currentDirection, Position currentPosition){
		Position newPosition = null;
		Long xAbs = currentPosition.getxCol();
		Long yOrd = currentPosition.getyRow();
		switch (currentDirection){
		case EAST:
			xAbs += 1;
			break;
		case NORTH:
			yOrd -= 1;
			break;
		case SOUTH:
			yOrd += 1;
			break;
		case WEST:
			xAbs -= 1;
			break;
		}
		newPosition = new Position(xAbs, yOrd);
		LOGGER.info("The new position for the robot would be:" + newPosition.toString());
		return newPosition;
	}
	
	/**
	 * A second auxiliary method for the robot control, this time for the retreat operation
	 * 
	 * @param currentDirection - the direction of movement
	 * @param currentPosition - the position from where to move
	 * @return the new position for the robot in question
	 */
	private Position retreatToPreviousPosition(ValidDirections currentDirection, Position currentPosition){
		Position newPosition = null;
		Long xAbs = currentPosition.getxCol();
		Long yOrd = currentPosition.getyRow();
		switch (currentDirection){
		case EAST:
			xAbs -= 1;
			break;
		case NORTH:
			yOrd += 1;
			break;
		case SOUTH:
			yOrd -= 1;
			break;
		case WEST:
			xAbs += 1;
			break;
		}
		newPosition = new Position(xAbs, yOrd);
		LOGGER.info("The new position for the robot would be:" + newPosition.toString());
		return newPosition;		
	}

}