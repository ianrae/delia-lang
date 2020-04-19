package org.delia.compiler.ast;

public class InsertStatementExp extends CrudExp {
	public String typeName;
	public DsonExp dsonExp;

	public InsertStatementExp(int pos, IdentExp typeName, DsonExp dsonExp) {
		super(pos);
		this.typeName = typeName == null ? null : typeName.name();
		this.dsonExp = dsonExp;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	@Override
	public String strValue() {
		return typeName + " " + dsonExp.strValue();
	}
	
	@Override
	public String toString() {
		String s = String.format("insert %s %s", typeName, dsonExp.toString());
		return s;
	}
}