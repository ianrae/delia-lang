package org.delia.compiler.ast;

public class UpdateStatementExp extends CrudExp {
	public String typeName;
	public DsonExp dsonExp;
	public QueryExp queryExp;
	public Exp assocCrudAction; //insert/delete/update

	public UpdateStatementExp(int pos, QueryExp queryExp, DsonExp dsonExp, Exp assocCrudAction) {
		super(pos);
		this.queryExp = queryExp;
		this.typeName = queryExp.typeName;
		this.dsonExp = dsonExp;
		this.assocCrudAction = assocCrudAction;
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
		String s = String.format("update %s %s", queryExp.toString(), dsonExp.toString());
		return s;
	}
}