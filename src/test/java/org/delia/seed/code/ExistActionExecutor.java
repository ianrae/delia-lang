package org.delia.seed.code;

import org.delia.db.sql.StrCreator;
import org.delia.seed.DeliaSeedTests;
import org.delia.type.DValue;

public class ExistActionExecutor extends ActionExecutorBase {
    @Override
    public void executeAction(DeliaSeedTests.SdAction action, StrCreator sc, DeliaSeedTests.SdExecutionResults res) {
        for (DValue dval : action.getData()) {
            sc.o("upsert %s[%s] ", action.getTable(), getKey(dval, action));
            sc.o("{ %s } ", buildDataValues(dval));
            sc.nl();
        }
    }


}
