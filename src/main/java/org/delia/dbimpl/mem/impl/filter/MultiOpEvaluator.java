package org.delia.dbimpl.mem.impl.filter;

import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;

import java.util.List;

/**
 * TODO: this only handles one level of and/or. Fix to handle nested expressions
 */
public class MultiOpEvaluator implements OpEvaluator {
    private final Tok.OperatorTok fullExp;
    //	private Object rightVar;
    private DStructType dtype;
    private OpEvaluator eval1;
    private OpEvaluator eval2;
    private DTypeRegistry registry;
    private DateFormatService fmtSvc;
    private FactoryService factorySvc;

    public MultiOpEvaluator(Tok.OperatorTok fullExp, OpEvaluator eval1, OpEvaluator eval2, DStructType dtype, DTypeRegistry registry, DateFormatService fmtSvc, FactoryService factorySvc) {
        this.dtype = dtype;
        this.registry = registry;
        this.fmtSvc = fmtSvc;
        this.factorySvc = factorySvc;

        this.eval1 = eval1;
        this.eval2 = eval2;
        this.fullExp = fullExp;
    }

    @Override
    public boolean match(Object left) {
        boolean b = doMatch(left);
        if (fullExp.negFlag) {
            return !b;
        } else {
            return b;
        }
    }

    private boolean doMatch(Object left) {
        boolean b1 = eval1.match(left);
        boolean isAnd = "and".equals(fullExp.op);
        if (isAnd && !b1) {
            return false;
        }
        boolean b2 = eval2.match(left);
        if (isAnd) {
            return b1 && b2;
        } else {
            return b1 || b2;
        }
    }


    @Override
    public void setRightVar(Object rightVar) {
//		this.rightVar = rightVar;
    }

    @Override
    public void setNegFlag(boolean negFlag) {
        //not used
    }

    @Override
    public void setFuncs(List<Tok.FunctionTok> funcs) {
        //unused
    }
}