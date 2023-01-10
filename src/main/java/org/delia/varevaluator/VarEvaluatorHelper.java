package org.delia.varevaluator;

import org.delia.type.DValue;

import java.util.List;

public class VarEvaluatorHelper {

    public static DValue lookupVarSingleValue(VarEvaluator varEvaluator, String varName) {
        List<DValue> list = varEvaluator.lookupVar(varName);
        if (list == null) return null;
        return list.isEmpty() ? null : list.get(0); //TODO later support multiple values
    }

}
