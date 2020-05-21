package org.delia.codegen;

import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

//====
public class CodeGenBase {
	protected DTypeRegistry registry;

	public CodeGenBase(DTypeRegistry registry) {
		this.registry = registry;
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


}