package org.delia.seed.code;

import org.delia.db.sql.StrCreator;
import org.delia.seed.DeliaSeedTests;
import org.delia.type.DValue;

public class UpdateActionExecutor extends ActionExecutorBase {
    @Override
    public void executeAction(DeliaSeedTests.SdAction action, StrCreator sc, DeliaSeedTests.SdExecutionResults res) {
        DeliaSeedTests.SdUpdateAction updateAction = (DeliaSeedTests.SdUpdateAction) action;

        for (DValue dval : action.getData()) {
            String filter = updateAction.getWhereClause() == null ? getKey(dval, action) : updateAction.getWhereClause();
            sc.o("update %s[%s] ", action.getTable(), filter);
            sc.o("{ %s } ", buildDataValues(dval, false));
            sc.nl();
        }
    }


}
