package com.feritoth.cleaningrobot;

import com.feritoth.cleaningrobot.processor.JSONInputConverter;

public class RobotAppLauncher {
	
    public static void main( String[] args ) {
    	String inputFileLocation = args[0];
    	String outputFileLocation = args[1];
        JSONInputConverter newInputConverter = new JSONInputConverter();        
        newInputConverter.prepareProgramInput(inputFileLocation, outputFileLocation);
    }
    
}