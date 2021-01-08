package org.delia.db.newhls;

import org.delia.assoc.DatIdMap;
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
	private DatIdMap datIdMap;

	public HLDManager(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
	}
	
	public HLDQuery fullBuildQuery(QueryExp queryExp) {
		HLDQueryBuilder hldBuilder = new HLDQueryBuilder(registry);

		HLDQuery hld = hldBuilder.build(queryExp);

		JoinTreeBuilder joinBuilder = new JoinTreeBuilder();
		joinBuilder.generateJoinTree(hld);

		HLDFieldBuilder fieldBuilder = new HLDFieldBuilder();
		fieldBuilder.generateFields(hld);
		
		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
		HLDAliasBuilder aliasBuilder = new HLDAliasBuilder(aliasMgr);
		aliasBuilder.assignAliases(hld);
		return hld;
	}
	
	public String generateRawSql(HLDQuery hld) {
		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		String sql = sqlgen.generateRawSql(hld);
		return sql;
	}
	
	SqlStatement generateSql(HLDQuery hld) {
		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		SqlStatement sql = sqlgen.generateSqlStatement(hld);
		return sql;
	}
}