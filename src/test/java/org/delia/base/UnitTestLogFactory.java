package org.delia.base;


import org.delia.log.DeliaLog;
import org.delia.log.SimpleLogFactory;

public class UnitTestLogFactory extends SimpleLogFactory {

	@Override
	public DeliaLog create(String name) {
		DeliaLog xlog = super.create(name);
		DeliaLog log = new UnitTestLog();
		log.setLevel(xlog.getLevel());
		return log;
	}

	@Override
	public DeliaLog create(Class<?> clazz) {
		DeliaLog xlog = super.create(clazz);
		DeliaLog log = new UnitTestLog();
		log.setLevel(xlog.getLevel());
		return log;
	}
}
