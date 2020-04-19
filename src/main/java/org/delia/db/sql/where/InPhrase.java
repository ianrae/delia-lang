package org.delia.db.sql.where;

import java.util.List;

import org.delia.compiler.ast.Exp;

public class InPhrase implements WhereExpression{
	public List<Exp> valueL;
	public WhereOperand op1;
}