package org.delia.zdb.mem;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.DBException;
import org.delia.db.InsertContext;
import org.delia.db.InternalException;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.HLSSimpleQueryService;
import org.delia.db.memdb.AllRowSelector;
import org.delia.db.memdb.MemDBTable;
import org.delia.db.memdb.RowSelector;
import org.delia.db.newhls.HLDQueryStatement;
import org.delia.db.newhls.cud.HLDDeleteStatement;
import org.delia.db.newhls.cud.HLDInsert;
import org.delia.db.newhls.cud.HLDInsertStatement;
import org.delia.db.newhls.cud.HLDUpdateStatement;
import org.delia.db.newhls.cud.HLDUpsertStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.dval.compare.DValueCompareService;
import org.delia.error.DeliaError;
import org.delia.error.DetailedError;
import org.delia.log.Log;
import org.delia.runner.FetchRunner;
import org.delia.runner.FilterEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.runner.ZFetchRunnerImpl;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.zdb.ZDBConnection;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

public class MemZDBExecutor extends MemDBExecutorBase implements ZDBExecutor {

	private DatIdMap datIdMap;
	private VarEvaluator varEvaluator;
	private DValueCompareService compareSvc;
	private HLSSimpleQueryService querySvc;

	public MemZDBExecutor(FactoryService factorySvc, MemZDBInterfaceFactory dbInterface) {
		super(factorySvc, dbInterface);
		this.compareSvc = new DValueCompareService(factorySvc);
	}

	@Override
	public ZDBConnection getDBConnection() {
		return null; //none for MEM
	}
	
	@Override
	public void close() {
	}
	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public void init1(DTypeRegistry registry) {
		this.registry = registry;
		this.querySvc = factorySvc.createSimpleQueryService(dbInterface, registry);
	}

	@Override
	public void init2(DatIdMap datIdMap, VarEvaluator varEvaluator) {
		this.datIdMap = datIdMap;
		this.varEvaluator = varEvaluator;
	}

	@Override
	public DValue rawInsert(DValue dval, InsertContext ctx) {
		return executeInsert(dval, ctx);
	}

//	@Override
//	public QueryResponse rawQuery(QuerySpec spec, QueryContext qtx) {
//		QueryResponse qresp = new QueryResponse();
//
//		try {
//			qresp = doExecuteQuery(spec, qtx);
//		} catch (InternalException e) {
//			qresp.ok = false;
//			qresp.err = e.getLastError();
//		}
//
//		return qresp;
//	}

	@Override
	public FetchRunner createFetchRunner() {
		return doCreateFetchRunner();
	}
	@Override
	public FetchRunner doCreateFetchRunner() {
		return new ZFetchRunnerImpl(factorySvc, this, registry, varEvaluator);
	}


	/**
	 * for unit tests we want to explicitly create tables during test startup.
	 * But at other times we want to create tables as needed.
	 * @param typeName a registered type
	 */
	@Override
	public MemDBTable handleUnknownTable(String typeName) {
		if (createTablesAsNeededFlag) {
			this.rawCreateTable(typeName);
			return tableMap.get(typeName);
		} else {
			DeliaError err = et.add("unknown-table-type", String.format("can't find type '%s'", typeName));
			throw new DBException(err);
		}
	}

	@Override
	public boolean rawTableDetect(String tableName) {
		return this.doesTableExist(tableName);
	}

	@Override
	public boolean rawFieldDetect(String tableName, String fieldName) {
		return this.doesFieldExist(tableName, fieldName);
	}

	@Override
	public void rawCreateTable(String tableName) {
		this.createTable(tableName);
	}

	@Override
	public DValue executeInsert(DValue dval, InsertContext ctx) {
		String typeName = dval.getType().getName();
		MemDBTable tbl = tableMap.get(typeName);
		if (tbl == null) {
			tbl = handleUnknownTable(typeName);
		}

		ZStuff stuff = findOrCreateStuff();
		ZMemInsert memInsert = new ZMemInsert(this.factorySvc);
		return memInsert.doExecuteInsert(tbl, dval, ctx, this, stuff);
	}

	@Override
	public DValue executeInsert(HLDInsertStatement hld, SqlStatementGroup stmgrp, InsertContext ctx) {
		//we can use ZMemInsert here
		HLDInsert hldinsert = hld.hldinsert;
		DValue dval = hldinsert.cres.dval;
		String typeName = dval.getType().getName();
		MemDBTable tbl = tableMap.get(typeName);
		if (tbl == null) {
			tbl = handleUnknownTable(typeName);
		}

		ZStuff stuff = findOrCreateStuff();
		ZMemInsert memInsert = new ZMemInsert(this.factorySvc);
		return memInsert.doExecuteInsert(tbl, dval, ctx, this, stuff);
	}

	@Override
	public int executeUpdate(QuerySpec spec, DValue dvalUpdate, Map<String, String> assocCrudMap) {
		int numRowsAffected = 0;

		try {
			numRowsAffected = doExecuteUpdate(spec, dvalUpdate, assocCrudMap);
		} catch (InternalException e) {
			throw new DBException(e.getLastError());
		}
		return numRowsAffected;
	}

	@Override
	public int executeUpdate(HLDUpdateStatement hld, SqlStatementGroup stmgrp) {
		//TODO later when we make new HLDRowSelector, rewrite this
		//for now use existing code
		QuerySpec spec = new QuerySpec();
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		spec.queryExp = hld.hldupdate.hld.originalQueryExp;
		spec.evaluator.init(spec.queryExp);
		return executeUpdate(spec, hld.hldupdate.cres.dval, hld.hldupdate.cres.assocCrudMap);
	}

	private int doExecuteUpdate(QuerySpec spec, DValue dvalUpdate, Map<String, String> assocCrudMap) {
		RowSelector selector = createSelector(spec); //may throw
		ZMemUpdate memUpdate = new ZMemUpdate(factorySvc, registry);
		return memUpdate.doExecuteUpdate(spec, dvalUpdate, assocCrudMap, selector, this);
	}

	@Override
	public int executeUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap,
			boolean noUpdateFlag) {
		int numRowsAffected = 0;

		try {
			numRowsAffected = doExecuteUpsert(spec, dvalFull, assocCrudMap, noUpdateFlag);
		} catch (InternalException e) {
			throw new DBException(e.getLastError());
		}
		return numRowsAffected;
	}
	private int doExecuteUpsert(QuerySpec spec, DValue dvalUpdate, Map<String, String> assocCrudMap, boolean noUpdateFlag) {
		RowSelector selector = createSelector(spec); //may throw
		if (selector instanceof AllRowSelector) {
			DeliaError err = et.add("upsert-filter-error", String.format("upsert filter must specify one row (at most), for type '%s'", spec.queryExp.typeName));
			throw new DBException(err);
		}

		ZMemUpsert memUpsert = new ZMemUpsert(factorySvc, registry);
		ZStuff stuff = findOrCreateStuff();
		return memUpsert.doExecuteUpsert(spec, dvalUpdate, assocCrudMap, noUpdateFlag, selector, this, stuff);
	}

	@Override
	public int executeUpsert(HLDUpsertStatement hld, SqlStatementGroup stmgrp, boolean noUpdateFlag) {
		//TODO later when we make new HLDRowSelector, rewrite this
		//for now use existing code
		QuerySpec spec = new QuerySpec();
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		spec.queryExp = hld.hldupdate.hld.originalQueryExp;
		spec.evaluator.init(spec.queryExp);
		return executeUpsert(spec, hld.hldupdate.cres.dval, hld.hldupdate.cres.assocCrudMap, noUpdateFlag);
	}

	@Override
	public void executeDelete(QuerySpec spec) {
		QueryResponse qresp = new QueryResponse();
		try {
			qresp = doExecuteDelete(spec);
		} catch (InternalException e) {
			throw new DBException(e.getLastError());
		}
	}
	private QueryResponse doExecuteDelete(QuerySpec spec) {
		QueryResponse qresp = new QueryResponse();
		RowSelector selector = createSelector(spec); //may throw
		MemDBTable tbl = selector.getTbl();
		List<DValue> dvalList = selector.match(tbl.rowL);
		String typeName = spec.queryExp.getTypeName();
		if (selector.wasError()) {
			DeliaError err = et.add("row-selector-error", String.format("xrow selector failed for type '%s'", typeName));
			throw new DBException(err);
		}

		if (CollectionUtils.isEmpty(dvalList)) {
			qresp.dvalList = null;
			qresp.ok = true;
		}

		for(DValue dval: dvalList) {
			tbl.rowL.remove(dval);
		}
		qresp.dvalList = null;
		qresp.ok = true;
		return qresp;
	}
	@Override
	public void executeDelete(HLDDeleteStatement hld, SqlStatementGroup stmgrp) {
		//TODO later when we make new HLDRowSelector, rewrite this
		//for now use existing code
		QuerySpec spec = new QuerySpec();
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		spec.queryExp = hld.hlddelete.hld.originalQueryExp;
		spec.evaluator.init(spec.queryExp);
		executeDelete(spec);
	}

	@Override
	public QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx) {
		return this.doExecuteQuery(hls.querySpec, qtx);
	}
	@Override
	public QueryResponse executeHLDQuery(HLDQueryStatement hld, String sql, QueryContext qtx) {
		QueryResponse qresp = doExecuteQuery(hld.querySpec, qtx);
		
		MemFunctionHelper helper = new MemFunctionHelper(factorySvc, dbInterface, registry, createFetchRunner());
		qresp = helper.executeHLDQuery(hld, qresp);
		return qresp;
	}

	@Override
	public boolean doesTableExist(String tableName) {
		return this.tableMap.containsKey(tableName);
	}

	@Override
	public boolean doesFieldExist(String tableName, String fieldName) {
		DStructType structType = findType(tableName);
		if (structType == null) {
			return false;
		}

		return DValueHelper.fieldExists(structType, fieldName);
	}

	@Override
	public void createTable(String tableName) {
		tableMap.put(tableName, new MemDBTable());
	}

	@Override
	public void deleteTable(String tableName) {
		tableMap.remove(tableName);
	}

	@Override
	public void renameTable(String tableName, String newTableName) {
		MemDBTable tbl = tableMap.get(tableName);
		tbl.name = newTableName;
		tableMap.remove(tableName);
		tableMap.put(newTableName, tbl);
	}

	@Override
	public void createField(String typeName, String field) {
		MemDBTable tbl = tableMap.get(typeName);
		if (tbl == null) {
			tbl = handleUnknownTable(typeName);
		}
	}

	@Override
	public void deleteField(String typeName, String field, int datId) {
		MemDBTable tbl = tableMap.get(typeName);
		for(DValue dval: tbl.rowL) {
			//subtle. get structType from registry (so is new type definition).
			//dval.getType may have type definition from a previous run.
			DStructType structType = (DStructType) registry.getType(typeName);
			removeFieldFromSingleDVal(dval, field, structType);
		}
		
		for(String tblName: tableMap.keySet()) {
			tbl = tableMap.get(tblName);
			for(DValue dval: tbl.rowL) {
				removeFetchedItems(dval, typeName, field);
			}
		}
	}


	@Override
	public void renameField(String typeName, String fieldName, String newName) {
		//nothing to do
	}

	@Override
	public void alterFieldType(String typeName, String fieldName, String newFieldType) {
		//nothing to do
	}

	@Override
	public void alterField(String typeName, String fieldName, String deltaFlags) {
		//nothing to do
	}

	void checkUniqueness(DValue dval, MemDBTable tbl, String typeName, DValue existing, boolean allowMissing) {
		List<TypePair> candidates = DValueHelper.findAllUniqueFieldPair(dval.getType());
		if (CollectionUtils.isEmpty(candidates)) {
			return;
		}

		DStructType structType = (DStructType) dval.getType();

		for(TypePair pair: candidates) {
			String uniqueField = pair.name;
			DValue inner = DValueHelper.getFieldValue(dval, uniqueField);
			if (inner == null) {
				if (structType.fieldIsOptional(pair.name)) {
					continue;
				}
				if (allowMissing) {
					continue; //update is not updating this field
				}
				DeliaError err = et.add("empty-key-value", String.format("primary key '%s' is null", uniqueField));
				throw new DBException(err);
			}

			if (existing != null) {
				DValue oldVal = DValueHelper.getFieldValue(existing, uniqueField);
				if (oldVal != null) {
					int n = compareSvc.compare(oldVal, inner);
					if (n == 0) {
						continue;
					}
				}
			}

			QuerySpec spec = new QuerySpec();
			QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
			spec.queryExp = builderSvc.createEqQuery(typeName, uniqueField, inner);

			spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
			spec.evaluator.init(spec.queryExp);
			QueryResponse qresp = querySvc.execQuery(spec.queryExp, this);
			if (qresp.ok && !qresp.emptyResults()) {
				DetailedError err = new DetailedError("duplicate-unique-value", String.format("%s. row with unique field '%s' = '%s' already exists", structType.getName(), uniqueField, inner.asString()));
				err.setFieldName(uniqueField);
				et.add(err);
				throw new DBException(err);
			}
		}
	}

	@Override
	public ZDBInterfaceFactory getDbInterface() {
		return dbInterface;
	}
	@Override
	public DatIdMap getDatIdMap() {
		return datIdMap;
	}
}