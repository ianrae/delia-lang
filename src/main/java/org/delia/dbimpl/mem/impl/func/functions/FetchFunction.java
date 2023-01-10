package org.delia.dbimpl.mem.impl.func.functions;

import org.delia.core.FactoryService;
import org.delia.core.QueryService;
import org.delia.lld.LLD;
import org.delia.runner.DeliaRunner;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;

import java.util.List;

public class FetchFunction extends FunctionBase {

    private final String fieldToFetch;
    private final DeliaRunner deliaRunner;
    private final DTypeRegistry registry;

    public FetchFunction(FactoryService factorySvc, LLD.LLDFuncEx funcEl, DeliaRunner deliaRunner, DTypeRegistry registry) {

        super(factorySvc, funcEl);
        this.fieldToFetch = getArgStr(0); //TODO: later support multiple fields comma-separated
        this.deliaRunner = deliaRunner;
        this.registry = registry;
    }

    @Override
    public List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx) {
        QueryService querySvc = new QueryService(factorySvc, deliaRunner);
        DStructType structType = null;

        for(DValue dval: dvalList) {
            DValue inner = dval.asStruct().getField(fieldToFetch);
            if (inner != null) {
                DRelation drel = inner.asRelation();
                if (structType == null) {
                    structType = registry.getStructType(drel.getTypeName());
                }

                for(DValue fkval: drel.getMultipleKeys()) {
                    List<DValue> list = querySvc.queryPK(structType, fkval);
                    drel.setFetchedItems(list);
                }
            }

        }
        return dvalList;
    }

}
