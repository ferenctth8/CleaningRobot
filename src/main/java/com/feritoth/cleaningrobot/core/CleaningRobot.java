package com.feritoth.cleaningrobot.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CleaningRobot {
	
	private Position position;
	private Long battery;
	private ValidDirections direction;
	private List<Position> visitedPositions;
	private List<Position> cleanedPositions;
	
	public CleaningRobot() {
		super();
		this.visitedPositions = new ArrayList<>();
		this.cleanedPositions = new ArrayList<>();
	}

	public CleaningRobot(Position position, Long battery, ValidDirections direction) {
		super();
		this.position = position;
		this.battery = battery;
		this.direction = direction;
		this.visitedPositions = new ArrayList<>();
		this.cleanedPositions = new ArrayList<>();
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Long getBattery() {
		return battery;
	}

	public void setBattery(Long battery) {
		this.battery = battery;
	}

	public ValidDirections getDirection() {
		return direction;
	}

	public void setDirection(ValidDirections direction) {
		this.direction = direction;
	}
	
	public List<Position> getVisitedPositions() {
		return Collections.unmodifiableList(visitedPositions);
	}

	public List<Position> getCleanedPositions() {
		return Collections.unmodifiableList(cleanedPositions);
	}
	
	public void addNewVisitedPosition(Position newPosition){
		if (!visitedPositions.contains(newPosition)){
			visitedPositions.add(0, newPosition);
		}
	}
	
	public void addNewCleanedPosition(Position newPosition){
		if (!cleanedPositions.contains(newPosition)){
			cleanedPositions.add(0, newPosition);
		}
	}

	@Override
	public String toString() {
		return "CleaningRobot [position=" + position + ", battery=" + battery
				+ ", direction=" + direction + ", visitedPositions="
				+ visitedPositions + ", cleanedPositions=" + cleanedPositions
				+ "]";
	}

}