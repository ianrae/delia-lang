package org.delia.lld.processor;

import org.delia.compiler.ast.Exp;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

/**
 * Detects the type of fields mentioned in whereExp
 * eg. Customer[birthDate < '2019']  we need to know that '2019' is a date, not a string
 */
public class MyHintVisitor implements Exp.ExpVisitor {
    public TypePair pkpair;
    public Exp.OperandExp top;
    public Exp.ExpBase prev;
    public DStructType structType;

    @Override
    public void visit(Exp.ExpBase exp) {
        if (exp instanceof Exp.ValueExp) {
            Exp.ValueExp vexp = (Exp.ValueExp) exp;
            if ((prev == top) && pkpair != null) {
                if (vexp.strValue().equals("true")) {
                    return;
                }
                vexp.hintPair = pkpair;
            }
        } else if (exp instanceof Exp.OperatorExp) {
            Exp.OperatorExp oexp = (Exp.OperatorExp) exp;
            //this handles x < 55.
            //TODO add support for subfields and functions
            String fieldName1 = getPossibleFieldName(oexp.op1);
            String fieldName2 = getPossibleFieldName(oexp.op2);
            if (fieldName1 != null) {
                addHint(oexp.op2, fieldName1);
            } else {
                addHint(oexp.op1, fieldName2);
            }
        }
        prev = exp;
    }

    private String getPossibleFieldName(Exp.OperandExp exp) {
        return VisitorUtils.getPossibleFieldName(exp);
    }

    private void addHint(Exp.OperandExp exp, String fieldName) {
        if (exp instanceof Exp.ValueExp) {
            Exp.ValueExp vexp = (Exp.ValueExp) exp;
            vexp.hintPair = DValueHelper.findField(structType, fieldName);
        } else if (exp instanceof Exp.DottedExp) {
            Exp.ValueExp vexp = getSingleChainValue(exp);
            if (vexp != null) {
                vexp.hintPair = DValueHelper.findField(structType, fieldName);
            }
        }
    }

    private Exp.ValueExp getSingleChainValue(Exp.OperandExp exp) {
        return VisitorUtils.getSingleChainValue(exp);
    }

}
