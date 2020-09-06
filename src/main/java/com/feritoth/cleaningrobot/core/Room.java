package com.feritoth.cleaningrobot.core;

import java.util.Arrays;

public class Room {
	
	private String[][] map;

	public Room(String[][] map) {
		super();
		this.map = map;
	}
	
	public String getMapElement(int xCol, int yRow){
		return map[yRow][xCol];
	}
	
	public int getNbOfColumns(){
		return map.length;
	}
	
	public int getNbOfRows(){
		return map[0].length;
	}

	@Override
	public String toString() {
		return "Room [map=" + Arrays.toString(map) + "]";
	}

}