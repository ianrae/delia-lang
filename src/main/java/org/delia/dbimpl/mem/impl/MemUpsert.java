package org.delia.dbimpl.mem.impl;

import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.runner.QueryResponse;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.dbimpl.mem.MemTableFinder;

import java.util.ArrayList;
import java.util.List;

public class MemUpsert extends MemFilterBase {

    private final FKResolver fkResolver;
    private final MemUpdate memUpdate;
    private final MemInsert memInsert;

    public MemUpsert(FactoryService factorySvc, DTypeRegistry registry, FKResolver fkResolver, MemTableFinder tableFinder) {
        super(factorySvc, registry, tableFinder);
        this.fkResolver = fkResolver;
        this.memUpdate = new MemUpdate(factorySvc, registry, fkResolver, tableFinder);
        this.memInsert = new MemInsert(factorySvc, registry, fkResolver);
    }

    //TODO should this be void? how to handle errors
    public QueryResponse executeUpsert(MemDBTable tbl, LLD.LLUpsert stmt, DValue dvalToInsert, DBStuff stuff) {
        QueryResponse qresp = new QueryResponse();
        RowSelector selector = createSelector(tbl, stmt.table.physicalType.getTypeName(), stmt.whereTok);
        if (selector == null) {
            //err!!
            return qresp;
        } else {
            List<DValue> dvalList = selector.match(selector.getTbl());
            if (selector.wasError()) {
                //err!!
                qresp.ok = false;
                return qresp;
            }

            DStructType structType = stmt.table.physicalType;
            if (dvalList.isEmpty()) {
                log.log("updateI");
                List<LLD.LLFieldValue> fullList = new ArrayList<>(stmt.fieldL);
                fullList.add(stmt.pkField);
                dvalToInsert.asMap().put(stmt.pkField.field.getFieldName(), stmt.pkField.dval);
                memInsert.executeInsert(tbl, structType, fullList, dvalToInsert, stuff);
            } else if (! stmt.noUpdateFlag) {
                log.log("updateU");
                for (DValue dval : dvalList) {
                    memUpdate.processMatches(qresp, stmt.table, stmt.fieldL, dvalList);
                }
            }
        }

        qresp.ok = true;
        return qresp;
    }
}
