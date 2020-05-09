package org.delia.relation;

import org.delia.type.DStructType;
import org.delia.type.TypeReplaceSpec;

public class RelationInfo {
	public DStructType nearType;
	public DStructType farType;
	public String fieldName;
	public RelationCardinality cardinality;
	public boolean isParent;
	public boolean isOneWay;
	
	public void performTypeReplacement(TypeReplaceSpec spec) {
		if (spec.needsReplacement(this, nearType)) {
			nearType = (DStructType) spec.newType;
		}
		if (spec.needsReplacement(this, farType)) {
			farType = (DStructType) spec.newType;
		}
	}
}
