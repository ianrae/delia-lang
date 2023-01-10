package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.dbimpl.mem.impl.QueryType;
import org.delia.dbimpl.mem.impl.QueryTypeDetector;
import org.delia.lld.processor.WhereClauseUtils;
import org.delia.tok.Tok;
import org.delia.type.*;
import org.delia.util.DValueHelper;

import java.util.List;

public class UpsertHLDStatementBuilder extends StatementBuilderBase {

    public UpsertHLDStatementBuilder(FactoryService factorySvc) {
        super(factorySvc);
    }

    @Override
    public void build(AST.StatementAst statementParam, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        AST.UpsertStatementAst statement = (AST.UpsertStatementAst) statementParam;
        DTypeName dtypeName = new DTypeName(statement.schemaName, statement.typeName);
        DType dtype = ctx.registry.getType(dtypeName);
        DStructType structType = (DStructType) dtype;//TODO check is structType and log error
        HLD.UpsertHLDStatement hld = new HLD.UpsertHLDStatement((DStructType) dtype, statement.getLoc()); //TODO check is structType and log error
        hld.whereClause = statement.whereClause;
        hld.whereTok = tokBuilder.buildWhere(statement.whereClause);
        hld.fields = buildFields(statement.fields, structType, ctx, false);

        TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(hld.hldTable);
        if (structType.fieldIsSerial(pkpair.name)) { //TODO later support this if filter is unique field
            String msg = String.format("upsert: not supported on type '%s' since it has a serial primary key", structType.getName());
            ctx.localET.add("upsert-filter-error", msg);
            return;
        }

        QueryType queryType = calcQueryType(hld.whereTok);
        if (!QueryType.PRIMARY_KEY.equals(queryType)) {
            String msg = String.format("upsert %s: filter must be a primary key", structType.getName(), hld.whereClause.strValue());
            ctx.localET.add("upsert-filter-error", msg);
            return;
        }

        DValue dvalPK = WhereClauseUtils.extractPKWhereClause(hld.whereClause);
        hld.fieldPK = buildSingleField(pkpair, dvalPK, structType, ctx);
        hld.noUpdateFlag = statement.noUpdateFlag;

        hld.whereTok.where = whereHelper.adjustWhereClauseForPKAndVars(hld.whereTok.where, hld.hldTable, ctx);
        hldStatements.add(hld);
    }

    private QueryType calcQueryType(Tok.WhereTok whereClause) {
        QueryTypeDetector detector = new QueryTypeDetector();
        return detector.detectQueryType(whereClause, null);
    }

    @Override
    public void assignDATs(HLD.HLDStatement statement, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
    }
}
