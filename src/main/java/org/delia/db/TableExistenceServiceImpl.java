package org.delia.db;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.db.sql.table.AssocTableCreator;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

public class TableExistenceServiceImpl implements TableExistenceService {

	private DBAccessContext dbctx;
	private ZDBInterfaceFactory dbInterface;
	
	public static boolean hackYesFlag; //for unit tests only
	
	public TableExistenceServiceImpl(ZDBInterfaceFactory dbInterface, DBAccessContext dbctx) {
		this.dbInterface = dbInterface;
		this.dbctx = dbctx;
	}
	
	@Override
	public boolean doesTableExist(String tableName) {
		if (hackYesFlag) {
			return true;
		}
		
		//TODO fix. very bad perf
		boolean exist = false;
		try(ZDBExecutor zexec = dbInterface.createExecutor()) {
			zexec.init1(dbctx.registry);
			exist = zexec.doesTableExist(tableName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return exist;
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
			if (assocTblName.startsWith(tbl1)) {
				TableInfo tblinfo = new TableInfo(tbl1, assocTblName);
				tblinfo.tbl1 = tbl1;
				tblinfo.tbl2 = tbl2;
				tblInfoL.add(tblinfo);
				return index;
			} else {
				TableInfo tblinfo = new TableInfo(tbl2, assocTblName);
				tblinfo.tbl1 = tbl2;
				tblinfo.tbl2 = tbl1;
				tblInfoL.add(tblinfo);
				return index;
			}
		}
		
//		//try other way around
//		assocTblName = "";//AssocTableCreator.createAssocTableName(tbl2, tbl1);
//		if (doesTableExist(assocTblName)) {
//			TableInfo tblinfo = new TableInfo(tbl2, assocTblName);
//			tblinfo.tbl1 = tbl2;
//			tblinfo.tbl2 = tbl1;
//			tblInfoL.add(tblinfo);
//			return index;
//		}
		return -1;
	}
//	public String createAssocTableName(RelationInfo info) {
//		return datIdMap.getAssocTblName(info.getDatId());
//	}
	


}
