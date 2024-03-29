package org.delia.dbimpl.mem.impl;

import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.tok.Tok;
import org.delia.type.DValue;
import org.delia.type.Shape;

import java.time.ZonedDateTime;

/**
 * @author Ian Rae
 */
public class FilterEvaluator extends ServiceBase {
    public Tok.WhereTok whereClause;
    public DValue resolvedFilterVars;
    //    private VarEvaluator varEvaluator;
    private DateFormatService fmtSvc;

    public FilterEvaluator(FactoryService factorySvc) { //VarEvaluator varEvaluator) {
        super(factorySvc);
//        this.varEvaluator = varEvaluator;
        this.fmtSvc = factorySvc.getDateFormatService();
    }

    public void init(Tok.WhereTok whereClause) {
        this.whereClause = whereClause; //was queryExp;
//        if (queryExp.filter != null && queryExp.filter.cond instanceof IdentExp) {
//            String varName = queryExp.filter.cond.strValue();
//
//            List<DValue> list = varEvaluator.lookupVar(varName);
//            if (list.isEmpty() || list.size() > 1) { //FUTURE later support 0 and > 1!!
//                String msg = String.format("var eval failed: %s", varName);
//                DeliaError err = et.add("var-eval-failed", msg);
//                throw new DeliaException(err);
//            }
//
//            resolvedFilterVars = list.get(0);
//        }
    }

    public boolean isEqualTo(DValue dval) {
        Object target = whereClause.strValue(); //.filter.cond.strValue();
        if (resolvedFilterVars != null) {
            //string comparison should work for int,long,string (PKs)
            target = resolvedFilterVars.asString();
        }
        return doIsEqualTo(dval, target);
    }

    private boolean doIsEqualTo(DValue dval, Object target) {
        if (dval.getType().isShape(Shape.DATE)) {
            ZonedDateTime zdt = fmtSvc.parseDateTime(target.toString());
            return dval.asDate().equals(zdt);
        } else {
            String tmp = dval.asString();
            if (tmp != null && tmp.equals(target)) {
                return true;
            }
            return false;
        }
    }

//    public boolean isIn(DValue key, ListExp listExp) {
//        for(Exp exp: listExp.valueL) {
//            if (doIsEqualTo(key, exp.strValue())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public String getRawValue() {
//        String target = queryExp.filter.cond.strValue();
//        return target;
//    }

//    public List<DValue> lookupVar(String varName) {
//        List<DValue> list = varEvaluator.lookupVar(varName);
//        return list;
//    }
//
//    public VarEvaluator getVarEvaluator() {
//        return varEvaluator;
//    }

}