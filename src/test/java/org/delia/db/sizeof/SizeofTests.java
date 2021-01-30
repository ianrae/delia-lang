package org.delia.db.sizeof;


import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.assoc.CreateNewDatIdVisitor;
import org.delia.base.UnitTestLog;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.log.Log;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.zdb.mem.MemDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * rules are built in RuleFuncFactory
 * @author Ian Rae
 *
 */
public class SizeofTests  { 
	
	@Test
	public void testOK() {
		String src = "let x = Flight[15]";
		execute(src);
	}	

	@Test(expected=DeliaException.class)
	public void testFail() {
		String src = "insert Flight {field1: 3, field2: 256 }";
		execute(src);
	}	
	


	//-------------------------
	private boolean addSizeof = true;
//	private boolean srcSimpleTypes;
	protected Delia delia;
	protected DeliaSession session;
	protected Log log = new UnitTestLog();

	@Before
	public void init() {
	}

	protected String buildSrc() {
		String s = addSizeof ? "wid.sizeof(8)" : "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 int } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: 10 %s}", s);
		src += String.format("\n insert Flight {field1: 2, field2: 20 %s}", s);
		return src;
	}

//	private void createNewDelia() {
//		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
//		this.delia = DeliaBuilder.withConnection(info).build();
//	}
	
	protected DeliaSessionImpl execute(String src) {
		String initialSrc = buildSrc();
		log.log("initial: " + initialSrc);
		
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(initialSrc);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		this.session = dao.getMostRecentSession();
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		return sessimpl;
	}
	
	protected DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		this.delia = DeliaBuilder.withConnection(info).build();
		MemDBInterfaceFactory memDBinterface = (MemDBInterfaceFactory) delia.getDBInterface();
		memDBinterface.createSingleMemDB();
		CreateNewDatIdVisitor.hackFlag = true;
		
//		if (flipAssocTbl) {
//			createTable(memDBinterface, "AddressCustomerDat1");
//		} else {
//			createTable(memDBinterface, "CustomerAddressDat1");
//		}
		
		return new DeliaGenericDao(delia);
	}

}
