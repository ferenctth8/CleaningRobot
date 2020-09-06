package com.feritoth.cleaningrobot.core;

public class Position {
	
	private long xCol;
	private long yRow;
	
	public Position() {
		super();
	}

	public Position(long xCol, long yRow) {
		super();
		this.xCol = xCol;
		this.yRow = yRow;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (xCol ^ (xCol >>> 32));
		result = prime * result + (int) (yRow ^ (yRow >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (xCol != other.xCol)
			return false;
		if (yRow != other.yRow)
			return false;
		return true;
	}

	public long getxCol() {
		return xCol;
	}

	public void setxCol(long xCol) {
		this.xCol = xCol;
	}

	public long getyRow() {
		return yRow;
	}

	public void setyRow(long yRow) {
		this.yRow = yRow;
	}

	@Override
	public String toString() {
		return "Position (" + xCol + ", " + yRow + ")";
	}
	
}