package org.delia.rule;

import org.delia.compiler.ast.Exp;
import org.delia.tok.Tok;
import org.delia.type.DType;

public interface RuleFunctionFactory {

	void addBuilder(RuleFunctionBulder builder);
	DRule createRule(Tok.DottedTok rfe, int index, DType dtype);

}