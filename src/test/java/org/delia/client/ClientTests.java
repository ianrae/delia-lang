package org.delia.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.api.Delia;
import org.delia.api.DeliaFactory;
import org.delia.api.DeliaOptions;
import org.delia.api.DeliaSession;
import org.delia.base.DBHelper;
import org.delia.base.DBTestHelper;
import org.delia.base.UnitTestLog;
import org.delia.base.UnitTestLogFactory;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.schema.MigrationPlan;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.delia.zdb.DBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class ClientTests {
	
	public static class DeliaClient {
		private Delia delia;
		private DeliaSession sess = null;
		private DBInterfaceFactory dbInterface ;
		private FactoryService factorySvc;
		
//		public static DBInterface forcedDBInterface; //for injecting other dbinterfaces

		public DeliaClient(DBInterfaceFactory dbInterfaceParam) {
//			this.dbInterface = forcedDBInterface == null ? dbInterfaceParam : forcedDBInterface;
			this.dbInterface = dbInterfaceParam;
			Log log = new UnitTestLog(); //TODO fix later
			ErrorTracker et = new SimpleErrorTracker(log);
			this.factorySvc = new FactoryServiceImpl(log, et, new UnitTestLogFactory());
			delia = DeliaFactory.create(dbInterface, new UnitTestLog(), factorySvc);
		}
		
		public DeliaOptions getOptions() {
			return delia.getOptions();
		}

		public ResultValue beginExecution(String src) {
			//TODO: ensure sess is null
			sess = delia.beginSession(src);
			return sess.getFinalResult();
		}
		public ResultValue executeMPlan(String src, MigrationPlan plan) {
			//TODO: ensure sess is null
			sess = delia.executeMigrationPlan(src, plan);
			return sess.getFinalResult();
		}

		public ResultValue continueExecution(String src) {
			ResultValue res = delia.continueExecution(src, sess);
			return res;
		}
		
		public DeliaSession getSession() {
			return sess;
		}

		public FactoryService getFactorySvc() {
			return factorySvc;
		}
	}
	
	public static class InsertBuilder {

		private String typeName;
		public List<String> fieldL = new ArrayList<>();
		public List<Object> valueL = new ArrayList<>();

		public InsertBuilder(String typeName) {
			this.typeName = typeName;
		}
		
		public InsertBuilder add(String fieldName, Object val) {
			fieldL.add(fieldName);
			valueL.add(val);
			return this;
		}
		
		public String end() {
			StringJoiner joiner = new StringJoiner(",");
			int i = 0;
			for(String field: fieldL) {
				Object val = valueL.get(i);
				String valStr = val == null ? "null" : val.toString();
				String s = String.format("%s: %s", field, valStr);
				joiner.add(s);
				i++;
			}
			
			String src = String.format("insert %s {%s}", typeName, joiner.toString());
			return src;
		}
		
	}
	
	static public class SourceBuilder {
		
		public InsertBuilder beginInsert(String typeName) {
			return new InsertBuilder(typeName);
		}
		
		public static InsertBuilder insert(String typeName) {
			SourceBuilder self = new SourceBuilder();
			return self.beginInsert(typeName);
		}
		
	}

	@Test
	public void test() {
		createOneToMany("OO");
		
		execStatement("insert Customer { id:44 }");
		queryAndChkNull("Customer", 44, "addr");
	}
	
	@Test
	public void testIB() {
		createOneToMany("OO");
		
		String src = SourceBuilder.insert("Customer").add("id", 44).end();
		execStatement(src);
		queryAndChkNull("Customer", 44, "addr");
	}
	
	// --
	private DeliaClient client;
//	private DBSession sess = null;
	private boolean addIdFlag;
	private DBInterfaceFactory dbInterface ;
	private int nextVarNum = 1;

	public void xinit() {
		addIdFlag = true;
		Log log = new UnitTestLog();
		FactoryService factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
		dbInterface = DBTestHelper.createMEMDb(factorySvc);
		client = new DeliaClient(dbInterface);
		DBHelper.createTable(dbInterface, "Address"); //!! fake schema
		DBHelper.createTable(dbInterface, "Customer"); //!! fake schema
	}
	
	@Before
	public void init() {
		xinit();
	}
	
	private void createOneToMany(String mo) {
		String src = null;
		if (mo.equals("OO")) {
			src = createTypeSrc("Customer", "relation addr Address many optional");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		} else if (mo.equals("MO")) {
			src = createTypeSrc("Customer", "relation addr Address many optional");
			src += createTypeSrc("Address", "relation cust Customer one");
		} else if (mo.equals("OM")) {
			src = createTypeSrc("Customer", "relation addr Address many");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		} else if (mo.equals("OneWay")) {
			src = createTypeSrc("Customer", "");
			src += createTypeSrc("Address", "relation cust Customer one");
		} else if (mo.equals("OneWayO")) {
			src = createTypeSrc("Customer", "");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		}
		execTypeStatement(src);
	}

	private String createTypeSrc(String type, String relField) {
		String sid = addIdFlag ? String.format(" id int unique") : "";
		relField = sid.isEmpty() ? relField : ", " + relField;
		String src = String.format("type %s struct { %s %s} end", type, sid, relField);
		src += "\n";
		return src;
	}
	private ResultValue execTypeStatement(String src) {
//		if (sess != null) {
//			log("rebuilding..");
//		}
//		sess = client.beginExecution(src);
		ResultValue res = client.beginExecution(src);
		chkResOK(res);
		return res;
	}
	private void createTypeFail(String initialSrc, String type, String rel, String errId) {
		String sid = addIdFlag ? String.format(" id int unique") : "";
		String src = String.format("type %s struct { %s %s }  end", type, sid, rel);
		execTypeStatementFail(initialSrc + src, errId);
	}
	private void execTypeStatementFail(String src, String errId) {
		boolean pass = false;
		try {
			execTypeStatement(src);
			pass = true;
		} catch (DeliaException e) {
			DeliaError err = e.getLastError();
			assertEquals(true, err.getId().contains(errId));
		}
		assertEquals(false, pass);
	}
	private ResultValue execStatement(String src) {
//		assertEquals(true, sess != null);
		ResultValue res = client.continueExecution(src);
		chkResOK(res);
		return res;
	}

	private void chkResOK(ResultValue res) {
		assertEquals(true, res.ok);
		assertEquals(true, res.errors.isEmpty());
	}
	private void log(String s) {
		System.out.println(s);
	}

	private void chkInt(ResultValue res, String fieldName, int expected) {
		DValue dval = res.getAsDValue();
		assertEquals(expected, dval.asStruct().getField(fieldName).asInt());
	}

	private String buildLet() {
		return String.format("let x%d = ", nextVarNum++);
	}

	private void chkNullField(ResultValue res, String fieldName) {
		DValue dval = res.getAsDValue();
		assertEquals(null, dval.asStruct().getField(fieldName));
	}

	private ResultValue queryAndChk(String typeName, int expected) {
		ResultValue res = execStatement(String.format("%s %s[%d]", buildLet(), typeName, expected));
		chkInt(res, "id", expected);
	//	chkNullField(res, "addr");
		return res;
	}


	private ResultValue queryAndChkNull(String typeName, int id, String fieldName) {
		ResultValue res = queryAndChk(typeName, id);
		chkNullField(res, fieldName);
		return res;
	}
}
