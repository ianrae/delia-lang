package org.delia.seed.code;

import org.delia.seed.DeliaSeedTests;
import org.delia.type.*;
import org.delia.util.DValueHelper;

public class DeleteActionValidator extends ActionValidatorBase {

    @Override
    public void validateAction(DeliaSeedTests.SdAction action, DeliaSeedTests.SdValidationResults res) {
        validateTableExists(action, res);
        validateKeyOrPK(action, res);

        validateData(action, res);

    }


}
