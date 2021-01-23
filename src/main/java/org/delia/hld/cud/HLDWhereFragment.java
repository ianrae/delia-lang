//package org.delia.hld.cud;
//
//import org.delia.db.sql.fragment.SqlFragment;
//
//public class HLDWhereFragment implements SqlFragment {
//	public String sql;
//	private int numParams;
//	public boolean isPKFilter;
//	
//	public HLDWhereFragment(String sql, int numParams) {
//		this.sql = sql;
//		this.numParams = numParams;
//	}
//	
//	@Override
//	public String render() {
//		return sql;
//	}
//	
//	@Override
//	public int getNumSqlParams() {
//		return numParams;
//	}
//	
//}