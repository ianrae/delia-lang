package org.delia.compiler.ast;

public class DeleteStatementExp extends CrudExp {
	public String typeName;
	public QueryExp queryExp;

	public DeleteStatementExp(int pos, String typeName, QueryExp queryExp) {
		super(pos);
		this.typeName = typeName;
		this.queryExp = queryExp;
	}
	public String getTypeName() {
		return typeName;
	}
	@Override
	public String strValue() {
		return typeName + " " + queryExp.strValue();
	}
	
	@Override
	public String toString() {
		String s = String.format("delete %s", queryExp.toString());
		return s;
	}
}