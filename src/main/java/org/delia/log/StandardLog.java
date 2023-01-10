package org.delia.log;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Logs to slf4j
 * 
 * @author Ian Rae
 *
 */
public class StandardLog implements DeliaLog {
	private LogLevel logLevel = LogLevel.INFO;
	private Logger logger;
	private String logName;
	private Class<?> logNameClazz;

	public StandardLog(String name) {
		this.logName = name;
		logger = LoggerFactory.getLogger(name);
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
		this.logLevel = level;
		//this only sets logLevel field, not the underlying SLF4J logger's level. SLF4J doesn't support setLevel so if you want to
		//programmatically set its log level you need to create a sub-class of StandardLog (or Log) and implement it using
		//the actual logger library. For example:
//		//https://stackoverflow.com/questions/21368757/sl4j-and-logback-is-it-possible-to-programmatically-set-the-logging-level-for/21601319
	}
	@Override
	public LogLevel getLevel() {
		return logLevel;
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
		return (logLevel.getLevelNum() >= info.getLevelNum());
	}

	@Override
	public void logException(LogLevel targetLevel, String message, Throwable ex) {
		if (exceeds(targetLevel)) {
			log("EXCEPTION: %s", message);
			//ex.printStackTrace();
		}
	}
	@Override
	public boolean isLevelEnabled(LogLevel level) {
		return exceeds(level);
	}

}