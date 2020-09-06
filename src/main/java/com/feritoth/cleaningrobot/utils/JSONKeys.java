package com.feritoth.cleaningrobot.utils;

public interface JSONKeys {
	
	/* The main keys inside the 2 JSON files - common zone */
	/* Primary - outer - keys */
	static String BATTERY_KEY = "battery";
	/* Secondary - inner - keys */
	static String X_KEY = "X";
	static String Y_KEY = "Y";
	static String DIRECTION_KEY = "facing";
	/* The main keys inside the 2 JSON files - differentiated zone */
	/* Keys only for the input */
	static String START_KEY = "start";
	static String MAP_KEY = "map";
	static String COMMAND_KEY = "commands";
	/* Keys only for the output */
	static String VISIT_KEY = "visited";
	static String CLEAN_KEY = "cleaned";
	static String FINAL_KEY = "final";

}