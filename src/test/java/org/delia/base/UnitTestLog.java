package org.delia.base;

import org.delia.log.LogLevel;
import org.delia.log.SimpleLog;

public class UnitTestLog extends SimpleLog {
	//change flag to true to disable logging in unit tests (so they run faster)
	public static boolean disableLogging = true;
	public static LogLevel defaultLogLevel = LogLevel.INFO;
	
	public UnitTestLog() {
		if (disableLogging) {
			setLevel(LogLevel.OFF);
		} else {
			setLevel(defaultLogLevel);
		}
	}
}