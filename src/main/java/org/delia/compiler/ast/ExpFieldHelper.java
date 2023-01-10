package org.delia.compiler.ast;

import org.delia.compiler.ast.Exp;
import org.delia.lld.LLD;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExpFieldHelper {

    public static List<Exp.FunctionExp> extractFuncs(Exp.DottedExp funcs) {
        if (funcs == null) return Collections.emptyList();
        List<Exp.FunctionExp> list = funcs.chainL.stream().filter(x -> x instanceof Exp.FunctionExp)
                .map(x -> (Exp.FunctionExp) x).collect(Collectors.toList());
        return list;
    }

    public static Exp.FunctionExp findFunc(Exp.DottedExp funcs, String fnName) {
        if (funcs == null) return null;
        for(Exp.ElementExp ex: funcs.chainL) {
            if (ex instanceof Exp.FunctionExp) {
                Exp.FunctionExp func = (Exp.FunctionExp) ex;
                if (func.fnName.equals(fnName)) {
                    return func;
                }
            }
        }
        return null;
    }


    public static List<Exp.FieldExp> extractFields(Exp.DottedExp dexp) {
        if (dexp == null) return Collections.emptyList();
        List<Exp.FieldExp> list = dexp.chainL.stream().filter(x -> x instanceof Exp.FieldExp).map(x -> (Exp.FieldExp)x).collect(Collectors.toList());
        return list;
    }

}
