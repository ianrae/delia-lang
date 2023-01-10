package org.delia.rule.rules;

import org.delia.error.DetailedError;
import org.delia.rule.*;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.RelationValueBuilder;

import java.util.Map;

public class RelationManyRule extends RelationRuleBase {

    public RelationManyRule(RuleGuard guard, RuleOperand oper1,
                            DStructType owningType, DTypeRegistry registry, String relationName) {
        super("relationMany", guard, oper1, owningType, registry, relationName);
    }
    @Override
    public String renderAsDelia(RuleGeneratorContext ctx) {
        return String.format(""); //not used
    }

    @Override
    protected boolean onValidate(DValue dval, DRuleContext ctx) {
        DRelation drel = oper1.asRelation(dval);
        if (drel == null) {
            if (isMandatoryFK() && !ctx.isSoftMandatoryRelationFlag()) {
                String key = oper1.getSubject();
                String msg = String.format("relation field '%s' many -  a foreign key value must be specified.", key);
                addDetailedError(ctx, msg, getSubject(), dval.getType().getName());
                return false;
            }
            return true;
        }

        return true;
    }

    private void addDetailedError(DRuleContext ctx, String msg, String fieldName, String typeName) {
        DetailedError err = ctx.addError(this, msg);
        err.setFieldName(fieldName);
        err.setTypeName(typeName);
    }


    private boolean isMandatoryFK() {
        String fieldName = oper1.getSubject();
        boolean optional = owningType.fieldIsOptional(fieldName);
        if (optional) {
            return false;
        } else {
            DStructType relType = (DStructType) DValueHelper.findFieldType(owningType, fieldName);
            if (relType.fieldIsOptional(fieldName)) {
                return false;
            }
            DRule someRule = findRuleFor(relType, fieldName);
            if (someRule instanceof RelationOneRule) {
                return false;
            }
        }
        return true;
    }

    private DRule findRuleFor(DStructType relType, String x) {
        for (DRule rule : relType.getRawRules()) {
            if (x.equals(rule.getSubject())) {
                return rule;
            }
        }
        return null;
    }
}