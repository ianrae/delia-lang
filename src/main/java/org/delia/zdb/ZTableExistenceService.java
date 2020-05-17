package org.delia.zdb;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.db.TableExistenceService;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;

public class ZTableExistenceService implements TableExistenceService {

	private ZDBExecutor dbexecutor;
	
	public static boolean hackYesFlag; //for unit tests only
	
	public ZTableExistenceService(ZDBExecutor dbexecutor) {
		this.dbexecutor = dbexecutor;
	}
	public ZTableExistenceService(ZDBInterfaceFactory dbInterface) {
		//TODO: this is bad. fix! should not be using dbexecutor
		//need to close!
		this.dbexecutor = dbInterface.createExecutor();
	}

	
	@Override
	public boolean doesTableExist(String tableName) {
		if (hackYesFlag) {
			return true;
		}
		return dbexecutor.doesTableExist(tableName);
	}
	
	@Override
	public int fillTableInfoIfNeeded(List<TableInfo> tblInfoL, RelationInfo info, DatIdMap datIdMap) {
		String tbl1 = info.nearType.getName();
		String tbl2 = info.farType.getName();

		int index = 0;
		for(TableInfo inf: tblInfoL) {
			if (inf.tbl1 == null) {
				continue;
			}
			if (inf.tbl1.equalsIgnoreCase(tbl1) && inf.tbl2.equalsIgnoreCase(tbl2)) {
				return index;
			}
			index++;
		}
		
		index = tblInfoL.size();
		
		//try tbl1 tbl2 Assoc
//		String assocTblName = AssocTableCreator.createAssocTableName(tbl1, tbl2);
		String assocTblName = datIdMap.getAssocTblName(info.getDatId());
		if (doesTableExist(assocTblName)) {
			TableInfo tblinfo = new TableInfo(tbl1, assocTblName);
			tblinfo.tbl1 = tbl1;
			tblinfo.tbl2 = tbl2;
			tblInfoL.add(tblinfo);
			return index;
		}
		
//		//try other way around
//		assocTblName = AssocTableCreator.createAssocTableName(tbl2, tbl1);
//		if (doesTableExist(assocTblName)) {
//			TableInfo tblinfo = new TableInfo(tbl2, assocTblName);
//			tblinfo.tbl1 = tbl2;
//			tblinfo.tbl2 = tbl1;
//			tblInfoL.add(tblinfo);
//			return index;
//		}
		return -1;
	}
	

}
