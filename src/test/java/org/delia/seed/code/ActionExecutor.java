package org.delia.seed.code;

import org.delia.DeliaSession;
import org.delia.db.sql.StrCreator;
import org.delia.seed.DeliaSeedTests;
import org.delia.type.DTypeRegistry;

public interface ActionExecutor {
    void init(DeliaSeedTests.DBInterface dbInterface, DTypeRegistry registry, DeliaSession sess);

    void executeAction(DeliaSeedTests.SdAction action, StrCreator sc, DeliaSeedTests.SdExecutionResults res);


}
