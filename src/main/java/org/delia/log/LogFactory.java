package org.delia.log;

import java.util.List;

/**
 * Factory to create log instances
 * @author Ian Rae
 *
 */
public interface LogFactory {
	Log create(String name);
	Log create(Class<?> clazz);
	void setDefaultLogLevel(LogLevel level);
	LogLevel getDefaultLogLevel();
	void setLogLevelMap(List<String> levelMapList);
}
