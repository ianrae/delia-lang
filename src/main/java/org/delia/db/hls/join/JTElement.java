package org.delia.db.hls.join;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;

public class JTElement  {
	public DStructType dtype;
	public String fieldName;
	public DStructType fieldType;
	public List<JTElement> nextL = new ArrayList<>();
	public RelationInfo relinfo;
	public boolean usedForFK; //if true then fks(). but this join for other reasons too
	
	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner("|");
		joiner.add(dtype.getName());
		joiner.add(fieldName);
		joiner.add(fieldType.getName());
		return joiner.toString();
	}

	public boolean matches(TypePair pair) {
		if (pair.name.equals(fieldName) && pair.type == fieldType) {
			return true;
		}
		return false;
	}

	public TypePair createPair() {
		return new TypePair(fieldName, fieldType);
	}
}