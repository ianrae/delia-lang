package org.delia.sql.fragment;

import org.delia.sql.fragment.FragmentParserTests.AliasedFragment;
import org.delia.sql.fragment.FragmentParserTests.FieldFragment;
import org.delia.sql.fragment.FragmentParserTests.SelectStatementFragment;
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
	
	public static AliasedFragment buildAliasedFrag(String alias, String name) {
		AliasedFragment fieldF = new AliasedFragment();
		fieldF.alias = alias;
		fieldF.name = name;
		return fieldF;
	}

}
