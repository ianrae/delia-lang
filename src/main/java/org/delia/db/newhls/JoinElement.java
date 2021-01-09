package org.delia.db.newhls;

import java.util.StringJoiner;

import org.delia.relation.RelationInfo;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

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
	public String aliasNameAdditional; //M:M we sometimes need to join assoctbl and fk table
	
	public boolean usedForFK() {
		return fetchSpec != null && fetchSpec.isFK;
	}
	public boolean usedForFetch() {
		return fetchSpec != null && !fetchSpec.isFK;
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
		String addStr = aliasNameAdditional == null ? "" : "/" + aliasNameAdditional;
		String s = String.format("%s/%s%s", joiner.toString(), aliasName, addStr);
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
	
	public TypePair getThisSidePK() {
		//need to reverse, since parent doesn't have child id
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(relationField.dtype);
		return pkpair;
	}
	public TypePair getOtherSidePK() {
		//need to reverse, since parent doesn't have child id
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(relinfo.farType);
		return pkpair;
	}
	public String getOtherSideField() {
		String parentName = relinfo.otherSide.fieldName; //TODO. can otherSide ever be null??
		return parentName;
	}
}