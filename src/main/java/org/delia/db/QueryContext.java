package org.delia.db;

import java.util.List;

import org.delia.hld.JoinElement;
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
	public QueryResponse existingQResp; //normally null. used when doing x=y.field1.min()
	public boolean isSimpleSvc; //used by db-observer
	public List<JoinElement> implicitFetchL; //used by MEM only
}
