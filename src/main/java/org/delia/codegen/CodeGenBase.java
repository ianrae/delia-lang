package org.delia.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.Shape;

//====
public class CodeGenBase {
	protected DTypeRegistry registry;
	protected String packageName;

	public CodeGenBase(DTypeRegistry registry, String packageName) {
		this.registry = registry;
		this.packageName = packageName;
	}
	protected String convertToJava(DType ftype) {
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
			return "Date";
		case STRUCT:
		{
			return ftype.getName();
		}
		default:
			return null;
		}
	}

	protected String convertToJava(DStructType structType, String fieldName) {
		boolean flag = !structType.fieldIsOptional(fieldName);
		DType ftype = structType.getDeclaredFields().get(fieldName);
		return convertToJava(structType, fieldName, ftype, flag);
	}
	protected String convertToJava(DStructType structType, String fieldName, DType ftype, boolean flag) {
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
			return "Date";
		case STRUCT:
		{
			return ftype.getName();
		}
		default:
			return null;
		}
	}

	protected String convertToAsFn(DType ftype) {
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
		case STRUCT:
		{
			return "TODOfix" + ftype.getName();
		}
		default:
			return null;
		}
	}

	protected List<String> getImportList(DStructType structType) {
		List<String> list = new ArrayList<>();
		Map<String,String> alreadyMap = new HashMap<>();
		for(String fieldName: structType.getDeclaredFields().keySet()) {
 			DType ftype = structType.getDeclaredFields().get(fieldName);
			if (ftype.isShape(Shape.DATE)) {
				if (alreadyMap.containsKey("Date")) {
					continue;
				}
				String s = String.format("import java.util.Date;");
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

	protected void addImports(StrCreator sc, DStructType structType) {
		sc.o("package %s;", packageName);
		sc.nl();
		for(String s: getImportList(structType)) {
			sc.o(s);
			sc.nl();
		}
		sc.nl();
	}


}