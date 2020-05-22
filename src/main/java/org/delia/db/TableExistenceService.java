package org.delia.db;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;

public interface TableExistenceService {

	boolean doesTableExist(String tableName);
	int fillTableInfoIfNeeded(List<TableInfo> tblInfoL, RelationInfo info, DatIdMap datIdMap);
	
}
