package org.delia.db.newhls;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.QuerySpec;
import org.delia.db.newhls.cud.HLDDelete;
import org.delia.db.newhls.cud.HLDDsonBuilder;
import org.delia.db.newhls.cud.HLDInsert;
import org.delia.db.newhls.cud.HLDInsertSQLGenerator;
import org.delia.db.newhls.cud.HLDInsertStatement;
import org.delia.db.newhls.cud.HLDUpdate;
import org.delia.db.newhls.cud.HLDWhereGen;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.log.Log;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
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
	private SprigService sprigSvc;
	private HLDEngine engine;

	public HLDManager(DTypeRegistry registry, FactoryService factorySvc, Log log, DatIdMap datIdMap, SprigService sprigSvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.log = log;
		this.sprigSvc = sprigSvc;
		this.engine = new HLDEngine(registry, factorySvc, log, datIdMap, sprigSvc);
	}
	
	public HLDQueryStatement fullBuildQuery(QueryExp queryExp) {
		HLDQuery hld = engine.fullBuildQuery(queryExp);
		HLDQueryStatement stmt = new HLDQueryStatement(hld);
		return stmt;
	}
	public HLDDelete fullBuildDelete(QueryExp queryExp) {
		HLDQueryStatement hld = fullBuildQuery(queryExp);
		HLDDelete hlddel = new HLDDelete(hld.hldquery);
		return hlddel;
	}
	public HLDInsertStatement fullBuildInsert(InsertStatementExp insertExp) {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);
		
		HLDInsertStatement stmt = new HLDInsertStatement();
		stmt.hldinsert = hldBuilder.buildInsert(insertExp);
		return stmt;
	}
	public HLDUpdate fullBuildUpdate(UpdateStatementExp updateExp) {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);
		HLDUpdate hld = hldBuilder.buildUpdate(updateExp);
		hld.hld = engine.fullBuildQuery(updateExp.queryExp);
		hld.querySpec = new QuerySpec();
		hld.querySpec.evaluator = null; //TOOD fix
		hld.querySpec.queryExp = updateExp.queryExp;
		return hld;
	}
	
	public String generateRawSql(HLDQueryStatement hld) {
		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		String sql = sqlgen.generateRawSql(hld.hldquery);
		return sql;
	}
	
	public SqlStatement generateSql(HLDQueryStatement hld) {
		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		SqlStatement sql = sqlgen.generateSqlStatement(hld.hldquery);
		return sql;
	}
	public HLDSQLGenerator createSQLGenerator() {
		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		return sqlgen;
	}
	public SqlStatement generateSql(HLDDelete hlddel) {
		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		SqlStatement sql = sqlgen.generateSqlStatement(hlddel);
		return sql;
	}

	public SqlStatementGroup generateSql(HLDInsertStatement hldins) {
		HLDWhereGen whereGen = new HLDWhereGenImpl(this, engine);
		HLDInsertSQLGenerator insertSqlGen = new HLDInsertSQLGenerator(registry, factorySvc, datIdMap, whereGen);
		
		SqlStatementGroup stmgrp = insertSqlGen.generate(hldins.hldinsert.cres.dval);
		return stmgrp;
	}

	public SqlStatementGroup generateSql(HLDUpdate hldupdate) {
		HLDWhereGen whereGen = new HLDWhereGenImpl(this, engine);
		HLDInsertSQLGenerator updateSqlGen = new HLDInsertSQLGenerator(registry, factorySvc, datIdMap, whereGen);
		
		SqlStatementGroup stmgrp = updateSqlGen.generateUpdate(hldupdate);
		return stmgrp;
	}
}