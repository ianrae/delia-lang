package org.delia.runner;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.NullExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UserFnCallExp;
import org.delia.compiler.ast.UserFunctionDefStatementExp;
import org.delia.core.ConfigureService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBExecutor;
import org.delia.db.DBInterface;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.manager.HLSManager;
import org.delia.db.hls.manager.HLSManagerResult;
import org.delia.error.DeliaError;
import org.delia.error.SimpleErrorTracker;
import org.delia.queryresponse.LetSpanEngine;
import org.delia.queryresponse.LetSpanRunnerImpl;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.validation.ValidationRuleRunner;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.zdb.ZDBExecutor;

/**
 * This class is not thread-safe. Only use it as a local var.
 * @author Ian Rae
 *
 */
public class LetStatementRunner extends ServiceBase {

	private DTypeRegistry registry;
	private DBInterface dbInterface;
	private DBExecutor dbexecutor;
	private ZDBExecutor zexec;
	private LetSpanEngine letSpanEngine;
	private FetchRunner fetchRunner;
	private ScalarBuilder scalarBuilder;
	private RunnerImpl runner;
	private HLSManager mgr;

	public LetStatementRunner(FactoryService factorySvc, DBInterface dbInterface, DBExecutor dbexecutor, ZDBExecutor zexec, DTypeRegistry registry, 
			FetchRunner fetchRunner, HLSManager mgr, RunnerImpl runner) {
		super(factorySvc);
		this.dbInterface = dbInterface;
		this.runner = runner;
		this.registry = registry;
		this.fetchRunner = fetchRunner;
		this.dbexecutor = dbexecutor;
		this.zexec = zexec;
		this.mgr = mgr;
		this.scalarBuilder = new ScalarBuilder(factorySvc, registry);

	}

	private ValidationRuleRunner createValidationRunner() {
		return new ValidationRuleRunner(factorySvc, dbInterface.getCapabilities(), fetchRunner);
	}

	private void addError(ResultValue res, String id, String msg) {
		DeliaError error = et.add(id, msg);
		res.errors.add(error);
		res.ok = false;
	}

	public ResultValue executeLetStatement(LetStatementExp exp, ResultValue res) {
		LetSpanRunnerImpl spanRunner = new LetSpanRunnerImpl(factorySvc, registry, fetchRunner);
		this.letSpanEngine = new LetSpanEngine(factorySvc, registry, fetchRunner, spanRunner);
		
		if (exp.isType(LetStatementExp.USER_FUNC_TYPE)) {
			return invokeUserFunc(exp, res);
		}

		if (exp.value instanceof QueryExp) {
			QueryExp queryExp = (QueryExp) exp.value;
			VarRef varRef = resolveScalarVarReference(queryExp);
			if (varRef != null) {
				res.ok = true;
				res.shape = varRef.dval == null ? varRef.nullShape : varRef.dval.getType().getShape();
				res.val = varRef.dval;
				if (varRef.qresp != null) {
					res.val = varRef.qresp;
				}

				assignVar(exp, res);
				return res;
			}
		}

		if (exp.isType(LetStatementExp.QUERY_RESPONSE_TYPE)) {
			QueryExp queryExp = (QueryExp) exp.value;
			VarRef varRef = resolveVarReference(queryExp);
			if (varRef != null) {
				res.ok = true;
				res.shape = null;
				varRef.qresp.bindFetchFlag = true;
				runQueryFnsIfNeeded(queryExp, varRef.qresp, res);
				varRef.qresp.bindFetchFlag = false; //reset

				assignVar(exp, res);
				return res;
			}

			if (queryExp.filter != null && queryExp.filter.cond instanceof NullExp) {
				DeliaError err = et.add("null-filter-not-allowed", "[null] is not allowed");
				throw new DeliaException(err);
			}

			//** call the database **
			QueryResponse qresp = executeDBQuery(queryExp);
			res.ok = qresp.ok;
			res.addIfNotNull(qresp.err);
			res.shape = null;
			res.val = qresp;

			if (qresp.ok) {
				runQueryFnsIfNeeded(queryExp, qresp, res);
			}

			assignVar(exp, res);
			return res; //!!fill in rest
		}

		res.val = toObject(exp.value, exp, res);
		if (exp.typeName != null){
			res.shape = toShape(exp.typeName, res.val);
		}
		res.ok = res.errors.isEmpty();

		assignVar(exp, res);
		return res;
	}

	private QueryResponse executeDBQuery(QueryExp queryExp) {
		QuerySpec spec = resolveFilterVars(queryExp);
		QueryContext qtx = buildQueryContext(spec);
		
		boolean flag = mgr != null;
		QueryResponse qresp;
		if (flag) {
			spec.queryExp = queryExp;
			HLSManagerResult result = mgr.execute(spec, qtx, zexec);
			qresp = result.qresp;
		} else {
			qresp = zexec.rawQuery(spec, qtx);
		}
		return qresp;
	}

	private QueryContext buildQueryContext(QuerySpec spec) {
		QueryFuncContext ctx = new QueryFuncContext();
		//			this.qffRunner.buildPendingTrail(ctx, spec.queryExp);

		QueryContext qtx = new QueryContext();
		qtx.letSpanEngine = letSpanEngine;
		//TODO: fix buglet that is other fn contains 'fks' this won't work
		//			qtx.loadFKs = ctx.pendingTrail.getTrail().contains("fks");
		qtx.loadFKs = this.letSpanEngine.containsFKs(spec.queryExp);
		if (!qtx.loadFKs) {
			ConfigureService configSvc = factorySvc.getConfigureService();
			qtx.loadFKs = configSvc.isPopulateFKsFlag();
		}

		if (! qtx.loadFKs) {
			qtx.pruneParentRelationFlag = true;
		}

		return qtx;
	}
	private QuerySpec resolveFilterVars(QueryExp queryExp) {
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, runner);
		spec.evaluator.init(queryExp);
		return spec;
	}
	private ResultValue invokeUserFunc(LetStatementExp exp, ResultValue resParam) {
		RunnerImpl innerRunner = new RunnerImpl(factorySvc, dbInterface);
		ExecutionState execState = runner.getExecutionState();
		execState.varMap.clear(); //user fn has its own variables

		boolean b = innerRunner.init(execState);
		if (!b) {
			//err
			return resParam;
		}

		UserFnCallExp callExp = (UserFnCallExp) exp.value;

		UserFunctionDefStatementExp userFnExp = runner.userFnMap.get(callExp.funcName);
		if (userFnExp == null) {
			//error
			return resParam;
		}

		//avoid stack overflow. fn can't call itself
		if (runner.activeUserFnMap.containsKey(userFnExp.funcName)) {
			String msg = String.format("A user function may not invoke itself - function %s", userFnExp.funcName);
			DeliaError err = et.add("user-func-self-invoke", msg);
			throw new DeliaException(err);
		}

		//set fns args as local vars
		int i = 0;
		for (Exp argExp: userFnExp.argsL) {
			String argName = argExp.strValue();
			Exp tmpExp = callExp.argL.get(i);
			//hack hack hack - rewrite all this!!
			ResultValue tmpRes = new ResultValue();
			tmpRes.ok = true;
			tmpRes.shape = Shape.INTEGER;
			QueryResponse qr = new QueryResponse();
			qr.dvalList = new ArrayList<>();
			qr.dvalList.add(createDValFrom(tmpExp));
			tmpRes.val = qr;
			innerRunner.varMap.put(argName, tmpRes);
			i++;
		}

		ResultValue finalRes = null;
		ResultValue tmpres = innerRunner.executeProgram(userFnExp.bodyExp.statementL);
		//stop on error
		if (! tmpres.ok) {
			resParam.ok = false;
			resParam.errors.addAll(tmpres.errors);
			return resParam;
		}
		finalRes = tmpres;

		if (finalRes != null) {
			assignVar(exp, finalRes);
		}

		resParam.errors = finalRes.errors;
		resParam.ok = finalRes.ok;
		resParam.shape = finalRes.shape;
		resParam.val = finalRes.val;

		return resParam; 
	}

	private DValue createDValFrom(Exp tmpExp) {
		ScalarValueBuilder builder = factorySvc.createScalarValueBuilder(registry);
		DValue dval = builder.buildInt(tmpExp.strValue());
		return dval;
	}

	private QueryResponse runLetSpanEngine(QueryExp queryExp, QueryResponse qresp) {
		if (dbInterface.getCapabilities().supportsOffsetAndLimit()) {
			return qresp; //don't need span engine. db does it.
		}
		//			boolean flag = true;
		//			if (flag) {
		QueryResponse qresp2 = letSpanEngine.process(queryExp, qresp);
		return qresp2;
		//			} else {
		//				QueryResponse qresp2 = this.qffRunner.process(queryExp, qresp);
		//				return qresp2;
		//			}
	}
	private void runQueryFnsIfNeeded(QueryExp queryExp, QueryResponse qresp, ResultValue res) {
		//extract fields or invoke fns (optional)
		QueryResponse qresp2 = runLetSpanEngine(queryExp, qresp);
		res.ok = qresp2.ok;
		res.addIfNotNull(qresp2.err);
		res.shape = null;
		res.val = qresp2;

		//validate (assume that we don't fully trust db storage - someone may have tampered with data)
		if (qresp2.ok && CollectionUtils.isNotEmpty(qresp2.dvalList)) {
			ValidationRuleRunner ruleRunner = createValidationRunner();
			if (! ruleRunner.validateDVals(qresp.dvalList)) {
				ruleRunner.propogateErrors(res);
			}
		}                              
	}

	private void assignVar(LetStatementExp exp, ResultValue res) {
		String varName = exp.varName;
		if (! varName.equals(RunnerImpl.DOLLAR_DOLLAR) && runner.exists(varName)) {
			addError(res, "var-already-exists", String.format("variable '%s' already exists. Cannot re-assign", varName));
			return;
		}

		res.varName = varName;
		runner.varMap.put(varName, res);
		runner.varMap.put(RunnerImpl.DOLLAR_DOLLAR, res);
	}
	private void assignSerialVar(DValue generatedId) {
		ResultValue res = new ResultValue();
		res.ok = true;
		res.shape = generatedId.getType().getShape();
		res.val = generatedId;
		res.varName = RunnerImpl.VAR_SERIAL;
		runner.varMap.put(RunnerImpl.VAR_SERIAL, res);
	}

	private VarRef resolveScalarVarReference(QueryExp queryExp) {
		ResultValue res = runner.varMap.get(queryExp.typeName);
		if (res == null) {
			return null;
		}

		VarRef varRef = new VarRef();
		varRef.varRef = queryExp.typeName;

		if (res.val instanceof DValue) {
			varRef.dval = (DValue) res.val;
			return varRef;
		} else {
			if (res.val instanceof QueryResponse) {
				QueryResponse qresp = (QueryResponse) res.val;
				//extract fields or invoke fns (optional)
				qresp.bindFetchFlag = true;
				QueryResponse qresp2 = this.letSpanEngine.processVarRef(queryExp, qresp);
				qresp.bindFetchFlag = false;
				//TODO: propogate errors from qresp2.err
				if (qresp2.ok) {
					if (qresp2.dvalList == null) {
						varRef.dval = null;
						return varRef;
					}
					varRef.qresp = qresp2;
					return varRef;
				}
			} else if (res.val == null) { //handle null values
				varRef.dval = null;
				varRef.nullShape = res.shape;
				return varRef;
			}
		}
		return null;
	}
	private VarRef resolveVarReference(QueryExp queryExp) {
		ResultValue res = runner.varMap.get(queryExp.typeName);
		if (res == null) {
			return null;
		}

		VarRef varRef = new VarRef();
		varRef.varRef = queryExp.typeName;

		QueryResponse qresp = (QueryResponse) res.val;
		varRef.qresp = qresp;
		return varRef;
	}

	private DValue toObject(Exp valueExp, LetStatementExp exp, ResultValue res) {
		int numErr = et.errorCount();
		DValue dval = scalarBuilder.buildDValue(valueExp, exp.isTypeExplicit ? exp.typeName : null);
		if (et.errorCount() != numErr) {
			SimpleErrorTracker set = (SimpleErrorTracker) et; //hack hack hack
			List<DeliaError> list = set.getErrorsSinceMark(numErr);
			res.errors.addAll(list);
		}
		if (dval != null) {
			ValidationRuleRunner ruleRunner = createValidationRunner();
			if (! ruleRunner.validateDVal(dval)) {
				ruleRunner.propogateErrors(res);
			}

		}
		return dval;
	}

	private Shape toShape(String typeName, Object val) {
		Shape shape = Shape.createFromDeliaType(typeName);
		if (shape != null) {
			return shape;
		}
		if (val instanceof DValue) {
			DValue dval = (DValue) val;
			return dval.getType().getShape();
		} else {
			DType dtype = registry.getType(typeName);
			return dtype == null ? null : dtype.getShape();
		}
	}
}