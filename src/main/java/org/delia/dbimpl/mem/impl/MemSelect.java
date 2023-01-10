package org.delia.dbimpl.mem.impl;


import org.delia.core.FactoryService;
import org.delia.db.SelectDBContext;
import org.delia.dval.DValueCopyService;
import org.delia.lld.LLD;
import org.delia.runner.DeliaRunner;
import org.delia.runner.QueryResponse;
import org.delia.sql.LLFieldHelper;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.dbimpl.mem.impl.func.QScope;
import org.delia.dbimpl.mem.impl.func.ScopedFunctionExecutor;
import org.delia.dbimpl.mem.MemTableFinder;

import java.util.List;

public class MemSelect extends MemFilterBase {

    private final DValueCopyService copySvc;
    private final FKResolver fkResolver;
    private final ScopedFunctionExecutor scopedFuncExecutor;
    private final SelectDBContext ctx;

    public MemSelect(FactoryService factorySvc, DTypeRegistry registry, FKResolver fkResolver, MemTableFinder tableFinder, DeliaRunner deliaRunner, SelectDBContext ctx) {
        super(factorySvc, registry, tableFinder);
        this.copySvc = new DValueCopyService(factorySvc, registry);
        this.fkResolver = fkResolver;
        this.scopedFuncExecutor = new ScopedFunctionExecutor(factorySvc, registry, fkResolver, tableFinder, deliaRunner);
        this.ctx = ctx;
    }

    public QueryResponse executeSelect(MemDBTable tbl, LLD.LLSelect stmt) {
        QueryResponse qresp = new QueryResponse();

        SelectSpec spec = createSelectorEx(tbl, stmt.table.physicalType.getTypeName(), stmt.whereTok, stmt.whereAllOrPKType);
        if (spec.selector == null) {
            //err!!
            return qresp;
        } else {
            List<QScope> scopes = scopedFuncExecutor.buildScopeList(tbl, stmt, spec.details, ctx);

//            adjustSpecIfWhereAll(spec, stmt, scopes);
            List<DValue> dvalList = execSpec(spec, stmt);
            if (spec.selector.wasError()) {
                //err!!
                qresp.ok = false;
                return qresp;
            }

            qresp = scopedFuncExecutor.executeFunctionsAndFields(scopes, dvalList);
            return qresp;
        }
    }

    private List<DValue> execSpec(SelectSpec spec, LLD.LLSelect stmt) {
        List<DValue> dvalList = spec.selector.match(spec.selector.getTbl().rowL);
//        if (spec.isTable(stmt.getTableName())) { //normal case?
//        } else {
//        }
        return dvalList;
    }

    private boolean containsFKs(LLD.LLSelect stmt) {
        return LLFieldHelper.findFunc(stmt.finalFieldsL, "fks") != null;
    }

}