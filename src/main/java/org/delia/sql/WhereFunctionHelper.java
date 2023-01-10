package org.delia.sql;

import org.delia.tok.Tok;
import org.delia.tok.TokVisitorUtils;
import org.delia.util.DeliaExceptionHelper;

import java.util.Arrays;
import java.util.List;

public class WhereFunctionHelper {

    public static String genNameOrFn(Tok.OperandTok op1) {
        Tok.FieldTok fexp = TokVisitorUtils.getSingleChainFieldExp(op1);
        if (fexp != null) {
            if (!fexp.funcL.isEmpty()) {
                return execFunc(fexp);
            }
        }

        return op1.strValue();
    }


    private static String execFunc(Tok.FieldTok fexp) {
        if (fexp.funcL.size() > 1) {
            DeliaExceptionHelper.throwNotImplementedError("SDSDFD"); //TODO fix
        }

        Tok.FunctionTok fntok = fexp.funcL.get(0);

        List<String> allFns = Arrays.asList("year", "month", "day", "hour", "minute", "second");

        //DATEPART(year, '2017/08/25')
        if (allFns.contains(fntok.fnName)) {
            boolean useDatePart = true; //H2 not support DATEPART
            if (useDatePart) {
                String fieldName = fexp.fieldName;
                String ss = fexp.alias == null ? fieldName : String.format("%s.%s", fexp.alias, fieldName);
//                String s = String.format("DATEPART('%s',%s)", fntok.fnName, ss);
                String s = String.format("date_part('%s',%s)", fntok.fnName, ss);
                return s;
            } else { //SELECT EXTRACT(MONTH FROM "2017-06-15");
                DeliaExceptionHelper.throwNotImplementedError("for h2");
//                String fieldName = val1.exp.strValue();
//                String ss = val1.alias == null ? fieldName : renderTerm(val1.alias, fieldName);
//                String s = String.format("EXTRACT(%s FROM %s)", fn.fnName.toUpperCase(), ss);
                return null;
            }
        } else {
            DeliaExceptionHelper.throwNotImplementedError("unknown filter fn '%s'", fntok.fnName);
            return null;
        }
    }

}
