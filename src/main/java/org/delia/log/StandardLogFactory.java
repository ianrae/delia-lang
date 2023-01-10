package org.delia.log;

import java.util.List;

public class StandardLogFactory implements LogFactory {
	private LogLevel defaultLevel = LogLevel.INFO;
	private LogLevelMapBuilder levelMapBuilder = new LogLevelMapBuilder();

	@Override
	public DeliaLog create(String name) {
		DeliaLog log = new StandardLog(name);
		log.setLevel(calcLevel(name));
		return log;
	}

	@Override
	public DeliaLog create(Class<?> clazz) {
		DeliaLog log = new StandardLog(clazz);
		log.setLevel(calcLevel(clazz.getName()));
		return log;
	}

	@Override
	public void setDefaultLogLevel(LogLevel level) {
		this.defaultLevel = level;
	}
	@Override
	public LogLevel getDefaultLogLevel() {
		return defaultLevel;
	}

	@Override
	public void setLogLevelMap(List<String> levelMapList) {
		levelMapBuilder.buildMap(levelMapList);
	}
	
	protected LogLevel calcLevel(String name) {
		return levelMapBuilder.calcLevel(name, defaultLevel);
	}


}
