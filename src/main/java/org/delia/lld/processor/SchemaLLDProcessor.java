package org.delia.lld.processor;

import org.delia.hld.HLD;
import org.delia.lld.LLD;

import java.util.List;

public class SchemaLLDProcessor implements LLDProcessor {
    @Override
    public void build(HLD.HLDStatement hldStatementParam, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx) {
        HLD.SchemaHLDStatement statement = (HLD.SchemaHLDStatement) hldStatementParam;
        LLD.LLCreateSchema stmt = new LLD.LLCreateSchema(hldStatementParam.getLoc());
        stmt.schema = statement.schema;
        lldStatements.add(stmt);
    }
}
