package org.delia.seed.code;

import org.delia.seed.DeliaSeedTests;
import org.delia.type.DTypeRegistry;

public interface ActionExecutor {
    void init(DeliaSeedTests.DBInterface dbInterface, DTypeRegistry registry);
    void executeAction(DeliaSeedTests.SdAction action, DeliaSeedTests.SdValidationResults res);

}
