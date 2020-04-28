package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.List;

import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class WhereListHelper  {

	public static List<OpFragment> cloneWhereList(List<SqlFragment> existingWhereL) {
		List<OpFragment> tmpL = new ArrayList<>();
		for(SqlFragment fff: existingWhereL) {
			if (fff instanceof OpFragment) {
				tmpL.add((OpFragment) fff);
			}
		}
		return tmpL;
	}

	public static List<OpFragment> findPrimaryKeyQuery(List<SqlFragment> existingWhereL, DStructType farType) {
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(farType);
		List<OpFragment> oplist = new ArrayList<>();

		for(SqlFragment ff: existingWhereL) {
			if (ff instanceof OpFragment) {
				OpFragment opff = (OpFragment) ff;
				if (opff.left.name.equals(pair.name) || opff.right.name.equals(pair.name)) {
					oplist.add(opff);
				}
			}
		}
		return oplist;
	}
	public static boolean isOnlyPrimaryKeyQuery(List<SqlFragment> existingWhereL, DStructType farType) {
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(farType);

		int failCount = 0;
		for(SqlFragment ff: existingWhereL) {
			if (ff instanceof OpFragment) {
				OpFragment opff = (OpFragment) ff;
				if (!opff.left.name.equals(pair.name) && !leftIsQuestionMark(opff)) {
					failCount++;
				}
				if (!opff.right.name.equals(pair.name) && !rightIsQuestionMark(opff)) {
					failCount++;
				}
			}
		}
		return failCount == 0;
	}
	public static List<OpFragment> changeIdToAssocFieldName(boolean cloneOthers, List<OpFragment> existingWhereL, DStructType farType, String alias, String assocFieldName) {
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(farType);
		List<OpFragment> oplist = new ArrayList<>();

		for(OpFragment opff: existingWhereL) {
			if (opff.left.name.equals(pair.name)) {
				OpFragment clone = new OpFragment(opff);
				clone.left.alias = alias;
				clone.left.name = assocFieldName;
				oplist.add(clone);
			} else if (opff.right.name.equals(pair.name)) {
				OpFragment clone = new OpFragment(opff);
				clone.right.alias = alias;
				clone.right.name = assocFieldName;
				oplist.add(clone);
			} else if (cloneOthers){
				OpFragment clone = new OpFragment(opff);
				oplist.add(clone);
			}
		}
		return oplist;
	}

	public static boolean leftIsQuestionMark(OpFragment opff) {
		return opff.left.name.equals("?");
	}
	public static boolean rightIsQuestionMark(OpFragment opff) {
		return opff.right.name.equals("?");
	}


}