package org.delia.core;

import org.delia.DeliaSession;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.hld.DeliaExecutable;
import org.delia.runner.DeliaRunner;
import org.delia.runner.ExpHelper;
import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.List;

/**
 * Perform simple queries programmatically
 */
public class QueryService extends ServiceBase {

    private final DeliaRunner deliaRunner;

    public QueryService(FactoryService factorySvc, DeliaRunner deliaRunner) {
        super(factorySvc);
        this.deliaRunner = deliaRunner;
    }

    public List<DValue> queryAll(DStructType structType) {
        return queryAll(structType.getTypeName());
    }

    public List<DValue> queryAll(DTypeName typeName) {
        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();
        Exp.WhereClause whereClause = ExpHelper.buildTrueWhereClause(valueBuilder);
        return buildAndRun(typeName, whereClause);
    }

    public List<DValue> queryPK(DStructType structType, DValue pkval) {
        ScalarValueBuilder valueBuilder = deliaRunner.createValueBuilder();
        Exp.WhereClause whereClause = ExpHelper.buildPKWhereClause(valueBuilder, pkval.asString()); //TODO use pkval directly later
        return buildAndRun(structType.getTypeName(), whereClause);
    }

    protected DeliaExecutable buildQuery(DTypeName typeName, Exp.WhereClause whereClause) {
        //build AST script
        AST.DeliaScript script = new AST.DeliaScript();
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.schemaName = typeName.getSchema();
        letStmt.typeName = typeName.getTypeName();

        letStmt.whereClause = whereClause;
        script.add(letStmt);

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        return executable;
    }

    protected List<DValue> buildAndRun(DTypeName typeName, Exp.WhereClause whereClause) {
        DeliaExecutable executable = buildQuery(typeName, whereClause);
        DeliaSession session = deliaRunner.execute(executable);
        return session.getFinalResult().getAsDValueList();
    }

}
