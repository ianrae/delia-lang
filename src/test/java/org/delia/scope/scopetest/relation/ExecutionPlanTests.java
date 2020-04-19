package org.delia.scope.scopetest.relation;
//NOT NEEDED -use optimization at level of dbInterface actions instead
//package org.dang.scope.scopetest.relation;
//
//import static org.junit.Assert.assertEquals;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.dang.compiler.DangCompiler;
//import org.dang.compiler.ast.DeleteStatementExp;
//import org.dang.compiler.ast.DsonExp;
//import org.dang.compiler.ast.Exp;
//import org.dang.compiler.ast.InsertStatementExp;
//import org.dang.compiler.ast.TypeStatementExp;
//import org.dang.compiler.ast.UpdateStatementExp;
//import org.dang.core.ServiceBase;
//import org.dang.db.DBInterface;
//import org.dang.db.memdb.MockDBInterface;
//import org.dang.error.DangError;
//import org.dang.error.ErrorTracker;
//import org.dang.error.SimpleErrorTracker;
//import org.dang.log.Log;
//import org.dang.parser.LetTests.LetStatementExp;
//import org.dang.parser.UserFnTests.UserFunctionDefStatementExp;
//import org.dang.runner.ConversionResult;
//import org.dang.runner.DangException;
//import org.dang.runner.DsonToDValueTests.DsonToDValueConverter;
//import org.dang.runner.FetchRunnerImpl;
//import org.dang.runner.NewExecutionContext;
//import org.dang.runner.QueryResponse;
//import org.dang.runner.ResultValue;
//import org.dang.runner.VarEvaluator;
//import org.dang.scope.scopetest.relation.RelationTests.DBSession;
//import org.dang.scope.scopetest.relation.RelationTests.Delia;
//import org.dang.scope.scopetest.relation.RelationTests.DeliaFactory;
//import org.dang.type.DRelation;
//import org.dang.type.DStructType;
//import org.dang.type.DType;
//import org.dang.type.DTypeRegistry;
//import org.dang.type.DTypeRegistryBuilder;
//import org.dang.type.DValue;
//import org.dang.type.TypePair;
//import org.dang.validation.ValidationRuleRunner;
//import org.junit.Before;
//import org.junit.Test;
//
//public class ExecutionPlanTests { 
//	
//	public static class PlannedStatment {
//		public Exp statementExp;
//		public DValue dval;
//	}
//	
//	public class PlanOptimizer extends ServiceBase {
//		
//		public PlanOptimizer(Log log, ErrorTracker et) {
//			super(log, et);
//		}
//		
//		public List<PlannedStatment> process(List<PlannedStatment> planL) {
//			
//			for(PlannedStatment stmt : planL) {
//				if (stmt.statementExp instanceof InsertStatementExp) {
//					doInsert(stmt);
//				}
//			}
//			return planL;
//		}
//
//		private void doInsert(PlannedStatment stmt) {
//			InsertStatementExp insertExp = (InsertStatementExp) stmt.statementExp;
//			if (! stmt.dval.getType().isStructShape()) {
//				return;
//			}
//			
//			DStructType dtype = (DStructType) stmt.dval.getType();
//			for(TypePair pair: dtype.getAllFields()) {
//				if (pair.type != null && pair.type.isStructShape()) {
//					log("found rel " + pair.name);
//					//if is child and list contains matching parent
//				}
//			}
//			
//		}
//		
//	}
//	
//	public class EPRunner extends ServiceBase implements VarEvaluator {
//		public static final String DOLLAR_DOLLAR = "$$";
//		private Map<String,ResultValue> varMap = new HashMap<>();
//		private DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
//		protected DTypeRegistry registry;
//		private DBInterface dbInterface;
//		protected FetchRunnerImpl fetchRunner;
//		private boolean initRegistryFlag;
//		private List<PlannedStatment> planL = new ArrayList<>();
//
//		public EPRunner(Log log, ErrorTracker et, DBInterface dbInterface) {
//			this(log, et, dbInterface, true);
//		}
//		public EPRunner(Log log, ErrorTracker et, DBInterface dbInterface, boolean initRegistryFlag) {
//			super(log, et);
//			this.dbInterface = dbInterface;
//			this.initRegistryFlag = initRegistryFlag;
//		}
//		
//		public List<PlannedStatment> getPlanList() {
//			return planL;
//		}
//		
//		public boolean init() {
//			return init(null);
//		}
//		public boolean init(NewExecutionContext ctx) {
//			if (ctx != null) {
//				this.registry = ctx.registry;
//				this.varMap = ctx.varMap;
//			} else {
//				if (initRegistryFlag) {
//					registryBuilder.init();
//				}
//				this.registry = registryBuilder.getRegistry();
//			}
//			
//			this.fetchRunner = new FetchRunnerImpl(log, et, dbInterface);
//			
//			return true;
//		}
//		
//		public ResultValue executeStatement(Exp exp) {
//			log.log("ep-exec: " + exp.toString());
//			ResultValue res = new ResultValue();
//			if (exp instanceof TypeStatementExp) {
//			} else if (exp instanceof LetStatementExp) {
//			} else if (exp instanceof InsertStatementExp) {
//				executeInsertStatement((InsertStatementExp)exp, res);
//			} else if (exp instanceof UpdateStatementExp) {
//				executeUpdateStatement((UpdateStatementExp)exp, res);
//			} else if (exp instanceof DeleteStatementExp) {
//				executeDeleteStatement((DeleteStatementExp)exp, res);
//			} else if (exp instanceof UserFunctionDefStatementExp) {
//			}
//			
//			return res;
//		}
//		
//		private void executeUpdateStatement(UpdateStatementExp exp, ResultValue res) {
//			//find DType for typename Actor
//			DType dtype = registry.getType(exp.getTypeName());
//			if (failIfNull(dtype, exp.typeName, res)) {
//				return;
//			} else if (failIfNotStruct(dtype, exp.typeName, res)) {
//				return;
//			}
//			
//			if (dtype == null) {
//				addError(res, "type.not.found", String.format("can't find type '%s'", exp.getTypeName()));
//				return;
//			}
//			
//			//get list of changed fields
//			ConversionResult cres = buildPartialValue((DStructType) dtype, exp.dsonExp);
//			if (cres.dval == null) {
//				res.errors.addAll(cres.localET.getErrors());
//				res.ok = false;
//				return;
//			} else {
//				//validate the fields of the partial DValue
//				ValidationRuleRunner ruleRunner = createValidationRunner();
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
//
//				if (!res.errors.isEmpty()) {
//					res.ok = false;
//					return;
//				}
//			}
//			
//			addPlannedStatement(exp, cres.dval);
//			res.ok = true;
////			try {
////				QuerySpec spec = resolveFilterVars(exp.queryExp);
////				int numRowsAffected = dbInterface.executeUpdate(spec, cres.dval);
////				
////				res.ok = true;
////				res.shape = Shape.INTEGER;
////				res.val = numRowsAffected;
////			} catch (DBException e) {
////				res.errors.add(e.err);
////				res.ok = false;
////				return;
////			}
//		}
//		private ValidationRuleRunner createValidationRunner() {
//			ValidationRuleRunner ruleRunner = new ValidationRuleRunner(log, et);
//			ruleRunner.setPlanModeFlg(true);
//			return ruleRunner;
//		}
//		private void addPlannedStatement(Exp exp, DValue dval) {
//			PlannedStatment stmt = new PlannedStatment();
//			stmt.statementExp = exp;
//			stmt.dval = dval;
//			planL.add(stmt);
//		}
//		private void executeDeleteStatement(DeleteStatementExp exp, ResultValue res) {
//			//find DType for typename Actor
//			DType dtype = registry.getType(exp.getTypeName());
//			if (failIfNull(dtype, exp.typeName, res)) {
//				return;
//			} else if (failIfNotStruct(dtype, exp.typeName, res)) {
//				return;
//			}
//			
//			if (dtype == null) {
//				addError(res, "type.not.found", String.format("can't find type '%s'", exp.getTypeName()));
//				return;
//			}
//			
//			addPlannedStatement(exp, null);
//			res.ok = true;
////			try {
////				QuerySpec spec = this.resolveFilterVars(exp.queryExp);
////				dbInterface.executeDelete(spec);
////			} catch (DBException e) {
////				res.errors.add(e.err);
////				res.ok = false;
////				return;
////			}
////			
////			//DELETE has no return value
////			res.ok = true;
////			res.shape = null;
////			res.val = null;
//		}
//
//		private void addError(ResultValue res, String id, String msg) {
//			DangError error = et.add(id, msg);
//			res.errors.add(error);
//			res.ok = false;
//		}
//
//		private void executeInsertStatement(InsertStatementExp exp, ResultValue res) {
//			//find DType for typename Actor
//			DType dtype = registry.getType(exp.getTypeName());
//			if (failIfNull(dtype, exp.typeName, res)) {
//				return;
//			} else if (failIfNotStruct(dtype, exp.typeName, res)) {
//				return;
//			}
//			
//			//execute db insert
//			ConversionResult cres = buildValue((DStructType) dtype, exp.dsonExp);
//			if (cres.dval == null) {
//				res.errors.addAll(cres.localET.getErrors());
//				res.ok = false;
//				return;
//			} else {
//				ValidationRuleRunner ruleRunner = createValidationRunner();
//				if (! ruleRunner.validateDVal(cres.dval)) {
//					ruleRunner.propogateErrors(res);
//				}
//				
//				if (!res.errors.isEmpty()) {
//					res.ok = false;
//					return;
//				}
//			}
//			
//			addPlannedStatement(exp, cres.dval);
//			res.ok = true;
////			try {
////				dbInterface.executeInsert(cres.dval);
////			} catch (DBException e) {
////				res.errors.add(e.err);
////				res.ok = false;
////				return;
////			}
////			
////			//INSERT has no return value
////			res.ok = true;
////			res.shape = null;
////			res.val = null;
//		}
//		private boolean failIfNotStruct(DType dtype, String typeName, ResultValue res) {
//			if (! dtype.isStructShape()) {
//				addError(res, "type.not.struct", String.format("cannot insert a scalar type '%s'", typeName));
//				return true;
//			}
//			return false;
//		}
//		private boolean failIfNull(DType dtype, String typeName, ResultValue res) {
//			if (dtype == null) {
//				addError(res, "type.not.found", String.format("can't find type '%s'", typeName));
//				return true;
//			}
//			return false;
//		}
//		private ConversionResult buildValue(DStructType dtype, DsonExp dsonExp) {
//			ConversionResult cres = new ConversionResult();
//			cres.localET = new SimpleErrorTracker(log);
//			DsonToDValueConverter converter = new DsonToDValueConverter(log, cres.localET, registry, this);
//			cres.dval = converter.convertOne(dtype.getName(), dsonExp);
//			return cres;
//		}
//		private ConversionResult buildPartialValue(DStructType dtype, DsonExp dsonExp) {
//			ConversionResult cres = new ConversionResult();
//			cres.localET = new SimpleErrorTracker(log);
//			DsonToDValueConverter converter = new DsonToDValueConverter(log, cres.localET, registry, this);
//			cres.dval = converter.convertOnePartial(dtype.getName(), dsonExp);
//			return cres;
//		}
//
//		public boolean exists(String varName) {
//			return varMap.containsKey(varName);
//		}
//		public ResultValue getVar(String varName) {
//			return varMap.get(varName);
//		}
//
//		public DTypeRegistry getRegistry() {
//			return registry;
//		}
//		
//		@Override
//		public List<DValue> lookupVar(String varName) {
//			ResultValue res = varMap.get(varName);
//			if (res == null) {
//				//err!!
//				return null;
//			}
//			QueryResponse qresp = (QueryResponse) res.val;
//			return qresp.dvalList;
//		}
//		@Override
//		public String evalVarAsString(String varName) {
//			ResultValue res = varMap.get(varName);
//			if (res == null) {
//				//err!!
//				return null;
//			}
//			
//			if (res.val == null) {
//				return null;
//			} else if (res.val instanceof DValue) {
//				return res.getAsDValue().asString(); //TODO: handle null later
//			}
//			QueryResponse qresp = (QueryResponse) res.val;
//			return qresp.getOne().asString();
//		}
//	}	
//
//	@Test
//	public void testOK() {
//		String src = createTypeSrc("Address", "relation cust Customer one");
//		src += createTypeSrc("Customer", "relation addr Address one optional");
//		execTypeStatement(src);
//		
//		src = "insert Customer { id:44 }";
////		ResultValue res = execStatement("let x1 = Customer[44]");
////		chkInt(res, "id", 44);
////		chkNullField(res, "addr");
//		
//		src += " insert Address { id:100, cust:44 }";
//		continueExecution(src);
//		assertEquals(2, planL.size());
//		assertEquals("Customer", planL.get(0).dval.getType().getName());
//		assertEquals("Address", planL.get(1).dval.getType().getName());
//
//		
////		res = execStatement("let x2 = Customer[44]");
////		chkInt(res, "id", 44);
//
////		res = execStatement("let x3 = Address[100]");
////		chkInt(res, "id", 100);
////		
////		chkRelation(res, "cust", 44, "Customer", false);
////		res = execStatement("let x4 = Customer[44]");
//	}
//	
//	@Test
//	public void testSwapOrder() {
//		String src = createTypeSrc("Address", "relation cust Customer one");
//		src += createTypeSrc("Customer", "relation addr Address one optional");
//		execTypeStatement(src);
//		
//		src = " insert Address { id:100, cust:44 }";
//		src += " insert Customer { id:44 }";
//		continueExecution(src);
//		assertEquals(2, planL.size());
//		assertEquals("Customer", planL.get(0).dval.getType().getName());
//		assertEquals("Address", planL.get(1).dval.getType().getName());
//
//		
////		res = execStatement("let x2 = Customer[44]");
////		chkInt(res, "id", 44);
//
////		res = execStatement("let x3 = Address[100]");
////		chkInt(res, "id", 100);
////		
////		chkRelation(res, "cust", 44, "Customer", false);
////		res = execStatement("let x4 = Customer[44]");
//	}
//	
//	public ResultValue continueExecution(String src) {
//		DangCompiler compiler = delia.createCompiler();
//		List<Exp> extL = compiler.parse(src);
//		for(Exp exp: extL) {
//			if (exp instanceof TypeStatementExp) {
//				String msg = String.format("'type' statements not allowed in continueExecution - %s", exp.strValue());
//				DangError err = new DangError("type-statement-not-allowed", msg, null);
//				throw new DangException(err);
//			}
//		}
//		
//		EPRunner eprunner = createEPRunner(sess);
//		ResultValue res = null;
//		for(Exp exp: extL) {
//			res = eprunner.executeStatement(exp);
//			if (! res.ok) {
//				throw new DangException(res.errors);
//			}
//		}
//		
//		planL = planOptimizer.process(eprunner.getPlanList());
//		return res;
//	}
//	
//
//	
//	// --
//	private Delia delia;
//	private DBSession sess = null;
//	private boolean addIdFlag;
//	private DBInterface dbInterface ;
//	private int nextVarNumn = 1;
//	private PlanOptimizer planOptimizer;
//	private List<PlannedStatment> planL;
//
//	@Before
//	public void init() {
//		addIdFlag = true;
//		dbInterface = new MockDBInterface();
//		delia = DeliaFactory.create(dbInterface);
//		dbInterface.createTable("Address"); //!! fake schema
//		dbInterface.createTable("Customer"); //!! fake schema
//		planOptimizer = new PlanOptimizer(delia.getLog(), new SimpleErrorTracker(delia.getLog()));
//	}
//	
//	
//	protected EPRunner createEPRunner(DBSession dbsess) {
//		Log log = delia.getLog();
//		ErrorTracker et = new SimpleErrorTracker(log);
//		EPRunner runner = new EPRunner(log, et, dbInterface);
//		
//		boolean b; 
//		if (dbsess == null) {
//			b = runner.init();
//		} else {
//			b = runner.init(dbsess.getExecutionContext());
//		}
//		
//		if (! b) {
//			DangError err = et.add("runner-init-failed", "runner init failed");
//			throw new DangException(err);
//		}
//		return runner;
//	}
//	
//	protected void createType(String type, String relField) {
//		String src = createTypeSrc(type, relField);
//		ResultValue res = execTypeStatement(src);
//		chkResOK(res);
//	}
//	protected String createTypeSrc(String type, String relField) {
//		String sid = addIdFlag ? String.format(" id int unique") : "";
//		relField = sid.isEmpty() ? relField : ", " + relField;
//		String src = String.format("type %s struct { %s %s} end", type, sid, relField);
//		src += "\n";
//		return src;
//	}
//	protected ResultValue execTypeStatement(String src) {
//		if (sess != null) {
//			log("rebuilding..");
//		}
//		sess = delia.beginExecution(src);
//		
//		ResultValue res = sess.getFinalResult();
//		chkResOK(res);
//		return res;
//	}
//	protected void createTypeFail(String initialSrc, String type, String rel, String errId) {
//		String sid = addIdFlag ? String.format(" id int unique") : "";
//		String src = String.format("type %s struct { %s %s }  end", type, sid, rel);
//		execTypeStatementFail(initialSrc + src, errId);
//	}
//	protected void execTypeStatementFail(String src, String errId) {
//		boolean pass = false;
//		try {
//			execTypeStatement(src);
//			pass = true;
//		} catch (DangException e) {
//			DangError err = e.getLastError();
//			assertEquals(true, err.getId().contains(errId));
//		}
//		assertEquals(false, pass);
//	}
//	protected ResultValue execStatement(String src) {
//		assertEquals(true, sess != null);
//		ResultValue res = delia.continueExecution(src, sess);
//		chkResOK(res);
//		return res;
//	}
//	protected void execStatementFail(String src, String errId) {
//		assertEquals(true, sess != null);
//		boolean pass = false;
//		try {
//			delia.continueExecution(src, sess);
//		} catch (DangException e) {
//			DangError err = e.getLastError();
//			assertEquals(true, err.getId().equals(errId));
//			pass = true;
//		}
//		assertEquals(true, pass);
//	}
//	
//	private void chkResFail(ResultValue res, String errId) {
//		assertEquals(false, res.ok);
//		DangError err = res.getLastError();
//		assertEquals(true, err.getId().equals(errId));
//	}
//
//	public void chkResOK(ResultValue res) {
//		assertEquals(true, res.ok);
//		assertEquals(true, res.errors.isEmpty());
//	}
//	private void log(String s) {
//		System.out.println(s);
//	}
//	private void chkRelation(ResultValue res, String fieldName, int expected, String typeName, boolean b) {
//		DValue dval = res.getAsDValue();
//		DRelation drel = dval.asStruct().getField(fieldName).asRelation();
//		assertEquals(expected, drel.getForeignKey().asInt());
//		assertEquals(typeName, drel.getTypeName());
//	}
//	private void chkRelationNull(ResultValue res, String fieldName) {
//		DValue dval = res.getAsDValue();
//		DValue inner = dval.asStruct().getField(fieldName);
//		assertEquals(null, inner);
//	}
//
//	private void chkInt(ResultValue res, String fieldName, int expected) {
//		DValue dval = res.getAsDValue();
//		assertEquals(expected, dval.asStruct().getField(fieldName).asInt());
//	}
//	private void chkEntityNoRel(String typeName, int id, String nullField) {
//		String src = String.format("%s%s[%d]", buildLet(), typeName, id);
//		ResultValue res = execStatement(src);
//		chkInt(res, "id", id);
//		chkNullField(res, nullField);
//	}
//	private ResultValue chkEntity(String typeName, int id) {
//		String src = String.format("%s%s[%d]", buildLet(), typeName, id);
//		ResultValue res = execStatement(src);
//		chkInt(res, "id", id);
//		return res;
//	}
//
//	private String buildLet() {
//		return String.format("let x%d = ", nextVarNumn++);
//	}
//
//	private void chkNullField(ResultValue res, String fieldName) {
//		DValue dval = res.getAsDValue();
//		assertEquals(null, dval.asStruct().getField(fieldName));
//	}
//
//
//	
//}
