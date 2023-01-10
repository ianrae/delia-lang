package org.delia.rule;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.tok.Tok;
import org.delia.type.DType;

public class RuleFuncFactoryImpl implements RuleFunctionFactory {
	private FactoryService factorySvc;
	private List<RuleFunctionBulder> builderL = new ArrayList<>();

	public RuleFuncFactoryImpl(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
		builderL.add(new DefaultRuleFunctionBuilder(factorySvc));
	}

	/* (non-Javadoc)
	 * @see org.delia.typebuilder.RuleFunctionFactory#createRule(org.delia.compiler.astx.XNAFMultiExp, int, org.delia.type.DType)
	 */
	@Override
	public DRule createRule(Tok.DottedTok rfe, int index, DType dtype) {

		for(RuleFunctionBulder builder: builderL) {
			DRule rule = builder.createRule(rfe, index, dtype);
			if (rule != null) {
				return rule;
			}
		}

		//error handled at higher level
		return null;
	}

	@Override
	public void addBuilder(RuleFunctionBulder builder) {
		builderL.add(builder);
	}

}