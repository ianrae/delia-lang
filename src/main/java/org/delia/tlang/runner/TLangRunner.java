package org.delia.tlang.runner;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.other.StringTrail;
import org.delia.runner.VarEvaluator;
import org.delia.tlang.statement.ElseIfStatement;
import org.delia.tlang.statement.EndIfStatement;
import org.delia.tlang.statement.IfStatement;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;

public class TLangRunner extends ServiceBase {

	private DTypeRegistry registry;
	private ScalarValueBuilder scalarBuilder;
	public StringTrail trail = new StringTrail();
	private VarEvaluator varEvaluator;

	public TLangRunner(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
	}
	public void setVarEvaluator(VarEvaluator varEvaluator) {
		this.varEvaluator = varEvaluator;
	}

	public TLangResult execute(TLangProgram program, DValue initialValue) {
		trail = new StringTrail();

		DValue dval = initialValue;
		TLangResult res = new TLangResult();
		int ipIndex;
		for(ipIndex = 0; ipIndex < program.statements.size(); ipIndex++) {
			TLangStatement statement = program.statements.get(ipIndex);
			if (statement.evalCondition(dval)) {
				TLangContext ctx = new TLangContext();
				ctx.builder = scalarBuilder;
				ctx.varEvaluator = varEvaluator;
				
				res.ok = true;
				trail.add(statement.getName());
				statement.execute(dval, res, ctx);
				if (! res.ok) {
					break;
				}
				dval = (DValue) res.val;
			} else {
				if (statement instanceof IfStatement) {
					trail.add(statement.getName());
				}
				
				ipIndex = findNext(program, ipIndex);
				if (ipIndex < 0) {
					//err missing endif
				}
			}
		}

		TLangResult result = res;
		result.ok = true;
		result.val = dval;
		return result;
	}

	private int findNext(TLangProgram program, int ipIndexCurrent) {
		for(int ipIndex = ipIndexCurrent + 1; ipIndex < program.statements.size(); ipIndex++) {
			TLangStatement statement = program.statements.get(ipIndex);
			if (statement instanceof EndIfStatement || statement instanceof ElseIfStatement) {
				return ipIndex - 1; //so elseif/endif get executed
			}
		}
		return -1;
	}
}