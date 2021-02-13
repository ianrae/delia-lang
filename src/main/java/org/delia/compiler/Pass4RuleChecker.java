package org.delia.compiler;

import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.error.SimpleErrorTracker;
import org.delia.rule.DRule;
import org.delia.rule.FieldExistenceServiceImpl;
import org.delia.runner.InternalCompileState;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

//final checks using registry
public class Pass4RuleChecker extends CompilerPassBase {
	private DTypeRegistry registry;
	
	public Pass4RuleChecker(FactoryService factorySvc, ErrorLineFinder errorLineFinder, InternalCompileState execCtx, DTypeRegistry registry) {
		super(factorySvc, errorLineFinder, execCtx);
		this.registry = registry;
	}

	@Override
	public CompilerResults process(List<Exp> list) {
		DeliaExceptionHelper.throwNotImplementedError("don't call this method");
		return null;
	}

	public void checkType(TypeStatementExp typeExp, CompilerResults results) {
		DType dtype = registry.getType(typeExp.typeName);
		for(DRule rule: dtype.getRawRules()) {
			checkRule(rule, dtype, results);
		}
	}

	private void checkRule(DRule rule, DType dtype, CompilerResults results) {
		FieldExistenceServiceImpl fieldExistSvc = new FieldExistenceServiceImpl(registry, dtype);
		SimpleErrorTracker et = new SimpleErrorTracker(log);
		rule.performCompilerPass4Checks(fieldExistSvc, et);
		results.errors.addAll(et.getErrors());
	}
}