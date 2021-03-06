package org.delia.db.sqlgen;

import org.delia.db.SqlStatement;
import org.delia.db.sql.StrCreator;
import org.delia.hld.cud.HLDDelete;
import org.delia.type.DValue;

public class SqlDeleteInClause implements SqlClauseGenerator {

	protected SqlWhereClause whereClause;
	protected DValue deleteInDVal;
	protected String alias;
	protected String mergeOtherKey;
	protected String mergeKey;
	protected String mergePKField;
	protected String mergeType;
	
	public SqlDeleteInClause(SqlWhereClause whereClause) {
		this.whereClause = whereClause;
	}
	
	public void init(HLDDelete hld) {
		this.alias = hld.typeOrTbl.getAlias();
		this.deleteInDVal = hld.deleteInDVal;
		this.mergeOtherKey = hld.mergeOtherKey;
		this.mergeKey = hld.mergeKey;
		this.mergePKField = hld.mergePKField; 
		this.mergeType = hld.mergeType;
	}
	
	@Override
	public String render(SqlStatement stm) {
		StrCreator sc = new StrCreator();
		int n = stm.paramL.size();
		String whereStr = whereClause.render(stm);
		DValue dval = deleteInDVal;
		DValue sav = stm.paramL.remove(n);
		stm.paramL.add(dval);
		stm.paramL.add(sav);
		
		whereStr = whereStr.replace(String.format("%s.", alias), "a.");
		String s2 = String.format("%s.%s <> ?",  alias, mergeOtherKey); //whereStr.substring(pos1 + 5);

		String s = String.format(" %s AND %s.%s IN (SELECT %s FROM %s as a WHERE%s)", s2, alias, mergeKey, mergePKField, mergeType, whereStr);
		sc.o(" WHERE%s", s);
		
		return sc.toString();
	}

}
