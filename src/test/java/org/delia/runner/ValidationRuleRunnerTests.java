package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.base.FakeTypeCreator;
import org.delia.db.DBAccessContext;
import org.delia.rule.AlwaysRuleGuard;
import org.delia.rule.DRule;
import org.delia.rule.DRuleContext;
import org.delia.rule.DValueRuleOperand;
import org.delia.rule.StructDValueRuleOperand;
import org.delia.rule.rules.ContainsRule;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.StringValueBuilder;
import org.delia.zdb.ZDBExecutor;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO:
 * 
 * @author Ian Rae
 *
 */
public class ValidationRuleRunnerTests extends RunnerTestBase {

	@Test
	public void testScalarString() {
		initRunner();
		runner.begin("");
		assertEquals(1,1);
		DValue dval = createStringDVal("abc");
		
		AlwaysRuleGuard guard = new AlwaysRuleGuard();
		ContainsRule rule = new ContainsRule(guard, new DValueRuleOperand(), "a");
		chkPass(rule, dval);
		guard = new AlwaysRuleGuard();
		rule = new ContainsRule(guard, new DValueRuleOperand(), "z");
		chkFail(rule, dval);
	}

	@Test
	public void testStructString() {
		initRunner();
		runner.begin("");
		assertEquals(1,1);
		DValue dval = CustomerHelper.createCustomer();
		
		AlwaysRuleGuard guard = new AlwaysRuleGuard();
		ContainsRule rule = new ContainsRule(guard, new StructDValueRuleOperand("firstName"), "bob");
		chkPass(dval, rule);
		guard = new AlwaysRuleGuard();
		rule = new ContainsRule(guard, new StructDValueRuleOperand("firstName"), "z");
		chkFail(dval, rule);
	}

	// --
	
	@Before
	public void init() {
		initRunner();
	}
	
	private DValue createStringDVal(String s) {
		DTypeRegistry registry = TypeRegistryHelper.init();
		FakeTypeCreator creator = new FakeTypeCreator();
		creator.createFakeTypes(registry);
		
		DType itype = TypeRegistryHelper.getIntType();
		DType stype = TypeRegistryHelper.getStringType();
		DType btype = TypeRegistryHelper.getBooleanType();
		
		StringValueBuilder builder = new StringValueBuilder(stype);
		builder.buildFromString(s);
		boolean b = builder.finish();
		assertEquals(true, b);
		DValue dval = builder.getDValue();
		return dval;
	}
	private void chkPass(DRule rule, DValue dval) {
		chkPass(dval, rule);
//		assertEquals(ValidationState.VALID, dval.getValidationState());
	}
	private void chkFail(DRule rule, DValue dval) {
		chkFail(dval, rule);
//		assertEquals(ValidationState.INVALID, dval.getValidationState());
	}

	private void chkPass(DValue dval, DRule rule) {
		FetchRunner fetchRunner = createFetchRunner();
		DRuleContext ctx = new DRuleContext(et, "abc", false, dbInterface.getCapabilities(), true, fetchRunner);
		assertEquals(true, rule.validate(dval, ctx));
		assertEquals(false, ctx.hasErrors());
	}
	private void chkFail(DValue dval, DRule rule) {
		FetchRunner fetchRunner = createFetchRunner();
		DRuleContext ctx = new DRuleContext(et, "abc", false, dbInterface.getCapabilities(), true, fetchRunner);
		assertEquals(false, rule.validate(dval, ctx));
		assertEquals(true, ctx.hasErrors());
	}

	private FetchRunner createFetchRunner() {
		DBAccessContext dbctx = runner.createDBAccessContext();
		ZDBExecutor dbexecutor = dbInterface.createExecutor();
		Runner run = runner.getDeliaRunner();
		FetchRunner fetchRunner = new FetchRunnerImpl(factorySvc, dbexecutor, runner.getRegistry(), run);
		return fetchRunner;
	}

}
