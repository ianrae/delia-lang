package org.delia.zdb;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.db.TableExistenceService;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;

public class ZTableExistenceService implements TableExistenceService {

	public ZTableExistenceService() {
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
		//careful. with self-join tbl1 and tbl2 are the same. 
		//TODO: review this. i think this code is ok.
		String assocTblName = datIdMap.getAssocTblName(info.getDatId());
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
	

}
