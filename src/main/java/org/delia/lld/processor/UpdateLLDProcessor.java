package org.delia.lld.processor;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.hld.HLD;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.tok.Tok;
import org.delia.type.DStructType;

import java.util.*;

public class UpdateLLDProcessor extends LLDProcessorBase {

    //avoid creating multiple instances of LLTable for same table
    //However key is log.phys name because Customer.Customer can exist (main select part)
    //and Customer.Address can also exist (in a join)
    private Map<String, LLD.LLTable> tableMap = new HashMap<>(); //logicalTypeName.physicalTypeName,it's table
    private AssocLLDGenerator assocLLDGenerator;
    private final LLTokWhereAdjuster whereAdjuster;

    public UpdateLLDProcessor(FactoryService factorySvc, DatService datSvc) {
        super(factorySvc, datSvc);
        this.assocLLDGenerator = new AssocLLDGeneratorImpl(factorySvc, datSvc);
        this.whereAdjuster = new LLTokWhereAdjuster(factorySvc);
    }

    @Override
    public void build(HLD.HLDStatement hldStatementParam, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx) {
        HLD.UpdateHLDStatement statement = (HLD.UpdateHLDStatement) hldStatementParam;
        LLD.LLTable llTable = createLLTable(statement.hldTable);
//            public int datId; //0 means not an assoc table

        LLD.LLUpdate updateLL = new LLD.LLUpdate(hldStatementParam.getLoc());
        updateLL.table = llTable;
//        updateLL.whereClause = adjustWhereClause(statement.whereClause, ctx);
//        updateLL.whereTok = statement.whereTok;
        updateLL.whereTok = adjustWhereClause(statement.whereTok, statement.hldTable, ctx);

        List<LLD.LLInsert> assocInserts = new ArrayList<>();
        List<LLD.LLDelete> assocDeletes = new ArrayList<>();
        updateLL.fieldL = createFields(llTable, statement.fields, assocInserts, assocDeletes, null, ctx);
        if (updateLL.fieldL.isEmpty()) {
            return; //nothing to do
        }

        fillInWhereExpHints(updateLL.table, updateLL.whereTok);
        lldStatements.add(updateLL);

        //and now any assoc tbl inserts
        if (!ctx.isMEMDb()) {
            lldStatements.addAll(convertToUpdates(statement, assocInserts, ctx));
            lldStatements.addAll(assocDeletes);
        }
    }

    protected Collection<? extends LLD.LLStatement> convertToUpdates(HLD.HLDUpdateUpsertBase hldStatement, List<LLD.LLInsert> assocInserts, LLDBuilderContext ctx) {
        return assocLLDGenerator.convertToUpdates(hldStatement, assocInserts, ctx);
    }

    protected void fillInWhereExpHints(LLD.LLTable table, Tok.WhereTok whereClause) {
        whereAdjuster.fillInWhereExpHints(table, whereClause);
    }

    protected Tok.WhereTok adjustWhereClause(Tok.WhereTok whereClause, DStructType structType, LLDBuilderContext ctx) {
        whereAdjuster.adjustWhereClause(whereClause, structType, ctx);
        return whereClause;
    }

    protected LLD.LLTable createLLTable(DStructType hldTable) {
        String key = String.format("%s.%s", hldTable.getName(), hldTable.getName()); //doesn't includes schema. careful!
        if (tableMap.containsKey(key)) {
            return tableMap.get(key);
        }
        //TODO: fix schema. later will be inside DStructType
        LLD.LLTable llTable = new LLD.LLTable(hldTable, hldTable, new LLD.DefaultLLNameFormatter());
        tableMap.put(key, llTable);
        return llTable;
    }

}
