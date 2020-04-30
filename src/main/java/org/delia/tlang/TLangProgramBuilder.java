package org.delia.tlang;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.ast.inputfunction.EndIfStatementExp;
import org.delia.compiler.ast.inputfunction.IfStatementExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.memdb.filter.OP;
import org.delia.error.SimpleErrorTracker;
import org.delia.tlang.runner.BasicCondition;
import org.delia.tlang.runner.Condition;
import org.delia.tlang.runner.DValueOpEvaluator;
import org.delia.tlang.runner.IsMissingCondition;
import org.delia.tlang.runner.OpCondition;
import org.delia.tlang.runner.StatementOpEvaluator;
import org.delia.tlang.runner.TLangProgram;
import org.delia.tlang.runner.TLangStatement;
import org.delia.tlang.statement.ElseIfStatement;
import org.delia.tlang.statement.EndIfStatement;
import org.delia.tlang.statement.FailsStatement;
import org.delia.tlang.statement.IfStatement;
import org.delia.tlang.statement.ValueStatement;
import org.delia.tlang.statement.VariableStatement;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

public class TLangProgramBuilder extends ServiceBase {

//	private DTypeRegistry registry;
	private ScalarValueBuilder builder;
	private TLangStatementFactory statementFactory;

	public TLangProgramBuilder(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.et = new SimpleErrorTracker(log); //local et
//		this.registry = registry;
		this.builder = factorySvc.createScalarValueBuilder(registry);
		this.statementFactory = new TLangStatementFactory(factorySvc);
	}

	public TLangProgram build(InputFuncMappingExp mappingExp) {
		if (mappingExp.tlangBody == null || mappingExp.tlangBody.statementL.isEmpty()) {
			return null; //no tlang
		}

		TLangProgram program = new TLangProgram();

		for(Exp exp: mappingExp.tlangBody.statementL) {
			TLangStatement statement = parseStatement(exp);
			if (statement != null) {
				program.statements.add(statement);
			}
		}

		validateProgram(program);
		return program;
	}

	private void validateProgram(TLangProgram program) {
		boolean inIf = false;
		for(TLangStatement statement: program.statements) {
			if (statement instanceof IfStatement) {
				if (inIf) {
					DeliaExceptionHelper.throwError("tlang-nested-if-not-allowed", "Nested if statements not allowed in TLANG");
				} 
				inIf = true;
			} else if (statement instanceof ElseIfStatement) {
				if (!inIf) {
					DeliaExceptionHelper.throwError("tlang-missing-if", "elseif without a preceeding if statement.");
				}
			} else if (statement instanceof EndIfStatement) {
				if (!inIf) {
					DeliaExceptionHelper.throwError("tlang-missing-if", "endif without a preceeding if statement.");
				}
				inIf = false;
			}
		}

		if (inIf) {
			DeliaExceptionHelper.throwError("tlang-missing-endif", "if without an endif statement.");
		}
	}

	private TLangStatement parseStatement(Exp exp) {
		if (exp instanceof XNAFMultiExp) {
			XNAFMultiExp multiExp = (XNAFMultiExp) exp;
			if (multiExp.qfeL.size() > 2) {
				//err
				return null;
			} else if (multiExp.qfeL.size() == 2) {
				XNAFSingleExp field = multiExp.qfeL.get(0);
				XNAFSingleExp fnn = multiExp.qfeL.get(1);
				DeliaExceptionHelper.throwError("tlang-x-y-not-supported", "tlang x.y not supported");
				return null;
			} else {
				XNAFSingleExp fieldOrFn = multiExp.qfeL.get(0);
				if (fieldOrFn.isRuleFn) {
					return buildFnStatement(fieldOrFn);
				} else {
					//var reference
					return new VariableStatement(fieldOrFn.funcName);
				}
			}
		} else if (exp instanceof IntegerExp) {
			DValue dval = builder.buildInt(((IntegerExp) exp).val);
			return new ValueStatement(dval);
		} else if (exp instanceof LongExp) {
			DValue dval = builder.buildLong(((LongExp) exp).val);
			return new ValueStatement(dval);
		} else if (exp instanceof NumberExp) {
			DValue dval = builder.buildNumber(((NumberExp) exp).val);
			return new ValueStatement(dval);
		} else if (exp instanceof BooleanExp) {
			DValue dval = builder.buildBoolean(((BooleanExp) exp).val);
			return new ValueStatement(dval);
		} else if (exp instanceof StringExp) {
			DValue dval = builder.buildString(((StringExp) exp).val);
			//TODO date later!1
			return new ValueStatement(dval);
		} else if (exp instanceof IfStatementExp) {
			IfStatementExp ifexp = (IfStatementExp) exp;
			Condition cond = buildCondition(ifexp); //new BasicCondition(true); //!!!
			
			if (ifexp.isIf) {
				return new IfStatement(cond, true);
			} else {
				return new ElseIfStatement(cond);
			}
		} else if (exp instanceof EndIfStatementExp) {
			return new EndIfStatement();
		} else {
			//err
			return null;
		}
	}

	private Condition buildCondition(IfStatementExp ifexp) {
		if (ifexp.condition instanceof BooleanExp) {
			BooleanExp bexp = (BooleanExp) ifexp.condition;
			return new BasicCondition(bexp.val);
		} else if (ifexp.condition instanceof IdentExp) {
			String str = ifexp.condition.strValue();
			if (str.equals("missing")) {
				return new IsMissingCondition();
			}
			DeliaExceptionHelper.throwError("unknown-condition", "Unknown condition '%s'", str);
		}

		FilterOpExp filter = (FilterOpExp) ifexp.condition;
		OP op = OP.createFromString(filter.op);

		TLangStatement stat1 = this.parseStatement(filter.op1); //** recursion **
		TLangStatement stat2 = this.parseStatement(filter.op2); //** recursion **
		//how set stat1?
		StatementOpEvaluator evaluator = new StatementOpEvaluator(op);
		evaluator.setLeftStatement(stat1);
		evaluator.setRightVar(stat2);
		DValueOpEvaluator dvalEval = new DValueOpEvaluator(op);
		evaluator.setDvalEvaluator(dvalEval);

		OpCondition cond = new OpCondition(evaluator);
		return cond;
	}

	private TLangStatement buildFnStatement(XNAFSingleExp fieldOrFn) {
		return statementFactory.create(fieldOrFn);
	}

}