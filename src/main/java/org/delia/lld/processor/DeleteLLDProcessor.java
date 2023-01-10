package org.delia.lld.processor;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dbimpl.mem.impl.QueryType;
import org.delia.dbimpl.mem.impl.QueryTypeDetector;
import org.delia.dval.DRelationHelper;
import org.delia.hld.HLD;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.tok.Tok;
import org.delia.tok.TokVisitorUtils;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteLLDProcessor extends LLDProcessorBase {

    private final LLTokWhereAdjuster whereAdjuster;
    //avoid creating multiple instances of LLTable for same table
    //However key is log.phys name because Customer.Customer can exist (main select part)
    //and Customer.Address can also exist (in a join)
    private Map<String, LLD.LLTable> tableMap = new HashMap<>(); //logicalTypeName.physicalTypeName,it's table

    public DeleteLLDProcessor(FactoryService factorySvc, DatService datSvc) {
        super(factorySvc,datSvc);
        this.whereAdjuster = new LLTokWhereAdjuster(factorySvc);
    }

    @Override
    public void build(HLD.HLDStatement hldStatementParam, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx) {
        HLD.DeleteHLDStatement statement = (HLD.DeleteHLDStatement) hldStatementParam;
        LLD.LLTable llTable = createLLTable(statement.hldTable);
//            public int datId; //0 means not an assoc table


        //and now any assoc tbl deletes
        if (!ctx.isMEMDb()) {
            List<LLD.LLDelete> assocDeletes = new ArrayList<>();
            createAssocDeletes(statement, llTable, assocDeletes, ctx);
            lldStatements.addAll(assocDeletes);
        }

        LLD.LLDelete deleteLL = new LLD.LLDelete(hldStatementParam.getLoc());
        deleteLL.table = llTable;
        deleteLL.whereTok = statement.whereTok;
        deleteLL.whereTok = statement.whereTok;

        fillInWhereExpHints(deleteLL);
        lldStatements.add(deleteLL);
    }

    private void createAssocDeletes(HLD.DeleteHLDStatement statement, LLD.LLTable llTable, List<LLD.LLDelete> assocDeletes, LLDBuilderContext ctx) {
        QueryTypeDetector detector = new QueryTypeDetector();
        QueryType queryType = detector.detectQueryType(statement.whereTok, null);

        for(TypePair pair: llTable.physicalType.getAllFields()) {
            if (pair.type.isStructShape()) {
                RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(llTable.physicalType, pair);
                if (relinfo != null && relinfo.isManyToMany() && otherSideIsOptional(relinfo)) {
                    Tok.PKWhereTok pktok = TokVisitorUtils.getIfWherePK(statement.whereTok.where);

                    DValue fkvalue = pktok != null ? pktok.value.value : null;
                    LLD.LLDelete llDelete =  doCreateAssocDeleteEx(llTable.physicalType, pair, queryType, fkvalue, statement.whereTok, ctx);
                    if (llDelete != null) {
                        assocDeletes.add(llDelete);
                    }
                }
            }
        }
    }

    private boolean otherSideIsOptional(RelationInfo relinfo) {
        if (relinfo.otherSide == null) {
            return true;
        }
        return relinfo.farType.fieldIsOptional(relinfo.otherSide.fieldName);
    }

    private void fillInWhereExpHints(LLD.LLDelete deleteLL) {
        whereAdjuster.fillInWhereExpHints(deleteLL.table, deleteLL.whereTok);
        //don't need other adjustments because DELETE doesn't allow joins
    }

    private LLD.LLTable createLLTable(DStructType hldTable) {
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
