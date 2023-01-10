package org.delia.dbimpl.mem.impl.filter;

import org.delia.dbimpl.mem.impl.filter.filterfn.FilterFunctionService;
import org.delia.tok.Tok;
import org.delia.type.DValue;

public class IntOpEvaluator extends OpEvaluatorBase {

    public IntOpEvaluator(OP op, String fieldName, FilterFunctionService filterFnSvc) {
        super(op, fieldName, filterFnSvc);
    }

    @Override
    protected boolean doMatch(Object left) {
        DValue dval = (DValue) left;
        Boolean b = checkNull(dval, rightVar);
        if (b != null) {
            return b;
        }
        Integer n1 = resolveToInt(dval);
        Integer n2 = ((Tok.ValueTok) rightVar).value.asInt();

        switch (op) {
            case LT:
                return n1.compareTo(n2) < 0;
            case LE:
                return n1.compareTo(n2) <= 0;
            case GT:
                return n1.compareTo(n2) > 0;
            case GE:
                return n1.compareTo(n2) >= 0;
            case EQ:
                return n1.compareTo(n2) == 0;
            case NEQ:
                return n1.compareTo(n2) != 0;
            default:
                return false; //err!
        }
    }
}