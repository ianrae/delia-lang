package org.delia.rule;

import java.util.List;

import org.delia.db.DBCapabilties;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.runner.FetchRunner;

public class DRuleContext {
	private ErrorTracker et;
	private String ruleText; //for error msg
	private boolean planModeFlg;
	private boolean enableRelationModifierFlag;
	private boolean populateFKsFlag;
	private DBCapabilties dbCapabilities;
	private FetchRunner fetchRunner;

	public DRuleContext(ErrorTracker et, String ruleText, boolean enableRelationModifierFlag, DBCapabilties dbCapabilties, 
					boolean populateFKsFlag, FetchRunner fetchRunner) {
		this.et = et;
		this.ruleText = ruleText;
		this.enableRelationModifierFlag = enableRelationModifierFlag;
		this.dbCapabilities = dbCapabilties;
		this.populateFKsFlag = populateFKsFlag;
		this.fetchRunner = fetchRunner;
	}
	public DeliaError addError(String id, String msg) {
		String msg2 = String.format("%s - in rule: %s", msg, ruleText);
		DeliaError err = et.add(id, msg2);
		return err;
	}
	public DeliaError addError(DRule rule, String msg) {
		return addError("rule-" + rule.getName(), msg);
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
}