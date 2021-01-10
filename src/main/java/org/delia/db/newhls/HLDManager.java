package org.delia.db.newhls;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.newhls.cud.HLDDeleteStatement;
import org.delia.db.newhls.cud.HLDInsertSQLGenerator;
import org.delia.db.newhls.cud.HLDInsertStatement;
import org.delia.db.newhls.cud.HLDUpdateStatement;
import org.delia.db.newhls.cud.HLDWhereGen;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.log.Log;
import org.delia.sprig.SprigService;
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
	public HLDDeleteStatement fullBuildDelete(QueryExp queryExp) {
		HLDDeleteStatement stmt = new HLDDeleteStatement();
		stmt.hlddelete = engine.fullBuildDelete(queryExp);
		return stmt;
	}
	public HLDInsertStatement fullBuildInsert(InsertStatementExp insertExp) {
		HLDInsertStatement stmt = new HLDInsertStatement();
		stmt.hldinsert = engine.fullBuildInsert(insertExp);
		return stmt;
	}
	public HLDUpdateStatement fullBuildUpdate(UpdateStatementExp updateExp) {
		HLDUpdateStatement stmt = new HLDUpdateStatement();
		stmt.hldupdate = engine.fullBuildUpdate(updateExp);
		return stmt;
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
	public SqlStatement generateSql(HLDDeleteStatement hlddel) {
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

	public SqlStatementGroup generateSql(HLDUpdateStatement hldupdate) {
		HLDWhereGen whereGen = new HLDWhereGenImpl(this, engine);
		HLDInsertSQLGenerator updateSqlGen = new HLDInsertSQLGenerator(registry, factorySvc, datIdMap, whereGen);
		
		SqlStatementGroup stmgrp = updateSqlGen.generateUpdate(hldupdate.hldupdate);
		return stmgrp;
	}
}