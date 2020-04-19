package org.delia.db.sql.where;

public class LogicalPhrase implements WhereExpression {
	public Boolean isAnd; //if false then is or. if null then only phrase1 is set
	public WhereExpression express1;
	public WhereExpression express2;
}