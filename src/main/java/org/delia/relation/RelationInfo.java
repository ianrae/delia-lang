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
	public RelationInfo otherSide; //null if one-sided relation
	public String relationName; //never null
	private Integer datId; //many-to-many table id

	public void performTypeReplacement(TypeReplaceSpec spec) {
		if (spec.needsReplacement(this, nearType)) {
			nearType = (DStructType) spec.newType;
		}
		if (spec.needsReplacement(this, farType)) {
			farType = (DStructType) spec.newType;
		}
	}
	
	public boolean isManyToMany() {
		return RelationCardinality.MANY_TO_MANY.equals(cardinality);
	}
	public Integer getDatId() {
		return datId;
	}
	public void forceDatId(Integer datId) {
		this.datId = datId;
	}

}
