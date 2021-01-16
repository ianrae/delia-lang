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
import org.delia.db.newhls.HLDManager;
import org.delia.error.DeliaError;
import org.delia.error.SimpleErrorTracker;
import org.delia.sprig.SprigService;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.Shape;
import org.delia.util.PrimaryKeyHelperService;
import org.delia.validation.ValidationRunner;
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
		private ZDBExecutor dbexecutor;
		private FetchRunner fetchRunner;
		private SprigService sprigSvc;
		private DValueIterator insertPrebuiltValueIterator;
		private VarEvaluator varEvaluator;
		private HLDManager hldManager;
		
		public UpdateStatementRunner(FactoryService factorySvc, ZDBInterfaceFactory dbInterface, VarEvaluator varEvaluator, DTypeRegistry registry) {
			super(factorySvc);
			this.dbInterface = dbInterface;
			this.varEvaluator = varEvaluator;
			this.registry = registry;
		}
		
		private ValidationRunner createValidationRunner() {
			return factorySvc.createValidationRunner(dbInterface, fetchRunner);
		}

		public void executeUpdateStatement(UpdateStatementExp exp, ResultValue res, HLDManager hldManager, ZDBExecutor dbexecutor2, FetchRunner fetchRunner2, DValueIterator insertPrebuiltValueIterator2, SprigService sprigSvc2) {
			this.hldManager = hldManager;
			this.dbexecutor = dbexecutor2;
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
			
			//get list of changed fields
			ConversionResult cres = buildPartialValue((DStructType) dtype, exp.dsonExp);
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
		public void executeUpsertStatement(UpsertStatementExp exp, ResultValue res, HLDManager hldManager2, ZDBExecutor dbexecutor2, FetchRunner fetchRunner2, DValueIterator insertPrebuiltValueIterator2, SprigService sprigSvc2) {
			this.hldManager = hldManager;
			this.dbexecutor = dbexecutor2;
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
			
			//get list of changed fields
			ConversionResult cres = buildPartialValue((DStructType) dtype, exp.dsonExp);
			if (cres.dval == null) {
				res.errors.addAll(cres.localET.getErrors());
				res.ok = false;
				return;
			} else {
				cres.assocCrudMap = null; //clear. not supported for upsert
				
				//validate the fields of the partial DValue
				ValidationRunner ruleRunner = createValidationRunner();
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