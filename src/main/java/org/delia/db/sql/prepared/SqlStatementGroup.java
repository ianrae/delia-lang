package org.delia.db.sql.prepared;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;

/**
 * H2 doesn't support a prepared-statement with multiple sql statments,
 * so we need a list of sqlstatements.
 * @author Ian Rae
 *
 */
public class SqlStatementGroup {
	
	public List<SqlStatement> statementL = new ArrayList<>();

	public void add(SqlStatement statement) {
		statementL.add(statement);
	}
	
	public String flatten() {
		StrCreator sc = new StrCreator();
		ListWalker<SqlStatement> walker = new ListWalker<>(statementL);
		while(walker.hasNext()) {
			SqlStatement statement = walker.next();
			sc.addStr(statement.sql);
			walker.addIfNotLast(sc, ";\n ");
		}
		String sql = sc.toString(); 
		return sql;
	}
	public int size() {
		return statementL.size();
	}
	public SqlStatement getFirst() {
		return statementL.get(0);
	}
}
