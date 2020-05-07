package org.delia.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.ConfigureStatementExp;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.EndSourceStatementExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.NullExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UpsertStatementExp;
import org.delia.compiler.ast.UserFnCallExp;
import org.delia.compiler.ast.UserFunctionDefStatementExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.core.ConfigureService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBException;
import org.delia.db.DBExecutor;
import org.delia.db.DBHelper;
import org.delia.db.DBInterface;
import org.delia.db.DBValidationException;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.error.DeliaError;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.queryresponse.function.QueryFuncOrFieldRunner;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
import org.delia.sprig.SprigVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.util.PrimaryKeyHelperService;
import org.delia.validation.ValidationRuleRunner;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.zqueryresponse.LetSpanEngine;
import org.delia.zqueryresponse.LetSpanRunnerImpl;

/**
 * This class is not thread-safe. Only use it as a local var.
 * @author Ian Rae
 *
 */
public class RunnerImpl extends ServiceBase implements Runner {
		public static final String DOLLAR_DOLLAR = "$$";
		public static final String VAR_SERIAL = "_serial";
		private Map<String,ResultValue> varMap = new HashMap<>(); //ok for thread-safety
		protected DTypeRegistry registry;
		private DBInterface dbInterface;
		private DBExecutor dbexecutor;
		private QueryFuncOrFieldRunner qffRunner;
		private LetSpanEngine letSpanEngine;
		protected FetchRunner fetchRunner;
		private Map<String,UserFunctionDefStatementExp> userFnMap = new HashMap<>(); //ok for thread-safety
		private Map<String,InputFunctionDefStatementExp> inputFnMap = new HashMap<>(); //ok for thread-safety
		private Map<String,String> activeUserFnMap = new HashMap<>(); //what's executing.  //ok for thread-safety
		private ScalarBuilder scalarBuilder;
		private SprigService sprigSvc;
		private DValueIterator insertPrebuiltValueIterator;
		private FetchRunner prebuiltFetchRunnerToUse;

		public RunnerImpl(FactoryService factorySvc, DBInterface dbInterface) {
			super(factorySvc);
			this.dbInterface = dbInterface;
		}
		@Override
		public Log getLog() {
			return log;
		}
		@Override
		public DeliaGeneratePhase createGenerator() {
			return new DeliaGeneratePhase(factorySvc, registry);
		}
		@Override
		public InternalCompileState getCompileState() {
			InternalCompileState ctx = new InternalCompileState();
			for(String typeName: registry.getAll()) {
				ctx.compiledTypeMap.put(typeName, buildFieldList(typeName));
			}
			ctx.delcaredVarMap.putAll(this.varMap);
			ctx.declaredUserFnMap.putAll(this.userFnMap);
			ctx.declaredInputFnMap.putAll(this.inputFnMap);
			return ctx;
		}
		@Override
		public ExecutionState getExecutionState() {
			//make copies of varmap and userFnMap
			ExecutionState ctx = new ExecutionState();
			ctx.registry = registry;
			ctx.varMap.putAll(this.varMap);
			ctx.userFnMap.putAll(this.userFnMap);
			ctx.inputFnMap.putAll(this.inputFnMap);
			ctx.generator = this.createGenerator();
			ctx.sprigSvc = sprigSvc;
			return ctx;
		}
		
		private TypeSpec buildFieldList(String typeName) {
			DType type = registry.getType(typeName);
			TypeSpec spec = new TypeSpec();
			spec.fieldL = new ArrayList<>();
			spec.baseTypeName = type.getBaseType() == null ? null : type.getBaseType().getName();
			if (! type.isStructShape()) {
				return spec;
			}
			DStructType dtype = (DStructType) registry.getType(typeName);
			for(String key: dtype.getDeclaredFields().keySet()) {
				spec.fieldL.add(key);
			}
			return spec;
		}
		@Override
		public boolean init(ExecutionState ctx) {
			if (ctx == null) {
				DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
				registryBuilder.init();
				this.registry = registryBuilder.getRegistry();
				this.sprigSvc = new SprigServiceImpl(factorySvc, registry);
			} else {
				this.registry = ctx.registry;
				this.varMap = ctx.varMap;
				this.userFnMap = ctx.userFnMap;
				this.inputFnMap = ctx.inputFnMap;
				this.sprigSvc = ctx.sprigSvc;
			}
			
			this.scalarBuilder = new ScalarBuilder(factorySvc, registry);
			
			return true;
		}
		
		@Override
		public TypeRunner createTypeRunner() {
			TypeRunner typeRunner = new TypeRunner(factorySvc, registry);
			return typeRunner;
		}

		@Override
		public ResultValue executeProgram(List<Exp> expL) {
			ResultValue res = null;
			DBAccessContext dbctx = new DBAccessContext(registry, this);
			this.dbexecutor = dbInterface.createExector(dbctx);
			this.fetchRunner = prebuiltFetchRunnerToUse != null ? prebuiltFetchRunnerToUse : dbexecutor.createFetchRunner(factorySvc);
			this.qffRunner = new QueryFuncOrFieldRunner(factorySvc, registry, fetchRunner, dbInterface.getCapabilities());
			LetSpanRunnerImpl spanRunner = new LetSpanRunnerImpl(factorySvc, registry, fetchRunner);
			this.letSpanEngine = new LetSpanEngine(factorySvc, registry, fetchRunner, spanRunner);

			try {
				for(Exp exp: expL) {
					res = executeStatement(exp);
					//stop on error
					if (! res.ok) {
						return res;
					}
				}
			} finally {
				if (dbexecutor != null) {
					try {
						dbexecutor.close();
					} catch (Exception e) {
						DBHelper.handleCloseFailure(e);
					}
				}
			}
			return res;
		}
		@Override
		public ResultValue executeOneStatement(Exp exp) {
			return executeProgram(Collections.singletonList(exp));
		}
		
		public ResultValue executeStatement(Exp exp) {
			log.logDebug("exec: " + exp.toString());
			ResultValue res = new ResultValue();
			if (exp instanceof TypeStatementExp) {
				executeTypeStatement((TypeStatementExp)exp, res);
			} else if (exp instanceof LetStatementExp) {
				executeLetStatement((LetStatementExp)exp, res);
			} else if (exp instanceof InsertStatementExp) {
				executeInsertStatement((InsertStatementExp)exp, res);
			} else if (exp instanceof UpdateStatementExp) {
				executeUpdateStatement((UpdateStatementExp)exp, res);
			} else if (exp instanceof UpsertStatementExp) {
				executeUpsertStatement((UpsertStatementExp)exp, res);
			} else if (exp instanceof DeleteStatementExp) {
				executeDeleteStatement((DeleteStatementExp)exp, res);
			} else if (exp instanceof UserFunctionDefStatementExp) {
				executeUserFuncDefStatement((UserFunctionDefStatementExp)exp, res);
			} else if (exp instanceof EndSourceStatementExp) {
				executeEndSource((EndSourceStatementExp)exp, res); //TODO: what is this??
			} else if (exp instanceof ConfigureStatementExp) {
				executeConfigureStatement((ConfigureStatementExp)exp, res);
			} else if (exp instanceof InputFunctionDefStatementExp) {
				executeInputFuncDefStatement((InputFunctionDefStatementExp)exp, res);
			}
			
			return res;
		}
		
		private void executeConfigureStatement(ConfigureStatementExp exp, ResultValue res) {
			ConfigureService configSvc = factorySvc.getConfigureService();
			try {
				configSvc.execute(exp, registry, sprigSvc);
				res.ok = true;
			} catch (DeliaException e) {
				res.ok = false;
				res.errors.add(e.getLastError());
			}
		}
		private void executeEndSource(EndSourceStatementExp exp, ResultValue res) {
			ValidationRuleRunner ruleRunner = createValidationRunner(); 
			//TODO: hmm. need to validate insert/update dvals!!
			if (! ruleRunner.validateEndSource()) {
				ruleRunner.propogateErrors(res);
			}
			
			if (!res.errors.isEmpty()) {
				res.ok = false;
			}
		}
		private ValidationRuleRunner createValidationRunner() {
			return new ValidationRuleRunner(factorySvc, dbInterface.getCapabilities(), fetchRunner);
		}
		private void executeUserFuncDefStatement(UserFunctionDefStatementExp exp, ResultValue res) {
			//TODO pass2 should ensure same fn not defined twice
			userFnMap.put(exp.funcName, exp);
			res.ok = true;
			res.shape = null;
			res.val = null;
		}
		private void executeInputFuncDefStatement(InputFunctionDefStatementExp exp, ResultValue res) {
			//TODO pass2 should ensure same fn not defined twice
			inputFnMap.put(exp.funcName, exp);
			res.ok = true;
			res.shape = null;
			res.val = null;
		}
		private void executeTypeStatement(TypeStatementExp exp, ResultValue res) {
			//code moved to TypeRunner
			res.ok = true;
		}

		private void executeUpdateStatement(UpdateStatementExp exp, ResultValue res) {
			//find DType for typename Actor
			DType dtype = registry.getType(exp.getTypeName());
			if (failIfNull(dtype, exp.typeName, res)) {
				return;
			} else if (failIfNotStruct(dtype, exp.typeName, res)) {
				return;
			}
			
			if (dtype == null) {
				addError(res, "type.not.found", String.format("can't find type '%s'", exp.getTypeName()));
				return;
			}
			
			//get list of changed fields
			ConversionResult cres = buildPartialValue((DStructType) dtype, exp.dsonExp);
			if (cres.dval == null) {
				res.errors.addAll(cres.localET.getErrors());
				res.ok = false;
				return;
			} else {
				//validate the fields of the partial DValue
				ValidationRuleRunner ruleRunner = createValidationRunner();
				if (! ruleRunner.validateFieldsOnly(cres.dval)) {
					ruleRunner.propogateErrors(res);
				}
				
				//then validate the affected rules (of the struct)
				//We determine the rules dependent on each field in partial dval
				//and execute those rules only
				if (! ruleRunner.validateDependentRules(cres.dval)) {
					ruleRunner.propogateErrors(res);
				}

				if (!res.errors.isEmpty()) {
					res.ok = false;
					return;
				}
			}
			
			try {
				QuerySpec spec = resolveFilterVars(exp.queryExp);
				int numRowsAffected = dbexecutor.executeUpdate(spec, cres.dval, cres.assocCrudMap);
				
				res.ok = true;
				res.shape = Shape.INTEGER;
				res.val = numRowsAffected;
			} catch (DBException e) {
				res.errors.add(e.getLastError());
				res.ok = false;
				return;
			}
		}
		private void executeUpsertStatement(UpsertStatementExp exp, ResultValue res) {
			//find DType for typename Actor
			DType dtype = registry.getType(exp.getTypeName());
			if (failIfNull(dtype, exp.typeName, res)) {
				return;
			} else if (failIfNotStruct(dtype, exp.typeName, res)) {
				return;
			}
			
			if (dtype == null) {
				addError(res, "type.not.found", String.format("can't find type '%s'", exp.getTypeName()));
				return;
			}
			
			//get list of changed fields
			ConversionResult cres = buildPartialValue((DStructType) dtype, exp.dsonExp);
			if (cres.dval == null) {
				res.errors.addAll(cres.localET.getErrors());
				res.ok = false;
				return;
			} else {
				cres.assocCrudMap = null; //clear. not supported for upsert
				
				//validate the fields of the partial DValue
				ValidationRuleRunner ruleRunner = createValidationRunner();
				ruleRunner.enableRelationModifier(true);
				ruleRunner.enableInsertFlag(true);
				ConfigureService configSvc = factorySvc.getConfigureService();
				
				//upsert doesn't have primary key in field set, so temporarily add it
				//so we can run validation 
				PrimaryKeyHelperService pkSvc = new PrimaryKeyHelperService(factorySvc, registry);
				QuerySpec spec = resolveFilterVars(exp.queryExp);
				boolean addedPK = pkSvc.addPrimaryKeyIfMissing(spec, cres.dval);

				ruleRunner.setPopulateFKsFlag(configSvc.isPopulateFKsFlag());
				if (! ruleRunner.validateDVal(cres.dval)) {
					ruleRunner.propogateErrors(res);
				}
				
				if (addedPK) {
					pkSvc.removePrimayKey(cres.dval);
				}
				
				
//				if (! ruleRunner.validateFieldsOnly(cres.dval)) {
//					ruleRunner.propogateErrors(res);
//				}
//				
//				//then validate the affected rules (of the struct)
//				//We determine the rules dependent on each field in partial dval
//				//and execute those rules only
//				if (! ruleRunner.validateDependentRules(cres.dval)) {
//					ruleRunner.propogateErrors(res);
//				}

				if (!res.errors.isEmpty()) {
					res.ok = false;
					return;
				}
			}
			
			try {
				QuerySpec spec = resolveFilterVars(exp.queryExp);
				boolean noUpdateFlag = exp.optionExp != null;
				int numRowsAffected = dbexecutor.executeUpsert(spec, cres.dval, cres.assocCrudMap, noUpdateFlag);
				
				res.ok = true;
				res.shape = Shape.INTEGER;
				res.val = numRowsAffected;
			} catch (DBException e) {
				res.errors.add(e.getLastError());
				res.ok = false;
				return;
			}
		}
		private void executeDeleteStatement(DeleteStatementExp exp, ResultValue res) {
			//find DType for typename Actor
			DType dtype = registry.getType(exp.getTypeName());
			if (failIfNull(dtype, exp.typeName, res)) {
				return;
			} else if (failIfNotStruct(dtype, exp.typeName, res)) {
				return;
			}
			
			if (dtype == null) {
				addError(res, "type.not.found", String.format("can't find type '%s'", exp.getTypeName()));
				return;
			}
			
			try {
				QuerySpec spec = this.resolveFilterVars(exp.queryExp);
				dbexecutor.executeDelete(spec);
			} catch (DBException e) {
				res.errors.add(e.getLastError());
				res.ok = false;
				return;
			}
			
			//DELETE has no return value
			res.ok = true;
			res.shape = null;
			res.val = null;
		}

		private void addError(ResultValue res, String id, String msg) {
			DeliaError error = et.add(id, msg);
			res.errors.add(error);
			res.ok = false;
		}

		private void executeInsertStatement(InsertStatementExp exp, ResultValue res) {
			//find DType for typename Actor
			DType dtype = registry.getType(exp.getTypeName());
			if (failIfNull(dtype, exp.typeName, res)) {
				return;
			} else if (failIfNotStruct(dtype, exp.typeName, res)) {
				return;
			}
			
			//execute db insert
			ConversionResult cres = buildValue((DStructType) dtype, exp.dsonExp);
			if (cres.dval == null) {
				res.errors.addAll(cres.localET.getErrors());
				res.ok = false;
				return;
			} else {
				ValidationRuleRunner ruleRunner = createValidationRunner();
				ruleRunner.enableRelationModifier(true);
				ruleRunner.enableInsertFlag(true);
				ConfigureService configSvc = factorySvc.getConfigureService();

				ruleRunner.setPopulateFKsFlag(configSvc.isPopulateFKsFlag());
				if (! ruleRunner.validateDVal(cres.dval)) {
					ruleRunner.propogateErrors(res);
				}
				
				if (!res.errors.isEmpty()) {
					res.ok = false;
					return;
				}
			}
			
			try {
				String typeName = cres.dval.getType().getName();
				InsertContext ctx = new InsertContext();
				boolean hasSerialId = DValueHelper.typeHasSerialPrimaryKey(cres.dval.getType());
				if (hasSerialId) {
					ctx.extractGeneratedKeys = true;
					ctx.genKeytype = DValueHelper.findPrimaryKeyFieldPair(cres.dval.getType()).type;
					DValue generatedId = dbexecutor.executeInsert(cres.dval, ctx);
					assignSerialVar(generatedId);
					boolean sprigFlag = sprigSvc.haveEnabledFor(typeName);
					if (sprigFlag) {
						sprigSvc.rememberSynthId(typeName, cres.dval, generatedId, cres.extraMap);
					}
				} else {
					dbexecutor.executeInsert(cres.dval, ctx);
				}
				
			} catch (DBException e) {
				res.errors.add(e.getLastError());
				res.ok = false;
				return;
			} catch (DBValidationException e) {
				//TODO detect which field(s) failed and convert to a validation error
				res.errors.add(e.getLastError());
				res.ok = false;
				return;
			}
			
			//INSERT has no return value
			res.ok = true;
			res.shape = null;
			res.val = null;
		}
		private boolean failIfNotStruct(DType dtype, String typeName, ResultValue res) {
			if (! dtype.isStructShape()) {
				addError(res, "type.not.struct", String.format("cannot insert a scalar type '%s'", typeName));
				return true;
			}
			return false;
		}
		private boolean failIfNull(DType dtype, String typeName, ResultValue res) {
			if (dtype == null) {
				addError(res, "type.not.found", String.format("can't find type '%s'", typeName));
				return true;
			}
			return false;
		}
		private ConversionResult buildValue(DStructType dtype, DsonExp dsonExp) {
			ConversionResult cres = new ConversionResult();
			cres.localET = new SimpleErrorTracker(log);
			if (insertPrebuiltValueIterator != null) {
				cres.dval = insertPrebuiltValueIterator.next();
				return cres;
			}
			
			//TODO need local error tracker!!
			
			VarEvaluator varEvaluator = this;
//			if (sprigSvc.haveEnabledFor(dtype.getName())) {
				varEvaluator = new SprigVarEvaluator(factorySvc, this);
//			}
			
			DsonToDValueConverter converter = new DsonToDValueConverter(factorySvc, cres.localET, registry, varEvaluator, sprigSvc);
			cres.dval = converter.convertOne(dtype.getName(), dsonExp, cres);
			return cres;
		}
		private ConversionResult buildPartialValue(DStructType dtype, DsonExp dsonExp) {
			ConversionResult cres = new ConversionResult();
			cres.localET = new SimpleErrorTracker(log);
			if (insertPrebuiltValueIterator != null) {
				cres.dval = insertPrebuiltValueIterator.next();
				return cres;
			}
			
			DsonToDValueConverter converter = new DsonToDValueConverter(factorySvc, cres.localET, registry, this, sprigSvc);
			cres.dval = converter.convertOnePartial(dtype.getName(), dsonExp);
			cres.assocCrudMap = converter.getAssocCrudMap();
			return cres;
		}

		private ResultValue executeLetStatement(LetStatementExp exp, ResultValue res) {
//			if (exp.varName.equals(DOLLAR_DOLLAR)) {
//				ResultValue previousRes = varMap.get(DOLLAR_DOLLAR);
//				if (previousRes == null) {
//					DeliaError err = et.add("dollar-dollar-not-set", "There is no previous statement. Cannot use $$");
//					throw new DeliaException(err);
//				}
//				res.copyFrom(previousRes);
//				return res;
//			}
			
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
				
				QuerySpec spec = resolveFilterVars(queryExp);
				QueryContext qtx = buildQueryContext(spec);
				QueryResponse qresp = dbexecutor.executeQuery(spec, qtx);
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
		
		private QueryContext buildQueryContext(QuerySpec spec) {
			QueryFuncContext ctx = new QueryFuncContext();
			this.qffRunner.buildPendingTrail(ctx, spec.queryExp);
			
			QueryContext qtx = new QueryContext();
			//TODO: fix buglet that is other fn contains 'fks' this won't work
			qtx.loadFKs = ctx.pendingTrail.getTrail().contains("fks");
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
			spec.evaluator = new FilterEvaluator(factorySvc, this);
			spec.evaluator.init(queryExp);
			return spec;
		}
		private ResultValue invokeUserFunc(LetStatementExp exp, ResultValue resParam) {
			RunnerImpl innerRunner = new RunnerImpl(factorySvc, dbInterface);
			ExecutionState execState = getExecutionState();
			execState.varMap.clear(); //user fn has its own variables
	
			boolean b = innerRunner.init(execState);
			if (!b) {
				//err
				return resParam;
			}
			
			UserFnCallExp callExp = (UserFnCallExp) exp.value;
			
			UserFunctionDefStatementExp userFnExp = this.userFnMap.get(callExp.funcName);
			if (userFnExp == null) {
				//error
				return resParam;
			}
			
			//avoid stack overflow. fn can't call itself
			if (activeUserFnMap.containsKey(userFnExp.funcName)) {
				DeliaError err = et.add("user-func-self-invoke", "A user function may not invoke itself - function %s", userFnExp.funcName);
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
			boolean flag = true;
			if (flag) {
				QueryResponse qresp2 = this.letSpanEngine.process(queryExp, qresp);
				return qresp2;
			} else {
				QueryResponse qresp2 = this.qffRunner.process(queryExp, qresp);
				return qresp2;
			}
		}
		public void runQueryFnsIfNeeded(QueryExp queryExp, QueryResponse qresp, ResultValue res) {
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
			if (! varName.equals(DOLLAR_DOLLAR) && exists(varName)) {
				addError(res, "var-already-exists", String.format("variable '%s' already exists. Cannot re-assign", varName));
				return;
			}
			
			res.varName = varName;
			varMap.put(varName, res);
			varMap.put(DOLLAR_DOLLAR, res);
		}
		private void assignSerialVar(DValue generatedId) {
			ResultValue res = new ResultValue();
			res.ok = true;
			res.shape = generatedId.getType().getShape();
			res.val = generatedId;
			res.varName = VAR_SERIAL;
			varMap.put(VAR_SERIAL, res);
		}

		private VarRef resolveScalarVarReference(QueryExp queryExp) {
			ResultValue res = varMap.get(queryExp.typeName);
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
					QueryResponse qresp2 = runLetSpanEngine(queryExp, qresp);
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
			ResultValue res = varMap.get(queryExp.typeName);
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
		
		@Override
		public boolean exists(String varName) {
			return varMap.containsKey(varName);
		}
		@Override
		public ResultValue getVar(String varName) {
			return varMap.get(varName);
		}

		@Override
		public DTypeRegistry getRegistry() {
			return registry;
		}
		
		//-varevaluator-
		@Override
		public List<DValue> lookupVar(String varName) {
			ResultValue res = varMap.get(varName);
			if (res == null) {
				//err!!
				return null;
			}
			
			if (res.val instanceof DValue) {
				DValue dval = (DValue) res.val;
				return Collections.singletonList(dval);
			}
			
			QueryResponse qresp = (QueryResponse) res.val;
			return qresp.dvalList;
		}
		@Override
		public String evalVarAsString(String varName, String typeName) {
			ResultValue res = varMap.get(varName);
			if (res == null) {
				DeliaExceptionHelper.throwError("unknown-variable", "Can't find variable '%s", varName);
			}
			
			if (res.val == null) {
				return null;
			} else if (res.val instanceof DValue) {
				return res.getAsDValue().asString(); //TODO: handle null later
			}
			QueryResponse qresp = (QueryResponse) res.val;
			return qresp.getOne().asString();
		}
		@Override
		public SprigService getSprigSvc() {
			return sprigSvc;
		}
		@Override
		public void setInsertPrebuiltValueIterator(DValueIterator insertPrebuiltValueIterator) {
			this.insertPrebuiltValueIterator = insertPrebuiltValueIterator;
		}
		public Map<String, InputFunctionDefStatementExp> getInputFnMap() {
			return inputFnMap;
		}
		@Override
		public FetchRunner getPrebuiltFetchRunnerToUse() {
			return prebuiltFetchRunnerToUse;
		}
		@Override
		public void setPrebuiltFetchRunnerToUse(FetchRunner prebuiltFetchRunnerToUse) {
			this.prebuiltFetchRunnerToUse = prebuiltFetchRunnerToUse;
		}
	}