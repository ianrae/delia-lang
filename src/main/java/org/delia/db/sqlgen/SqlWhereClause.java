package org.delia.db.sqlgen;

import org.delia.core.FactoryService;
import org.delia.db.newhls.HLDQuery;
import org.delia.db.newhls.SQLWhereGenerator;
import org.delia.db.newhls.SqlParamGenerator;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DTypeRegistry;

public class SqlWhereClause implements SqlClauseGenerator {

	private SqlParamGenerator paramGen;
	private SQLWhereGenerator sqlWhereGen;
	private HLDQuery hld;

	public SqlWhereClause(DTypeRegistry registry, FactoryService factorySvc) {
		this.paramGen = new SqlParamGenerator(registry, factorySvc); 
		this.sqlWhereGen = new SQLWhereGenerator(registry, factorySvc);
	}

	public void init(HLDQuery hld) {
		this.hld = hld;
	}
	
	@Override
	public String render(SqlStatement stm) {
		StrCreator sc = new StrCreator();
		String fragment = generateWhereClause(stm);
		if (fragment != null) {
			sc.o(" WHERE %s", fragment);
		}
		
		return sc.toString();
	}

	private String generateWhereClause(SqlStatement stm) {
		FilterCond filter = hld.filter;
		String fragment = sqlWhereGen.doFilter(filter, paramGen, stm);
		return fragment;
	}

}
