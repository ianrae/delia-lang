package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.tok.TokClauseBuilder;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeName;

import java.util.List;

public class DeleteHLDStatementBuilder extends ServiceBase implements HLDStatementBuilder {

    private final TokClauseBuilder tokBuilder;
    private final HLDWhereHelper whereHelper;

    public DeleteHLDStatementBuilder(FactoryService factorySvc) {
        
        super(factorySvc);
        this.tokBuilder = new TokClauseBuilder(factorySvc);
        this.whereHelper = new HLDWhereHelper(factorySvc);
    }

    @Override
    public void build(AST.StatementAst statementParam, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        AST.DeleteStatementAst statement = (AST.DeleteStatementAst) statementParam;
        DTypeName dtypeName = new DTypeName(statement.schemaName, statement.typeName);
        DType dtype = ctx.registry.getType(dtypeName);
        HLD.DeleteHLDStatement hld = new HLD.DeleteHLDStatement((DStructType) dtype, statementParam.getLoc()); //TODO check is structType and log error
        hld.whereClause = statement.whereClause;
        hld.whereTok = tokBuilder.buildWhere(statement.whereClause);

        hldStatements.add(hld);
        hld.whereTok.where = whereHelper.adjustWhereClauseForPKAndVars(hld.whereTok.where, hld.hldTable, ctx);
    }


    @Override
    public void assignDATs(HLD.HLDStatement statement, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
    }
}
