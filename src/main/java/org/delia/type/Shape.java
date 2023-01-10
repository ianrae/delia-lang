package org.delia.type;

/**
 * 9 Shapes.
 * The shape represents the underlying type of data
 * (int, long, string, boolean, etc)
 *
 */
public enum Shape {
	INTEGER,
	LONG,
	NUMBER,
	BOOLEAN,
	STRING,
	DATE,
	BLOB,
	STRUCT,
	RELATION;
	
	public static Shape createFromDeliaType(String deliaType) {
		switch(deliaType) {
		case "int": return INTEGER;
		case "long": return LONG;
		case "number": return NUMBER;
		case "string": return STRING;
		case "boolean": return BOOLEAN;
		case "date": return DATE;
		case "blob": return BLOB;
		case "relation": return RELATION;
		default: return null;
		}
    }
	
	public static int getScalarIndex(Shape shape) {
		switch(shape) {
		case INTEGER: return 0;
		case LONG: return 1;
		case NUMBER: return 2;
		case STRING: return 3;
		case BOOLEAN: return 4;
		case DATE: return 5;
		default: return -1;
		}
    }
}