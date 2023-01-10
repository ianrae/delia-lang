package org.delia.sql;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.lld.LLD;
import org.delia.type.BuiltInTypes;
import org.delia.type.DTypeRegistry;

import java.util.Optional;

public class SelectHelper extends ServiceBase {
    protected DTypeRegistry registry;

    public SelectHelper(FactoryService factorySvc, DTypeRegistry registry) {
        super(factorySvc);
        this.registry = registry;
    }

    public ResultTypeInfo getSelectResultType(LLD.LLSelect hld) {

        ResultTypeInfo resultTypeInfo = new ResultTypeInfo();
        resultTypeInfo.logicalType = hld.resultType;
        if (isExistsFn(hld)) {
            resultTypeInfo.physicalType = registry.getType(BuiltInTypes.BOOLEAN_SHAPE);
        } else {
            resultTypeInfo.physicalType = hld.resultType;

        }
        return resultTypeInfo;
    }

    private boolean isExistsFn(LLD.LLSelect llSelect) {
        return Optional.ofNullable(LLFieldHelper.findFunc(llSelect.finalFieldsL, "exists")).isPresent();
    }


}