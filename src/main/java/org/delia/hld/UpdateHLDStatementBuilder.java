package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeName;

import java.util.List;

public class UpdateHLDStatementBuilder extends StatementBuilderBase {

    public UpdateHLDStatementBuilder(FactoryService factorySvc) {
        super(factorySvc);
    }

    @Override
    public void build(AST.StatementAst statementParam, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        AST.UpdateStatementAst statement = (AST.UpdateStatementAst) statementParam;
        DTypeName dTypeName = new DTypeName(statement.schemaName, statement.typeName);
        DType dtype = ctx.registry.getType(dTypeName);
        DStructType structType = (DStructType) dtype;//TODO check is structType and log error
        HLD.UpdateHLDStatement hld = new HLD.UpdateHLDStatement((DStructType) dtype, statementParam.getLoc()); //TODO check is structType and log error
        hld.whereClause = statement.whereClause;
        hld.whereTok = tokBuilder.buildWhere(statement.whereClause);
        hld.fields = buildFields(statement.fields, structType, ctx, false);

        hldStatements.add(hld);
        hld.whereTok.where = whereHelper.adjustWhereClauseForPKAndVars(hld.whereTok.where, hld.hldTable, ctx);
    }


    @Override
    public void assignDATs(HLD.HLDStatement statement, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
    }
}
