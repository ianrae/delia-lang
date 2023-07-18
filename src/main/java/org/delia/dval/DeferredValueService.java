package org.delia.dval;

import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.runner.DeferredDValueHelper;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.varevaluator.VarEvaluator;

import java.util.List;

public class DeferredValueService {

    private final DValueConverterService dvalConverterService;
    private final FactoryService factorySvc;

    public DeferredValueService(FactoryService factorySvc) {
        this.factorySvc = factorySvc;
        this.dvalConverterService = new DValueConverterService(factorySvc);
    }

    public void resolveAllVars(List<LLD.LLFieldValue> fieldL, DTypeRegistry registry, VarEvaluator varEvaluator) {
        ScalarValueBuilder valueBuilder = new ScalarValueBuilder(factorySvc, registry);
        for (LLD.LLFieldValue field : fieldL) {
            if (field.dval != null) {
                resolveSingleDeferredVar(field.dval, valueBuilder, varEvaluator);
            } else if (field.dvalList != null) {
                field.dvalList.forEach(d -> resolveSingleDeferredVar(d, valueBuilder, varEvaluator));
            }
        }
    }

    public void resolveSingleDeferredVar(DValue dval, ScalarValueBuilder valueBuilder, VarEvaluator varEvaluator) {
        if (dval != null) {
            DValue realVal = DeferredDValueHelper.preResolveDeferredDval(dval, varEvaluator);
            realVal = dvalConverterService.normalizeValue(realVal, dval.getType(), valueBuilder);
            DeferredDValueHelper.resolveTo(dval, realVal); //note. realVal can be null
        }
    }

}
