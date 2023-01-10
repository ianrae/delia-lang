package org.delia.rule.rules;

import org.delia.error.DetailedError;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGeneratorContext;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.type.*;
import org.delia.util.DRuleHelper;

public class RelationOneRule extends RelationRuleBase {
    private boolean isParent;

    public RelationOneRule(RuleGuard guard, RuleOperand oper1,
                           DStructType owningType, DTypeRegistry registry, boolean isParent, String relationName) {
        super("relationOne", guard, oper1, owningType, registry, relationName);
        this.isParent = isParent;
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
                String msg = String.format("relation field '%s' one -  a foreign key value must be specified.", key);
                addDetailedError(ctx, msg, getSubject());
                return false;
            }
            return true;
        }

        return true;
    }

    private void addDetailedError(DRuleContext ctx, String msg, String fieldName) {
        DetailedError err = ctx.addError(this, msg);
        err.setFieldName(fieldName);
    }

    private boolean isOtherSideMany(DValue otherSide, TypePair otherRelPair) {
        return DRuleHelper.isOtherSideMany(otherSide.getType(), otherRelPair);
    }

    private boolean isMandatoryFK() {
        String fieldName = oper1.getSubject();
        boolean optional = owningType.fieldIsOptional(fieldName);
        if (optional || isParent) {
            return false;
        }
        return true;
    }

    public boolean isParent() {
        return isParent;
    }

    public void forceParentFlag(boolean b) {
        isParent = b;
    }

}