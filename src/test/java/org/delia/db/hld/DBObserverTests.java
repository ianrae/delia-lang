package org.delia.db.hld;


import static org.junit.Assert.*;

import org.delia.hld.HLDQueryStatement;
import org.delia.zdb.DBObserverAdapter;
import org.delia.zdb.DBObserverFactory;
import org.delia.zdb.ZDBExecutor;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class DBObserverTests extends NewHLSTestBase {
	
	public static class MyObsFactory implements DBObserverFactory {
		public DBObserverAdapter observer;

		@Override
		public ZDBExecutor createObserver(ZDBExecutor actual) {
			observer = new DBObserverAdapter(actual);
			return observer;
		}
	}
	
	@Test
	public void testHLDField() {
		String src = "let x = Flight[15]";
		HLDQueryStatement hld = buildFromSrc(src, 0); 
		MyObsFactory observer = new MyObsFactory();
		delia.getOptions().dbObserverFactory = observer;
		chkFullSql(hld, "SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1=?", "15");
		
		assertEquals(22, observer.observer.statements.size());
	}	



	//-------------------------

	@Before
	public void init() {
		//createDao();
	}


}
