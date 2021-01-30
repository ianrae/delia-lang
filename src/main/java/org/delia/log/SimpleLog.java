package org.delia.log;

/**
 * Logs to System.out
 * 
 * @author Ian Rae
 *
 */
public class SimpleLog implements Log {
	private LogLevel level = LogLevel.INFO;
	
	public SimpleLog() {
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
		String prefix = createPrefix(level); 
		if (args.length == 0) {
			System.out.println(prefix + fmt);
		} else {
			String s = String.format(fmt, args);
			System.out.println(prefix + s);
		}
	}
	protected String createPrefix(LogLevel info) {
		long threadId = Thread.currentThread().getId();
		String prefix = String.format("[%d] ", threadId);
		return prefix;
	}

	protected boolean exceeds(LogLevel info) {
		return (level.getLevelNum() >= info.getLevelNum());
	}

	@Override
	public void logException(LogLevel targetLevel, String message, Throwable ex) {
		if (exceeds(targetLevel)) {
			log("EXCEPTION: %s", message);
			ex.printStackTrace();
		}
	}

	@Override
	public boolean isLevelEnabled(LogLevel level) {
		return exceeds(level);
	}

}