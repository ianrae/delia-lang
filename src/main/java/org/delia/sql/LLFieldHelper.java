package org.delia.sql;

import org.delia.lld.LLD;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LLFieldHelper {

    public static List<LLD.LLDFuncEx> extractFuncs(List<LLD.LLEx> funcs) {
        if (funcs == null) return Collections.emptyList();
        List<LLD.LLDFuncEx> list = funcs.stream().filter(x -> x instanceof LLD.LLDFuncEx)
                .map(x -> (LLD.LLDFuncEx) x).collect(Collectors.toList());
        return list;
    }
    public static List<LLD.LLFinalFieldEx> extractFields(List<LLD.LLEx> funcs) {
        if (funcs == null) return Collections.emptyList();
        List<LLD.LLFinalFieldEx> list = funcs.stream().filter(x -> x instanceof LLD.LLFinalFieldEx)
                .map(x -> (LLD.LLFinalFieldEx) x).collect(Collectors.toList());
        return list;
    }

    public static LLD.LLDFuncEx findFunc(List<LLD.LLEx> funcs, String fnName) {
        if (funcs == null) return null;
        for(LLD.LLEx ex: funcs) {
            if (ex instanceof LLD.LLDFuncEx) {
                LLD.LLDFuncEx func = (LLD.LLDFuncEx) ex;
                if (func.fnName.equals(fnName)) {
                    return func;
                }
            }
        }
        return null;
    }

    public static boolean existsFunc(List<LLD.LLEx> funcs, String fnName) {
        if (funcs == null) return false;
        for(LLD.LLEx ex: funcs) {
            if (ex instanceof LLD.LLDFuncEx) {
                LLD.LLDFuncEx func = (LLD.LLDFuncEx) ex;
                if (func.fnName.equals(fnName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isFuncAfter(List<LLD.LLEx> funcs, LLD.LLDFuncEx func1, LLD.LLDFuncEx func2) {
        List<LLD.LLDFuncEx> list = extractFuncs(funcs);
        int index1 = list.indexOf(func1);
        int index2 = list.indexOf(func2);
        return index1 >= 0 && index2 >= 0 && index2 > index1;
    }

    public static LLD.LLFuncArg getIthArg(LLD.LLDFuncEx func, int index) {
        if (func == null) return null;
        List<LLD.LLFuncArg> list = func.argsL.stream().filter(x -> x instanceof LLD.LLFuncArg)
                .map(x -> (LLD.LLFuncArg) x).collect(Collectors.toList());
        return list.get(index); //TODO add range-check
    }
}
