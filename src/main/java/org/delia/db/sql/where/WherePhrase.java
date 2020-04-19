package org.delia.db.sql.where;

public class WherePhrase implements WhereExpression{
	public String op;
	public WhereOperand op1;
	public WhereOperand op2;
	public boolean notFlag;
}