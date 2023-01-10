package org.delia.runner;

import org.delia.core.ConfigureService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBInterfaceFactory;

public class BasicRunner extends ServiceBase {
    public static final String DOLLAR_DOLLAR = "$$";

    private final DBInterfaceFactory dbInterface;
    private final ConfigureService configSvc;

    public BasicRunner(FactoryService factorySvc, DBInterfaceFactory dbInterface) {
        super(factorySvc);
        this.dbInterface = dbInterface;
        this.configSvc = factorySvc.getConfigureService();
    }

}
