package org.delia.db.postgres;

import org.delia.db.SqlStatement;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;
import org.delia.db.sqlgen.SqlMergeAllIntoStatement;
import org.delia.db.sqlgen.SqlTableNameClause;
import org.delia.db.sqlgen.SqlValueListClause;
import org.delia.hld.HLDField;
import org.delia.type.DValue;

public class PostgresSqlMergeAllIntoStatement extends SqlMergeAllIntoStatement {

	public PostgresSqlMergeAllIntoStatement(SqlTableNameClause tblClause, SqlValueListClause valueClause) {
		super(tblClause, valueClause);
	}

	@Override
	public SqlStatement render() {
//INSERT INTO customers (name, email)
//		VALUES('Microsoft','hotline@microsoft.com') 
//		ON CONFLICT (name) 
//		DO 
//		   UPDATE SET email = EXCLUDED.email || ';' || customers.email;
		
		
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("INSERT INTO");
		sc.o(tblClause.render(stm));
		
		sc.o(valueClause.render(stm));

		sc.o(" ON CONFLICT DO UPDATE");
		sc.o(" SET ");
		int index = 0;
		ListWalker<HLDField> walker = new ListWalker<>(hld.fieldL);
		String conditionStr = null;
		while(walker.hasNext()) {
			HLDField ff = walker.next();
			DValue inner = hld.valueL.get(index);
			stm.paramL.add(valueClause.renderValue(inner));
			
			conditionStr = String.format("%s = %s", ff.render(), "?");
			sc.o(conditionStr);
			walker.addIfNotLast(sc, ", ");
			index++;
		}

		
		stm.sql = sc.toString();
		return stm;
	}
	
}
