package org.delia.relation;

import org.delia.type.DStructType;

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

	public boolean isManyToMany() {
		return RelationCardinality.MANY_TO_MANY.equals(cardinality);
	}
	public boolean isOneToOne() {
		return RelationCardinality.ONE_TO_ONE.equals(cardinality);
	}
	public boolean isOneToMany() {
		return RelationCardinality.ONE_TO_MANY.equals(cardinality);
	}
	public Integer getDatId() {
		return datId;
	}
	public void forceDatId(Integer datId) {
		this.datId = datId;
	}
	
	/**
	 * Used to determine if a join is needed (generally if this fn returns true then yes)
	 * @return true if the near side of this relation does NOT hold the fk
	 */
	public boolean notContainsFK() {
		if (isManyToMany() || isParent) {
			return true;
		}
		return false;
	}

}
