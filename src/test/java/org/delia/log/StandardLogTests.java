package org.delia.log;


import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class StandardLogTests {
	
	@Test
	public void testInfo() {
		StandardLogFactory factory = new StandardLogFactory();
		Log log = factory.create("abc");
		log.logDebug("a DEBUG msg");
		log.log("a INFO msg");
		log.logError("an ERROR msg");
	}

	@Test
	public void testError() {
		StandardLogFactory factory = new StandardLogFactory();
		Log log = factory.create("abc");
		log.setLevel(LogLevel.ERROR);
		log.logDebug("a DEBUG msg");
		log.log("a INFO msg");
		log.logError("an ERROR msg");
	}
	

	@Test
	public void testDebug() {
		StandardLogFactory factory = new StandardLogFactory();
		Log log = factory.create("abc");
		log.setLevel(LogLevel.DEBUG);
		log.logDebug("a DEBUG msg");
		log.log("a INFO msg");
		log.logError("an ERROR msg");
	}
	
	//---

	@Before
	public void init() {
	}
	

}
