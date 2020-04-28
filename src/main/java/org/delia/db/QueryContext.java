package org.delia.db;

/**
 * Additional parameters for DBInterface.executeQuery.
 * 
 * @author Ian Rae
 *
 */
public class QueryContext { 
	public boolean loadFKs;
	public boolean pruneParentRelationFlag;
}
