package org.delia.compiler;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBType;
import org.delia.error.DeliaError;
import org.delia.error.ErrorFormatter;
import org.delia.hld.HLDFirstPassResults;
import org.delia.type.DTypeRegistry;

public class CompilerPassBase extends ServiceBase {
    protected final DTypeRegistry registry;
    protected final DBType dbType;
    private final ErrorFormatter errorFormatter;

    public CompilerPassBase(FactoryService factorySvc, HLDFirstPassResults firstPassResults, DBType dbType, ErrorFormatter errorFormatter) {
        super(factorySvc);
        this.registry = firstPassResults.registry;
        this.dbType = dbType;
        this.errorFormatter = errorFormatter;
    }

    protected void addError(CompilerPassResults results, String errId, String msg, Object ast) {
        DeliaError err = createError(errId, msg, ast);
        results.errors.add(err);
    }

    protected DeliaError createError(String id, String errMsg, Object exp) {
        AST.Loc loc = null;
        if (exp instanceof AST.StatementBaseAst) {
            AST.StatementBaseAst baseAst = (AST.StatementBaseAst) exp;
            if (baseAst.loc != null) {
                loc = baseAst.loc;
            }
        } else if (exp instanceof AST.TypeFieldAst) {
            AST.TypeFieldAst fieldAst = (AST.TypeFieldAst) exp;
            if (fieldAst.loc != null) {
                loc = fieldAst.loc;
            }
        }
        String msg = String.format("%s", errMsg);

        DeliaError err = new DeliaError(id, msg);
        err.setLoc(loc);
        if (errorFormatter != null) {
            String str = errorFormatter.format(err);
            log.logError(str);
            et.addNoLog(err);
        } else {
            et.add(err);
        }
        return err;
    }

}