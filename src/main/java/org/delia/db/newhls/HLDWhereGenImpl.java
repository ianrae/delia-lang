package org.delia.db.newhls;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.QuerySpec;
import org.delia.db.newhls.cud.HLDWhereFragment;
import org.delia.db.newhls.cud.HLDWhereGen;
import org.delia.db.sql.fragment.SqlFragment;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DStructType;

public class HLDWhereGenImpl implements HLDWhereGen {
	
	private HLDManager mgr;

	public HLDWhereGenImpl(HLDManager mgr) {
		this.mgr = mgr;
	}

	@Override
	public List<SqlFragment> createWhere(QuerySpec spec, DStructType structType, SqlStatement statement) {
		HLDQuery hld = mgr.fullBuildQuery(spec.queryExp);
		
		HLDSQLGenerator gen = mgr.createSQLGenerator();
		int n1 = statement.paramL.size();
		String sqlwhere = gen.generateSqlWhere(hld, statement);
		int n2 = statement.paramL.size();
		
		
		HLDWhereFragment frag = new HLDWhereFragment(sqlwhere, n2 - n1);
		List<SqlFragment> list = new ArrayList<>();
		list.add(frag);
		return list;
	}

}
