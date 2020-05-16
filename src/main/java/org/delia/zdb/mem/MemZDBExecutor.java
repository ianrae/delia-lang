package org.delia.zdb.mem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.delia.assoc.DatIdMap;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBException;
import org.delia.db.InsertContext;
import org.delia.db.InternalException;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.memdb.AllRowSelector;
import org.delia.db.memdb.MemDBTable;
import org.delia.db.memdb.OpRowSelector;
import org.delia.db.memdb.PrimaryKeyRowSelector;
import org.delia.db.memdb.RowSelector;
import org.delia.db.sql.QueryType;
import org.delia.error.DeliaError;
import org.delia.error.DetailedError;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.FetchRunner;
import org.delia.runner.FilterEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.runner.ZFetchRunnerImpl;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.DValueImpl;
import org.delia.type.TypePair;
import org.delia.type.TypeReplaceSpec;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.validation.ValidationRuleRunner;
import org.delia.zdb.ZDBConnection;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

public class MemZDBExecutor extends ServiceBase implements ZDBExecutor {

	private DTypeRegistry registry;
	private DatIdMap datIdMap;
	private VarEvaluator varEvaluator;

	private Map<String,MemDBTable> tableMap;
	private ZStuff stuff; //created lazily
	DateFormatService fmtSvc;
	public boolean createTablesAsNeededFlag = true;
	private MemZDBInterfaceFactory dbInterface;


	public MemZDBExecutor(FactoryService factorySvc, MemZDBInterfaceFactory dbInterface) {
		super(factorySvc);
		this.dbInterface = dbInterface;
		this.tableMap = dbInterface.createSingleMemDB();
		this.log = factorySvc.getLog();
		this.et = factorySvc.getErrorTracker();
		this.fmtSvc = factorySvc.getDateFormatService();
	}

	@Override
	public ZDBConnection getDBConnection() {
		return null; //none for MEM
	}
	
	@Override
	public void close() {
	}

	@Override
	public void init1(DTypeRegistry registry) {
		this.registry = registry;
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

	@Override
	public QueryResponse rawQuery(QuerySpec spec, QueryContext qtx) {
		QueryResponse qresp = new QueryResponse();

		try {
			qresp = doExecuteQuery(spec, qtx);
		} catch (InternalException e) {
			qresp.ok = false;
			qresp.err = e.getLastError();
		}

		return qresp;
	}

	private QueryResponse doExecuteQuery(QuerySpec spec, QueryContext qtx) {
		QueryResponse qresp = new QueryResponse();
		RowSelector selector = createSelector(spec); 
		if (selector == null) {
			//err!!
			return qresp;
		} else {
			List<DValue> dvalList = selector.match(selector.getTbl().rowL);
			if (selector.wasError()) {
				//err!!
				qresp.ok = false;
				return qresp;
			}

			//add fks if needed
			if (qtx.loadFKs) {
				for(DValue dval: dvalList) {
					addAnyFKs(dval);
				}
			} else if (qtx.pruneParentRelationFlag) {
				dvalList = removeParentSideRelations(dvalList);
			}
			//TODO: if query does NOT include fks or fetch then we should
			//remove all parent side relations

			qresp.dvalList = dvalList;
			qresp.ok = true;
			return qresp;
		}
	}
	private List<DValue> removeParentSideRelations(List<DValue> dvalList) {
		List<DValue> list = new ArrayList<>();
		for(DValue dval: dvalList) {
			dval = removeParentSideRelationsOne(dval);
			list.add(dval);
		}

		return list;
	}
	private DValue removeParentSideRelationsOne(DValue dval) {
		if (! dval.getType().isStructShape()) {
			return dval;
		}
		DStructType structType = (DStructType) dval.getType();
		List<String> doomedL =  new ArrayList<>();
		for(TypePair pair: structType.getAllFields()) {
			RelationOneRule oneRule = DRuleHelper.findOneRule(structType, pair.name);
			if (oneRule != null) {
				if (oneRule.relInfo != null && oneRule.relInfo.isParent) {
					doomedL.add(pair.name);
				}
			} else {
				RelationManyRule manyRule = DRuleHelper.findManyRule(structType, pair.name);
				if (manyRule != null) {
					if (manyRule.relInfo != null && manyRule.relInfo.isParent) {
						doomedL.add(pair.name);
					}
				}
			}
		}

		//clone without doomed fields
		if (doomedL.isEmpty()) {
			return dval;
		}

		Map<String,DValue> cloneMap = new TreeMap<>(dval.asMap());
		for(String doomed: doomedL) {
			cloneMap.remove(doomed);
		}

		DValueImpl clone = new DValueImpl(structType, cloneMap);
		return clone;
	}

	@Override
	public FetchRunner createFetchRunner() {
		return new ZFetchRunnerImpl(factorySvc, this, registry, varEvaluator);
	}


	private void addAnyFKs(DValue dval) {
		FetchRunner fetchRunner = createFetchRunner();
		ValidationRuleRunner ruleRunner = new ValidationRuleRunner(factorySvc, dbInterface.getCapabilities(), fetchRunner);
		ruleRunner.enableRelationModifier(true);
		ruleRunner.setPopulateFKsFlag(true);
		ruleRunner.validateRelationRules(dval);
	}

	private RowSelector createSelector(QuerySpec spec) {
		String typeName = spec.queryExp.getTypeName();
		MemDBTable tbl = tableMap.get(typeName);
		if (tbl == null) {
			tbl = handleUnknownTable(typeName);
		}

		ZStuff stuff = findOrCreateStuff();
		RowSelector selector;
		QueryType queryType = stuff.queryDetectorSvc.detectQueryType(spec);
		switch(queryType) {
		case ALL_ROWS:
			selector = new AllRowSelector();
			break;
		case OP:
			selector = new OpRowSelector();
			break;
		case PRIMARY_KEY:
		default:
			selector = new PrimaryKeyRowSelector();
		}

		selector.setTbl(tbl);
		DStructType dtype = findType(typeName); 
		if (dtype == null) {
			DeliaExceptionHelper.throwError("struct-unknown-type-in-query", "unknown struct type '%s'", typeName);
		}

		selector.init(et, spec, dtype, registry); 
		if (selector.wasError()) {
			DeliaError err = et.add("row-selector-error", String.format("row selector failed for type '%s'", typeName));
			throw new DBException(err);
		}
		return selector;
	}
	/**
	 * for unit tests we want to explicitly create tables during test startup.
	 * But at other times we want to create tables as needed.
	 * @param typeName a registered type
	 */
	private MemDBTable handleUnknownTable(String typeName) {
		if (createTablesAsNeededFlag) {
			this.rawCreateTable(typeName);
			return tableMap.get(typeName);
		} else {
			DeliaError err = et.add("unknown-table-type", String.format("can't find type '%s'", typeName));
			throw new DBException(err);
		}
	}

	private DStructType findType(String typeName) {
		return registry.findTypeOrSchemaVersionType(typeName);
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
	public void performTypeReplacement(TypeReplaceSpec spec) {
		for (String typeName: tableMap.keySet()) {
			MemDBTable tbl = tableMap.get(typeName);
			for(DValue dval: tbl.rowL) {
				DType dtype = dval.getType();

				//in addition the DValues stored here may be from a previous entire run
				//of Runner (and its registry).
				//so also check by name
				boolean shouldReplace = dtype.getName().equals(spec.newType.getName());

				if (shouldReplace || spec.needsReplacement(this, dtype)) {
					DValueImpl impl = (DValueImpl) dval;
					impl.forceType(spec.newType);
				} else {
					dtype.performTypeReplacement(spec);
				}
			}
		}
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
	public int executeUpdate(QuerySpec spec, DValue dvalUpdate, Map<String, String> assocCrudMap) {
		int numRowsAffected = 0;

		try {
			numRowsAffected = doExecuteUpdate(spec, dvalUpdate, assocCrudMap);
		} catch (InternalException e) {
			throw new DBException(e.getLastError());
			//				qresp.ok = false;
			//				qresp.err = e.getLastError();
		}
		return numRowsAffected;
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
			//				qresp.ok = false;
			//				qresp.err = e.getLastError();
		}
		return numRowsAffected;
	}
	private int doExecuteUpsert(QuerySpec spec, DValue dvalUpdate, Map<String, String> assocCrudMap, boolean noUpdateFlag) {
		RowSelector selector = createSelector(spec); //may throw
		if (selector instanceof AllRowSelector) {
			DeliaError err = et.add("upsert-filter-error", String.format("upsert filter must specify one row (at most), for type '%s'", spec.queryExp.typeName));
			throw new DBException(err);
		}

		ZMemUpsert memUpdate = new ZMemUpsert(factorySvc, registry);
		ZStuff stuff = findOrCreateStuff();
		return memUpdate.doExecuteUpsert(spec, dvalUpdate, assocCrudMap, noUpdateFlag, selector, this, stuff);
	}

	@Override
	public void executeDelete(QuerySpec spec) {
		QueryResponse qresp = new QueryResponse();
		try {
			qresp = doExecuteDelete(spec);
		} catch (InternalException e) {
			throw new DBException(e.getLastError());
			//				qresp.ok = false;
			//				qresp.err = e.getLastError();
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
	public QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx) {
		return this.doExecuteQuery(hls.querySpec, qtx);
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
		//nothing to do
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

	/**
	 * Ugly. we need a serial provider per registry (really per runner i thinkg)
	 * TODO fix later
	 * @param ctx db context
	 * @return stuff
	 */
	private ZStuff findOrCreateStuff() {
		if (stuff == null) {
			stuff = new ZStuff();
			stuff.init(factorySvc, registry);
		}
		return stuff;
	}

	void checkUniqueness(DValue dval, MemDBTable tbl, String typeName, DValue existing, boolean allowMissing) {
		//TODO: later support types without primarykey
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
				//TODO: need way to compare dvals
				if (oldVal != null) {
					String ss1 = inner.asString();
					String ss2 = oldVal.asString();
					if (ss1.equals(ss2)) {
						continue;
					}
				}
			}

			//				Exp xx = createExpFor(inner);
			//				FilterOpExp cond0 = new FilterOpExp(0, new IdentExp(uniqueField), new StringExp("=="), xx);
			//				FilterOpFullExp cond = new FilterOpFullExp(0, cond0);
			//				
			////				FilterExp filter = new FilterExp(99, new StringExp(inner.asString()));
			//				FilterExp filter = new FilterExp(99, cond);
			QuerySpec spec = new QuerySpec();
			//				spec.queryExp = new QueryExp(99, new IdentExp(typeName), filter, null);

			QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
			spec.queryExp = builderSvc.createEqQuery(typeName, uniqueField, inner);


			spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
			spec.evaluator.init(spec.queryExp);
			QueryContext qtx = new QueryContext();
			QueryResponse qresp = rawQuery(spec, qtx);
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

}