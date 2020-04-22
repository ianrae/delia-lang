package org.delia.db.sql;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.db.DBAccessContext;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.h2.SqlHelperFactory;
import org.delia.db.sql.prepared.FKSqlGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.TableInfo;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.sort.topo.TopoTestBase;
import org.junit.Before;
import org.junit.Test;

public class SmartSqlGeneratorTests extends TopoTestBase {

	@Test
	public void test0() {
		chkOne("one optional parent", "one optional", true); //O-O
		chkSorting("Customer,Address"); //parent first

//		SmartSqlGenerator gen = new SmartSqlGenerator(delia.getFactoryService(), this.sess.getExecutionContext().registry, tblinfoL, nameFormatter);
		FKSqlGenerator gen = createFKGen(); 
		QuerySpec spec = new QuerySpec();
		FilterExp filter = new FilterExp(0, new IntegerExp(55));
		spec.queryExp = new QueryExp(0, new IdentExp("Customer"), filter, null);
		QueryDetails details = new QueryDetails();
		SqlStatement statement = gen.generateFKsQuery(spec, details);
		String s = statement.sql;
//		String expected = "SELECT c.id,a.id as addr FROM Customer as c JOIN Address as a ON a.cust=c.id WHERE c.id=55;";
		String expected = "SELECT a.id,b.id as addr FROM Customer as a LEFT JOIN Address as b ON b.cust=a.id  WHERE  a.id=?;";
		assertEquals(expected, s);
		chkParamInt(statement, 1, 55);
	}
	
	private FKSqlGenerator createFKGen() {
		List<TableInfo> tblinfoL = new ArrayList<>();
		return createFKGen(tblinfoL);
	}
	private FKSqlGenerator createFKGen(List<TableInfo> tblinfoL) {
		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
//		SmartSqlGenerator gen = new SmartSqlGenerator(delia.getFactoryService(), this.sess.getExecutionContext().registry, tblinfoL, nameFormatter);
		SqlHelperFactory sqlHelperFactory = new SqlHelperFactory(factorySvc);
		TableExistenceService existSvc = new TableExistenceServiceImpl(dbInterface, new DBAccessContext(this.sess.getExecutionContext().registry, null));
		FKSqlGenerator gen = new FKSqlGenerator(delia.getFactoryService(), this.sess.getExecutionContext().registry, tblinfoL, sqlHelperFactory, 
					new DoNothingVarEvaluator(), existSvc);
		return gen;
	}

	private void chkParamInt(SqlStatement statement, int n, int expected) {
		assertEquals(n, statement.paramL.size());
		assertEquals(expected, statement.paramL.get(n-1).asInt());
	}

	@Test
	public void test1OtherWayRound() {
		chkOne("one optional", "one optional parent", true); //O-O
		chkSorting("Address,Customer"); //parent first

//		SmartSqlGenerator gen = createSmartGen(tblinfoL);
		FKSqlGenerator gen = createFKGen(); 
		QuerySpec spec = new QuerySpec();
		FilterExp filter = new FilterExp(0, new IntegerExp(55));
		spec.queryExp = new QueryExp(0, new IdentExp("Customer"), filter, null);
		QueryDetails details = new QueryDetails();
		SqlStatement statement = gen.generateFKsQuery(spec, details);
		
		String expected = "SELECT * FROM Customer  WHERE  id=?;";
		assertEquals(expected, statement.sql);
		chkParamInt(statement, 1, 55);
	}
	
	@Test
	public void testManyToMany() {
		createOneToOne("OO");
		chkSorting("Customer,Address"); //parent first

		List<TableInfo> tblinfoL = new ArrayList<>();
		TableInfo info = new TableInfo("Customer", "AddressCustomerAssoc");
		info.fieldName = "cust";
		info.tbl1 = "Address";
		info.tbl2 = "Customer";
		tblinfoL.add(info);
		
//		SmartSqlGenerator gen = createSmartGen(tblinfoL);
		FKSqlGenerator gen = createFKGen(tblinfoL); 
		QuerySpec spec = new QuerySpec();
		FilterExp filter = new FilterExp(0, new IntegerExp(55));
		spec.queryExp = new QueryExp(0, new IdentExp("Customer"), filter, null);
		QueryDetails details = new QueryDetails();
		SqlStatement statement = gen.generateFKsQuery(spec, details);
		
//		String expected = "SELECT b.idc,c.leftv as cust FROM Customer as b LEFT JOIN AddressCustomerAssoc as c ON b.idc=c.leftv WHERE b.idc=100;";
		String expected = "SELECT a.idc,c.leftv as addr FROM Customer as a LEFT JOIN AddressCustomerAssoc as c ON a.idc=c.rightv  WHERE  a.idc=?;";
		assertEquals(expected, statement.sql);
		chkParamInt(statement, 1, 55);
	}
	
	@Test
	public void testManyToMany2() {
		createOneToOne("OO");
		chkSorting("Customer,Address"); //parent first

		List<TableInfo> tblinfoL = new ArrayList<>();
		TableInfo info = new TableInfo("Customer", "AddressCustomerAssoc");
		info.fieldName = "cust";
		info.tbl1 = "Address";
		info.tbl2 = "Customer";
		tblinfoL.add(info);
		
//		SmartSqlGenerator gen = createSmartGen(tblinfoL);
		FKSqlGenerator gen = createFKGen(tblinfoL); 
		QuerySpec spec = new QuerySpec();
		FilterExp filter = new FilterExp(0, new IntegerExp(100));
		spec.queryExp = new QueryExp(0, new IdentExp("Address"), filter, null);
		QueryDetails details = new QueryDetails();
		SqlStatement statement = gen.generateFKsQuery(spec, details);
		
//		String expected = "SELECT c.id,a.leftv as addr FROM Customer as c JOIN AddressCustomerAssoc as a ON c.id=a.leftv WHERE c.id=55;";
		String expected = "SELECT a.id,c.rightv as cust FROM Address as a LEFT JOIN AddressCustomerAssoc as c ON a.id=c.leftv  WHERE  a.id=?;";
		assertEquals(expected, statement.sql);
		chkParamInt(statement, 1, 100);
	}

//	private SmartSqlGenerator createSmartGen(List<TableInfo> tblinfoL) {
//		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
//		SmartSqlGenerator gen = new SmartSqlGenerator(delia.getFactoryService(), this.sess.getExecutionContext().registry, tblinfoL, nameFormatter);
//		return gen;
//	}

	// --

	@Before
	public void init() {
		super.init();
	}

	private void createOneToOne(String mo) {
		String src = null;
		if (mo.equals("OO")) {
			src = createTypeSrc("Customer", "relation addr Address many optional");
			src = src.replace(" id", "idc"); //want customer and address to have different key names
			src += createTypeSrc("Address", "relation cust Customer many optional");
		} else if (mo.equals("MO")) {
			src = createTypeSrc("Customer", "relation addr Address one optional");
			src += createTypeSrc("Address", "relation cust Customer one");
		} else if (mo.equals("OM")) {
			src = createTypeSrc("Customer", "relation addr Address one");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		} else if (mo.equals("OneWay")) {
			src = createTypeSrc("Customer", "");
			src += createTypeSrc("Address", "relation cust Customer one");
		} else if (mo.equals("OneWayO")) {
			src = createTypeSrc("Customer", "");
			src += createTypeSrc("Address", "relation cust Customer one optional");
		} else if (mo.equals("OneWayOReversed")) {
			src = createTypeSrc("Address", "relation cust Customer one optional");
			src += createTypeSrc("Customer", "");
		}
		execTypeStatement(src);
	}

	private void chkOne(String custModifiers, String addrModifiers, boolean allowed) {
		chkOne(custModifiers, addrModifiers, allowed, "relation-parent-not-allowed");
	}
	private void chkOne(String custModifiers, String addrModifiers, boolean allowed, String errId) {
		String src = createTypeSrc("Customer", String.format("relation addr Address %s", custModifiers));
		String s = String.format("relation cust Customer %s", addrModifiers);
		if (allowed) {
			String src2 = createTypeSrc("Address", s);
			execTypeStatement(src + " " + src2);
		} else {
			createTypeFail(src, "Address", s, errId);
		}
	}
}
