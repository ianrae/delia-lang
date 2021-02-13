package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.base.UnitTestLog;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.StringExp;
import org.delia.core.DateFormatService;
import org.delia.core.DateFormatServiceImpl;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.core.TimeZoneServiceImpl;
import org.delia.db.memdb.filter.OpEvaluator;
import org.delia.db.memdb.filter.OpFactory;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class OpEvaluatorTests {

	@Test
	public void testStr() {
		DValue dval = createCustomer();
		StringExp exp2 = new StringExp("def");
		OpFactory factory = new OpFactory(null, fmtSvc, factorySvc, null); //don't need registry for this test
		IdentExp exp = new IdentExp("firstName");
		OpEvaluator eval = factory.create("<", exp, exp2, null, null, false);
		evaluate(eval, false, dval, new StringExp("aob"));
		evaluate(eval, false, dval, new StringExp("bob"));
		evaluate(eval, true, dval, new StringExp("cob"));

		eval = factory.create("<=", exp, exp2, null, null, false);
		evaluate(eval, false, dval, new StringExp("aob"));
		evaluate(eval, true, dval, new StringExp("bob"));
		evaluate(eval, true, dval, new StringExp("cob"));

		eval = factory.create(">", exp, exp2, null, null, false);
		evaluate(eval, true, dval, new StringExp("aob"));
		evaluate(eval, false, dval, new StringExp("bob"));
		evaluate(eval, false, dval, new StringExp("cob"));

		eval = factory.create(">=", exp, exp2, null, null, false);
		evaluate(eval, true, dval, new StringExp("aob"));
		evaluate(eval, true, dval, new StringExp("bob"));
		evaluate(eval, false, dval, new StringExp("cob"));
	}

	private void evaluate(OpEvaluator eval, boolean expected, DValue dval, Object right) {
		eval.setRightVar(right);
		assertEquals(expected, eval.match(dval));
	}

	@Test
	public void testInt() {
		DValue dval = createCustomer();
		IntegerExp exp2 = new IntegerExp(55);
		DateFormatService fmtSvc = new DateFormatServiceImpl(new TimeZoneServiceImpl());
		OpFactory factory = new OpFactory(null, fmtSvc, factorySvc, null); //don't need registry for this test
		IdentExp exp = new IdentExp("id");
		OpEvaluator eval = factory.create("<", exp, exp2, null, null, false);
		IntegerExp n43 = new IntegerExp(43);
		IntegerExp n44 = new IntegerExp(44);
		IntegerExp n45 = new IntegerExp(45);

		evaluate(eval, false, dval, n43);
		evaluate(eval, false, dval, n44);
		evaluate(eval, true, dval, n45);

		eval = factory.create("<=", exp, exp2, null, null, false);
		evaluate(eval, false, dval, n43);
		evaluate(eval, true, dval, n44);
		evaluate(eval, true, dval, n45);

		eval = factory.create(">", exp, exp2, null, null, false);
		evaluate(eval, true, dval, n43);
		evaluate(eval, false, dval, n44);
		evaluate(eval, false, dval, n45);

		eval = factory.create(">=", exp, exp2, null, null, false);
		evaluate(eval, true, dval, n43);
		evaluate(eval, true, dval, n44);
		evaluate(eval, false, dval, n45);
	}

	// --
	private DateFormatService fmtSvc;
	private FactoryService factorySvc;
	
	private DValue createCustomer() {
		return CustomerHelper.createCustomer();
	}

	@Before
	public void init() {
		this.fmtSvc = new DateFormatServiceImpl(new TimeZoneServiceImpl());
		Log log = new UnitTestLog();
		this.factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));

	}
}
