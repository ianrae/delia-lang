package org.delia.dbimpl.mem.impl.filter;

import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.dbimpl.mem.impl.filter.filterfn.FilterFunctionService;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

public class OpFactory {

    private DTypeRegistry registry;
    private DateFormatService fmtSvc;
    private FactoryService factorySvc;
    private DStructType structType;
    private FilterFunctionService filterFnSvc;

    public OpFactory(DTypeRegistry registry, DateFormatService fmtSvc, FactoryService factorySvc, DStructType dtype) {
        this.registry = registry;
        this.fmtSvc = fmtSvc;
        this.factorySvc = factorySvc;
        this.structType = dtype; //may be null
        this.filterFnSvc = new FilterFunctionService(factorySvc, registry);
    }

    public OpEvaluator create(String opStr, Object op1, Object op2, DType op1HintType, DType op2HintType, boolean negFlag) {

        OpEvaluator eval = doCreate(opStr, op1, op2, op1HintType, op2HintType);
        if (eval != null) {
            eval.setNegFlag(negFlag);
            setFuncsIfNeeded(eval, op1);
            setFuncsIfNeeded(eval, op2); //TOD we assume only one of op1 and op2 can be a field. is this correct?
        }
        return eval;
    }

    private void setFuncsIfNeeded(OpEvaluator eval, Object op1) {
        if (op1 instanceof Tok.FieldTok) {
            Tok.FieldTok fexp = (Tok.FieldTok) op1;
            if (!fexp.funcL.isEmpty()) {
                eval.setFuncs(fexp.funcL);
            }
        }
    }

    private OpEvaluator doCreate(String opStr, Object op1, Object op2, DType op1HintType, DType op2HintType) {
        OP op = OP.createFromString(opStr);
        if (op1 instanceof Tok.FieldTok) {
            Tok.FieldTok exp = (Tok.FieldTok) op1;
            String fieldName = exp.fieldName; //exp.strValue();
            if (op1HintType != null) {
                return creatFromHint(op, fieldName, op1HintType);
            }

            OpEvaluator eval = createFromExp(op, op2, fieldName);
            return eval;
        } else if (op2 instanceof Tok.FieldTok) {
            Tok.FieldTok exp = (Tok.FieldTok) op2;
            String fieldName = exp.strValue();
            if (op2HintType != null) {
                return creatFromHint(op, fieldName, op2HintType);
            }

            OpEvaluator eval = createFromExp(op, op1, fieldName);
            return eval;
        } else {
            DeliaExceptionHelper.throwNotImplementedError("3405958");
            return null; //TODO fix
        }
    }

    private OpEvaluator createFromExp(OP op, Object op2, String fieldName) {
        if (op2 instanceof Tok.ValueTok) {
            Tok.ValueTok vexp = (Tok.ValueTok) op2;
			if (vexp.value != null) {
				switch (vexp.value.getType().getShape()) {
					case INTEGER:
						return new IntOpEvaluator(op, fieldName, filterFnSvc);
					case NUMBER:
						return new NumberOpEvaluator(op, fieldName, filterFnSvc);
//					case LONG:
//						return new LongOpEvaluator(op, fieldName);
				}
			}
		}
        if (op2 instanceof Tok.NullTok) {
            return new NullOpEvaluator(op, fieldName);
        } else {
            return new StringOpEvaluator(op, fieldName, filterFnSvc);
        }
    }

    private OpEvaluator creatFromHint(OP op, String fieldName, DType hintType) {
        switch (hintType.getShape()) {
            case INTEGER:
                return new IntOpEvaluator(op, fieldName, filterFnSvc);
//            case LONG:
//                return new LongOpEvaluator(op, fieldName);
            case NUMBER:
                return new NumberOpEvaluator(op, fieldName, filterFnSvc);
            case BOOLEAN:
                return new BooleanOpEvaluator(op, fieldName, filterFnSvc);
            case DATE:
                return new DateOpEvaluator(op, fieldName, fmtSvc, filterFnSvc);
            case STRUCT:
                return new RelationOpEvaluator(op, fieldName, factorySvc, filterFnSvc);
            default:
                return new StringOpEvaluator(op, fieldName, filterFnSvc);
        }
    }
}