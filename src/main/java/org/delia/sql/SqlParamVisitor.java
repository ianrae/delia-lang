package org.delia.sql;

import org.delia.compiler.ast.Exp;
import org.delia.lld.LLD;
import org.delia.tok.Tok;
import org.delia.type.DValue;
import org.delia.type.TypePair;

import java.util.ArrayList;
import java.util.List;

public class SqlParamVisitor implements Tok.TokVisitor {
    public TypePair pkpair;
    public Tok.OperandTok top;
    public Tok.TokBase prev;
    public List<DValue> sqlParams = new ArrayList<>();
    public List<Tok.ValueTok> fieldValues = new ArrayList<>(); //parallel list of sqlParams

    @Override
    public void visit(Tok.TokBase exp, Tok.TokBase parent) {
        if (exp instanceof Tok.ValueTok) {
            Tok.ValueTok vexp = (Tok.ValueTok) exp;
//            if ((prev == top) && pkpair != null) {
            if ((prev == top)) {
                if (vexp.strValue().equals("true")) {
                    return;
                }
            }
            sqlParams.add(vexp.value);
            fieldValues.add(vexp);
        }
        prev = exp;
    }
}
