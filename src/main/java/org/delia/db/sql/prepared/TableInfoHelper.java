//package org.delia.db.sql.prepared;
//
//import java.util.List;
//
//import org.delia.db.sql.table.TableInfo;
//import org.delia.relation.RelationInfo;
//import org.delia.type.DStructType;
//import org.delia.type.TypePair;
//
//public class TableInfoHelper {
//
//	public static TableInfo findTableInfo(List<TableInfo> tblInfoL, TypePair pair, RelationInfo info) {
//		for(TableInfo tblinfo: tblInfoL) {
//			if (tblinfo.tbl1 != null && tblinfo.tbl2 != null) {
//				if (tblinfo.tbl1.equals(info.nearType.getName())) {
//					if (tblinfo.tbl2.equals(info.farType.getName())) {
//						return tblinfo;
//					}
//				}
//
//				if (tblinfo.tbl2.equals(info.nearType.getName())) {
//					if (tblinfo.tbl1.equals(info.farType.getName())) {
//						return tblinfo;
//					}
//				}
//			}
//		}
//		return null;
//	}
//	
//	public static TableInfo findTableInfoAssoc(List<TableInfo> tblInfoL, DStructType nearType, DStructType farType) {
//		for(TableInfo tblinfo: tblInfoL) {
//			if (tblinfo.assocTblName != null) {
//				if (tblinfo.tbl1.equals(nearType.getName()) &&
//						tblinfo.tbl2.equals(farType.getName())) {
//					return tblinfo;
//				}
//
//				if (tblinfo.tbl2.equals(nearType.getName()) &&
//						tblinfo.tbl1.equals(farType.getName())) {
//					return tblinfo;
//				}
//			}
//		}
//		return null;
//	}
//	
//	
//}
