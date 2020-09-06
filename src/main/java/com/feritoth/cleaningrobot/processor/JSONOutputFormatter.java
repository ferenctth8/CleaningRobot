package com.feritoth.cleaningrobot.processor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feritoth.cleaningrobot.core.CleaningRobot;
import com.feritoth.cleaningrobot.core.Position;
import com.feritoth.cleaningrobot.utils.JSONKeys;

/**
 * The current class is the last in the cleaning flow chain -
 * its processes the output of the cleaning flow procedure completed 
 * on the controller level. 
 * 
 * @author Ferenc Toth
 */
public class JSONOutputFormatter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JSONOutputFormatter.class);
	
	/**
	 * The current method is responsible for the generation of the final program output under a text file.
	 * 
	 * @param cleaningRobot - the final configuration of the cleaning robot in question
	 * @param outputFileLocation - the output file location
	 */
	@SuppressWarnings("unchecked")
	public void createCleaningProcedureOutput(CleaningRobot cleaningRobot, String outputFileLocation){
		//create the main JSONObject first
		JSONObject mainOutput = new JSONObject();
		//add there the attributes as follows
		//start with the visited positions
		mainOutput.put(JSONKeys.VISIT_KEY, createFormattedJSONArray(cleaningRobot.getVisitedPositions()));
		//as second field add all positions which have been cleaned by the robot
		mainOutput.put(JSONKeys.CLEAN_KEY, createFormattedJSONArray(cleaningRobot.getCleanedPositions()));
		//as third field add there the final position
		JSONObject finalPosition = new JSONObject();
		finalPosition.put(JSONKeys.X_KEY, cleaningRobot.getPosition().getxCol());
		finalPosition.put(JSONKeys.Y_KEY, cleaningRobot.getPosition().getyRow());
		finalPosition.put(JSONKeys.DIRECTION_KEY, cleaningRobot.getDirection().getDirection());
		mainOutput.put(JSONKeys.FINAL_KEY, finalPosition);
		//last, but not least, add the remaining battery value
		mainOutput.put(JSONKeys.BATTERY_KEY, cleaningRobot.getBattery());
		//create the final output
		LOGGER.info("The final JSON is:" + mainOutput.toJSONString());
		createFinalOutputFile(outputFileLocation, mainOutput.toJSONString());		
	}

	@SuppressWarnings("unchecked")
	private JSONArray createFormattedJSONArray(List<Position> initialPositions) {
		JSONArray finalPositions = new JSONArray();
		for (Position position : initialPositions){
			JSONObject newPosition = new JSONObject();
			newPosition.put(JSONKeys.X_KEY, position.getxCol());
			newPosition.put(JSONKeys.Y_KEY, position.getyRow());
			finalPositions.add(newPosition);
		}
		return finalPositions;
	}
	
	private void createFinalOutputFile(String outputFileLocation, String content) {
		try (FileWriter fileWriter = new FileWriter(outputFileLocation);
			 BufferedWriter bufWriter = new BufferedWriter(fileWriter)){
			 bufWriter.write(content);
			 bufWriter.flush();
		} catch (IOException e) {
			LOGGER.error("An error has been detected during the generation of the final output file:" + e.getMessage());
			throw new RuntimeException(e.getMessage());
		}		
	}

}