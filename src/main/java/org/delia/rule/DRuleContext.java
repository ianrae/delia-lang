package org.delia.rule;

import java.util.List;

import org.delia.core.FactoryService;
import org.delia.db.DBCapabilties;
import org.delia.dval.compare.DValueCompareService;
import org.delia.error.DeliaError;
import org.delia.error.DetailedError;
import org.delia.error.ErrorTracker;
import org.delia.runner.FetchRunner;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.zdb.DBInterfaceFactory;

public class DRuleContext {
	private ErrorTracker et;
	private String ruleText; //for error msg
	private boolean planModeFlg;
	private boolean enableRelationModifierFlag;
	private boolean populateFKsFlag;
	private DBCapabilties dbCapabilities;
	private FetchRunner fetchRunner;
	private DValueCompareService compareSvc;
	private boolean insertFlag;
	private boolean upsertFlag;
	private DValue upsertPKVal;
	private boolean softMandatoryRelationFlag;
	private FactoryService factorySvc;
	private DBInterfaceFactory dbInterface;
	private DTypeRegistry registry;

	public DRuleContext(FactoryService factorySvc, DBInterfaceFactory dbInterface, DTypeRegistry registry,
					ErrorTracker et, String ruleText, boolean enableRelationModifierFlag, DBCapabilties dbCapabilties, 
					boolean populateFKsFlag, FetchRunner fetchRunner, DValueCompareService compareSvc, 
					boolean insertFlag, boolean upsertFlag, DValue upsertPKVal, boolean softMandatoryRelationFlag) {
		this.factorySvc = factorySvc;
		this.dbInterface = dbInterface;
		this.registry = registry;
		this.et = et;
		this.ruleText = ruleText;
		this.enableRelationModifierFlag = enableRelationModifierFlag;
		this.dbCapabilities = dbCapabilties;
		this.populateFKsFlag = populateFKsFlag;
		this.fetchRunner = fetchRunner;
		this.compareSvc = compareSvc;
		this.insertFlag = insertFlag;
		this.upsertFlag = upsertFlag;
		this.upsertPKVal = upsertPKVal;
		this.softMandatoryRelationFlag = softMandatoryRelationFlag;
	}
	public DetailedError addError(String id, String msg) {
		String msg2 = String.format("%s - in rule: %s", msg, ruleText);
		DetailedError err = new DetailedError(id, msg2);
		et.add(err);
		return err;
	}
	public DetailedError addError(DRule rule, String msg) {
		return addError(rule, msg, null, null);
	}
	public DetailedError addError(DRule rule, String msg, RuleOperand oper1) {
		return addError(rule, msg, oper1, null);
	}
	public DetailedError addError(DRule rule, String msg, RuleOperand oper1, RuleOperand oper2) {
		DetailedError err = addError("rule-" + rule.getName(), msg);
		String field1 = oper1 == null ? null : oper1.getSubject();
		String field2 = oper2 == null ? null : oper2.getSubject();
		
		if (oper1 instanceof RuleRuleOperand) {
			RuleRuleOperand rrop = (RuleRuleOperand) oper1;
			field1 = rrop.getFieldName();
		}
		if (oper2 instanceof RuleRuleOperand) {
			RuleRuleOperand rrop = (RuleRuleOperand) oper2;
			field2 = rrop.getFieldName();
		}
		
		if (field1 != null) {
			err.setFieldName(field1);
		} else if (field2 != null) {
			err.setFieldName(field2);
		}
		return err;
	}
	public boolean hasErrors() {
		return !et.areNoErrors();
	}
	public String getRuleText() {
		return ruleText;
	}
	public List<DeliaError> getErrors() {
		return et.getErrors();
	}
	public boolean isPlanModeFlg() {
		return planModeFlg;
	}
	public void setPlanModeFlg(boolean planModeFlg) {
		this.planModeFlg = planModeFlg;
	}
	public boolean isEnableRelationModifierFlag() {
		return enableRelationModifierFlag;
	}
	public DBCapabilties getDBCapabilities() {
		return dbCapabilities;
	}
	public boolean isPopulateFKsFlag() {
		return populateFKsFlag;
	}
	public FetchRunner getFetchRunner() {
		return fetchRunner;
	}
	public DValueCompareService getCompareSvc() {
		return compareSvc;
	}
	public boolean isInsertFlag() {
		return insertFlag;
	}
	public boolean isUpsertFlag() {
		return upsertFlag;
	}
	public DValue getUpsertPKVal() {
		return upsertPKVal;
	}
	public boolean isSoftMandatoryRelationFlag() {
		return softMandatoryRelationFlag;
	}
	public FactoryService getFactorySvc() {
		return factorySvc;
	}
	public DBInterfaceFactory getDbInterface() {
		return dbInterface;
	}
	public DTypeRegistry getRegistry() {
		return registry;
	}
	
}