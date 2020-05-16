package org.delia.db;

import org.delia.queryresponse.LetSpanEngine;

/**
 * Additional parameters for DBInterface.executeQuery.
 * 
 * @author Ian Rae
 *
 */
public class QueryContext { 
	public boolean loadFKs;
	public boolean pruneParentRelationFlag;
	public LetSpanEngine letSpanEngine;
}
