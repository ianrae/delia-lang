package org.delia.db.sql;

import static org.junit.Assert.assertEquals;

import java.util.StringJoiner;

import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.db.QuerySpec;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.where.InPhrase;
import org.delia.db.sql.where.LogicalPhrase;
import org.delia.db.sql.where.SqlWhereConverter;
import org.delia.db.sql.where.WherePhrase;
import org.delia.sort.topo.TopoTestBase;
import org.junit.Before;
import org.junit.Test;

public class SqlWhereConverterTests extends TopoTestBase {
	
	// A: op1 op op2
	// A or/and B
	
	@Test
	public void test0() {
		QuerySpec spec = genSpec("let x = Customer[id==10]"); 
		chkPhrase(spec, "==", "INTEGER_SHAPE", "id", "INTEGER_SHAPE", "10");
		assertEquals(false, wherephrase.op1.typeDetails.isRelation);
		assertEquals(false, wherephrase.op1.typeDetails.isParent);
	}
	
	//TODO fix later
//	@Test
//	public void test0OtherWayRound() {
//		QuerySpec spec = genSpec("let x = Customer[10==id]"); 
//		chkPhrase(spec, "==");
//	}
	
	@Test
	public void testFK() {
		QuerySpec spec = genSpec("let x = Customer[addr==10]"); 
		chkPhrase(spec, "==", "Address", "addr", "Address", "10");
		assertEquals(true, wherephrase.op1.typeDetails.isRelation);
		assertEquals(true, wherephrase.op1.typeDetails.isParent);
	}
	
	@Test
	public void testFKChild() {
		QuerySpec spec = genSpec("let x = Address[cust==10]"); 
		chkPhrase(spec, "==", "Customer", "cust", "Customer", "10");
		assertEquals(true, wherephrase.op1.typeDetails.isRelation);
		assertEquals(false, wherephrase.op1.typeDetails.isParent);
	}
	
	@Test
	public void testLT() {
		QuerySpec spec = genSpec2("OO", "let z = Customer[x < 10]"); 
		chkPhrase(spec, "<", "INTEGER_SHAPE", "x", "INTEGER_SHAPE", "10");
		assertEquals(false, wherephrase.op1.typeDetails.isRelation);
		assertEquals(false, wherephrase.op1.typeDetails.isParent);
	}
	@Test
	public void testAnd() {
		QuerySpec spec = genSpec2("OO", "let z = Customer[x < 10 and y < 5]"); 
//		chkPhrase(spec, "<", "INTEGER_SHAPE", "x", "INTEGER_SHAPE", "10");
		LogicalPhrase lphrase = genPhrase(spec);
		assertEquals(true, lphrase.isAnd);
		wherephrase = (WherePhrase) lphrase.express1;
		chkPhrase("<", "INTEGER_SHAPE", "x", "INTEGER_SHAPE", "10");

		wherephrase = (WherePhrase) lphrase.express2;
		chkPhrase("<", "INTEGER_SHAPE", "y", "INTEGER_SHAPE", "5");
	}
	@Test
	public void testNot() {
		QuerySpec spec = genSpec2("OO", "let z = Customer[!(x < 10)]"); 
		chkPhrase(spec, "<", "INTEGER_SHAPE", "x", "INTEGER_SHAPE", "10");
		assertEquals(true, wherephrase.notFlag);
	}
	@Test
	public void testIn() {
		QuerySpec spec = genSpec2("OO", "let z = Customer[x in [5,6]]");
		SqlWhereConverter whereConverter = new SqlWhereConverter(delia.getFactoryService(), this.sess.getExecutionContext().registry, null);
		InPhrase phrase = (InPhrase) whereConverter.convert(spec);
		
		chkInPhrase(phrase, "INTEGER_SHAPE", "x", "5,6");
	}
	
	// --
	private WherePhrase wherephrase;

	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}
	private QuerySpec genSpec(String letSrc) {
		chkOne("one optional parent", "one optional", letSrc); //O-O
		chkSorting("Customer,Address"); //parent first
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) sess;
		assertEquals(3, sessimpl.expL.size());
		LetStatementExp letexp = (LetStatementExp) sessimpl.expL.get(2);
		
		QuerySpec spec = new QuerySpec();
		spec.queryExp = (QueryExp) letexp.value;
		spec.evaluator = null; //not set for this test, but normally would be
		return spec;
	}

	private QuerySpec genSpec2(String srcName, String srcExtra) {
		createSrc(srcName, srcExtra);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) sess;
		assertEquals(2, sessimpl.expL.size());
		LetStatementExp letexp = (LetStatementExp) sessimpl.expL.get(1);
		
		QuerySpec spec = new QuerySpec();
		spec.queryExp = (QueryExp) letexp.value;
		spec.evaluator = null; //not set for this test, but normally would be
		return spec;
	}
	
	private LogicalPhrase genPhrase(QuerySpec spec) {
		SqlWhereConverter whereConverter = new SqlWhereConverter(delia.getFactoryService(), this.sess.getExecutionContext().registry, null);
		return (LogicalPhrase) whereConverter.convert(spec);
	}

	private void chkPhrase(QuerySpec spec, String op, String type1, String v1, String type2, String v2) {
		SqlWhereConverter whereConverter = new SqlWhereConverter(delia.getFactoryService(), this.sess.getExecutionContext().registry, null);
		wherephrase = (WherePhrase) whereConverter.convert(spec);
		chkPhrase(op, type1, v1, type2, v2);
	}
	private void chkPhrase(String op, String type1, String v1, String type2, String v2) {
		assertEquals(op, wherephrase.op);
		assertEquals(type1, wherephrase.op1.typeDetails.dtype.getName());
		assertEquals(v1, wherephrase.op1.exp.strValue());
		assertEquals(type2, wherephrase.op2.typeDetails.dtype.getName());
		assertEquals(v2, wherephrase.op2.exp.strValue());
	}
	private void chkInPhrase(InPhrase phrase, String type1, String v1, String listStr) {
		assertEquals(type1, phrase.op1.typeDetails.dtype.getName());
		assertEquals(v1, phrase.op1.exp.strValue());
		StringJoiner joiner = new StringJoiner(",");
		for(Exp exp: phrase.valueL) {
			joiner.add(exp.strValue());
		}
		assertEquals(listStr, joiner.toString());
	}

	private void chkOne(String custModifiers, String addrModifiers, String letSrc) {
		String src = createTypeSrc("Customer", String.format("relation addr Address %s", custModifiers));
		String s = String.format("relation cust Customer %s", addrModifiers);
		String src2 = createTypeSrc("Address", s);
		src += " " + src2;
		src += " " + letSrc;
		execTypeStatement(src);
	}
	
	private void createSrc(String mo, String srcExtra) {
		String src = null;
		if (mo.equals("OO")) {
			src = createTypeSrc("Customer", "x int, y int");
		}
		execTypeStatement(src + " " + srcExtra);
	}
}
