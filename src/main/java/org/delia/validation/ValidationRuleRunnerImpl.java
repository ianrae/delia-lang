package org.delia.validation;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.dval.compare.DValueCompareService;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.rule.AlwaysRuleGuard;
import org.delia.rule.DRule;
import org.delia.rule.DRuleContext;
import org.delia.rule.rules.MandatoryRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.ResultValue;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.db.DBInterfaceFactory;
import org.delia.runner.DeliaRunner;

import java.util.ArrayList;
import java.util.List;

public class ValidationRuleRunnerImpl extends ServiceBase implements ValidationRunner {

    private final DeliaRunner deliaRunner;
    private SimpleErrorTracker localET;
    private boolean enableRelationModifierFlag;
    private DBCapabilties dbCapabilties;
    boolean populateFKsFlag;
    private boolean insertFlag;
    private boolean upsertFlag;
    //		private FetchRunner fetchRunner;
    private DValueCompareService compareSvc;
    private DValue upsertPKVal;
    private boolean softMandatoryRelationFlag;
    private DBInterfaceFactory dbInterface;
    private DTypeRegistry registry;

    public ValidationRuleRunnerImpl(FactoryService factorySvc, DBInterfaceFactory dbInterface, DTypeRegistry registry,
                                    DBCapabilties dbCapabilties, DeliaRunner deliaRunner) {
        super(factorySvc);
        this.dbInterface = dbInterface;
        this.registry = registry;
        this.localET = new SimpleErrorTracker(log);
        this.dbCapabilties = dbCapabilties;
//        this.fetchRunner = fetchRunner;
        this.compareSvc = factorySvc.getDValueCompareService();
        this.deliaRunner = deliaRunner;
    }

    /* (non-Javadoc)
     * @see org.delia.validation.ValidationRunner#validateFieldsOnly(org.delia.type.DValue)
     */
    @Override
    public boolean validateFieldsOnly(DValue dval) {
        localET.clear();
        List<DRule> ruleL = buildRuleList(dval);
        validateStruct(dval, ruleL, true);
        return (localET.areNoErrors());
    }

    private List<DRule> buildRuleList(DValue dval) {
        List<DRule> list = new ArrayList<>();
        //rules of all base types too
        doBuildRuleList(list, dval.getType());
        return list;
    }

    private void doBuildRuleList(List<DRule> list, DType dtype) {
        if (dtype.getBaseType() != null) {
            doBuildRuleList(list, dtype.getBaseType()); //** recursion **
        }

        for (DRule rule : dtype.getRawRules()) {
            list.add(rule);
        }
    }

    /* (non-Javadoc)
     * @see org.delia.validation.ValidationRunner#validateDVal(org.delia.type.DValue)
     */
    @Override
    public boolean validateDVal(DValue dval) {
        if (dval == null) return true;
        List<DRule> ruleL = buildRuleList(dval);
        return doValidateDVal(dval, ruleL);
    }

    private boolean doValidateDVal(DValue dval, List<DRule> ruleL) {
        if (ruleL.isEmpty()) {
            if (ValidationState.UNKNOWN.equals(dval.getValidationState())) {
                //set inner dvals for struct too
                if (dval.getType().isStructShape()) {
                    //first validate fields
                    validateStruct(dval, ruleL, false);
                    setValidIfNeeded(dval);

                    DStructType dtype = (DStructType) dval.getType();
                    for (TypePair pair : dtype.getAllFields()) {
                        DValue inner = dval.asStruct().getField(pair.name);
                        if (inner != null) {
                            setValidIfNeeded(inner);
                        }
                    }
                } else {
                    setValidIfNeeded(dval);
                }
            }
            return ValidationState.VALID.equals(dval.getValidationState());
        }

        //only validate if haven't already validated this dval
        if (!ValidationState.UNKNOWN.equals(dval.getValidationState())) {
            return true;
        }

        int errCount = localET.errorCount();
        if (dval.getType().isStructShape()) {
            validateStruct(dval, ruleL, false);
        } else {
            validateScalar(dval, ruleL);
        }
        return (localET.errorCount() == errCount);
    }

    /* (non-Javadoc)
     * @see org.delia.validation.ValidationRunner#validateRelationRules(org.delia.type.DValue)
     */
    @Override
    public void validateRelationRules(DValue dval) {
        List<DRule> ruleL = buildRuleList(dval);
        //we are only using validation to set parent fks
        //so don't set validation state!

        for (DRule rule : ruleL) {
            if (rule instanceof RelationOneRule) {
                RelationOneRule rr = (RelationOneRule) rule;
//                rr.populateFK(dval, fetchRunner);
            } else if (rule instanceof RelationManyRule) {
                RelationManyRule rr = (RelationManyRule) rule;
//                rr.populateFK(dval, fetchRunner);
            }
        }
    }


    private void setValidIfNeeded(DValue dval) {
        if (ValidationState.UNKNOWN.equals(dval.getValidationState())) {
            DValueInternal dvi = (DValueInternal) dval;
            dvi.setValidationState(ValidationState.VALID);
        }
    }

    private void validateStruct(DValue dval, List<DRule> ruleL, boolean validateFieldsOnly) {
        //first, validated each member dval
        DStructType dtype = (DStructType) dval.getType();
        TypePair pkpair = (upsertFlag) ? DValueHelper.findPrimaryKeyFieldPair(dtype) : null;
        int failCount = 0;
        for (TypePair pair : dtype.getAllFields()) {
            DValue inner = dval.asStruct().getField(pair.name);
            if (inner == null) {
                //on update validateFieldsOnly is true and not all fields are present
                boolean skip = false;
                if (upsertFlag && pair.name.equals(pkpair.name)) {
                    skip = true;
                } else if (dtype.fieldIsSerial(pair.name) && insertFlag) {
                    skip = true;
                }

                if (!validateFieldsOnly && !skip && !softMandatoryRelationFlag) {
                    MandatoryRule mandatoryRule = new MandatoryRule(new AlwaysRuleGuard(), pair.name);
                    if (!execRule(mandatoryRule, dval)) {
                        failCount++;
                    }
                }

                continue;
            }
            List<DRule> innerRuleL = buildRuleList(inner);
            if (!doValidateDVal(inner, innerRuleL)) { //** recursion **
                failCount++;
            }
        }

        //then, validate the struct itself
        if (!validateFieldsOnly) {
            for (DRule rule : ruleL) {
                if (!execRule(rule, dval)) {
                    failCount++;
                }
            }
        }

        DValueInternal dvi = (DValueInternal) dval;
        dvi.setValidationState(failCount == 0 ? ValidationState.VALID : ValidationState.INVALID);
    }

    private void validateScalar(DValue dval, List<DRule> ruleL) {
        int failCount = 0;
        for (DRule rule : ruleL) {
            if (!execRule(rule, dval)) {
                failCount++;
            }
        }
        DValueInternal dvi = (DValueInternal) dval;
        dvi.setValidationState(failCount == 0 ? ValidationState.VALID : ValidationState.INVALID);
    }

    private boolean execRule(DRule rule, DValue dval) {
        //execute guard
        if (!rule.executeGuard(dval)) {
            log.log("skip rule: %s", rule.getName());
            return true;
        }

        ErrorTracker tmpET = new SimpleErrorTracker(log);
        DValueCompareService compareSvc = factorySvc.getDValueCompareService();
        DRuleContext ctx = new DRuleContext(factorySvc, registry, log,
                tmpET, rule.getName(), enableRelationModifierFlag, populateFKsFlag,
                insertFlag, upsertFlag, upsertPKVal, softMandatoryRelationFlag,
                compareSvc, deliaRunner, dbCapabilties);
        boolean b = rule.validate(dval, ctx);
        if (!b) {
            localET.getErrors().addAll(ctx.getErrors());
        }
        return b;
    }

    /* (non-Javadoc)
     * @see org.delia.validation.ValidationRunner#propogateErrors(org.delia.runner.ResultValue)
     */
    @Override
    public void propogateErrors(ResultValue res) {
        if (!localET.areNoErrors()) {
            res.errors.addAll(localET.getErrors());
        }
    }

    @Override
    public void propogateErrors(ErrorTracker errorTracker) {
        if (!localET.areNoErrors()) {
            errorTracker.getErrors().addAll(localET.getErrors());
        }
    }

    /* (non-Javadoc)
     * @see org.delia.validation.ValidationRunner#validateDVals(java.util.List)
     */
    @Override
    public boolean validateDVals(List<DValue> dvalList) {
        for (DValue dval : dvalList) {
            if (dval == null) {
                continue;
            }
            validateDVal(dval);
        }
        return localET.errorCount() > 0;
    }

    /* (non-Javadoc)
     * @see org.delia.validation.ValidationRunner#validateDependentRules(org.delia.type.DValue)
     */
    @Override
    public boolean validateDependentRules(DValue partialDVal) {
        //validate the struct itself, but only rules that depend
        //on fields in partialDVal
        //used in update when dvalue may not contain all values
        List<DRule> ruleL = buildRuleList(partialDVal);
        for (String fieldName : partialDVal.asMap().keySet()) {
            for (DRule rule : ruleL) {
                if (rule.dependsOn(fieldName)) {
                    execRule(rule, partialDVal);
                }
            }
        }

        return localET.areNoErrors();
    }

    /* (non-Javadoc)
     * @see org.delia.validation.ValidationRunner#enableRelationModifier(boolean)
     */
    @Override
    public void enableRelationModifier(boolean b) {
        this.enableRelationModifierFlag = b;
    }

    /* (non-Javadoc)
     * @see org.delia.validation.ValidationRunner#isPopulateFKsFlag()
     */
    @Override
    public boolean isPopulateFKsFlag() {
        return populateFKsFlag;
    }

    /* (non-Javadoc)
     * @see org.delia.validation.ValidationRunner#setPopulateFKsFlag(boolean)
     */
    @Override
    public void setPopulateFKsFlag(boolean populateFKsFlag) {
        this.populateFKsFlag = populateFKsFlag;
    }

    /* (non-Javadoc)
     * @see org.delia.validation.ValidationRunner#enableInsertFlag(boolean)
     */
    @Override
    public void enableInsertFlag(boolean b) {
        this.insertFlag = b;
    }

    @Override
    public void enableUpsertFlag(boolean b) {
        this.upsertFlag = b;
    }

    @Override
    public void setUpsertPKVal(DValue keyval) {
        this.upsertPKVal = keyval;
    }

    @Override
    public void setSoftMandatoryRelationFlag(boolean b) {
        this.softMandatoryRelationFlag = b;
    }

    @Override
    public List<DeliaError> getErrors() {
        return localET.getErrors();
    }
}