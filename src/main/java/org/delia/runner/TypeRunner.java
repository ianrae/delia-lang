package org.delia.runner;

import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.rule.rules.RulePostProcessor;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.typebuilder.FutureDeclError;
import org.delia.typebuilder.PreTypeRegistry;
import org.delia.typebuilder.TypeBuilder;

/**
 * A sub-runner that executes Delia type statements.
 * @author Ian Rae
 *
 */
public class TypeRunner extends ServiceBase {
	private DTypeRegistry registry;
	private PreTypeRegistry preRegistry;

	public TypeRunner(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
	}

	public void executeStatements(List<Exp> extL, List<DeliaError> allErrors, boolean runRulePostProcessor) {
		for(Exp exp: extL) {
			ResultValue res = executeStatement(exp);
			if (! res.ok) {
				allErrors.addAll(res.errors);
			}
		}
		
		if (allErrors.isEmpty() && runRulePostProcessor) {
			executeRulePostProcessor(allErrors);
		}
	}
	
	public void executeRulePostProcessor(List<DeliaError> allErrors) {
		if (allErrors.isEmpty()) {
			RulePostProcessor postProcessor = new RulePostProcessor(factorySvc);
			postProcessor.process(registry, allErrors);
		}
	}
	
	private ResultValue executeStatement(Exp exp) {
		ResultValue res = new ResultValue();
		if (exp instanceof TypeStatementExp) {
			//log.log("exec: " + exp.toString());
			executeTypeStatement((TypeStatementExp)exp, res);
		}

		return res;
	}

	private void executeTypeStatement(TypeStatementExp exp, ResultValue res) {
		TypeBuilder typeBuilder = new TypeBuilder(factorySvc, registry, preRegistry);
		
		DType dtype = typeBuilder.createType(exp);
		res.ok = dtype != null;
		if (! res.ok) {
			res.errors.addAll(typeBuilder.getErrorTracker().getErrors());
		}
	}

	public DTypeRegistry getRegistry() {
		return registry;
	}
	
	public boolean hasFutureDeclErrors() {
		for(DeliaError err: et.getErrors()) {
			if (err instanceof FutureDeclError) {
				return true;
			}
		}
		return false;
	}


	public PreTypeRegistry getPreRegistry() {
		return preRegistry;
	}

	public void setPreRegistry(PreTypeRegistry preRegistry) {
		this.preRegistry = preRegistry;
	}

}