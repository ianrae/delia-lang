package org.delia.runner;

import java.util.Map;

import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.core.ConfigureService;
import org.delia.core.DiagnosticService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBException;
import org.delia.db.DBValidationException;
import org.delia.db.InsertContext;
import org.delia.db.hld.HLDManager;
import org.delia.db.newhls.cud.HLDInsertStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.error.DeliaError;
import org.delia.error.SimpleErrorTracker;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;
import org.delia.validation.ValidationRunner;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

/**
 * @author Ian Rae
 *
 */
public class InsertStatementRunner extends ServiceBase {
	Map<String,ResultValue> varMap;
	private DTypeRegistry registry;
	private ZDBInterfaceFactory dbInterface;
	private Runner runner;
	private DValueIterator insertPrebuiltValueIterator;

	public InsertStatementRunner(FactoryService factorySvc, ZDBInterfaceFactory dbInterface, Runner runner, 
			DTypeRegistry registry, Map<String,ResultValue> varMap) {
		super(factorySvc);
		this.dbInterface = dbInterface;
		this.runner = runner;
		this.registry = registry;
		this.varMap = varMap;
	}

	private ValidationRunner createValidationRunner(FetchRunner fetchRunner) {
		return factorySvc.createValidationRunner(dbInterface, fetchRunner);
	}

	public void executeInsertStatement(InsertStatementExp exp, ResultValue res, HLDManager hldManager, ZDBExecutor dbexecutor, FetchRunner fetchRunner, 
			DValueIterator insertPrebuiltValueIterator2, SprigService sprigSvc) {

		this.insertPrebuiltValueIterator = insertPrebuiltValueIterator2;
		DType dtype = registry.getType(exp.getTypeName());
		if (failIfNull(dtype, exp.typeName, res)) {
			return;
		} else if (failIfNotStruct(dtype, exp.typeName, res)) {
			return;
		}
		
		ConversionResult cres = null;
		HLDInsertStatement hldins = null;
		SqlStatementGroup stmgrp = null;
		if (hldManager != null) {
			VarEvaluator varEvaluator = new SprigVarEvaluator(factorySvc, runner);
			hldins = hldManager.buildHLD(exp, dbexecutor, varEvaluator, insertPrebuiltValueIterator);
			stmgrp = hldManager.generateSQL(hldins, dbexecutor);
			cres = hldins.hldinsert.cres;
		} else {
			cres = buildValue((DStructType) dtype, exp.dsonExp, insertPrebuiltValueIterator, sprigSvc);
		}
		

		//execute db insert
		if (cres.dval == null) {
			res.errors.addAll(cres.localET.getErrors());
			res.ok = false;
			return;
		} else {
			ValidationRunner ruleRunner = createValidationRunner(fetchRunner);
			ruleRunner.enableRelationModifier(true);
			ruleRunner.enableInsertFlag(true);
			ConfigureService configSvc = factorySvc.getConfigureService();
			
			//hack. not sure why i needed to add this for MEM
			//when inserting values we need to add relation to other side
			boolean flag = configSvc.isPopulateFKsFlag();
//			if (DBType.MEM.equals(dbInterface.getDBType())) {
//				flag = true;
//			}
			ruleRunner.setPopulateFKsFlag(flag);
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
				DValue generatedId = doDBInsert(hldins, stmgrp, cres, ctx, dbexecutor); 
				assignSerialVar(generatedId);
				boolean sprigFlag = sprigSvc.haveEnabledFor(typeName);
				if (sprigFlag) {
					sprigSvc.rememberSynthId(typeName, cres.dval, generatedId, cres.extraMap);
				}
			} else {
				doDBInsert(hldins, stmgrp, cres, ctx, dbexecutor);
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

		DiagnosticService diagnosticSvc = factorySvc.getDiagnosticService();
		if (diagnosticSvc.isActive("I")) {
			diagnosticSvc.log("I", cres.dval, registry);
		}
		
		//INSERT has no return value
		res.ok = true;
		res.shape = null;
		res.val = null;
	}
	private DValue doDBInsert(HLDInsertStatement hldins, SqlStatementGroup stmgrp, ConversionResult cres,
			InsertContext ctx, ZDBExecutor dbexecutor) {
		if (hldins != null) {
			return dbexecutor.executeInsert(hldins, stmgrp, ctx);
		} else {
			return dbexecutor.executeInsert(cres.dval, ctx);
		}
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
	private void addError(ResultValue res, String id, String msg) {
		DeliaError error = et.add(id, msg);
		res.errors.add(error);
		res.ok = false;
	}
	private ConversionResult buildValue(DStructType dtype, DsonExp dsonExp, DValueIterator insertPrebuiltValueIterator, SprigService sprigSvc) {
		ConversionResult cres = new ConversionResult();
		cres.localET = new SimpleErrorTracker(log);
		if (insertPrebuiltValueIterator != null) {
			cres.dval = insertPrebuiltValueIterator.next();
			return cres;
		}

		VarEvaluator varEvaluator = runner;
		//			if (sprigSvc.haveEnabledFor(dtype.getName())) {
		varEvaluator = new SprigVarEvaluator(factorySvc, runner);
		//			}

		DsonToDValueConverter converter = new DsonToDValueConverter(factorySvc, cres.localET, registry, varEvaluator, sprigSvc);
		cres.dval = converter.convertOne(dtype.getName(), dsonExp, cres);
		return cres;
	}

	private void assignSerialVar(DValue generatedId) {
		ResultValue res = new ResultValue();
		res.ok = true;
		res.shape = generatedId.getType().getShape();
		res.val = generatedId;
		res.varName = RunnerImpl.VAR_SERIAL;
		varMap.put(RunnerImpl.VAR_SERIAL, res);
	}

}