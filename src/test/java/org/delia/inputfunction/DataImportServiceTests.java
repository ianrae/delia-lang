package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaDao;
import org.delia.dataimport.DataImportService;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.log.LogLevel;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class DataImportServiceTests  extends NewBDDBase {

	@Test
	public void test1() {
		createDelia("'able'");
		DataImportService importSvc = new DataImportService(delia, session);

		LineObjIterator lineObjIter = createIter(1, true);
		InputFunctionResult result = importSvc.buildAndRun("foo", lineObjIter);
		assertEquals(0, result.totalErrorL.size());
		assertEquals(1, result.numRowsProcessed);
		assertEquals(1, result.numDValuesProcessed);

		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", "1");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("able", dval.asStruct().getField("name").asString());

		long n  = dao.count("Customer");
		assertEquals(1L, n);
	}
	
	@Test
	public void test2() {
		createDelia("var1");
		DataImportService importSvc = new DataImportService(delia, session);

		LineObjIterator lineObjIter = createIter(1, true);
		InputFunctionResult result = importSvc.buildAndRun("foo", lineObjIter);
		assertEquals(0, result.totalErrorL.size());
		assertEquals(1, result.numRowsProcessed);
		assertEquals(1, result.numDValuesProcessed);

		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", "1");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("55", dval.asStruct().getField("name").asString());

		long n  = dao.count("Customer");
		assertEquals(1L, n);
	}


	// --
	//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;

	@Before
	public void init() {
		DeliaDao dao = this.createDao();
		this.delia = dao.getDelia();
	}
	private void createDelia(String tlang) {
		String src = buildSrc(tlang);
		this.delia.getLog().setLevel(LogLevel.DEBUG);
		delia.getLog().log(src);
		this.session = delia.beginSession(src);
	}
	private String buildSrc(String tlang) {
		String src = String.format(" type Customer struct {id int primaryKey, wid int, name string } end");
		src += String.format(" input function foo(Customer c) { ID -> c.id, WID -> c.wid, NAME -> c.name using { %s }}", tlang);
		src += String.format(" let var1 = 55");

		return src;
	}
	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

	private LineObjIterator createIter(int n, boolean goodObj) {
		List<LineObj> list = new ArrayList<>();
		for(int i = 0; i < n; i++) {
			list.add(this.createLineObj(i + 1, goodObj));
		}
		return new LineObjIterator(list);
	}
	private LineObj createLineObj(int id, boolean goodObj) {
		String[] ar = { "", "33","bob" };
		ar[0] = String.format("%d", id);
		if (! goodObj) {
			ar[1] = "bbb"; //not an int
		}

		LineObj lineObj = new LineObj(ar, 1);
		return lineObj;
	}
}
