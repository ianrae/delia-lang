package org.delia.db.sql.fragment;

import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class FragmentHelper {
	public static FieldFragment buildFieldFrag(DStructType structType, SelectStatementFragment selectFrag, String fieldName) {
		TypePair pair = DValueHelper.findField(structType, fieldName);
		if (pair == null) {
			return null;
		}
		return buildFieldFrag(structType, selectFrag, pair);
	}
	public static FieldFragment buildFieldFrag(DStructType structType, SelectStatementFragment selectFrag, TypePair pair) {
		FieldFragment fieldF = new FieldFragment();
		fieldF.alias = findAlias(structType, selectFrag); //selectFrag.aliasMap.get(spec.queryExp.typeName).alias;
		fieldF.fieldType = pair.type;
		fieldF.name = pair.name;
		fieldF.isStar = false;
		fieldF.structType = structType;
		return fieldF;
	}
	public static FieldFragment buildFieldFragForTable(TableFragment tblFrag, SelectStatementFragment selectFrag, TypePair pair) {
		FieldFragment fieldF = new FieldFragment();
		fieldF.alias = findAliasForTable(tblFrag, selectFrag); //selectFrag.aliasMap.get(spec.queryExp.typeName).alias;
		fieldF.fieldType = pair.type;
		fieldF.name = pair.name;
		fieldF.isStar = false;
		fieldF.structType = tblFrag.structType;
		return fieldF;
	}
	public static FieldFragment buildEmptyFieldFrag(DStructType structType, SelectStatementFragment selectFrag) {
		FieldFragment fieldF = new FieldFragment();
		fieldF.alias = findAlias(structType, selectFrag); //selectFrag.aliasMap.get(spec.queryExp.typeName).alias;
		fieldF.fieldType = null;
		fieldF.name = null;
		fieldF.isStar = false;
		fieldF.structType = structType;
		return fieldF;
	}
	public static AliasedFragment buildParam(SelectStatementFragment selectFrag) {
		AliasedFragment fieldF = new AliasedFragment();
		fieldF.alias = null;
		fieldF.name = "?";
		return fieldF;
	}
	
	public static String findAlias(DStructType structType, SelectStatementFragment selectFrag) {
		String s = selectFrag.aliasMap.get(structType.getName()).alias;
		return s;
	}
	public static String findAliasForTable(TableFragment tblFrag, SelectStatementFragment selectFrag) {
		String s = selectFrag.aliasMap.get(tblFrag.name).alias;
		return s;
	}
	
	public static AliasedFragment buildAliasedFrag(String alias, String name) {
		AliasedFragment fieldF = new AliasedFragment();
		fieldF.alias = alias;
		fieldF.name = name;
		return fieldF;
	}
	public static OrderByFragment buildOrderByFrag(DStructType structType, String fieldName, String asc, SelectStatementFragment selectFrag) {
		OrderByFragment fieldF = new OrderByFragment();
		fieldF.alias = findAlias(structType, selectFrag);
		fieldF.name = fieldName;
		fieldF.asc = asc;
		return fieldF;
	}
	public static OrderByFragment buildRawOrderByFrag(DStructType structType, String str, String asc, SelectStatementFragment selectFrag) {
		OrderByFragment fieldF = new OrderByFragment();
		fieldF.alias = null;
		fieldF.name = str;
		fieldF.asc = asc;
		return fieldF;
	}

}
