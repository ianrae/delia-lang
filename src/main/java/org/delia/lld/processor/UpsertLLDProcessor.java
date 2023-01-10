package org.delia.lld.processor;

import org.delia.core.FactoryService;
import org.delia.hld.HLD;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

import java.util.ArrayList;
import java.util.List;

public class UpsertLLDProcessor extends UpdateLLDProcessor {
    public UpsertLLDProcessor(FactoryService factorySvc, DatService datSvc) {
        super(factorySvc, datSvc);
    }

    @Override
    public void build(HLD.HLDStatement hldStatementParam, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx) {
        HLD.UpsertHLDStatement statement = (HLD.UpsertHLDStatement) hldStatementParam;
        LLD.LLTable llTable = createLLTable(statement.hldTable);
//            public int datId; //0 means not an assoc table

        LLD.LLUpsert upsertLL = new LLD.LLUpsert(hldStatementParam.getLoc());
        upsertLL.table = llTable;
        upsertLL.whereTok = adjustWhereClause(statement.whereTok, statement.hldTable, ctx);
//        upsertLL.whereTok = statement.whereTok;

        List<LLD.LLInsert> assocInserts = new ArrayList<>();
        List<LLD.LLDelete> assocDeletes = new ArrayList<>();
        upsertLL.fieldL = createFields(llTable, statement.fields, assocInserts, assocDeletes, statement.fieldPK, ctx);

        //if single field (the pk) then we still want to execute the upsert
//        if (upsertLL.fieldL.isEmpty()) {
//            return; //nothing to do
//        }
        upsertLL.pkField = createSingleField(llTable, statement.fieldPK.hldField.pair, statement.fieldPK.dvalue);
        upsertLL.noUpdateFlag = statement.noUpdateFlag;

        fillInWhereExpHints(upsertLL.table, upsertLL.whereTok);
        lldStatements.add(upsertLL);

        //and now any assoc tbl inserts
        if (!ctx.isMEMDb()) {
            lldStatements.addAll(convertToUpdates(statement, assocInserts, ctx));
            lldStatements.addAll(assocDeletes);
        }
    }
}
