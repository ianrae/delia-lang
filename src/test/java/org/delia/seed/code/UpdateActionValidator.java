package org.delia.seed.code;

import org.delia.DeliaSession;
import org.delia.error.DeliaError;
import org.delia.runner.ResultValue;
import org.delia.seed.DeliaSeedTests;
import org.delia.type.DStructType;

public class UpdateActionValidator extends ActionValidatorBase {

    @Override
    public void validateAction(DeliaSeedTests.SdAction action, DeliaSeedTests.SdValidationResults res) {
        validateTableExists(action, res);

        DeliaSeedTests.SdUpdateAction updateAction = (DeliaSeedTests.SdUpdateAction) action;
        if (updateAction.getWhereClause() != null) {
            validateWhereClause(updateAction, res);
            validateIsNoKey(updateAction, res);
            validateData(action, res, true);
        } else {
            validateKeyOrPK(action, res);
            validateData(action, res);
        }
    }

    private void validateIsNoKey(DeliaSeedTests.SdUpdateAction action, DeliaSeedTests.SdValidationResults res) {
        if (action.getKey() != null) {
            res.errors.add(new SbError("key.not.allowed", String.format("update can have key or whereClause, but not both in table: '%s'", action.getTable())));
        }
    }

    private void validateWhereClause(DeliaSeedTests.SdUpdateAction action, DeliaSeedTests.SdValidationResults res) {
        DStructType structType = (DStructType) registry.getType(action.getTable());
        String src = String.format("let x = %s[%s]", structType.getName(), action.getWhereClause());

        DeliaSession childSess = sess.createChildSession();
        ResultValue resValue = childSess.getDelia().continueExecution(src, childSess);
        if (!resValue.isSuccess()) {
            //TODO: process all delia errors later
            DeliaError err = resValue.errors.get(0);
            res.errors.add(new SbError("whereClause.error", String.format("%s", err.toString())));
        }
    }


}
