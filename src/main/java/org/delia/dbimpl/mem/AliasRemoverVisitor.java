package org.delia.dbimpl.mem;

import org.delia.compiler.ast.Exp;

public class AliasRemoverVisitor implements Exp.ExpVisitor {

    @Override
    public void visit(Exp.ExpBase exp) {
        if (exp instanceof Exp.FieldExp) {
            Exp.FieldExp fexp = (Exp.FieldExp) exp;
            fexp.alias = null;
        }
    }
}
