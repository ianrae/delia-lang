package org.delia.compiler;

import org.delia.compiler.ast.AST;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;

import java.util.ArrayList;
import java.util.List;

public class CompilerPassResults {
    public List<DeliaError> errors = new ArrayList<>();

    public boolean success() {
        return errors.isEmpty();
    }

    public DeliaError addError(DeliaError err, AST.StatementBaseAst statement) {
        if (statement != null) {
            err.setLoc(statement.loc);
        }
        errors.add(err);
        return err;
    }
    public DeliaError addError(String id, String msg, AST.StatementBaseAst statement, ErrorTracker et) {
        DeliaError err = new DeliaError(id, msg);
        et.add(err);
        return addError(err,statement);
    }

}
