package org.delia.db.newhls;


import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.assoc.CreateNewDatIdVisitor;
import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.After;

/**
 * 
 * 
 * @author Ian Rae
 *
 */
public class NewHLSTestBase extends BDDBase {
	
	protected HLDManager mgr;


	protected HLDQuery buildFromSrc(String src, int expectedJoins) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		
		mgr = new HLDManager(this.session.getExecutionContext().registry, delia.getFactoryService(), this.session.getDatIdMap());
		HLDQuery hld = mgr.fullBuildQuery(queryExp);
		log.log(hld.toString());
		assertEquals(expectedJoins, hld.joinL.size());
		return hld;
	}
	protected void chkRawSql(HLDQuery hld, String expected) {
		String sql = mgr.generateRawSql(hld);
		log.log(sql);
		assertEquals(expected, sql);
	}
	protected void chkFullSql(HLDQuery hld, String expected, String...args) {
		SqlStatement stm = mgr.generateSql(hld);
		chkStm(stm, expected, args);
	}
	protected void chkStm(SqlStatement stm, String expected, String... args) {
		log.log(stm.sql);
		assertEquals(expected, stm.sql);
		assertEquals(args.length, stm.paramL.size());
		for(int i = 0; i < args.length; i++) {
			String arg = args[i];
			DValue dval = stm.paramL.get(i);
			assertEquals(arg, dval.asString());
		}
	}


	
	
	protected QueryExp compileQuery(String src) {
		LetStatementExp letStatement = compileQueryToLetStatement(src);
		
		QueryExp queryExp = (QueryExp) letStatement.value;
		return queryExp;
	}
	
	protected LetStatementExp compileQueryToLetStatement(String src) {
		String initialSrc;
		if  (useCustomerManyToManySrc) {
			initialSrc = buildCustomerNNSrc();
		} else if (useCustomer11Src) {
			initialSrc = buildCustomer11Src();
		} else if (useCustomer11OtherWaySrc) {
			initialSrc = buildCustomer11OtherWasySrc();
		} else if (useCustomer1NSrc) {
			initialSrc = buildCustomer1NSrc();
		} else if (useCustomer1NOtherWaySrc) {
			initialSrc = buildCustomer1NOtherWaySrc();
		} else {
			initialSrc = buildSrc();
		}
		log.log("initial: " + initialSrc);
		
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(initialSrc);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		this.session = dao.getMostRecentSession();
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		LetStatementExp letStatement = findLet(sessimpl);
		return letStatement;
	}

	protected LetStatementExp findLet(DeliaSession session) {
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		for(Exp exp: sessimpl.mostRecentContinueExpL) {
			if (exp instanceof LetStatementExp) {
				return (LetStatementExp) exp;
			}
		}
		return null;
	}

	//---
	protected Delia delia;
	protected DeliaSession session;
	protected boolean useCustomerManyToManySrc = false;
	protected boolean useCustomer11Src = false;
	protected boolean useCustomer11OtherWaySrc = false;
	protected boolean useCustomer1NSrc = false;
	protected boolean useCustomer1NOtherWaySrc = false;
	protected boolean insertSomeRecords = false;
	protected boolean flipAssocTbl = false; //mosts tests assume CustomerAddressAssoc
	
	//---

	protected DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		this.delia = DeliaBuilder.withConnection(info).build();
		MemZDBInterfaceFactory memDBinterface = (MemZDBInterfaceFactory) delia.getDBInterface();
		memDBinterface.createSingleMemDB();
		CreateNewDatIdVisitor.hackFlag = true;
		
//		if (flipAssocTbl) {
//			createTable(memDBinterface, "AddressCustomerDat1");
//		} else {
//			createTable(memDBinterface, "CustomerAddressDat1");
//		}
		
		return new DeliaGenericDao(delia);
	}
	
//	protected void createTable(MemZDBInterfaceFactory db, String tableName) {
//		TestCreatorHelper.createTable(db, tableName);
//	}

	protected String buildSrc() {
		String src = "type Flight struct {field1 int primaryKey, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
	protected String buildCustomerNNSrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address many optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  many optional } end";
		
		if (insertSomeRecords) {
			src += "\n insert Customer {cid:55, x:10}";
			src += "\n insert Customer {cid:56, x:11}";
			src += "\n insert Address {id:100, y:20, cust:55}";
			src += "\n insert Address {id:101, y:20, cust:55}";
		}
		return src;
	}
	protected String buildCustomer11Src() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address one optional parent  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  one optional } end";
		return src;
	}
	protected String buildCustomer11OtherWasySrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address one optional   } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  one optional parent} end";
		return src;
	}
	protected String buildCustomer1NSrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address many optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  one optional } end";
		return src;
	}
	protected String buildCustomer1NOtherWaySrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address one optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer many optional } end";
		return src;
	}

	@Override
	public ZDBInterfaceFactory createForTest() {
		return null;
//		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
//		db.createSingleMemDB();
//		return db;
	}
	

	@After
	public void shutdown() {
//		System.out.println("sdfffffffffffffffffffffffffffffff");
//		TableExistenceServiceImpl.hackYesFlag = false;
		CreateNewDatIdVisitor.hackFlag = false;
	}
}
