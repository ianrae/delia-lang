package org.delia.log;

/**
 * Delia log levels.
 * 
 * @author Ian Rae
 *
 */
public enum LogLevel {
	OFF(0),
	ERROR(1),
	INFO(2),
	DEBUG(3);

	private int levelNum;

	LogLevel(int num) {
		this.levelNum = num;
	}
	public int getLevelNum() {
		return levelNum;
	}
}