package org.delia.runner;

import java.util.List;

import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public interface FetchRunner {
	
	public QueryResponse load(DRelation drel);
	public boolean queryFKExists(DRelation drel);
	public boolean queryFKExists(DStructType owningType, String subject, DRelation drel);
	public List<DValue> queryFKs(DStructType owningType, String subject, DRelation drel);
	
	public QueryResponse loadFKOnly(String typeName, String fieldName, DValue keyVal); 
}
