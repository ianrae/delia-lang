package org.delia.seed.code;

import org.delia.db.sql.StrCreator;
import org.delia.seed.DeliaSeedTests;
import org.delia.type.DTypeRegistry;

public abstract class ActionExecutorBase implements ActionExecutor {
    protected DeliaSeedTests.DBInterface dbInterface;
    protected DTypeRegistry registry;

    @Override
    public void init(DeliaSeedTests.DBInterface dbInterface, DTypeRegistry registry) {
        this.dbInterface = dbInterface;
        this.registry = registry;
    }

    @Override
    public abstract void executeAction(DeliaSeedTests.SdAction action, StrCreator sc, DeliaSeedTests.SdExecutionResults res);

}
