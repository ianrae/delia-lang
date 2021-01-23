package org.delia.db.sqlgen;

import org.apache.commons.lang3.StringUtils;
import org.delia.db.SqlStatement;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;
import org.delia.hld.HLDField;
import org.delia.hld.HLDQuery;
import org.delia.hld.cud.HLDUpdate;
import org.delia.type.DValue;

public class SqlUpdateStatement implements SqlStatementGenerator {

	private HLDUpdate hld;
	private SqlTableNameClause tblClause;
	private SqlWhereClause whereClause;
	private SqlValueListClause valueClauseHelper;
	
	public SqlUpdateStatement(SqlTableNameClause tblClause, SqlValueListClause valueClause, SqlWhereClause whereClause) {
		this.tblClause = tblClause;
		this.whereClause = whereClause;
		this.valueClauseHelper = valueClause;
	}
	
	public void init(HLDUpdate hld) {
		this.hld = hld;
		tblClause.init(hld.typeOrTbl);
//		fieldClause.init(hld.fieldL);
//		valueClause.init(hld.valueL);
		whereClause.init(hld.hld);
	}
	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("UPDATE");
		sc.o(tblClause.render(stm));
		
		if (hld.fieldL.isEmpty()) {
			stm.sql = sc.toString();
			return stm;
		}
		
		sc.o(" SET ");
		int index = 0;
		ListWalker<HLDField> walker = new ListWalker<>(hld.fieldL);
		String conditionStr = null;
		while(walker.hasNext()) {
			HLDField ff = walker.next();
			DValue inner = hld.valueL.get(index);
			stm.paramL.add(valueClauseHelper.renderValue(inner));
			
			conditionStr = String.format("%s = %s", ff.render(), "?");
			sc.o(conditionStr);
			walker.addIfNotLast(sc, ", ");
			index++;
		}
		
		if (hld.isSubSelect) {
			addSubSelectWhere(sc, hld.hld, stm, conditionStr);
		} else {
			sc.o(whereClause.render(stm));
		}

//		renderIfPresent(sc, orderByFrag);
//		renderIfPresent(sc, limitFrag);  TODO is this needed?
		
		stm.sql = sc.toString();
		return stm;
	}
	
	//TODO: should we use a SqlSelectStatement here??
	private void addSubSelectWhere(StrCreator sc, HLDQuery hld, SqlStatement stm, String conditionStr) {
//		WHERE t1.cust IN (SELECT t2.cid FROM Customer as t2 WHERE t2.x > ?", "10");

		conditionStr = StringUtils.substringBefore(conditionStr, "=").trim();
		sc.o(" WHERE %s IN ", conditionStr);
		String alias = "t9"; //TODO fix later
		sc.o("(SELECT %s.cid FROM %s as %s", alias, hld.fromType.getName(), alias);
		String whereStr = whereClause.render(stm);

		String s1 = "t9.";
		String source = String.format("%s.", hld.fromAlias);
		whereStr = whereStr.replace(source, s1);
		sc.o("%s", whereStr);
	}
}
