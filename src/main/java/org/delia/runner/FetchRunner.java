package org.delia.runner;

import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public interface FetchRunner {
	
	public QueryResponse load(DRelation drel, String targetFieldName);
	public QueryResponse load(String typeName, String fieldName, DValue keyVal); //TODO: later support multiplekeys
	public QueryResponse queryOwningType(DStructType owningType, String subject, DRelation drel);
	
	public QueryResponse loadFKOnly(String typeName, String fieldName, DValue keyVal); //TODO: later support multiplekeys
}
