package org.delia.type;

/**
 * There are six scalar types and the Relation type.
 * 
 * @author Ian Rae
 *
 */
public enum BuiltInTypes {
    INTEGER_SHAPE,
    LONG_SHAPE,
	NUMBER_SHAPE,
	STRING_SHAPE,
	BOOLEAN_SHAPE,
	DATE_SHAPE,
	RELATION_SHAPE;
	
	public static String getDeliaTypeName(BuiltInTypes builtIn) {
    	switch(builtIn) {
    	case INTEGER_SHAPE: return "int";
    	case BOOLEAN_SHAPE: return "boolean";
    	case NUMBER_SHAPE: return "number";
    	case DATE_SHAPE: return "date";
    	case LONG_SHAPE: return "long";
    	case STRING_SHAPE: return "string";
    	case RELATION_SHAPE: return "relation";
    	default: return "unknown!";
    	}
    }
	public static String convertDTypeNameToDeliaName(String typeName) {
    	switch(typeName) {
    	case "INTEGER_SHAPE": return "int";
    	case "BOOLEAN_SHAPE": return "boolean";
    	case "NUMBER_SHAPE": return "number";
    	case "DATE_SHAPE": return "date";
    	case "LONG_SHAPE": return "long";
    	case "STRING_SHAPE": return "string";
    	case "RELATION_SHAPE": return "relation";
    	default: return typeName;
    	}
    }
	public static Shape getShapeOf(BuiltInTypes builtIn) {
    	switch(builtIn) {
    	case INTEGER_SHAPE: return Shape.INTEGER;
    	case BOOLEAN_SHAPE: return Shape.BOOLEAN;
    	case NUMBER_SHAPE: return Shape.NUMBER;
    	case DATE_SHAPE: return Shape.DATE;
    	case LONG_SHAPE: return Shape.LONG;
    	case STRING_SHAPE: return Shape.STRING;
    	case RELATION_SHAPE: return Shape.STRUCT; //TODO: is this ok?
    	default: return null;
    	}
    }
	public static BuiltInTypes fromDeliaTypeName(String typeName) {
    	switch(typeName) {
    	case "int": return INTEGER_SHAPE;
    	case "boolean": return BOOLEAN_SHAPE;
    	case "number": return NUMBER_SHAPE;
    	case "date": return DATE_SHAPE;
    	case "long": return LONG_SHAPE;
    	case "string": return STRING_SHAPE;
    	default: return null;
    	}
    }
	public static boolean isBuiltInScalarType(String typeName) {
    	switch(typeName) {
    	case "int": return true;
    	case "boolean": return true;
    	case "number": return true;
    	case "date": return true;
    	case "long": return true;
    	case "string": return true;
    	default: return false;
    	}
    }
	
	public static String getDeliaTypeNameFromShape(Shape shape) {
    	switch(shape) {
    	case INTEGER: return "int";
    	case BOOLEAN: return "boolean";
    	case NUMBER: return "number";
    	case DATE: return "date";
    	case LONG: return "long";
    	case STRING: return "string";
    	case RELATION: return "relation";
    	default: return "unknown!";
    	}
    }
}