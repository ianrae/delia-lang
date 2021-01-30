package org.delia.hld;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UpsertStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.db.sqlgen.SqlGeneratorFactory;
import org.delia.db.sqlgen.SqlSelectStatement;
import org.delia.hld.cud.HLDDeleteStatement;
import org.delia.hld.cud.HLDInsertStatement;
import org.delia.hld.cud.HLDUpdateStatement;
import org.delia.hld.cud.HLDUpsertStatement;
import org.delia.hld.cud.HLDToSQLConverterImpl;
import org.delia.runner.DValueIterator;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

/**
 * Main HLD class for building sql and HLDQuery objects
 * @author ian
 *
 */
public class HLDBuildServiceImpl extends HLDServiceBase implements HLDBuildService {
	private HLDEngine engine;

	private HLDEngineAssoc engineAssoc;
//	private ConversionHelper conversionHelper;

	private DBType dbType;

	public HLDBuildServiceImpl(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap, SprigService sprigSvc, DBType dbType) {
		super(registry, factorySvc, datIdMap, sprigSvc);
		this.engine = new HLDEngine(registry, factorySvc, datIdMap, sprigSvc);
		this.engineAssoc = new HLDEngineAssoc(registry, factorySvc, datIdMap, sprigSvc);
//		this.conversionHelper = new ConversionHelper(registry, factorySvc);
		this.dbType = dbType;
	}

	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#fullBuildQuery(org.delia.compiler.ast.QueryExp, org.delia.runner.VarEvaluator)
	 */
	@Override
	public HLDQueryStatement fullBuildQuery(QueryExp queryExp, VarEvaluator varEvaluator) {
		engine.setVarEvaluator(varEvaluator);
		HLDQuery hld = engine.buildQuery(queryExp);
		HLDQueryStatement stmt = new HLDQueryStatement(hld);
		engine.assignAliases(stmt);
		return stmt;
	}
	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#canBuildQuery(org.delia.compiler.ast.QueryExp, org.delia.runner.VarEvaluator)
	 */
	@Override
	public boolean canBuildQuery(QueryExp queryExp, VarEvaluator varEvaluator) {
		engine.setVarEvaluator(varEvaluator);
		return engine.canBuildQuery(queryExp, null);
	}
	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#fullBuildDelete(org.delia.compiler.ast.QueryExp)
	 */
	@Override
	public HLDDeleteStatement fullBuildDelete(QueryExp queryExp) {
		HLDDeleteStatement stmt = new HLDDeleteStatement();
		stmt.hlddelete = engine.buildDelete(queryExp);
		engine.addParentStatementsForDelete(stmt.hlddelete, stmt.moreL);
		engine.assignAliases(stmt);
		return stmt;
	}
	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#fullBuildInsert(org.delia.compiler.ast.InsertStatementExp, org.delia.runner.VarEvaluator, org.delia.runner.DValueIterator)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#fullBuildUpdate(org.delia.compiler.ast.UpdateStatementExp, org.delia.runner.VarEvaluator, org.delia.runner.DValueIterator)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#fullBuildUpsert(org.delia.compiler.ast.UpsertStatementExp, org.delia.runner.VarEvaluator, org.delia.runner.DValueIterator)
	 */
	@Override
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
	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#generateRawSql(org.delia.hld.HLDQueryStatement)
	 */
	@Override
	public String generateRawSql(HLDQueryStatement hld) {
		SqlGeneratorFactory genfact = factorySvc.createSqlFactory(dbType, registry);
		SqlSelectStatement selStmt = genfact.createSelect(datIdMap);
		selStmt.disableSqlParameterGen();
		selStmt.init(hld.hldquery);
		SqlStatement stm = selStmt.render();
		return stm.sql;
	}
	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#generateSql(org.delia.hld.HLDQueryStatement)
	 */
	@Override
	public SqlStatementGroup generateSql(HLDQueryStatement hld) {
		SqlGeneratorFactory genfact = factorySvc.createSqlFactory(dbType, registry);
		SqlSelectStatement sqlMergeInto = genfact.createSelect(datIdMap);
		sqlMergeInto.init(hld.hldquery);
		SqlStatement stm = sqlMergeInto.render();
		SqlStatementGroup stgrp = new SqlStatementGroup();
		stgrp.add(stm);
		return stgrp;
	}


	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#generateSql(org.delia.hld.cud.HLDDeleteStatement)
	 */
	@Override
	public SqlStatementGroup generateSql(HLDDeleteStatement hlddel) {
		HLDToSQLConverterImpl sqlgen = createInnerSqlGenerator(); 
		SqlStatementGroup stmgrp = sqlgen.generate(hlddel);
		return stmgrp;
	}

	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#generateSql(org.delia.hld.cud.HLDInsertStatement)
	 */
	@Override
	public SqlStatementGroup generateSql(HLDInsertStatement hldins) {
		HLDToSQLConverterImpl sqlgen = createInnerSqlGenerator(); 
		SqlStatementGroup stmgrp = sqlgen.generate(hldins);
		return stmgrp;
	}

	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#generateSql(org.delia.hld.cud.HLDUpdateStatement)
	 */
	@Override
	public SqlStatementGroup generateSql(HLDUpdateStatement hldupdate) {
		HLDToSQLConverterImpl sqlgen = createInnerSqlGenerator(); 
		SqlStatementGroup stmgrp = sqlgen.generate(hldupdate);
		return stmgrp;
	}

	/* (non-Javadoc)
	 * @see org.delia.hld.HLDBuildService#generateSql(org.delia.hld.cud.HLDUpsertStatement)
	 */
	@Override
	public SqlStatementGroup generateSql(HLDUpsertStatement hldupsert) {
		HLDToSQLConverterImpl sqlgen = createInnerSqlGenerator(); 
		SqlStatementGroup stmgrp = sqlgen.generate(hldupsert);
		return stmgrp;
	}

	private HLDToSQLConverterImpl createInnerSqlGenerator() {
		HLDSQLGenerator otherSqlGen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		HLDToSQLConverterImpl sqlgen = new HLDToSQLConverterImpl(factorySvc, registry, otherSqlGen, dbType);
		return sqlgen;
	}


}