package org.delia.typebuilder;

import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.rule.DRule;
import org.delia.type.DType;

public interface RuleFunctionFactory {

	DRule createRule(XNAFMultiExp rfe, int index, DType dtype);

}