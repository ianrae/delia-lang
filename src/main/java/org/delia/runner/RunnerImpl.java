package org.delia.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.ConfigureStatementExp;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UpsertStatementExp;
import org.delia.compiler.ast.UserFunctionDefStatementExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.core.ConfigureService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBException;
import org.delia.db.DBHelper;
import org.delia.db.QuerySpec;
import org.delia.db.SqlStatementGroup;
import org.delia.error.DeliaError;
import org.delia.hld.HLDFacade;
import org.delia.hld.HLDFactory;
import org.delia.hld.cud.HLDDeleteStatement;
import org.delia.log.Log;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

/**
 * This class is not thread-safe. Only use it as a local var.
 * @author Ian Rae
 *
 */
public class RunnerImpl extends ServiceBase implements Runner {
		public static final String DOLLAR_DOLLAR = "$$";
		public static final String VAR_SERIAL = "_serial";
		Map<String,ResultValue> varMap = new HashMap<>(); //ok for thread-safety
		protected DTypeRegistry registry;
		private DBInterfaceFactory dbInterface;
		private DBExecutor dbexecutor;
//		private ZDBExecutor zexec;
		protected FetchRunner fetchRunner;
		Map<String,UserFunctionDefStatementExp> userFnMap = new HashMap<>(); //ok for thread-safety
		private Map<String,InputFunctionDefStatementExp> inputFnMap = new HashMap<>(); //ok for thread-safety
		Map<String,String> activeUserFnMap = new HashMap<>(); //what's executing.  //ok for thread-safety
		private SprigService sprigSvc;
		private DValueIterator insertPrebuiltValueIterator;
		private FetchRunner prebuiltFetchRunnerToUse;
		private LetStatementRunner letStatementRunner;
		private InsertStatementRunner insertStatementRunner;
		private HLDFacade hldFacade;
		private DatIdMap datIdMap;
		private UpdateStatementRunner updateStatementRunner;
		private HLDFactory hldFactory;
		
		public RunnerImpl(FactoryService factorySvc, DBInterfaceFactory dbInterface, HLDFactory hldFactory) {
			super(factorySvc);
			this.dbInterface = dbInterface;
			this.hldFactory = hldFactory;
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
			this.insertStatementRunner = new InsertStatementRunner(factorySvc, dbInterface, hldFactory, this, registry, varMap);
			this.updateStatementRunner = new UpdateStatementRunner(factorySvc, dbInterface, hldFactory, this, registry);

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
			this.dbexecutor = dbInterface.createExecutor();
//			this.zexec = factorySvc.hackGetZDB(registry, dbInterface.getDBType());
			dbexecutor.init1(registry);
			dbexecutor.init2(datIdMap, this);
			
			this.fetchRunner = prebuiltFetchRunnerToUse != null ? prebuiltFetchRunnerToUse : dbexecutor.createFetchRunner();
//			this.qffRunner = new QueryFuncOrFieldRunner(factorySvc, registry, fetchRunner, dbInterface.getCapabilities());
//			LetSpanRunnerImpl spanRunner = new LetSpanRunnerImpl(factorySvc, registry, fetchRunner);
//			this.letSpanEngine = new LetSpanEngine(factorySvc, registry, fetchRunner, spanRunner);

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
		private void executeUserFuncDefStatement(UserFunctionDefStatementExp exp, ResultValue res) {
			userFnMap.put(exp.funcName, exp);
			res.ok = true;
			res.shape = null;
			res.val = null;
		}
		private void executeInputFuncDefStatement(InputFunctionDefStatementExp exp, ResultValue res) {
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
			updateStatementRunner.executeUpdateStatement(exp, res, hldFacade, dbexecutor, fetchRunner, insertPrebuiltValueIterator, sprigSvc);
		}
		private void executeUpsertStatement(UpsertStatementExp exp, ResultValue res) {
			updateStatementRunner.executeUpsertStatement(exp, res, hldFacade, dbexecutor, fetchRunner, insertPrebuiltValueIterator, sprigSvc);
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
				if (hldFacade != null) {
					HLDDeleteStatement hld = hldFacade.buildHLD(exp, dbexecutor);
					SqlStatementGroup stmgrp = hldFacade.generateSQL(hld, dbexecutor);
					
					dbexecutor.executeDelete(hld, stmgrp);
				} else {
					QuerySpec spec = this.resolveFilterVars(exp.queryExp);
					DeliaExceptionHelper.throwNotImplementedError("sdf");
//					dbexecutor.executeDelete(spec);
				}
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
			insertStatementRunner.executeInsertStatement(exp, res, hldFacade, dbexecutor, fetchRunner, insertPrebuiltValueIterator, sprigSvc);
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
		private ResultValue executeLetStatement(LetStatementExp exp, ResultValue res) {
			this.letStatementRunner = new LetStatementRunner(factorySvc, dbInterface, hldFactory, dbexecutor, registry, fetchRunner, hldFacade, this, datIdMap);
			return letStatementRunner.executeLetStatement(exp, res);
		}
		
		private QuerySpec resolveFilterVars(QueryExp queryExp) {
			QuerySpec spec = new QuerySpec();
			spec.queryExp = queryExp;
			spec.evaluator = new FilterEvaluator(factorySvc, this);
			spec.evaluator.init(queryExp);
			return spec;
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
				return res.getAsDValue().asString(); 
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
		@Override
		public void setDatIdMap(DatIdMap datIdMap) {
			this.datIdMap = datIdMap;
		}
		@Override
		public void setHLDFacade(HLDFacade mgr) {
			this.hldFacade = mgr;
			hldFacade.setSprigSvc(sprigSvc);
		}
	}