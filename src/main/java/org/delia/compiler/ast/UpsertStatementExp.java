package org.delia.compiler.ast;

public class UpsertStatementExp extends CrudExp {
	public String typeName;
	public DsonExp dsonExp;
	public QueryExp queryExp;

	public UpsertStatementExp(int pos, QueryExp queryExp, DsonExp dsonExp) {
		super(pos);
		this.queryExp = queryExp;
		this.typeName = queryExp.typeName;
		this.dsonExp = dsonExp;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	@Override
	public String strValue() {
		return queryExp.strValue() + " " + dsonExp.strValue();
	}
	
	@Override
	public String toString() {
		String s = String.format("upsert %s %s", queryExp.toString(), dsonExp.toString());
		return s;
	}
}