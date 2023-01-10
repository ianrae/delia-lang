package org.delia.rule;

import org.delia.core.FactoryService;
import org.delia.core.QueryService;
import org.delia.db.DBCapabilties;
import org.delia.dval.compare.DValueCompareService;
import org.delia.error.DeliaError;
import org.delia.error.DetailedError;
import org.delia.error.ErrorTracker;
import org.delia.log.DeliaLog;
import org.delia.runner.DeliaRunner;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

import java.util.List;

public class DRuleContext {
    private final DeliaRunner deliaRunner;
    private final DeliaLog log;
    private ErrorTracker et;
    private String ruleText; //for error msg
    private boolean planModeFlg;
    private boolean enableRelationModifierFlag;
    private boolean populateFKsFlag;
    private DBCapabilties dbCapabilities;
    //	private FetchRunner fetchRunner;
    private DValueCompareService compareSvc;
    private boolean insertFlag;
    private boolean upsertFlag;
    private DValue upsertPKVal;
    private boolean softMandatoryRelationFlag;
    private FactoryService factorySvc;
    //	private DBInterfaceFactory dbInterface;
    private DTypeRegistry registry;

    /*
        public DRuleContext(FactoryService factorySvc, DBInterfaceFactory dbInterface, DTypeRegistry registry,
                    ErrorTracker et, String ruleText, boolean enableRelationModifierFlag, DBCapabilties dbCapabilties,
                    boolean populateFKsFlag, FetchRunner fetchRunner, DValueCompareService compareSvc,
                    boolean insertFlag, boolean upsertFlag, DValue upsertPKVal, boolean softMandatoryRelationFlag) {

     */
    public DRuleContext(FactoryService factorySvc, DTypeRegistry registry,
                        DeliaLog log, ErrorTracker et, String ruleText, boolean enableRelationModifierFlag,
                        boolean populateFKsFlag,
                        boolean insertFlag, boolean upsertFlag, DValue upsertPKVal, boolean softMandatoryRelationFlag,
                        DValueCompareService compareSvc, DeliaRunner deliaRunner, DBCapabilties caps) {
//		this.dbInterface = dbInterface;
        this.factorySvc = factorySvc;
        this.registry = registry;
        this.log = log;
        this.et = et;
        this.ruleText = ruleText;
        this.enableRelationModifierFlag = enableRelationModifierFlag;
        this.dbCapabilities = caps;
        this.populateFKsFlag = populateFKsFlag;
//		this.fetchRunner = fetchRunner;
        this.compareSvc = compareSvc;
        this.insertFlag = insertFlag;
        this.upsertFlag = upsertFlag;
        this.upsertPKVal = upsertPKVal;
        this.softMandatoryRelationFlag = softMandatoryRelationFlag;
        this.deliaRunner = deliaRunner;
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

    //	public FetchRunner getFetchRunner() {
//		return fetchRunner;
//	}
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

    //	public DBInterfaceFactory getDbInterface() {
//		return dbInterface;
//	}
    public DTypeRegistry getRegistry() {
        return registry;
    }

    public DeliaLog getLog() {
        return log;
    }

    public DeliaRunner getDeliaRunner() {
        return deliaRunner;
    }

    public QueryService getQueryService() {
        return new QueryService(factorySvc, deliaRunner);
    }

}