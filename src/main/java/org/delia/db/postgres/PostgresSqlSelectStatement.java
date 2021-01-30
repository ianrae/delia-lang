package org.delia.db.postgres;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.SqlStatement;
import org.delia.db.sql.StrCreator;
import org.delia.db.sqlgen.SqlSelectStatement;
import org.delia.db.sqlgen.SqlTableNameClause;
import org.delia.db.sqlgen.SqlWhereClause;
import org.delia.type.DTypeRegistry;

public class PostgresSqlSelectStatement extends SqlSelectStatement {

	public PostgresSqlSelectStatement(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap,
			SqlTableNameClause tblClause, SqlWhereClause whereClause) {
		super(registry, factorySvc, datIdMap, tblClause, whereClause);
		this.supportsTop = false;
	}

	@Override
	public SqlStatement render() {
		SqlStatement stm = super.render();
		
		StrCreator sc = new StrCreator();
		sc.addStr(stm.sql);
		if (hld.hasFn("first")) { //TODO what if first and limit??/
			Integer n = 1; 
			sc.o(" LIMIT %s", n.toString());
		} else if (hld.hasFn("last")) { //TODO what if first and limit??/
			Integer n = 1; 
			sc.o(" LIMIT %s", n.toString());
		}
		stm.sql = sc.toString();
		return stm;
	}	
	
}
