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
	public RelationField structField;
	//		public List<JTElement> nextL = new ArrayList<>();
	public RelationInfo relinfo;
	public boolean usedForFK; //if true then fks(). but this join for other reasons too
	public boolean usedForFetch; //if true then fettch. but this join for other reasons too

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner("|");
		joiner.add(structField.dtype.getName());
		joiner.add(structField.fieldName);
		joiner.add(structField.fieldType.getName());
		return joiner.toString();
	}

	public boolean matches(TypePair pair) {
		if (pair.name.equals(structField.fieldName) && pair.type == structField.fieldType) {
			return true;
		}
		return false;
	}

	public TypePair createPair() {
		return new TypePair(structField.fieldName, structField.fieldType);
	}
}