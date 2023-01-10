package org.delia.log;

import java.util.List;

/**
 * Factory to create log instances
 * @author Ian Rae
 *
 */
public interface LogFactory {
	DeliaLog create(String name);
	DeliaLog create(Class<?> clazz);
	void setDefaultLogLevel(LogLevel level);
	LogLevel getDefaultLogLevel();
	void setLogLevelMap(List<String> levelMapList);
}
