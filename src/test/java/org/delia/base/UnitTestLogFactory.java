package org.delia.base;


import org.delia.log.Log;
import org.delia.log.SimpleLogFactory;

public class UnitTestLogFactory extends SimpleLogFactory {

	@Override
	public Log create(String name) {
		Log xlog = super.create(name);
		Log log = new UnitTestLog();
		log.setLevel(xlog.getLevel());
		return log;
	}

	@Override
	public Log create(Class<?> clazz) {
		Log xlog = super.create(clazz);
		Log log = new UnitTestLog();
		log.setLevel(xlog.getLevel());
		return log;
	}
}
