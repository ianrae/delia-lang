package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaDao;
import org.delia.dataimport.DataImportService;
import org.delia.dataimport.ImportLevel;
import org.delia.db.DBType;
import org.delia.log.LogLevel;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.runner.inputfunction.LineObjIteratorImpl;
import org.delia.type.DValue;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class InputFieldTests  extends NewBDDBase {

	@Test
	public void testColumnNameWithSpaces() {
		String src = buildSrc("'CUST ID'");
		runImport(src);
		chkCustomer(1, "bob");
	}
	
	@Test(expected=DeliaException.class)
	public void testSyntheticFieldIntBad() {
		String src = buildSrcSynthetic("66", "extra int");
		runImport(src);
		chkCustomer(1, "bob");
	}
	@Test
	public void testSyntheticFieldInt() {
		String src = buildSrcSynthetic("value(66)", "extra int");
		runImport(src);
		chkCustomer(1, "bob");
		DValue dval = getCustomerExtra(1);
		assertEquals(66, dval.asInt());
	}
	@Test
	public void testSyntheticFieldIntNull() {
		String src = buildSrcSynthetic("value(null)", "extra int optional");
		runImport(src);
		chkCustomer(1, "bob");
		DValue dval = getCustomerExtra(1);
		assertEquals(null, dval);
	}
	@Test
	public void testSyntheticFieldLong() {
		String src = buildSrcSynthetic("value(66)", "extra long");
		runImport(src);
		chkCustomer(1, "bob");
		DValue dval = getCustomerExtra(1);
		assertEquals(66L, dval.asLong());
	}
	@Test
	public void testSyntheticFieldNumber() {
		String src = buildSrcSynthetic("value(66.2)", "extra number");
		runImport(src);
		chkCustomer(1, "bob");
		DValue dval = getCustomerExtra(1);
		assertEquals(66.2, dval.asNumber(), 0.0001);
	}
	@Test
	public void testSyntheticFieldBoolean() {
		String src = buildSrcSynthetic("value(true)", "extra boolean");
		runImport(src);
		chkCustomer(1, "bob");
		DValue dval = getCustomerExtra(1);
		assertEquals(true, dval.asBoolean());
	}
	@Test
	public void testSyntheticFieldString() {
		String src = buildSrcSynthetic("value('a dog')", "extra string");
		runImport(src);
		chkCustomer(1, "bob");
		DValue dval = getCustomerExtra(1);
		assertEquals("a dog", dval.asString());
	}
	@Test
	public void testSyntheticFieldDate() {
		String src = buildSrcSynthetic("value('2019')", "extra date");
		runImport(src);
		chkCustomer(1, "bob");
		DValue dval = getCustomerExtra(1);
		assertEquals("2019-01-01T00:00:00.000+0000", dval.asString());
		Date dt = dval.asDate();
	}
	
	
	private void runImport(String src) {
		this.delia.getLog().setLevel(LogLevel.DEBUG);
		delia.getLog().log(src);
		this.session = delia.beginSession(src);
		
		LineObjIterator lineObjIter = createIter(1);
		buildAndRun(lineObjIter);
		
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
//	private void createDelia(String tlang) {
//		String src = buildSrc(tlang);
//		this.delia.getLog().setLevel(LogLevel.DEBUG);
//		delia.getLog().log(src);
//		this.session = delia.beginSession(src);
//	}
	private String buildSrc(String columnName) {
		String src = String.format(" type Customer struct {id int primaryKey, wid int, name string } end");
		src += String.format(" input function foo(Customer c) { %s -> c.id, WID -> c.wid, NAME -> c.name using {  }}", columnName);
		src += String.format(" let var1 = 55");

		return src;
	}
	private String buildSrcSynthetic(String columnName, String extraField) {
		String src = String.format(" type Customer struct {id int primaryKey, wid int, name string, %s } end", extraField);
		src += String.format(" input function foo(Customer c) { ID -> c.id, WID -> c.wid, NAME -> c.name using {  }, %s -> c.extra }", columnName);
		src += String.format(" let var1 = 55");

		return src;
	}
	
	
	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}
	private InputFunctionResult buildAndRun(LineObjIterator lineObjIter) {
		DataImportService importSvc = new DataImportService(session, 0);

		InputFunctionResult result = importSvc.executeImport("foo", lineObjIter, ImportLevel.ONE);
		assertEquals(0, result.errors.size());
		assertEquals(1, result.numRowsProcessed);
		assertEquals(1, result.numRowsInserted);
		return result;
	}
	private InputFunctionResult buildAndRunFail(LineObjIterator lineObjIter) {
		DataImportService importSvc = new DataImportService(session, 0);

		InputFunctionResult result = importSvc.executeImport("foo", lineObjIter, ImportLevel.ONE);
		assertEquals(0, result.errors.size());
		assertEquals(1, result.numRowsProcessed);
		assertEquals(0, result.numRowsInserted);
		return result;
	}
	private void chkCustomer(Integer id, String expected) {
		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", id.toString());
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(expected, dval.asStruct().getField("name").asString());
		long n  = dao.count("Customer");
		assertEquals(1L, n);
	}
	private void chkNoCustomer(Integer id) {
		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", id.toString());
		assertEquals(true, res.ok);
		assertEquals(0, res.getAsDValueList().size());
	}
	private DValue getCustomerExtra(Integer id) {
		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", id.toString());
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		return dval.asStruct().getField("extra");
	}


	

	@Override
	public ZDBInterfaceFactory createForTest() {
		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
		return db;
	}

	private LineObjIterator createIter(int n) {
		return createIter(n, "bob");
	}
	private LineObjIterator createIter(int n, String nameStr) {
		List<LineObj> list = new ArrayList<>();
		for(int i = 0; i < n; i++) {
			list.add(this.createLineObj(i + 1, nameStr));
		}
		return new LineObjIteratorImpl(list);
	}
	private LineObj createLineObj(int id, String nameStr) {
		String[] ar = { "", "33", "bob" };
		ar[0] = String.format("%d", id);
		ar[2] = nameStr;

		LineObj lineObj = new LineObj(ar, 1);
		return lineObj;
	}
}
