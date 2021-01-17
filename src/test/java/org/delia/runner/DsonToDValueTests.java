package org.delia.runner;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.log.SimpleLog;
import org.delia.runner.DsonToDValueConverter;
import org.delia.runner.Runner;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
import org.delia.type.DRelation;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;
import org.junit.Test;



public class DsonToDValueTests {
	
	@Test
	public void testInsertOK() {
		InsertStatementExp exp = chkInsert("insert Customer {id:44, firstName:'bob', flag:true }", "insert Customer {id: 44,firstName: 'bob',flag: true }");
		
		initConverter();
		DValue dval = converter.convertOne("Customer", exp.dsonExp, null);
		assertEquals(true, dval.getType().isStructShape());
		assertEquals("Customer", dval.getType().getName());

		int id = dval.asStruct().getField("id").asInt();
		assertEquals(44, id);
		String s = dval.asStruct().getField("firstName").asString();
		assertEquals("bob", s);
		Boolean b = dval.asStruct().getField("flag").asBoolean();
		assertEquals(true, b.booleanValue());
	}
	
	@Test
	public void testNullKey() {
		InsertStatementExp exp = chkInsert("insert Customer {firstName:'bob' }", "insert Customer {firstName: 'bob' }");
		
		initConverter();
		boolean ok = false;
		converter.convertOne("Customer", exp.dsonExp, null);
		String s = this.et.getLastError().getMsg();
		assertEquals(true, s.contains("'id' not added to struct"));
	}

	@Test
	public void testRelation() {
//		String src = "insert Customer {id:44, firstName:'bob', flag:true }";
		String src = "insert Employee {id:44, firstName:'bob', dept:33 }";
		InsertStatementExp exp = chkInsert(src, null);
		
		initConverter();
		DValue dval = converter.convertOne("Employee", exp.dsonExp, null);
		assertEquals(true, dval.getType().isStructShape());
		assertEquals("Employee", dval.getType().getName());

		int id = dval.asStruct().getField("id").asInt();
		assertEquals(44, id);
		String s = dval.asStruct().getField("firstName").asString();
		assertEquals("bob", s);
		
		DRelation drel = dval.asStruct().getField("dept").asRelation();
		assertEquals(33, drel.getForeignKey().asInt());
	}
	
	@Test
	public void testMerge() {
		String src = "insert Employee {id:44, firstName:'bob', dept:33 }";
		InsertStatementExp exp = chkInsert(src, null);
		
		initConverter();
		DValue dval = converter.convertOne("Employee", exp.dsonExp, null);
		assertEquals(true, dval.getType().isStructShape());
		assertEquals("Employee", dval.getType().getName());
		
		src = "update Employee[44] {firstName:'bobby', dept:44 }";
		UpdateStatementExp uexp = chelper.chkUpdate(src, null);
		
		DValue dvalPartial = converter.convertOnePartial("Employee", uexp.dsonExp);
		assertEquals(true, dvalPartial.getType().isStructShape());
		assertEquals("Employee", dvalPartial.getType().getName());
		

		DValue merged = DValueHelper.mergeOne(dvalPartial, dval);
		int id = merged.asStruct().getField("id").asInt();
		assertEquals(44, id);
		String s = merged.asStruct().getField("firstName").asString();
		assertEquals("bobby", s);
		DRelation drel = dval.asStruct().getField("dept").asRelation();
		assertEquals(44, drel.getForeignKey().asInt());
	}
	
	@Test
	public void testMergeSkip() {
		String src = "insert Employee {id:44, firstName:'bob', dept:33 }";
		InsertStatementExp exp = chkInsert(src, null);
		
		initConverter();
		DValue dval = converter.convertOne("Employee", exp.dsonExp, null);
		assertEquals(true, dval.getType().isStructShape());
		assertEquals("Employee", dval.getType().getName());
		
		src = "update Employee[44] {firstName:'bobby', dept:44 }";
		UpdateStatementExp uexp = chelper.chkUpdate(src, null);
		
		DValue dvalPartial = converter.convertOnePartial("Employee", uexp.dsonExp);
		assertEquals(true, dvalPartial.getType().isStructShape());
		assertEquals("Employee", dvalPartial.getType().getName());
		
		Map<String,String> skipMap = new HashMap<>();
		skipMap.put("firstName", "");
		DValue merged = DValueHelper.mergeOne(dvalPartial, dval, skipMap);
		int id = merged.asStruct().getField("id").asInt();
		assertEquals(44, id);
		String s = merged.asStruct().getField("firstName").asString();
		assertEquals("bob", s);
		DRelation drel = dval.asStruct().getField("dept").asRelation();
		assertEquals(44, drel.getForeignKey().asInt());
	}
	
	// --
	private DsonToDValueConverter converter;
	private RunnerHelper helper = new RunnerHelper();
	private CompilerHelper chelper = new CompilerHelper(null);
	private Log log = new SimpleLog();
	private ErrorTracker et = new SimpleErrorTracker(log);

	private void initConverter()  {
		DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
		registryBuilder.init();
//		registryBuilder.addFakeTypes();
		DTypeRegistry reg = registryBuilder.getRegistry();
		helper.addFakeTypes(reg);
		helper.addDeptAndEmployee(reg);
		
		Runner xrunner = helper.getCurrentRunner();
		FactoryService factorySvc = new FactoryServiceImpl(log, et);
		SprigService sprigSvc = new SprigServiceImpl(factorySvc, reg);
		converter = new DsonToDValueConverter(factorySvc, et, reg, xrunner, sprigSvc);
	}
	
	private InsertStatementExp chkInsert(String input, String output) {
		return chelper.chkInsert(input, output);
	}
}
