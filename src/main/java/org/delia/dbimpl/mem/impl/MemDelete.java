package org.delia.dbimpl.mem.impl;


import org.delia.core.FactoryService;
import org.delia.dbimpl.mem.MemTableFinder;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.runner.QueryResponse;
import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

import java.util.List;

public class MemDelete extends MemFilterBase {

    private final FKResolver fkResolver;

    public MemDelete(FactoryService factorySvc, DTypeRegistry registry, FKResolver fkResolver, MemTableFinder tableFinder) {
        super(factorySvc, registry, tableFinder);
        this.fkResolver = fkResolver;
    }

    //TODO should this be void? how to handle errors
    public QueryResponse executeDelete(MemDBTable tbl, LLD.LLDelete stmt) {
        QueryResponse qresp = new QueryResponse();
        RowSelector selector = createSelector(tbl, stmt.table.physicalType.getTypeName(), stmt.whereTok);
        if (selector == null) {
            //err!!
            return qresp;
        } else {
            List<DValue> dvalList = selector.match(selector.getTbl().rowL);
            if (selector.wasError()) {
                //err!!
                qresp.ok = false;
                return qresp;
            }

            DStructType structType = stmt.table.physicalType;
            List<RelationInfo> relationFields = fkResolver.findRelationsNeedingFK(structType);

            for (DValue dval : dvalList) {
                tbl.rowL.remove(dval);
                //remove dval from far side of any relations
                //TODO: what about one-sided relations??
                for (RelationInfo relinfo : relationFields) {
                    fkResolver.removeFromFarSide(dval, relinfo);
                }
            }

            qresp.ok = true;
            return qresp;
        }
    }

}