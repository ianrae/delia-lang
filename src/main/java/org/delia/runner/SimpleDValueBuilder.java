package org.delia.runner;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;

public class SimpleDValueBuilder extends ServiceBase {

    public SimpleDValueBuilder(FactoryService factorySvc) {
        super(factorySvc);
    }


    public DValue buildDValueString(String s) {
        DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
        registryBuilder.init();
        DTypeRegistry registry = registryBuilder.getRegistry();
        ScalarValueBuilder scalarBuilder = new ScalarValueBuilder(factorySvc, registry);
        DValue dval = scalarBuilder.buildString(s);
        return dval;
    }
    public DValue buildDValueInt(String s) {
        DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
        registryBuilder.init();
        DTypeRegistry registry = registryBuilder.getRegistry();
        ScalarValueBuilder scalarBuilder = new ScalarValueBuilder(factorySvc, registry);
        DValue dval = scalarBuilder.buildInt(s);
        return dval;
    }


    public ScalarValueBuilder createValueBuilder() {
        DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
        registryBuilder.init();
        DTypeRegistry registry = registryBuilder.getRegistry(); //for built-in types only
        return new ScalarValueBuilder(factorySvc, registry);
    }

}
