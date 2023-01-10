package org.delia.hld;

import org.delia.compiler.ast.Exp;

import java.util.ArrayList;
import java.util.List;

public class MyFieldVisitor implements Exp.ExpVisitor {
    public List<Exp.FieldExp> allFields = new ArrayList<>();
    public boolean onlyJoinFields = false;

    @Override
    public void visit(Exp.ExpBase exp) {
        if (exp instanceof Exp.FieldExp) {
            Exp.FieldExp fexp = (Exp.FieldExp) exp;
            if (onlyJoinFields) {
                if (fexp.joinInfo != null) {
                    this.allFields.add(fexp);
                }
            } else {
                this.allFields.add(fexp);
            }
        }
    }
}
