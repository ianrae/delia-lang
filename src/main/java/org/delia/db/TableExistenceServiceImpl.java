package org.delia.db;

import java.util.List;

import org.delia.db.sql.table.TableCreator;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;

public class TableExistenceServiceImpl implements TableExistenceService {

	private DBAccessContext dbctx;
	private DBInterface dbInterface;
	
	public TableExistenceServiceImpl(DBInterface dbInterface, DBAccessContext dbctx) {
		this.dbInterface = dbInterface;
		this.dbctx = dbctx;
	}
	
	@Override
	public boolean doesTableExist(String tableName) {
		return dbInterface.doesTableExist(tableName, dbctx);
	}
	
	@Override
	public void fillTableInfoIfNeeded(List<TableInfo> tblInfoL, RelationInfo info) {
		String tbl1 = info.nearType.getName();
		String tbl2 = info.farType.getName();
		
		//try tbl1 tbl2 Assoc
		String assocTblName = TableCreator.createAssocTableName(tbl1, tbl2);
		if (doesTableExist(assocTblName)) {
			TableInfo tblinfo = new TableInfo(tbl1, assocTblName);
			tblinfo.tbl1 = tbl1;
			tblinfo.tbl2 = tbl2;
			tblInfoL.add(tblinfo);
			return;
		}
		
		//try other way around
		assocTblName = TableCreator.createAssocTableName(tbl2, tbl1);
		if (doesTableExist(assocTblName)) {
			TableInfo tblinfo = new TableInfo(tbl2, assocTblName);
			tblinfo.tbl1 = tbl2;
			tblinfo.tbl2 = tbl1;
			tblInfoL.add(tblinfo);
		}
	}
	

}
