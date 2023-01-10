package org.delia.rule;

import org.delia.tok.Tok;
import org.delia.type.DType;

public interface RuleFunctionBulder {

	DRule createRule(Tok.DottedTok rfe, int index, DType dtype);

}