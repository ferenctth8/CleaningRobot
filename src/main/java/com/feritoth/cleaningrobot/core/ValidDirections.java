package com.feritoth.cleaningrobot.core;

public enum ValidDirections {	
	
	NORTH("N"),EAST("E"),SOUTH("S"),WEST("W");
	
	private final String direction;
	
	private ValidDirections(String direction){
		this.direction = direction;
	}

	public String getDirection() {
		return direction;
	}
	
}