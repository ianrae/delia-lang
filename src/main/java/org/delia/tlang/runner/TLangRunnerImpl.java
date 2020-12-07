package org.delia.tlang.runner;

import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.runner.VarEvaluator;
import org.delia.tlang.statement.ElseIfStatement;
import org.delia.tlang.statement.EndIfStatement;
import org.delia.tlang.statement.IfStatement;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.StringTrail;
import org.delia.valuebuilder.ScalarValueBuilder;

public class TLangRunnerImpl extends ServiceBase implements TLangRunner {

	private DTypeRegistry registry;
	private ScalarValueBuilder scalarBuilder;
	private StringTrail trail = new StringTrail();
	private VarEvaluator varEvaluator;
	private Map<String, Object> inputDataMap;
	private int lineNum;

	public TLangRunnerImpl(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
	}
	
	
	@Override
	public String getTrail() {
		return trail.getTrail();
	}
	@Override
	public void setVarEvaluator(VarEvaluator varEvaluator) {
		this.varEvaluator = varEvaluator;
	}

	@Override
	public TLangResult execute(TLangProgram program, DValue initialValue) {
		trail = new StringTrail();

		DValue dval = initialValue;
		TLangResult res = new TLangResult();
		int ipIndex;
		int stopCounter = Integer.MAX_VALUE;
		for(ipIndex = 0; ipIndex < program.statements.size(); ipIndex++) {
			TLangStatement statement = program.statements.get(ipIndex);
			statementFixup(statement);
			if (statement.evalCondition(dval)) {
				TLangContext ctx = new TLangContext();
				ctx.builder = scalarBuilder;
				ctx.varEvaluator = varEvaluator;
				ctx.inputDataMap = inputDataMap;
				ctx.lineNum = lineNum;

				res.ok = true;
				trail.add(statement.getName());
				statement.execute(dval, res, ctx);
				if (! res.ok) {
					break;
				}
				if (ctx.failFlag) {
					log.log("fail!!!");
					res.failFlag = true;
					break;
				} else if (ctx.stopFlag) {
					log.log("stop!"); //normal stop
					break;
				} else if (ctx.stopAfterNextFlag) {
					stopCounter = 1;
				}
				
				dval = (DValue) res.val;
			} else {
				if (statement instanceof IfStatement) {
					trail.add(statement.getName());
					IfStatement ifstat = (IfStatement) statement;
					if (ifstat.isIfThenReturn) {
						break; //we're done
					}
				}
				
				ipIndex = findNext(program, ipIndex);
				if (ipIndex < 0) {
					//err missing endif
					log.log("fail!!!");
					res.failFlag = true;
					break;
				}
			}
			
			if (stopCounter-- <= 0) {
				log.log("stop!"); //normal stop
				break;
			}
		}

		TLangResult result = res;
		result.ok = true;
		result.val = dval;
		return result;
	}
	
	private void statementFixup(TLangStatement statement) {
		if (statement instanceof NeedsTLangRunner) {
			NeedsTLangRunner needsRunner = (NeedsTLangRunner) statement;
			needsRunner.setTLangRunner(this);
		}
	}
	@Override
	public TLangResult executeOne(TLangStatement statement, DValue initialValue) {
		DValue dval = initialValue;
		TLangResult res = new TLangResult();
		TLangContext ctx = new TLangContext();
		ctx.builder = scalarBuilder;
		ctx.varEvaluator = varEvaluator;
		ctx.lineNum = lineNum;

		res.ok = true;
		statement.execute(dval, res, ctx);
		dval = (DValue) res.val;

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


	@Override
	public void setInputMap(Map<String, Object> inputData, int lineNum) {
		this.inputDataMap = inputData;
		this.lineNum = lineNum + 1; //convert to 1-based
	}
}