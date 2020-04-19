package org.delia.type;

/**
 * 8 Shapes.
 * The shape represents the underlying type of data
 * (int, long, string, boolean, ectc)
 *
 */
public enum Shape {
	INTEGER,
	LONG,
	NUMBER,
	BOOLEAN,
	STRING,
	DATE,
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
		case "relation": return RELATION;
		default: return null;
		}
    }
	
}