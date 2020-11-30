package org.delia.db;

import org.delia.queryresponse.LetSpanEngine;
import org.delia.runner.QueryResponse;

/**
 * Additional parameters for DBInterface.executeQuery.
 * 
 * @author Ian Rae
 *
 */
public class QueryContext { 
	public boolean loadFKs;
	public boolean pruneParentRelationFlag; //MEM only
	public LetSpanEngine letSpanEngine;
	public QueryResponse existingQResp; //normally null. used when doing x=y.field1.min()
}
