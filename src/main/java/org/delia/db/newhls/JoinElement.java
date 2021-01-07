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
	public RelationInfo relinfo;
	public FetchSpec fetchSpec; //if not null then this join is from a fetch
	public String aliasName;
	public String srcAlias; //needed for JOIN ON. alias of table we're joining froms
	public boolean usedForFK() {
		return fetchSpec != null && fetchSpec.isFK;
	}
	
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
		if (usedForFK()) {
			joiner.add("FK");
		}
		String s = String.format("%s/%s", joiner.toString(), aliasName);
		return s;
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