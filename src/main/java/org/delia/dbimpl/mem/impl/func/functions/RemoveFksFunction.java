package org.delia.dbimpl.mem.impl.func.functions;

import org.delia.core.FactoryService;
import org.delia.dval.DValueCopyService;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.sql.LLFieldHelper;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;

import java.util.List;

public class RemoveFksFunction extends FunctionBase {

    public RemoveFksFunction(FactoryService factorySvc, LLD.LLDFuncEx funcEl) {
        super(factorySvc, funcEl);
    }

    @Override
    public List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx) {
        List<DValue> newList = dvalList;

        DType dtype = detectType(dvalList);
        if (dtype == null || !dtype.isStructShape()) { //if we've got a scalar field then don't do fks
            return dvalList;
        }

        boolean containsFKs = containsFKs(ctx); //does current scope contain .fks()?
        if (!containsFKs) {
            //DValues in tbl always have a DRelation for relation field, even if empty
            DValueCopyService copySvc = new DValueCopyService(factorySvc, ctx.registry);

            newList = copySvc.clone(dvalList);
            log.log("fks!");
            List<RelationInfo> relinfos = ctx.fkResolver.findRelationsNeedingFK(ctx.scope.structType);
            for (DValue dval : newList) {
                ctx.fkResolver.clearRelations(dval, relinfos);
            }
        }

        return newList;
    }

    private boolean containsFKs(MemFunctionContext ctx) {
        return LLFieldHelper.existsFunc(ctx.scope.funcL, "fks");
    }
}
