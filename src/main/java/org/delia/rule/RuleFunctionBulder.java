package org.delia.rule;

import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.type.DType;

public interface RuleFunctionBulder {

	DRule createRule(XNAFMultiExp rfe, int index, DType dtype);

}