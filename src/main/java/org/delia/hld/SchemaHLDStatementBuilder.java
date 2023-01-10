package org.delia.hld;

import org.delia.compiler.ast.AST;

import java.util.List;

public class SchemaHLDStatementBuilder implements HLDStatementBuilder {

    @Override
    public void build(AST.StatementAst statementParam, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        AST.SchemaAst statement = (AST.SchemaAst) statementParam;
        HLD.SchemaHLDStatement hld = new HLD.SchemaHLDStatement(statementParam.getLoc());
        hld.schema = statement.schemaName;
        ctx.currentSchema = hld.schema;
        hldStatements.add(hld);
    }

    @Override
    public void assignDATs(HLD.HLDStatement statement, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {

    }
}
