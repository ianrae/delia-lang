package org.delia.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.db.sql.StrCreator;
import org.delia.rule.rules.RelationManyRule;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.StringUtil;

public class CodeGenHelper {
	public DTypeRegistry registry;
	public String packageName;

	public CodeGenHelper(DTypeRegistry registry, String packageName) {
		this.registry = registry;
		this.packageName = packageName;
	}
	public String convertToJava(DType ftype) {
		switch(ftype.getShape()) {
		case INTEGER:
			return "Integer";
		case LONG:
			return "Long";
		case NUMBER:
			return "Double";
		case BOOLEAN:
			return "Boolean";
		case STRING:
			return "String";
		case DATE:
			return "ZonedDateTime";
		case BLOB:
			return "WrappedBlob";
		case STRUCT:
		{
			return ftype.getName();
		}
		default:
			return null;
		}
	}

	public String convertToJava(DStructType structType, String fieldName) {
		boolean flag = !structType.fieldIsOptional(fieldName);
		DType ftype = structType.getDeclaredFields().get(fieldName);
		return convertToJava(ftype, flag);
	}
	public boolean isList(DStructType structType, String fieldName) {
		RelationManyRule manyRule = DRuleHelper.findManyRule(structType, fieldName);
		if (manyRule == null) {
			return false;
		}
		return true;
	}
	public String convertToJava(DType ftype, boolean flag) {
		switch(ftype.getShape()) {
		case INTEGER:
			return flag ? "int" : "Integer";
		case LONG:
			return flag ? "long": "Long";
		case NUMBER:
			return flag ? "double" : "Double";
		case BOOLEAN:
			return flag ? "boolean" : "Boolean";
		case STRING:
			return "String";
		case DATE:
			return "ZonedDateTime";
		case BLOB:
			return "WrappedBlob";
		case STRUCT:
		{
			return ftype.getName();
		}
		default:
			return null;
		}
	}

	public String convertToAsFn(DType ftype) {
		switch(ftype.getShape()) {
		case INTEGER:
			return "asInt";
		case LONG:
			return "asLong";
		case NUMBER:
			return "asNumber";
		case BOOLEAN:
			return "asBoolean";
		case STRING:
			return "asString";
		case DATE:
			return "asDate";
		case BLOB:
			return "asBlob";
		case STRUCT:
		{
			return "TODOfix" + ftype.getName();
		}
		default:
			return null;
		}
	}
	public String getNullValueFor(DStructType structType, String fieldName) {
		boolean flag = !structType.fieldIsOptional(fieldName);
		DType ftype = structType.getDeclaredFields().get(fieldName);
		switch(ftype.getShape()) {
		case INTEGER:
			return flag ? "0" : "null";
		case LONG:
			return flag ? "0": "null";
		case NUMBER:
			return flag ? "0.0" : "null";
		case BOOLEAN:
			return flag ? "false" : "null";
		case STRING:
		case DATE:
		case BLOB:
			return "null";
		case STRUCT:
		{
			return "null";
		}
		default:
			return null;
		}
	}

	public List<String> getImportList(DStructType structType) {
		List<String> list = new ArrayList<>();
		Map<String,String> alreadyMap = new HashMap<>();
		for(String fieldName: structType.getDeclaredFields().keySet()) {
 			DType ftype = structType.getDeclaredFields().get(fieldName);
			if (ftype.isShape(Shape.DATE)) {
				if (alreadyMap.containsKey("Date")) {
					continue;
				}
				String s = String.format("import java.time.ZonedDateTime;");
				list.add(s);
				alreadyMap.put("Date", "");
			} else if (ftype.isStructShape()) {
				if (alreadyMap.containsKey(ftype.getName())) {
					continue;
				}
				String s = String.format("import %s.%s;", packageName, ftype.getName());
				list.add(s);
				alreadyMap.put(ftype.getName(), "");
			}

		}
		return list;
	}

	public void addImports(StrCreator sc, DStructType structType) {
		sc.o("package %s;", packageName);
		sc.nl();
		for(String s: getImportList(structType)) {
			sc.o(s);
			sc.nl();
		}
		sc.nl();
		sc.nl();
	}
	public void addImports(StrCreator sc, List<String> importL) {
		sc.o("package %s;", packageName);
		sc.nl();
		for(String s: importL) {
			sc.o(s);
			sc.nl();
		}
		sc.nl();
		sc.nl();
	}


	public boolean hasPK(DType ftype) {
		TypePair pkPair = DValueHelper.findPrimaryKeyFieldPair(ftype);
		return pkPair != null;
	}

	public String getPKType(DType ftype) {
		TypePair pkPair = DValueHelper.findPrimaryKeyFieldPair(ftype);
		return convertToJava(pkPair.type);
	}
	public String getPKField(DType ftype) {
		TypePair pkPair = DValueHelper.findPrimaryKeyFieldPair(ftype);
		return StringUtil.uppify(pkPair.name);
	}

	public String getPKTypeAsFn(DType ftype) {
		TypePair pkPair = DValueHelper.findPrimaryKeyFieldPair(ftype);
		return convertToAsFn(pkPair.type);
	}

}