package org.delia.log;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * Logs to slf4j
 * 
 * @author Ian Rae
 *
 */
public class StandardLog implements Log {
	private LogLevel level = LogLevel.INFO;
	private Logger logger;
	private String logName;
	private Class<?> logNameClazz;

	public StandardLog(String name) {
		this.logName = name;
		logger = LoggerFactory.getLogger(name);
		//		ch.qos.logback.classic.Logger appLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.dnal");

	}
	public StandardLog(Class<?> clazz) {
		this.logNameClazz = clazz;
		logger = LoggerFactory.getLogger(clazz);
	}

	@Override
	public void log(String fmt, Object... args) {
		if (exceeds(LogLevel.INFO)) {
			doLog(LogLevel.INFO, fmt, args);
		}
	}

	@Override
	public void setLevel(LogLevel level) {
		this.level = level;
		//https://stackoverflow.com/questions/21368757/sl4j-and-logback-is-it-possible-to-programmatically-set-the-logging-level-for/21601319
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger logbackLogger = null;
		if (logName != null) {
			logbackLogger = loggerContext.getLogger(logName);
		} else {
			logbackLogger = loggerContext.getLogger(logNameClazz);
		}

		switch(level) {
		case DEBUG:
			logbackLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);		
			break;
		case ERROR:
			logbackLogger.setLevel(ch.qos.logback.classic.Level.ERROR);		
			break;
		case INFO:
			logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);		
			break;
		case OFF:
			logbackLogger.setLevel(ch.qos.logback.classic.Level.OFF);		
			break;
		}
	}
	@Override
	public LogLevel getLevel() {
		return level;
	}

	@Override
	public void logDebug(String fmt, Object... args) {
		if (exceeds(LogLevel.DEBUG)) {
			doLog(LogLevel.DEBUG, fmt, args);
		}
	}

	@Override
	public void logError(String fmt, Object... args) {
		if (exceeds(LogLevel.ERROR)) {
			doLog(LogLevel.ERROR, fmt, args);
		}
	}
	protected void doLog(LogLevel level, String fmt, Object... args) {
		String msg = (args.length == 0) ? fmt : String.format(fmt, args);
		switch(level) {
		case DEBUG:
			logger.debug(msg);
			break;
		case INFO:
			logger.info(msg);
			break;
		case ERROR:
			logger.error(msg);
			break;
		default:
			break;
		}
	}

	protected boolean exceeds(LogLevel info) {
		return (level.getLevelNum() >= info.getLevelNum());
	}

	@Override
	public void logException(LogLevel targetLevel, String message, Throwable ex) {
		if (exceeds(targetLevel)) {
			log("EXCEPTION: %s", message);
			//ex.printStackTrace();
		}
	}

}