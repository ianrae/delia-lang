package org.delia.log;

/**
 * Delia log.  Delia comes with a SimpleLog that uses System.out.
 * Most applications will implement their own implementation of this class
 * to connect up to their logging system (log4j, etc)
 * 
 * @author Ian Rae
 *
 */
public interface Log {
	void setLevel(LogLevel level);
	LogLevel getLevel();
	void log(String fmt, Object... args);
	void logDebug(String fmt, Object... args);
	void logError(String fmt, Object... args);
	void logException(LogLevel level, String message, Throwable ex);
}