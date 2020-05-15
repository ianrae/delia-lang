package org.delia.zdb.core;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.delia.log.Log;

public class ZDBExecuteContext  {
	public Log logToUse; //can be null
	
	//only for executeCommandStatementGenKey
	public List<ResultSet> genKeysL = new ArrayList<>();
}