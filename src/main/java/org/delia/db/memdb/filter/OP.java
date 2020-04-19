package org.delia.db.memdb.filter;

public enum OP {
	LT,
	LE,
	GT,
	GE,
	EQ,
	NEQ,
	LIKE;

	public static OP createFromString(String opStr) {
		switch(opStr) {
		case "<":
			return LT;
		case "<=":
			return LE;
		case ">":
			return GT;
		case ">=":
			return GE;
		case "==":
		case "=":
			return EQ;
		case "!=":
		case "<>":
			return NEQ;
		case "like":
			return LIKE;
		default:
			return null;
		}
	}
}