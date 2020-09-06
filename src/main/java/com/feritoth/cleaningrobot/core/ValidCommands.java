package com.feritoth.cleaningrobot.core;

public enum ValidCommands {
	
	TURN_LEFT("TL"), TURN_RIGHT("TR"), ADVANCE("A"), BACK("B"), CLEAN("C");
	
	private final String command;

	private ValidCommands(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

}