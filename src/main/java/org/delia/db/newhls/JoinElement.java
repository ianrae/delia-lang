package org.delia.db.newhls;

import java.util.StringJoiner;

import org.delia.relation.RelationInfo;
import org.delia.type.TypePair;

/**
 * A join needed by the query. Can be an explicit join (eg. fetch), or implicit join (eg [addr.city=='toronto')
 * @author ian
 *
 */
public class JoinElement  {
	public RelationField relationField;
	//		public List<JTElement> nextL = new ArrayList<>();
	public RelationInfo relinfo;
	public boolean usedForFK; //if true then fks(). but this join for other reasons too
	public boolean usedForFetch; //if true then fetch. but this join for other reasons too
	public FetchSpec fetchSpec;
	public String aliasName;
	
	public String makeKey() {
		StringJoiner joiner = new StringJoiner("|");
		joiner.add(relationField.dtype.getName());
		joiner.add(relationField.fieldName);
		joiner.add(relationField.fieldType.getName());
		return joiner.toString();
	}

	
	
	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner("|");
		joiner.add(relationField.dtype.getName());
		joiner.add(relationField.fieldName);
		joiner.add(relationField.fieldType.getName());
		if (usedForFK) {
			joiner.add("FK");
		}
		return joiner.toString();
	}

	public boolean matches(TypePair pair) {
		if (pair.name.equals(relationField.fieldName) && pair.type == relationField.fieldType) {
			return true;
		}
		return false;
	}

	public TypePair createPair() {
		return new TypePair(relationField.fieldName, relationField.fieldType);
	}
}