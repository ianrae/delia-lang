package org.delia.dbimpl.mem.impl.filter;

import org.delia.dbimpl.mem.impl.filter.filterfn.FilterFunctionService;
import org.delia.tok.Tok;
import org.delia.type.DValue;

public class BooleanOpEvaluator extends OpEvaluatorBase {

    public BooleanOpEvaluator(OP op, String fieldName, FilterFunctionService filterFnSvc) {
        super(op, fieldName, filterFnSvc);
    }

    @Override
    protected boolean doMatch(Object left) {
        DValue dval = (DValue) left;
        Boolean b = checkNull(dval, rightVar);
        if (b != null) {
            return b;
        }
        Boolean n1 = getFieldValue(dval).asBoolean();
        Boolean n2 = ((Tok.ValueTok) rightVar).value.asBoolean();

        switch (op) {
            case EQ:
                return n1.compareTo(n2) == 0;
            case NEQ:
                return n1.compareTo(n2) != 0;
            default:
                return false; //err!
        }
    }
}