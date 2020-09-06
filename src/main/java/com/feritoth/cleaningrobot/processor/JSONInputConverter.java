package com.feritoth.cleaningrobot.processor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feritoth.cleaningrobot.core.CleaningRobot;
import com.feritoth.cleaningrobot.core.Position;
import com.feritoth.cleaningrobot.core.Room;
import com.feritoth.cleaningrobot.core.ValidCommands;
import com.feritoth.cleaningrobot.core.ValidDirections;
import com.feritoth.cleaningrobot.utils.JSONKeys;

/**
 * The first class in the robot workflow chain - processes the input JSON data
 * and creates the parameters needed for triggering the cleaning workflow.
 * 
 * @author Ferenc Toth
 */
public class JSONInputConverter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JSONInputConverter.class);
	
	/**
	 * The method responsible for preparing all the needed input elements.
	 *  
	 * @param inputFileLocation - the source file from where the input data will be taken.
	 * @param outputFileLocation - the output file to which the output will need to written
	 */
	@SuppressWarnings("unchecked")
	public void prepareProgramInput(String inputFileLocation, String outputFileLocation){
		/* First get the map for the robot */
		Map<String, Object> allInputData = pickJSONfields(inputFileLocation);
		String[][] roomMap = (String[][]) allInputData.get(JSONKeys.MAP_KEY);
		Room room = new Room(roomMap);
		LOGGER.info("The final room is:" + room.toString());
		/* Second, get the commands to be executed */
		List<ValidCommands> allInputCommands = (List<ValidCommands>) allInputData.get(JSONKeys.COMMAND_KEY);
		LOGGER.info("The commands are:" + allInputCommands);
		/* Third, get the battery and the position info for building the robot */
		Long initialBatteryCapacity = (Long) allInputData.get(JSONKeys.BATTERY_KEY);		
		ValidDirections initialDirection = (ValidDirections) allInputData.get(JSONKeys.DIRECTION_KEY);
		Long xCol = (Long) allInputData.get(JSONKeys.X_KEY);
		Long yRow = (Long) allInputData.get(JSONKeys.Y_KEY);
		Position initialPosition = new Position(xCol, yRow);
		LOGGER.info("The current room location where the robot starts is marked as:" + room.getMapElement(Long.valueOf(initialPosition.getxCol()).intValue(), Long.valueOf(initialPosition.getyRow()).intValue()));
		CleaningRobot newRobot = new CleaningRobot(initialPosition, initialBatteryCapacity, initialDirection);
		LOGGER.info("The initial parameters for the cleaning robot are:" + newRobot.toString());
		/* Power on the robot in order to start cleaning */
		RobotController newController = new RobotController();
		newController.activateCleaningProcedureForRobot(newRobot, room, allInputCommands, outputFileLocation);
	}

	/**
	 * The sample method responsible for reading from the input JSON file.
	 * 
	 * @param inputFileLocation the input file to be processed
	 * @return the map of all input objects extracted from the processed JSON file
	 */
	private Map<String, Object> pickJSONfields(String inputFileLocation){        
        try(FileReader fileReader = new FileReader(inputFileLocation);
        	BufferedReader bufReader = new BufferedReader(fileReader)){
        	//create a map where to store the given input data
        	Map<String, Object> allFileInput = new HashMap<>();
        	//JSON parser object to parse read file
            JSONParser jsonParser = new JSONParser();
        	//Get next the objects from the parsed file
            JSONObject robotInput = (JSONObject) jsonParser.parse(bufReader);
            //store them inside an array of similar size to that of the input JSON keys
            //the input map - needs a little pre-processing before effective storage
            JSONArray mapString = (JSONArray) robotInput.get(JSONKeys.MAP_KEY);
            String[][] finalMap = preprocessInitialMapValue(mapString);
            allFileInput.put(JSONKeys.MAP_KEY, finalMap);
            //the command list - again a little pre-processing will be required here as well
            JSONArray commandList = (JSONArray) robotInput.get(JSONKeys.COMMAND_KEY);
            List<ValidCommands> finalCommands = preprocessCommands(commandList);
            allFileInput.put(JSONKeys.COMMAND_KEY, finalCommands);
            //the initial battery capacity
            allFileInput.put(JSONKeys.BATTERY_KEY, robotInput.get(JSONKeys.BATTERY_KEY));
            //the start position and the initial direction
            String directionAcronym = (String)((JSONObject)robotInput.get(JSONKeys.START_KEY)).get(JSONKeys.DIRECTION_KEY);
            allFileInput.put(JSONKeys.DIRECTION_KEY, returnValidDirection(directionAcronym));
            Long absCoordinate = (Long)((JSONObject)robotInput.get(JSONKeys.START_KEY)).get(JSONKeys.X_KEY);
            allFileInput.put(JSONKeys.X_KEY, absCoordinate);
            Long ordCoordinate = (Long)((JSONObject)robotInput.get(JSONKeys.START_KEY)).get(JSONKeys.Y_KEY);
            allFileInput.put(JSONKeys.Y_KEY, ordCoordinate);
            //return the given map
            return allFileInput;
        } catch (FileNotFoundException e) {
			LOGGER.error("Cannot find any file on the specified location:" + inputFileLocation);
        	throw new RuntimeException("No file located on the given position:" + inputFileLocation + "\n" + e.getMessage());
		} catch (IOException e) {
			LOGGER.error("Another type of IO problem detected for the file on position:" + inputFileLocation);
        	throw new RuntimeException("IO problem detected for the file on position:" + inputFileLocation + "\n" + e.getMessage());
		} catch (ParseException e) {
			LOGGER.error("Parsing problem detected for the file on position:" + inputFileLocation);
        	throw new RuntimeException("Parsing problem detected for the file on position:" + inputFileLocation + "\n" + e.getMessage());
		}				
	}

	/**
	 * The method responsible for the pre-processing of the given input map
	 *  
	 * @param mapString - the initial map given under raw form
	 * @return the converted map, stored as a String matrix
	 */
	@SuppressWarnings("unchecked")
	private String[][] preprocessInitialMapValue(JSONArray mapString) {
		String[][] finalMap = new String[mapString.size()][];		
		for (int i = 0; i < mapString.size(); i++) {
			JSONArray internalArray = (JSONArray) mapString.get(i);
			finalMap[i] = (String[]) internalArray.toArray(new String[internalArray.size()]);
		}
		return finalMap;
	}
	
	/**
	 * The method responsible for the command list pre-processing
	 * 
	 * @param commandList - the initial command list
	 * @return the converted command list
	 */
	private List<ValidCommands> preprocessCommands(JSONArray commandList) {
		List<ValidCommands> inputCommandList = new ArrayList<>();
		for (int i = 0; i < commandList.size(); i++){
			String commandAcronym = (String) commandList.get(i);
			switch (commandAcronym){
			case "TR":
				inputCommandList.add(ValidCommands.TURN_RIGHT);
			    break;
			case "TL":
				inputCommandList.add(ValidCommands.TURN_LEFT);
				break;
			case "A":
				inputCommandList.add(ValidCommands.ADVANCE);
				break;
			case "B":
				inputCommandList.add(ValidCommands.BACK);
				break;
			case "C":
				inputCommandList.add(ValidCommands.CLEAN);
				break;
			}
		}
		return inputCommandList;
	}
	
	/**
	 * A method for returning the direction based on the given acronym.
	 * 
	 * @param directionAcronym - the acronym of the direction to be found
	 * @return the direction corresponding to the given acronym
	 */
	private ValidDirections returnValidDirection(String directionAcronym) {
		switch(directionAcronym){
		case "E":
			return ValidDirections.EAST;
		case "N":
			return ValidDirections.NORTH;
		case "S":
			return ValidDirections.SOUTH;
		case "W":
			return ValidDirections.WEST;
		default:
			return null;
		}
	}

}