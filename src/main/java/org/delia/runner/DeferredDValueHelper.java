package org.delia.runner;

import org.delia.type.DValue;
import org.delia.type.DeferredDValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.varevaluator.VarEvaluator;
import org.delia.varevaluator.VarEvaluatorHelper;

public class DeferredDValueHelper {
    public static DValue preResolveDeferredDval(DValue dval, VarEvaluator varEvaluator) {
        if (dval instanceof DeferredDValue) {
            DeferredDValue defval = (DeferredDValue) dval;
            //error if var not exist
            if (!varEvaluator.exists(defval.deliaVarName)) {
                DeliaExceptionHelper.throwError("unknown-var-reference", "Var '%s' doesn't exist", defval.deliaVarName);
            }

            DValue realVal = VarEvaluatorHelper.lookupVarSingleValue(varEvaluator, defval.deliaVarName);
            return realVal;
        } else {
            return null;
        }
    }

    public static void resolveTo(DValue dval, DValue realVal) {
        if (dval instanceof DeferredDValue) {
            DeferredDValue defval = (DeferredDValue) dval;
            defval.resolveTo(realVal);
        }
    }

}
