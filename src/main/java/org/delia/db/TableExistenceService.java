package org.delia.db;

import java.util.List;

import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;

public interface TableExistenceService {

	boolean doesTableExist(String tableName);
	void fillTableInfoIfNeeded(List<TableInfo> tblInfoL, RelationInfo info);
	
}
