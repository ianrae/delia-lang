package org.delia.parser;

import org.delia.base.UnitTestLog;
import org.delia.compiler.DeliaCompiler;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;

public class ParserTestBase {
	
	
	// --
	protected DeliaCompiler initCompiler()  {
		Log log = new UnitTestLog();
		ErrorTracker et = new SimpleErrorTracker(log);
		FactoryService factorySvc = new FactoryServiceImpl(log, et);
		DeliaCompiler compiler = new DeliaCompiler(factorySvc);
		return compiler;
	}
}
