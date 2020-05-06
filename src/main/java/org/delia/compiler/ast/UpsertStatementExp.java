package org.delia.compiler.ast;

public class UpsertStatementExp extends CrudExp {
	public String typeName;
	public DsonExp dsonExp;
	public QueryExp queryExp;
	public OptionExp optionExp; //can be null

	public UpsertStatementExp(int pos, QueryExp queryExp, DsonExp dsonExp, OptionExp optionExp) {
		super(pos);
		this.queryExp = queryExp;
		this.typeName = queryExp.typeName;
		this.dsonExp = dsonExp;
		this.optionExp = optionExp;
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