package org.delia.runner;

import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UpsertStatementExp;
import org.delia.core.ConfigureService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBException;
import org.delia.db.QuerySpec;
import org.delia.db.SqlStatementGroup;
import org.delia.dval.DValueConverterService;
import org.delia.error.DeliaError;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.HLDManager;
import org.delia.hld.cud.HLDUpdateStatement;
import org.delia.hld.cud.HLDUpsertStatement;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigVarEvaluator;
import org.delia.type.DStructType;
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
public class UpdateStatementRunner extends ServiceBase {
	private DTypeRegistry registry;
	private ZDBInterfaceFactory dbInterface;
	private FetchRunner fetchRunner;
	private SprigService sprigSvc;
	private DValueIterator insertPrebuiltValueIterator;
	private VarEvaluator varEvaluator;
	private HLDManager hldManager;
	private Runner runner;
	private DValueConverterService dvalConverterSvc;

	public UpdateStatementRunner(FactoryService factorySvc, ZDBInterfaceFactory dbInterface, Runner runner, DTypeRegistry registry) {
		super(factorySvc);
		this.dbInterface = dbInterface;
		this.runner = runner;
		this.varEvaluator = runner;
		this.registry = registry;
		this.dvalConverterSvc = new DValueConverterService(factorySvc);
	}

	private ValidationRunner createValidationRunner() {
		return factorySvc.createValidationRunner(dbInterface, fetchRunner);
	}

	public void executeUpdateStatement(UpdateStatementExp exp, ResultValue res, HLDManager hldManager, ZDBExecutor dbexecutor, FetchRunner fetchRunner2, DValueIterator insertPrebuiltValueIterator2, SprigService sprigSvc2) {
		this.hldManager = hldManager;
		this.fetchRunner = fetchRunner2;
		this.insertPrebuiltValueIterator = insertPrebuiltValueIterator2;
		this.sprigSvc = sprigSvc2;
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

		ConversionResult cres = null;
		HLDUpdateStatement hldup = null;
		SqlStatementGroup stmgrp = null;
		if (hldManager != null) {
			VarEvaluator varEvaluator = new SprigVarEvaluator(factorySvc, runner);
			hldup = hldManager.buildHLD(exp, dbexecutor, varEvaluator, insertPrebuiltValueIterator);
			if (hldup.isEmpty()) {
				res.ok = true;
				res.shape = Shape.INTEGER;
				res.val = 0;
				return;
			}
			stmgrp = hldManager.generateSQL(hldup, dbexecutor);
			cres = hldup.hldupdate.cres;
		} else {
			cres = buildPartialValue((DStructType) dtype, exp.dsonExp);
		}

		//get list of changed fields
		if (cres.dval == null) {
			res.errors.addAll(cres.localET.getErrors());
			res.ok = false;
			return;
		} else {
			//validate the fields of the partial DValue
			ValidationRunner ruleRunner = createValidationRunner();
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
			int numRowsAffected = dbexecutor.executeUpdate(hldup, stmgrp);

			res.ok = true;
			res.shape = Shape.INTEGER;
			res.val = numRowsAffected;
		} catch (DBException e) {
			res.errors.add(e.getLastError());
			res.ok = false;
			return;
		}
	}
	public void executeUpsertStatement(UpsertStatementExp exp, ResultValue res, HLDManager hldManager2, ZDBExecutor dbexecutor, FetchRunner fetchRunner2, DValueIterator insertPrebuiltValueIterator2, SprigService sprigSvc2) {
		this.hldManager = hldManager2;
		this.fetchRunner = fetchRunner2;
		this.insertPrebuiltValueIterator = insertPrebuiltValueIterator2;
		this.sprigSvc = sprigSvc2;

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

		ConversionResult cres = null;
		HLDUpsertStatement hldup = null;
		SqlStatementGroup stmgrp = null;
		if (hldManager != null) {
			VarEvaluator varEvaluator = new SprigVarEvaluator(factorySvc, runner);
			hldup = hldManager.buildHLD(exp, dbexecutor, varEvaluator, insertPrebuiltValueIterator);
			if (hldup.isEmpty()) {
				res.ok = true;
				res.shape = Shape.INTEGER;
				res.val = 0;
				return;
			}

			stmgrp = hldManager.generateSQL(hldup, dbexecutor);
			cres = hldup.hldupdate.cres;
		} else {
			cres = buildPartialValue((DStructType) dtype, exp.dsonExp);
		}

		//get list of changed fields
		if (cres.dval == null) {
			res.errors.addAll(cres.localET.getErrors());
			res.ok = false;
			return;
		} else {
			cres.assocCrudMap = null; //clear. not supported for upsert

			//validate the fields of the partial DValue
			ValidationRunner ruleRunner = createValidationRunner();
			ruleRunner.enableRelationModifier(true);
			ruleRunner.enableUpsertFlag(true);
			if (hldManager != null) {
				ScalarValueBuilder scalarBuilder = factorySvc.createScalarValueBuilder(registry);
				DValue keyval = dvalConverterSvc.createDValueFrom(hldup.hldupdate.hld.filter, scalarBuilder, true);
				ruleRunner.setUpsertPKVal(keyval);
			}
			ConfigureService configSvc = factorySvc.getConfigureService();

			ruleRunner.setPopulateFKsFlag(configSvc.isPopulateFKsFlag());
			if (! ruleRunner.validateDVal(cres.dval)) {
				ruleRunner.propogateErrors(res);
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
			boolean noUpdateFlag = exp.optionExp != null;
			int numRowsAffected = dbexecutor.executeUpsert(hldup, stmgrp, noUpdateFlag);
			numRowsAffected = 1; //1 means success (not number of rows affected)

			res.ok = true;
			res.shape = Shape.INTEGER;
			res.val = numRowsAffected;
		} catch (DBException e) {
			res.errors.add(e.getLastError());
			res.ok = false;
			return;
		}
	}

	private void addError(ResultValue res, String id, String msg) {
		DeliaError error = et.add(id, msg);
		res.errors.add(error);
		res.ok = false;
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
	private ConversionResult buildPartialValue(DStructType dtype, DsonExp dsonExp) {
		ConversionResult cres = new ConversionResult();
		cres.localET = new SimpleErrorTracker(log);
		if (insertPrebuiltValueIterator != null) {
			cres.dval = insertPrebuiltValueIterator.next();
			return cres;
		}

		DsonToDValueConverter converter = new DsonToDValueConverter(factorySvc, cres.localET, registry, varEvaluator, sprigSvc);
		cres.dval = converter.convertOnePartial(dtype.getName(), dsonExp);
		cres.assocCrudMap = converter.getAssocCrudMap();
		return cres;
	}

	private QuerySpec resolveFilterVars(QueryExp queryExp) {
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		spec.evaluator.init(queryExp);
		return spec;
	}
}