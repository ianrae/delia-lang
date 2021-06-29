package org.delia.seed.code;

import org.delia.seed.DeliaSeedTests;
import org.delia.type.DTypeRegistry;

public interface ActionValidator {
    void init(DeliaSeedTests.DBInterface dbInterface, DTypeRegistry registry);
    void validateAction(DeliaSeedTests.SdAction action, DeliaSeedTests.SdValidationResults res);

}
