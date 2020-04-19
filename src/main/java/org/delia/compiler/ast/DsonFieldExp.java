package org.delia.compiler.ast;

public class DsonFieldExp extends ExpBase {
	public String nameExp;
	public Exp exp;

	public DsonFieldExp(int pos, IdentExp name, Exp exp) {
		super(pos);
		this.nameExp = name == null ? null : name.name();
		this.exp = exp;
	}
	
	public String getFieldName() {
		return nameExp;
	}
	
	@Override
	public String strValue() {
		String tmpName = (nameExp == null) ? "" : nameExp + ": ";
		String ss = String.format("%s%s", tmpName, formatValue(exp));
		return ss;
	}

	@Override
	public String toString() {
		return strValue();
	}
}