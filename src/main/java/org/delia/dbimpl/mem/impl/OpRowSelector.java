package org.delia.dbimpl.mem.impl;

import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.dbimpl.mem.impl.filter.InEvaluator;
import org.delia.dbimpl.mem.impl.filter.MultiOpEvaluator;
import org.delia.dbimpl.mem.impl.filter.OpEvaluator;
import org.delia.dbimpl.mem.impl.filter.OpFactory;
import org.delia.dbimpl.mem.impl.filter.filterfn.FilterFunctionService;
import org.delia.error.ErrorTracker;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public class OpRowSelector extends RowSelectorBase {
    private static class EvalSpec {
        Tok.OperandTok operand;
        Tok.OperatorTok owner;
        Tok.DToken elop;
        DType opHintType = null;

        public EvalSpec innerSpec1;
        public EvalSpec innerSpec2;

        OpEvaluator inEvaluator;

        boolean isSingleEval() {
            return elop != null;
        }

        boolean isInEval() {
            return inEvaluator != null;
        }
    }


    private OpEvaluator evaluator;
    private DateFormatService fmtSvc;
    private FactoryService factorySvc;
    private FilterEvaluator filterEvaluator;

    public OpRowSelector(DateFormatService fmtSvc, FactoryService factorySvc, FilterEvaluator evaluator) { //}, ImplicitFetchContext implicitCtx) {
        this.fmtSvc = fmtSvc;
        this.factorySvc = factorySvc;
        this.filterEvaluator = evaluator;
    }

    @Override
    public void init(ErrorTracker et, Tok.WhereTok whereClause, DStructType dtype, DTypeRegistry registry) {
        super.init(et, whereClause, dtype, registry);
        FilterFunctionService filterFnSvc = new FilterFunctionService(factorySvc, registry);
        Tok.OperatorTok fullexp = (Tok.OperatorTok) whereClause.where;

        EvalSpec spec1 = initZ0(fullexp.op1, dtype, filterFnSvc, fullexp, false);
        EvalSpec spec2 = initZ0(fullexp.op2, dtype, filterFnSvc, fullexp, spec1.isInEval());

        //TODO we only support certain mixes of single or multi. fix this!!
        if (spec1.isSingleEval() && spec2.isSingleEval()) {
            this.evaluator = initSingleOpExpression(spec1, spec2, dtype);
        } else if (spec1.isInEval() && spec2.isSingleEval()) {
            this.evaluator = spec1.inEvaluator;
        } else if (spec1.isSingleEval() && spec2.isInEval()) {
            this.evaluator = spec2.inEvaluator;
        } else {
            this.evaluator = initNestedOperator(spec1, spec2, fullexp, filterFnSvc);
        }
    }

    private OpEvaluator initNestedOperator(EvalSpec spec1, EvalSpec spec2, Tok.OperatorTok operand, FilterFunctionService filterFnSvc) {
        OpEvaluator eval1;
        if (spec1.isInEval()) {
            eval1 = spec1.inEvaluator;
        } else if (spec1.innerSpec1.isSingleEval()) { //assume innerSpec2 is too
            eval1 = initSingleOpExpression(spec1.innerSpec1, spec1.innerSpec2, dtype);
        } else {
            eval1 = initNestedOperator(spec1.innerSpec1, spec1.innerSpec2, operand, filterFnSvc); //*** recursion ***
        }

        OpEvaluator eval2;
        if (spec2.isInEval()) {
            eval2 = spec2.inEvaluator;
        } else if (spec2.innerSpec1.isSingleEval()) { //assume innerSpec2 is too
            eval2 = initSingleOpExpression(spec2.innerSpec1, spec2.innerSpec2, dtype);
        } else {
            eval2 = initNestedOperator(spec2.innerSpec1, spec2.innerSpec2, operand, filterFnSvc); //*** recursion ***
        }

        return new MultiOpEvaluator(operand, eval1, eval2, dtype, registry, fmtSvc, factorySvc);
    }

    private OpEvaluator initSingleOpExpression(EvalSpec spec1, EvalSpec spec2, DStructType dtype) {
        Tok.DToken elop1 = spec1.elop;
        Tok.DToken elop2 = spec2.elop;

        //handle int-long issues by determining field type
        DType op1HintType = spec1.opHintType;
        DType op2HintType = spec2.opHintType;

        OpFactory factory = new OpFactory(registry, fmtSvc, factorySvc, dtype);
        this.evaluator = factory.create(spec1.owner.op, elop1, elop2, op1HintType, op2HintType, spec1.owner.negFlag);
        //support id < 10 and also 10 < id
        boolean reversed = (elop2 instanceof Tok.FieldTok); // || (xop2 instanceof XNAFMultiExp);
        if (reversed) {
            evaluator.setRightVar(elop1);
        } else {
            evaluator.setRightVar(elop2);
        }
        return evaluator;
    }

    private EvalSpec initZ0(Tok.OperandTok operand, DStructType dtype, FilterFunctionService filterFnSvc, Tok.OperatorTok fullexp,
                            boolean otherSpecIsInExp) {
        if (!otherSpecIsInExp && "in".equals(fullexp.op)) {
            EvalSpec espec = new EvalSpec();
            espec.inEvaluator = new InEvaluator(fullexp, dtype);
            espec.owner = fullexp;
            return espec;
        }

        if (operand instanceof Tok.OperatorTok) {
            return initZ2((Tok.OperatorTok) operand, dtype, filterFnSvc);
        } else {
            return initZ1(operand, dtype, filterFnSvc, fullexp);
        }
    }

    private EvalSpec initZ1(Tok.OperandTok operand, DStructType dtype, FilterFunctionService filterFnSvc, Tok.OperatorTok owner) {
        Tok.DToken elop1 = ((Tok.DottedTok) operand).chainL.get(0);

        //handle int-long issues by determining field type
        DType op1HintType = null;
        if (elop1 instanceof Tok.FieldTok) {
            op1HintType = calcHint((Tok.FieldTok) elop1, dtype, filterFnSvc);
        }
        EvalSpec espec = new EvalSpec();
        espec.operand = operand;
        espec.elop = elop1;
        espec.opHintType = op1HintType;
        espec.owner = owner;
        return espec;
    }

    private EvalSpec initZ2(Tok.OperatorTok operand, DStructType dtype, FilterFunctionService filterFnSvc) {
        EvalSpec eval1;
        if ("in".equals(operand.op)) {
            EvalSpec espec = new EvalSpec();
            espec.inEvaluator = new InEvaluator(operand, dtype);
            espec.owner = operand;
            return espec;
        } else if (operand.op1 instanceof Tok.OperatorTok) {
            Tok.OperatorTok optok = (Tok.OperatorTok) operand.op1;
            if ("in".equals(optok.op)) {
                EvalSpec espec = new EvalSpec();
                espec.inEvaluator = new InEvaluator(optok, dtype);
                espec.owner = optok;
                return espec;
            }
            eval1 = initZ2((Tok.OperatorTok) operand.op1, dtype, filterFnSvc); //*** recursion ***
        } else {
            eval1 = initZ1(operand.op1, dtype, filterFnSvc, operand);
        }

        EvalSpec eval2;
        if (operand.op2 instanceof Tok.OperatorTok) {
            Tok.OperatorTok optok = (Tok.OperatorTok) operand.op2;
            if ("in".equals(optok.op)) {
                EvalSpec espec = new EvalSpec();
                espec.inEvaluator = new InEvaluator(optok, dtype);
                espec.owner = optok;
                return espec;
            }
            eval2 = initZ2((Tok.OperatorTok) operand.op2, dtype, filterFnSvc); //*** recursion ***
        } else {
            eval2 = initZ1(operand.op2, dtype, filterFnSvc, operand);
        }

        EvalSpec espec = new EvalSpec();
        espec.operand = operand;
        espec.innerSpec1 = eval1;
        espec.innerSpec2 = eval2;
        return espec;
    }


    private DType calcHint(Tok.FieldTok fexp, DStructType dtype, FilterFunctionService filterFnSvc) {
        if (!fexp.funcL.isEmpty()) {
            return filterFnSvc.getFunctionType(fexp);
        }
        return DValueHelper.findFieldType(dtype, fexp.strValue());
    }

    @Override
    public List<DValue> match(MemDBTable tbl) {
        List<DValue> list = tbl.getList();
        List<DValue> resultL = new ArrayList<>();
        for (DValue dval : list) {
            boolean b = evaluator.match(dval);
            if (b) {
                resultL.add(dval);
            }
        }
        return resultL;
    }
}