package org.delia.db;


import org.delia.lld.LLD;
import org.delia.log.DeliaLog;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DBExecuteContext {
    public DeliaLog logToUse; //can be null

    //only for executeCommandStatementGenKey
    public List<ResultSet> genKeysL = new ArrayList<>();
    public LLD.LLStatement currentStatement;
}