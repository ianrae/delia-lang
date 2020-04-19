package org.delia.compiler.ast;

public class TypeStatementExp extends ExpBase {
	
	public String typeName;
	public String baseTypeName;
	public StructExp structExp;
	public RuleSetExp ruleSetExp;

	public TypeStatementExp(int pos, IdentExp nameExp, IdentExp typeExp, StructExp structExp, RuleSetExp ruleSetExp) {
		super(pos);
		this.typeName = nameExp.name();
		this.baseTypeName = typeExp.name();
		this.structExp = structExp;
		this.ruleSetExp = ruleSetExp;
	}
	
	public boolean hasRules() {
		return ruleSetExp != null && ruleSetExp.hasRules();
	}

	@Override
	public String strValue() {
		String s = String.format("type %s %s ", typeName, baseTypeName);
		s += (structExp == null) ? "" : structExp.strValue();
		if (hasRules()) {
			s += " " + ruleSetExp.strValue();
		}
		s += String.format(" end");
		return s;
	}

	@Override
	public String toString() {
		return strValue();
	}
}