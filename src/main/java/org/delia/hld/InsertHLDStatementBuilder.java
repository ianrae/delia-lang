package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeName;

import java.util.List;
import java.util.Optional;

public class InsertHLDStatementBuilder extends StatementBuilderBase {

    public InsertHLDStatementBuilder(FactoryService factorySvc) {
        super(factorySvc);
    }

    @Override
    public void build(AST.StatementAst statementParam, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        AST.InsertStatementAst statement = (AST.InsertStatementAst) statementParam;
        HLD.InsertHLDStatement hld = new HLD.InsertHLDStatement(statementParam.getLoc());
        DTypeName dTypeName = new DTypeName(statement.schemaName, statement.typeName);
        DType dtype = ctx.registry.getType(dTypeName);
        DStructType structType = (DStructType) dtype;//TODO check is structType and log error
        hld.hldTable = structType;
        hld.fields = buildFields(statement.fields, structType, ctx, true);
        buildSyntheticFieldIfNeeded(statement, hld, ctx);

        hldStatements.add(hld);
    }

    private void buildSyntheticFieldIfNeeded(AST.InsertStatementAst statement, HLD.InsertHLDStatement hld, HLDBuilderContext ctx) {
        hld.syntheticIdField = getSyntheticField(hld.hldTable, ctx);
        if (hld.syntheticIdField != null) {
            Optional<AST.InsertFieldStatementAst> field = statement.fields.stream().filter(x -> x.fieldName.equals(hld.syntheticIdField)).findAny();
            if (field.isPresent()) {
                hld.syntheticIDValue = field.get().valueExp.value;
            }
        }
    }

    @Override
    public void assignDATs(HLD.HLDStatement statement, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {

    }

}
