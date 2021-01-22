package org.delia.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.NullExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UserFnCallExp;
import org.delia.compiler.ast.UserFunctionDefStatementExp;
import org.delia.core.ConfigureService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hld.HLDManager;
import org.delia.db.hld.HLDQuery;
import org.delia.db.hld.HLDQueryStatement;
import org.delia.db.hld.HLDSimpleQueryService;
import org.delia.db.hld.QueryFnSpec;
import org.delia.db.hls.manager.HLSManager;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.error.DeliaError;
import org.delia.error.SimpleErrorTracker;
import org.delia.queryresponse.LetSpanEngine;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.validation.ValidationRunner;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

/**
 * This class is not thread-safe. Only use it as a local var.
 * @author Ian Rae
 *
 */
public class LetStatementRunner extends ServiceBase {

	private DTypeRegistry registry;
	private ZDBInterfaceFactory dbInterface;
	private ZDBExecutor zexec;
	private LetSpanEngine letSpanEngine;
	private FetchRunner fetchRunner;
	private ScalarBuilder scalarBuilder;
	private RunnerImpl runner;
	private DatIdMap datIdMap;
	private HLDManager hldManager;
	private HLDQueryStatement mostRecentStatment;

	public LetStatementRunner(FactoryService factorySvc, ZDBInterfaceFactory dbInterface, ZDBExecutor zexec, DTypeRegistry registry, 
			FetchRunner fetchRunner, HLDManager hldManager, RunnerImpl runner, DatIdMap datIdMap) {
		super(factorySvc);
		this.dbInterface = dbInterface;
		this.runner = runner;
		this.registry = registry;
		this.fetchRunner = fetchRunner;
		this.zexec = zexec;
		this.hldManager = hldManager;
		this.scalarBuilder = new ScalarBuilder(factorySvc, registry);
		this.datIdMap = datIdMap;
	}

	private ValidationRunner createValidationRunner() {
		return factorySvc.createValidationRunner(dbInterface, fetchRunner);
	}

	private void addError(ResultValue res, String id, String msg) {
		DeliaError error = et.add(id, msg);
		res.errors.add(error);
		res.ok = false;
	}

	public ResultValue executeLetStatement(LetStatementExp exp, ResultValue res) {
		this.letSpanEngine = new LetSpanEngine(factorySvc, registry);
		
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
			VarRef varRef = resolveScalarVarReference(queryExp);
			if (varRef != null) {
				res.ok = true;
				res.shape = null;
//				runQueryFnsIfNeeded(queryExp, varRef.qresp, res);

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
			runValidation(queryExp, qresp, res);

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
		return executeDBQuery(queryExp, null);
	}
	private QueryResponse executeDBQuery(QueryExp queryExp, QueryResponse existingQResp) {
		QuerySpec spec = resolveFilterVars(queryExp);
		QueryContext qtx = buildQueryContext(spec, existingQResp);
		
		boolean flag1 = hldManager != null;
		QueryResponse qresp;
		if (flag1) {
			HLDQueryStatement hld = buildHLDQuery(spec, queryExp);
			SqlStatementGroup stgroup = hldManager.generateSqlForQuery(hld, zexec);

			qresp = zexec.executeHLDQuery(hld, stgroup, qtx); //** calll the db **
			doPostDBCallAdjustment(hld, qresp);
//			HLSManagerResult result = new HLSManagerResult();
//			result.qresp = qresp;
//			result.sql = stgroup.statementL.get(0).sql;

			mostRecentStatment = hld;
		} else { //mainly for legacy unit tests
			HLDSimpleQueryService querySvc = factorySvc.createHLDSimpleQueryService(dbInterface, registry);
			qresp = querySvc.execQueryEx(queryExp, zexec, spec.evaluator.getVarEvaluator());
		}
		return qresp;
	}
	private void doPostDBCallAdjustment(HLDQueryStatement hld, QueryResponse qresp) {
		//TODO: this is not quite correct. can exists be present but not the last fn
		Optional<QueryFnSpec> opt = hld.hldquery.funcL.stream().filter(x ->x.isFn("exists")).findAny();
		if (opt.isPresent() && qresp.ok) {
			if (qresp.emptyResults()) {
				ScalarValueBuilder builder = factorySvc.createScalarValueBuilder(registry);
				DValue dval = builder.buildBoolean(false);
				qresp.dvalList.add(dval);
				return;
			}
			boolean b = false; //if at least one is true then exists returns true
			for(DValue dval: qresp.dvalList) {
				if (dval.asBoolean()) {
					qresp.dvalList.clear();
					qresp.dvalList.add(dval);
					return;
				}
			}
		}
		
		
		// TODO Auto-generated method stub
		
	}

	private HLDQueryStatement buildHLDQuery(QuerySpec spec, QueryExp queryExp) {
		spec.queryExp = queryExp;
		HLDQueryStatement hld = hldManager.buildQueryStatement(spec, zexec, runner);
		return hld;
	}
	
	
	private QueryContext buildQueryContext(QuerySpec spec, QueryResponse existingQResp) {
		QueryContext qtx = new QueryContext();
		qtx.existingQResp = existingQResp;
		qtx.letSpanEngine = letSpanEngine;
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
		innerRunner.setDatIdMap(datIdMap);
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

//	private QueryResponse runLetSpanEngine(QueryExp queryExp, QueryResponse qresp) {
//		if (dbInterface.getCapabilities().supportsOffsetAndLimit()) {
//			return qresp; //don't need span engine. db does it.
//		}
//		//			boolean flag = true;
//		//			if (flag) {
//		QueryResponse qresp2 = letSpanEngine.process(queryExp, qresp, spanRunner);
//		return qresp2;
//		//			} else {
//		//				QueryResponse qresp2 = this.qffRunner.process(queryExp, qresp);
//		//				return qresp2;
//		//			}
//	}
	private void runValidation(QueryExp queryExp, QueryResponse qresp, ResultValue res) {
		//validate (assume that we don't fully trust db storage - someone may have tampered with data)
		if (qresp.ok && CollectionUtils.isNotEmpty(qresp.dvalList)) {
			ValidationRunner ruleRunner = createValidationRunner();
			if (mostRecentStatment != null) {
				HLDQuery hld = mostRecentStatment.hldquery;
				if (hld.fetchL.isEmpty()) {
					ruleRunner.setSoftMandatoryRelationFlag(true);
				}
			}
			
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
				
				//resolve varref to parseable query
				if (qresp.emptyResults()) {
					if (queryExp.qfelist.size() > 0) {
						String msg = String.format("var '%s' is null. cannot be evaluationed", varRef.varRef);
						DeliaError err = et.add("var-ref-is-null", msg);
						throw new DeliaException(err);
					}
					
					varRef.qresp = qresp.dvalList == null ? null : qresp;
					return varRef;
				} else {
					if (! hldManager.canBuildHLD(queryExp, zexec, runner)) {
						//usually means statement is scalar. let x = 5
						return doVarRef(varRef, qresp, qresp.dvalList);
					}

					QuerySpec spec = resolveFilterVars(queryExp);
					HLDQueryStatement hld = buildHLDQuery(spec, queryExp);
					//TODO: improve. this works for .wid but not .addr.wid.x.y
					if (hld.hldquery.finalField != null) {
						String fieldName = hld.hldquery.finalField.structField.fieldName; 
						
						List<DValue> newlist = new ArrayList<>();
						for(DValue listel: qresp.dvalList) {
							DValue inner = listel.asStruct().getField(fieldName);
							newlist.add(inner);
						}
						
						return properVarRef(varRef, hld, qresp, newlist);
					} else {
						return properVarRef(varRef, hld, qresp, qresp.dvalList);
					}
				}
			} else if (res.val == null) { //handle null values
				varRef.dval = null;
				varRef.nullShape = res.shape;
				return varRef;
			}
		}
		return null;
	}
	private VarRef properVarRef(VarRef varRef, HLDQueryStatement hld, QueryResponse qresp, List<DValue> newlist) {
		if (hld.hldquery.resultType.isStructShape()) {
			qresp.dvalList = newlist;
			varRef.qresp = qresp;
			return varRef;
		} else {
			return doVarRef(varRef, qresp, newlist);
		}
	}
	private VarRef doVarRef(VarRef varRef, QueryResponse qresp, List<DValue> newlist) {
		if (newlist.isEmpty()) {
			varRef.dval = null;
			return varRef;
//		} else if (newlist.size() == 1) {  no. leave lists as lists
//			varRef.dval = newlist.get(0);
//			return varRef;
		} else {
			qresp.dvalList = newlist;
			varRef.qresp = qresp;
			return varRef;
		}
	}

//	private VarRef resolveVarReference(QueryExp queryExp) {
//		ResultValue res = runner.varMap.get(queryExp.typeName);
//		if (res == null) {
//			return null;
//		}
//
//		VarRef varRef = new VarRef();
//		varRef.varRef = queryExp.typeName;
//
//		QueryResponse qresp = (QueryResponse) res.val;
//		varRef.qresp = qresp;
//		return varRef;
//	}

	private DValue toObject(Exp valueExp, LetStatementExp exp, ResultValue res) {
		int numErr = et.errorCount();
		DValue dval = scalarBuilder.buildDValue(valueExp, exp.isTypeExplicit ? exp.typeName : null);
		if (et.errorCount() != numErr) {
			SimpleErrorTracker set = (SimpleErrorTracker) et; //hack hack hack
			List<DeliaError> list = set.getErrorsSinceMark(numErr);
			res.errors.addAll(list);
		}
		if (dval != null) {
			ValidationRunner ruleRunner = createValidationRunner();
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