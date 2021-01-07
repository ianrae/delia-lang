package org.delia.db.newhls;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DTypeRegistry;

/**
 * Main HLD class for building sql and HLDQuery objects
 * @author ian
 *
 */
public class HLDManager {
	private DTypeRegistry registry;
	private FactoryService factorySvc;

	public HLDManager(DTypeRegistry registry, FactoryService factorySvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
	}
	
	public HLDQuery fullBuildQuery(QueryExp queryExp) {
		HLDQueryBuilder hldBuilder = new HLDQueryBuilder(registry);

		HLDQuery hld = hldBuilder.build(queryExp);

		JoinTreeBuilder joinBuilder = new JoinTreeBuilder();
		joinBuilder.generateJoinTree(hld);

		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc);
		HLDFieldBuilder fieldBuilder = new HLDFieldBuilder(aliasMgr);
		fieldBuilder.generateJoinTree(hld);
		return hld;
	}
	
	public String generateRawSql(HLDQuery hld) {
		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc);
		String sql = sqlgen.generateRawSql(hld);
		return sql;
	}
	
	SqlStatement generateSql(HLDQuery hld) {
		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc);
		SqlStatement sql = sqlgen.generateSqlStatement(hld);
		return sql;
	}
}