package org.delia.db.sql.where;

import org.delia.compiler.ast.Exp;

public class WhereOperand implements WhereExpression {
	public TypeDetails typeDetails;
	public Exp exp;
	public String alias; //can be null
	public boolean isValue; //if true then convert date,etc
	public String fnName; //can be null
}