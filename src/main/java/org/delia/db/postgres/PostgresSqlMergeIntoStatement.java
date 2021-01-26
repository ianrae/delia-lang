//package org.delia.db.postgres;
//
//import org.delia.db.SqlStatement;
//import org.delia.db.sql.StrCreator;
//import org.delia.db.sqlgen.SqlMergeIntoStatement;
//import org.delia.db.sqlgen.SqlTableNameClause;
//import org.delia.db.sqlgen.SqlValueListClause;
//
//public class PostgresSqlMergeIntoStatement extends SqlMergeIntoStatement {
//
//	public PostgresSqlMergeIntoStatement(SqlTableNameClause tblClause, SqlValueListClause valueClause) {
//		super(tblClause, valueClause);
//	}
//	
//	@Override
//	public SqlStatement render() {
//		SqlStatement stm = new SqlStatement(hld);
//		StrCreator sc = new StrCreator();
//		sc.o("xxxxMERGE INTO");
//		sc.o(tblClause.render(stm));
//		
//		sc.o(" KEY(%s)", hld.mergeKey);
//		sc.o(valueClause.render(stm));
//		
//		stm.sql = sc.toString();
//		return stm;
//	}
//
//}
