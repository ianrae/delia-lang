package org.delia.runner;

import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.typebuilder.FutureDeclError;
import org.delia.typebuilder.TypeBuilder;

/**
 * A sub-runner that executes Delia type statements.
 * @author Ian Rae
 *
 */
public class TypeRunner extends ServiceBase {
	private DTypeRegistry registry;
	private TypeBuilder typeBuilder;

	public TypeRunner(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.typeBuilder = new TypeBuilder(factorySvc, registry);
	}


	public void executeStatements(List<Exp> extL, List<DeliaError> allErrors) {
		for(Exp exp: extL) {
			ResultValue res = executeStatement(exp);
			if (! res.ok) {
				allErrors.addAll(res.errors);
			}
		}
		
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
		DType dtype = typeBuilder.createType(exp);
		//TODO: if futureL not-empty then re-run to handle forward delcs
		res.ok = dtype != null;
		if (! res.ok) {
			res.errors.addAll(typeBuilder.getErrorTracker().getErrors());
		}
	}

//	private void addError(ResultValue res, String id, String msg) {
//		DangError error = et.add(id, msg);
//		res.errors.add(error);
//		res.ok = false;
//	}

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

}