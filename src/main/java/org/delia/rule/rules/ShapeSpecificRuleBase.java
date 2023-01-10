package org.delia.rule.rules;

import org.delia.error.ErrorTracker;
import org.delia.rule.DRuleBase;
import org.delia.rule.RuleGuard;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.TypePair;

public abstract class ShapeSpecificRuleBase extends DRuleBase {

    public ShapeSpecificRuleBase(String name, RuleGuard guard) {
        super(name, guard);
    }

    protected DType foo(DType dtype, String fieldName, String errId, ErrorTracker et) {

        DType typeToCheck = dtype;
        if (dtype.isStructShape()) {
            DStructType structType = (DStructType) dtype;
            TypePair pair = structType.findField(fieldName);
            if (pair == null) {
                et.add(errId, String.format("type '%s': %s rule with unknown field '%s'", structType.getName(), getName(), fieldName));
                return null;
            }
            typeToCheck = structType.findField(fieldName).type;
        }
        return typeToCheck;
    }


}