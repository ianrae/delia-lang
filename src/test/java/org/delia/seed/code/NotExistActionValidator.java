package org.delia.seed.code;

import org.delia.seed.DeliaSeedTests;

public class NotExistActionValidator extends ActionValidatorBase {

    @Override
    public void validateAction(DeliaSeedTests.SdAction action, DeliaSeedTests.SdValidationResults res) {
        validateTableExists(action, res);
        validateKeyOrPK(action, res);
    }
}
