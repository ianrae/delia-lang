package org.delia.hld;

import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class RelationSpec {
	public RelationInfo relinfo;
	public boolean flipped;
	public DStructType structType;
	public String fieldName;
	public DStructType otherStructType;
	public String otherFieldName;
	public TypePair getPKPair() {
		return DValueHelper.findField(structType, fieldName);
	}
	public TypePair getOtherPKPair() {
		return DValueHelper.findField(otherStructType, otherFieldName);
	}

}
