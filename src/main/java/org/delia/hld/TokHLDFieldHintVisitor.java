package org.delia.hld;

import org.delia.tok.Tok;
import org.delia.tok.TokVisitorUtils;
import org.delia.type.DStructType;
import org.delia.util.DValueHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects the owner of fields mentioned in whereExp
 * eg. Customer[wid < 100]  identify wid as one of
 * -field of Customer
 * -field of one of the joins
 */
public class TokHLDFieldHintVisitor implements Tok.TokVisitor {
    public Tok.OperandTok top;
    public Tok.TokBase prev;
    public DStructType structType;
    public List<HLD.HLDJoin> joinL = new ArrayList<>(); //logical joins (and their fields)
    public List<Tok.FieldTok> allFields = new ArrayList<>();
    public boolean onlyLookInJoins;

    @Override
    public void visit(Tok.TokBase exp, Tok.TokBase parent) {
        if (exp instanceof Tok.FieldTok) {
            Tok.FieldTok fexp = (Tok.FieldTok) exp;
            if (prev == top) {
                //Customer[z]. z must be a var. we'll resolve it in OuterRunner
            }
        } else if (exp instanceof Tok.ValueTok) {
            Tok.ValueTok vexp = (Tok.ValueTok) exp;
//            if ((prev == top)) {
//                //this is a pk query such as Customer[55]
//                vexp.pkOwnerType = structType;
//            }
        } else if (exp instanceof Tok.OperatorTok) {
            Tok.OperatorTok oexp = (Tok.OperatorTok) exp;
            //this handles x < 55.
            //TODO add support for subfields and functions
            Tok.FieldTok fexp1 = TokVisitorUtils.getPossibleFieldExp(oexp.op1);
            Tok.FieldTok fexp2 = TokVisitorUtils.getPossibleFieldExp(oexp.op2);
            if (fexp1 != null) {
                addHint(oexp.op1, fexp1);
            }
            if (fexp2 != null) {
                addHint(oexp.op2, fexp2);
            }
        }
        prev = exp;
    }

    private void addHint(Tok.OperandTok exp, Tok.FieldTok fexp) {
        allFields.add(fexp);
        String fieldName = fexp.fieldName;
        if (!onlyLookInJoins) {
            if (DValueHelper.fieldExists(structType, fieldName)) {
                fexp.ownerType = structType;
                return;
            }
        }

        for (HLD.HLDJoin join : joinL) {
            //TODO: what about one-sided relations? will something be null here
            DStructType tmpType = join.joinInfo.leftType;
            if (DValueHelper.fieldExists(tmpType, fieldName)) {
                fexp.ownerType = tmpType;
//                fexp.ownerFoundInJoinInfo = join.joinInfo;
                return;
            }
            tmpType = join.joinInfo.rightType;
            if (DValueHelper.fieldExists(tmpType, fieldName)) {
                fexp.ownerType = tmpType;
//                fexp.ownerFoundInJoinInfo = join.joinInfo;
                return;
            }
        }
    }

}
