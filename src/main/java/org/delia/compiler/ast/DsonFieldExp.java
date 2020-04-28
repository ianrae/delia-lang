package org.delia.compiler.ast;

public class DsonFieldExp extends ExpBase {
	public String nameExp;
	public Exp exp;
	public StringExp assocCrudAction;

	public DsonFieldExp(int pos, IdentExp name, Exp exp, StringExp assocCrudAction) {
		super(pos);
		this.nameExp = name == null ? null : name.name();
		this.exp = exp;
		this.assocCrudAction = assocCrudAction;
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