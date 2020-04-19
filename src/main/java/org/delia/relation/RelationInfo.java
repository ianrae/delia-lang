package org.delia.relation;

import org.delia.type.DStructType;

public class RelationInfo {
	public DStructType nearType;
	public DStructType farType;
	public String fieldName;
	public RelationCardinality cardinality;
	public boolean isParent;
	public boolean isOneWay;
}
