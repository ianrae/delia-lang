package org.delia.lld.processor;

import org.delia.core.FactoryService;
import org.delia.hld.HLD;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.type.TypePair;

import java.util.ArrayList;
import java.util.List;

public class InsertLLDProcessor extends LLDProcessorBase {


    public InsertLLDProcessor(FactoryService factorySvc, DatService datSvc) {
        super(factorySvc, datSvc);
    }

    @Override
    public void build(HLD.HLDStatement hldStatementParam, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx) {
        HLD.InsertHLDStatement statement = (HLD.InsertHLDStatement) hldStatementParam;
        LLD.LLTable llTable = new LLD.LLTable(statement.hldTable, statement.hldTable, new LLD.DefaultLLNameFormatter());
//            llTable.alias;
//            public int datId; //0 means not an assoc table

        LLD.LLInsert insertLL = new LLD.LLInsert(hldStatementParam.getLoc());
        insertLL.table = llTable;

        List<LLD.LLInsert> assocInserts = new ArrayList<>();
        List<LLD.LLDelete> assocDeletes = new ArrayList<>();
        insertLL.fieldL = createFields(llTable, statement.fields, assocInserts,assocDeletes, null, ctx);
        addSyntheticFieldIfNeeded(statement, insertLL, ctx);

        lldStatements.add(insertLL);

        //and now any assoc tbl inserts
        if (!ctx.isMEMDb()) {
            lldStatements.addAll(assocInserts);
            lldStatements.addAll(assocDeletes);
        }
    }

    private void addSyntheticFieldIfNeeded(HLD.InsertHLDStatement statement, LLD.LLInsert insertLL, LLDBuilderContext ctx) {
        if (statement.syntheticIdField == null) return;

        TypePair pair = new TypePair(statement.syntheticIdField, statement.syntheticIDValue.getType());
        LLD.LLField field = new LLD.LLField(pair, insertLL.table, new LLD.DefaultLLNameFormatter());
        LLD.LLFieldValue fieldValue = new LLD.LLFieldValue(field, statement.syntheticIDValue);
        insertLL.syntheticField = fieldValue;
    }


}
