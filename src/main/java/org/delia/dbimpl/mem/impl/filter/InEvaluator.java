package org.delia.dbimpl.mem.impl.filter;

import org.delia.tok.Tok;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

import java.util.List;

public class InEvaluator implements OpEvaluator {
    private Tok.OperatorTok fullExp;
    private DStructType dtype;
    private Tok.DottedTok inExp;
    private String keyField;
//	private FilterEvaluator filterEvaluator;

    public InEvaluator(Tok.OperatorTok fullexp, DStructType dtype) { //}, FilterEvaluator filterEvaluator) {
        this.fullExp = fullexp;
        this.dtype = dtype;

        //TODO add , FilterFunctionService filterFnSvc

        this.inExp = (Tok.DottedTok) fullexp.op2;
        this.keyField = fullexp.op1.strValue();
        if (this.keyField == null) {
            //err!!
//			wasError = true;
        }

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
        if (keyField == null) {
//			wasError = true;
            //err!!
            return false;
        } else {
            DValue dval = (DValue) left;
            DValue key = dval.asStruct().getField(keyField);
            if (key == null) {
//				//try as var
//				List<DValue> varValueL = filterEvaluator.lookupVar(keyField);
//				if (varValueL != null) {
//					for(DValue vv: varValueL) {
//						if (isIn(vv, inExp.listExp)) {
//							return true;
//						}
//					}
//					return false;
//				}
////				wasError = true;
//				//err!!
                return false;
            }

            if (isIn(key, inExp)) {
                return true;
            }
            return false;
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

    //TODO: this is hacky. use dvalcomparesvc and truly extract ValueExp or FieldExp (and eval vars)
    public boolean isIn(DValue key, Tok.DottedTok listExp) {
        for (Tok.DToken exp : listExp.chainL) {
            if (isIn(key, exp)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIn(DValue key, Tok.DToken exp) {
        if (exp instanceof Tok.ValueTok) {
            Tok.ValueTok vexp = (Tok.ValueTok) exp;
            return doIsEqualTo(key, vexp.value.asString());
        } else if (exp instanceof Tok.ListTok) {
            Tok.ListTok lexp = (Tok.ListTok) exp;
            for (Tok.DToken el : lexp.listL) {
                if (isIn(key, el)) {  //*** recursion ***
                    return true;
                }
            }
        } else {
            DeliaExceptionHelper.throwNotImplementedError("unsupported exp in 'in'", exp.getClass().getSimpleName());
        }
        return false;
    }

    private boolean doIsEqualTo(DValue dval, Object target) {
        String tmp;
        if (dval.getType().isRelationShape()) {
            DRelation drel = dval.asRelation();
            tmp = drel.getForeignKey().asString(); //TODO later support multiple keys
        } else {
            tmp = dval.asString();
        }
        if (tmp != null && tmp.equals(target)) {
            return true;
        }
        return false;
    }

    @Override
    public void setFuncs(List<Tok.FunctionTok> funcs) {
        //unused
    }

}