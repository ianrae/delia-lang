package org.delia.seed.code;

import org.delia.db.sql.StrCreator;
import org.delia.seed.DeliaSeedTests;
import org.delia.type.DValue;

public class DeleteActionExecutor extends ActionExecutorBase {
    @Override
    public void executeAction(DeliaSeedTests.SdAction action, StrCreator sc, DeliaSeedTests.SdExecutionResults res) {
        if (action instanceof DeliaSeedTests.SdDeleteAction) {
            DeliaSeedTests.SdDeleteAction deleteAction = (DeliaSeedTests.SdDeleteAction) action;
            if (deleteAction.isDeleteAll()) {
                sc.o("delete %s[true] ", action.getTable());
                sc.nl();
                return;
            }
        }

        for (DValue dval : action.getData()) {
            sc.o("delete %s[%s] ", action.getTable(), getKey(dval, action));
            sc.nl();
        }
    }


}
