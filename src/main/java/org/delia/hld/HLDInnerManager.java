package org.delia.hld;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UpsertStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.db.sqlgen.SqlGeneratorFactory;
import org.delia.db.sqlgen.SqlSelectStatement;
import org.delia.hld.cud.HLDDeleteStatement;
import org.delia.hld.cud.HLDInsertStatement;
import org.delia.hld.cud.HLDUpdateStatement;
import org.delia.hld.cud.HLDUpsertStatement;
import org.delia.hld.cud.InsertInnerSQLGenerator;
import org.delia.runner.DValueIterator;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

/**
 * Main HLD class for building sql and HLDQuery objects
 * @author ian
 *
 */
public class HLDInnerManager extends HLDServiceBase {
	private HLDEngine engine;

	private HLDEngineAssoc engineAssoc;
//	private ConversionHelper conversionHelper;

	public HLDInnerManager(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap, SprigService sprigSvc) {
		super(registry, factorySvc, datIdMap, sprigSvc);
		this.engine = new HLDEngine(registry, factorySvc, datIdMap, sprigSvc);
		this.engineAssoc = new HLDEngineAssoc(registry, factorySvc, datIdMap, sprigSvc);
//		this.conversionHelper = new ConversionHelper(registry, factorySvc);
	}

	public HLDQueryStatement fullBuildQuery(QueryExp queryExp, VarEvaluator varEvaluator) {
		engine.setVarEvaluator(varEvaluator);
		HLDQuery hld = engine.buildQuery(queryExp);
		HLDQueryStatement stmt = new HLDQueryStatement(hld);
		engine.assignAliases(stmt);
		return stmt;
	}
	public boolean canBuildQuery(QueryExp queryExp, VarEvaluator varEvaluator) {
		engine.setVarEvaluator(varEvaluator);
		return engine.canBuildQuery(queryExp, null);
	}
	public HLDDeleteStatement fullBuildDelete(QueryExp queryExp) {
		HLDDeleteStatement stmt = new HLDDeleteStatement();
		stmt.hlddelete = engine.buildDelete(queryExp);
		engine.addParentStatementsForDelete(stmt.hlddelete, stmt.moreL);
		engine.assignAliases(stmt);
		return stmt;
	}
	public HLDInsertStatement fullBuildInsert(InsertStatementExp insertExp, VarEvaluator varEvaluator, DValueIterator insertPrebuiltValueIterator) {
		HLDInsertStatement stmt = new HLDInsertStatement();
		engine.setVarEvaluator(varEvaluator);
		engineAssoc.setVarEvaluator(varEvaluator);
		engine.setInsertPrebuiltValueIterator(insertPrebuiltValueIterator);
		stmt.hldinsert = engine.buildInsert(insertExp);
		if (stmt.hldinsert.buildSuccessful()) {
			engine.addParentUpdates(stmt.hldinsert, stmt.moreL);
			engine.addAssocInserts(stmt.hldinsert, stmt.moreL);
			engine.assignAliases(stmt);
		}
		return stmt;
	}

	public HLDUpdateStatement fullBuildUpdate(UpdateStatementExp updateExp, VarEvaluator varEvaluator, DValueIterator insertPrebuiltValueIterator) {
		HLDUpdateStatement stmt = new HLDUpdateStatement();
		engine.setVarEvaluator(varEvaluator);
		engine.setInsertPrebuiltValueIterator(insertPrebuiltValueIterator);
		engineAssoc.setVarEvaluator(varEvaluator);
		stmt.hldupdate = engine.buildUpdate(updateExp);
		engine.addParentUpdatesForUpdate(stmt.hldupdate, stmt.moreL);
		stmt.assocBundleL = engine.addMoreAssoc(stmt.hldupdate, engineAssoc, updateExp.queryExp, stmt.moreL);
		engine.assignAliases(stmt);
		return stmt;
	}

	public HLDUpsertStatement fullBuildUpsert(UpsertStatementExp upsertExp, VarEvaluator varEvaluator, DValueIterator insertPrebuiltValueIterator) {
		HLDUpsertStatement stmt = new HLDUpsertStatement();
		engine.setVarEvaluator(varEvaluator);
		engineAssoc.setVarEvaluator(varEvaluator);
		engine.setInsertPrebuiltValueIterator(insertPrebuiltValueIterator);
		stmt.hldupdate = engine.buildUpsert(upsertExp);
		engine.addParentUpdatesForUpdate(stmt.hldupdate, stmt.moreL);
		//		stmt.assocInsertL = engine.addAssocInserts(stmt.hldupdate);
		stmt.assocBundleL = engine.addMoreAssoc(stmt.hldupdate, engineAssoc, upsertExp.queryExp, stmt.moreL);
		engine.assignAliases(stmt);
		return stmt;
	}

	// -- sql generation --
	public String generateRawSql(HLDQueryStatement hld) {
		if (InsertInnerSQLGenerator.useSqlGenFactory) {
			SqlGeneratorFactory genfact = factorySvc.createSqlFactory(registry);
			SqlSelectStatement selStmt = genfact.createSelect(datIdMap);
			selStmt.disableSqlParameterGen();
			selStmt.init(hld.hldquery);
			SqlStatement stm = selStmt.render();
			return stm.sql;
		}
//		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
//		String sql = sqlgen.generateRawSql(hld.hldquery);
//		return sql;
		return null;
	}
	public SqlStatementGroup generateSql(HLDQueryStatement hld) {
		if (InsertInnerSQLGenerator.useSqlGenFactory) {
			SqlGeneratorFactory genfact = factorySvc.createSqlFactory(registry);
			SqlSelectStatement sqlMergeInto = genfact.createSelect(datIdMap);
			sqlMergeInto.init(hld.hldquery);
			SqlStatement stm = sqlMergeInto.render();
			SqlStatementGroup stgrp = new SqlStatementGroup();
			stgrp.add(stm);
			return stgrp;
		}
		
//		//TODO: arg we need to implement select with InsertInnerSQLGenerator!!
//		HLDSQLGenerator sqlgen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
//		SqlStatement sql = sqlgen.generateSqlStatement(hld.hldquery);
//
//		//convert strings to dates where needed
//		for(DValue dval: sql.paramL) {
//			if (dval != null) {
//				DValue xx = conversionHelper.convertDValToActual(dval.getType(), dval);
//			}
//		}
//
//		SqlStatementGroup stgrp = new SqlStatementGroup();
//		stgrp.add(sql);
//		return stgrp;
		return null;
	}


	public SqlStatementGroup generateSql(HLDDeleteStatement hlddel) {
		HLDSQLGenerator otherSqlGen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		InsertInnerSQLGenerator sqlgen = new InsertInnerSQLGenerator(factorySvc, registry, otherSqlGen);
		SqlStatementGroup stmgrp = sqlgen.generate(hlddel);
		return stmgrp;
	}

	public SqlStatementGroup generateSql(HLDInsertStatement hldins) {
		HLDSQLGenerator otherSqlGen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		InsertInnerSQLGenerator sqlgen = new InsertInnerSQLGenerator(factorySvc, registry, otherSqlGen);
		SqlStatementGroup stmgrp = sqlgen.generate(hldins);
		return stmgrp;
	}

	public SqlStatementGroup generateSql(HLDUpdateStatement hldupdate) {
		HLDSQLGenerator otherSqlGen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		InsertInnerSQLGenerator sqlgen = new InsertInnerSQLGenerator(factorySvc, registry, otherSqlGen);
		SqlStatementGroup stmgrp = sqlgen.generate(hldupdate);
		return stmgrp;
	}

	public SqlStatementGroup generateSql(HLDUpsertStatement hldupsert) {
		HLDSQLGenerator otherSqlGen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		InsertInnerSQLGenerator sqlgen = new InsertInnerSQLGenerator(factorySvc, registry, otherSqlGen);
		SqlStatementGroup stmgrp = sqlgen.generate(hldupsert);
		return stmgrp;
	}


}