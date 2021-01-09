package org.delia.db.newhls;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.newhls.cud.HLDDelete;
import org.delia.db.newhls.cud.HLDDsonBuilder;
import org.delia.db.newhls.cud.HLDInsert;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.log.Log;
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
	private Log log;

	public HLDManager(DTypeRegistry registry, FactoryService factorySvc, Log log, DatIdMap datIdMap) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.log = log;
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
	public HLDDelete fullBuildDelete(QueryExp queryExp) {
		HLDQuery hld = fullBuildQuery(queryExp);
		HLDDelete hlddel = new  HLDDelete(hld);
		return hlddel;
	}
	public HLDInsert fullBuildInsert(InsertStatementExp insertExp) {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log);

		HLDInsert hld = hldBuilder.buildInsert(insertExp);


//		HLDFieldBuilder fieldBuilder = new HLDFieldBuilder();
//		fieldBuilder.generateFields(hld);
//		
//		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
//		HLDAliasBuilder aliasBuilder = new HLDAliasBuilder(aliasMgr);
//		aliasBuilder.assignAliases(hld);
		return hld;
	}
	
	
	public String generateRawSql(HLDQuery hld) {
		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		String sql = sqlgen.generateRawSql(hld);
		return sql;
	}
	
	public SqlStatement generateSql(HLDQuery hld) {
		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		SqlStatement sql = sqlgen.generateSqlStatement(hld);
		return sql;
	}
	public SqlStatement generateSql(HLDDelete hlddel) {
		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		SqlStatement sql = sqlgen.generateSqlStatement(hlddel);
		return sql;
	}
}