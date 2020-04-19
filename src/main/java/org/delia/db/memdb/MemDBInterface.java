package org.delia.db.memdb;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBCapabilties;
import org.delia.db.DBException;
import org.delia.db.DBExecutor;
import org.delia.db.DBInterface;
import org.delia.db.DBInterfaceInternal;
import org.delia.db.DBType;
import org.delia.db.InsertContext;
import org.delia.db.InternalException;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.log.Log;
import org.delia.runner.FetchRunner;
import org.delia.runner.FetchRunnerImpl;
import org.delia.runner.FilterEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.validation.ValidationRuleRunner;

/**
 * Represents db access to a single memory db.
 * 
 * @author Ian Rae
 *
 */
public class MemDBInterface implements DBInterface, DBInterfaceInternal {

	//per database stuff
	public static class Stuff {
		public SerialProvider serialProvider;
		public QueryTypeDetector queryDetectorSvc;

		public void init(FactoryService factorySvc, DBAccessContext dbctx) {
			this.queryDetectorSvc = new QueryTypeDetector(factorySvc, dbctx.registry);
			if (serialProvider == null) {
				this.serialProvider = new SerialProvider(factorySvc, dbctx.registry);
			} else {
				//we want to keep the serial providers so don't generate ids already used
				this.serialProvider.setRegistry(dbctx.registry);
			}
		}

	}

	private Map<String,MemDBTable> tableMap = new ConcurrentHashMap<>();
	private Stuff stuff; //created lazily
	DateFormatService fmtSvc;
	private FactoryService factorySvc;
	protected Log log;
	protected ErrorTracker et;
	private DBCapabilties capabilities;
	public boolean createTablesAsNeededFlag = false;

	public MemDBInterface() {
		super();
		this.capabilities = new DBCapabilties(false, false, false, false);
	}

	@Override
	public DBCapabilties getCapabilities() {
		return capabilities;
	}

	@Override
	public void init(FactoryService factorySvc) {
		//TODO: we are re-initing, hopefully with same factorySvc
		this.factorySvc = factorySvc;
		this.log = factorySvc.getLog();
		this.et = factorySvc.getErrorTracker();
		this.fmtSvc = factorySvc.getDateFormatService();
	}

	@Override
	public DValue executeInsert(DValue dval, InsertContext ctx, DBAccessContext dbctx) {
		String typeName = dval.getType().getName();
		MemDBTable tbl = tableMap.get(typeName);
		if (tbl == null) {
			tbl = handleUnknownTable(typeName, dbctx);
		}

		DValue generatedId = addSerialValuesIfNeeded(dval, tbl, dbctx);
		checkUniqueness(dval, tbl, typeName, null, false, dbctx);

		tbl.rowL.add(dval);
		return generatedId;
	}

	private DValue addSerialValuesIfNeeded(DValue dval, MemDBTable tbl, DBAccessContext dbctx) {
		if (!dval.getType().isStructShape()) {
			return null;
		}
		DValue generatedId = null;
		DStructType structType = (DStructType) dval.getType();
		for(TypePair pair: structType.getAllFields()) {
			if (structType.fieldIsSerial(pair.name)) {
				if (dval.asStruct().getField(pair.name) != null) {
					DeliaError err = et.add("serial-value-cannot-be-provided", String.format("serial field '%s' must not have a value specified", pair.name));
					throw new DBException(err);
				}

				Stuff stuff = findOrCreateStuff(dbctx);

				DValue serialVal = stuff.serialProvider.generateSerialValue(structType, pair);
				dval.asMap().put(pair.name, serialVal);
				generatedId = serialVal;
				log.logDebug("serial id generated: %s", serialVal.asString());
			}
		}
		return generatedId;
	}

	/**
	 * Ugly. we need a serial provider per registry (really per runner i thinkg)
	 * TODO fix later
	 * @param ctx db context
	 * @return stuff
	 */
	private Stuff findOrCreateStuff(DBAccessContext ctx) {
		if (stuff == null) {
			stuff = new Stuff();
			stuff.init(factorySvc, ctx);
		}
		return stuff;
	}

	private void checkUniqueness(DValue dval, MemDBTable tbl, String typeName, DValue existing, boolean allowMissing, DBAccessContext dbctx) {
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


			spec.evaluator = new FilterEvaluator(factorySvc, dbctx.varEvaluator);
			spec.evaluator.init(spec.queryExp);
			QueryContext qtx = new QueryContext();
			QueryResponse qresp = executeQuery(spec, qtx, dbctx);
			if (qresp.ok && !qresp.emptyResults()) {
				DeliaError err = et.add("duplicate-unique-value", String.format("%s. row with unique field '%s' = '%s' already exists", structType.getName(), uniqueField, inner.asString()));
				throw new DBException(err);
			}
		}
	}

	@Override
	public QueryResponse executeQuery(QuerySpec spec, QueryContext qtx, DBAccessContext dbctx) {
		QueryResponse qresp = new QueryResponse();

		try {
			qresp = doExecuteQuery(spec, qtx, dbctx);
		} catch (InternalException e) {
			qresp.ok = false;
			qresp.err = e.getLastError();
		}

		return qresp;
	}

	private QueryResponse doExecuteQuery(QuerySpec spec, QueryContext qtx, DBAccessContext dbctx) {
		QueryResponse qresp = new QueryResponse();
		RowSelector selector = createSelector(spec, dbctx); 
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
					addAnyFKs(dval, dbctx);
				}
			}

			qresp.dvalList = dvalList;
			qresp.ok = true;
			return qresp;
		}
	}

	private void addAnyFKs(DValue dval, DBAccessContext dbctx) {
		DBExecutor dbexecutor = this.createExector(dbctx); //TODO handle close later
		FetchRunner fetchRunner = new FetchRunnerImpl(factorySvc, dbexecutor, dbctx.registry, dbctx.varEvaluator);
		ValidationRuleRunner ruleRunner = new ValidationRuleRunner(factorySvc, this.getCapabilities(), fetchRunner);
		ruleRunner.enableRelationModifier(true);
		ruleRunner.setPopulateFKsFlag(true);
		ruleRunner.validateRelationRules(dval);
	}

	private RowSelector createSelector(QuerySpec spec, DBAccessContext dbctx) {
		String typeName = spec.queryExp.getTypeName();
		MemDBTable tbl = tableMap.get(typeName);
		if (tbl == null) {
			tbl = handleUnknownTable(typeName, dbctx);
		}

		Stuff stuff = findOrCreateStuff(dbctx);
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
		DStructType dtype = findType(typeName, dbctx); 
		selector.init(et, spec, dtype, dbctx.registry); 
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
	private MemDBTable handleUnknownTable(String typeName, DBAccessContext dbctx) {
		if (createTablesAsNeededFlag) {
			createTable(typeName, dbctx);
			return tableMap.get(typeName);
		} else {
			DeliaError err = et.add("unknown-table-type", String.format("can't find type '%s'", typeName));
			throw new DBException(err);
		}
	}

	private DStructType findType(String typeName, DBAccessContext dbctx) {
		return dbctx.registry.findTypeOrSchemaVersionType(typeName);
	}

	@Override
	public void executeDelete(QuerySpec spec, DBAccessContext dbctx) {
		QueryResponse qresp = new QueryResponse();
		try {
			qresp = doExecuteDelete(spec, dbctx);
		} catch (InternalException e) {
			throw new DBException(e.getLastError());
			//				qresp.ok = false;
			//				qresp.err = e.getLastError();
		}
	}

	private QueryResponse doExecuteDelete(QuerySpec spec, DBAccessContext dbctx) {
		QueryResponse qresp = new QueryResponse();
		RowSelector selector = createSelector(spec, dbctx); //may throw
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

	//		@Override
	//		public void setRegistry(DTypeRegistry registry) {
	////			this.registry = registry;
	//			this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
	//			if (serialProvider == null) {
	//				this.serialProvider = new SerialProvider(factorySvc, registry);
	//			} else {
	//				//we want to keep the serial providers so don't generate ids already used
	//				this.serialProvider.setRegistry(registry);
	//			}
	//			
	//		}

	//		@Override
	//		public void setVarEvaluator(VarEvaluator varEvaluator) {
	////			this.varEvaluator = varEvaluator;
	//		}
	//		@Override
	//		public VarEvaluator getVarEvaluator() {
	//			return null; //varEvaluator;
	//		}

	@Override
	public boolean doesTableExist(String tableName, DBAccessContext dbctx) {
		return this.tableMap.containsKey(tableName);
	}

	@Override
	public boolean doesFieldExist(String tableName, String fieldName, DBAccessContext dbctx) {
		DStructType structType = findType(tableName, dbctx);
		if (structType == null) {
			return false;
		}
		
		return DValueHelper.fieldExists(structType, fieldName);
	}

	@Override
	public void createTable(String tableName, DBAccessContext dbctx) {
		tableMap.put(tableName, new MemDBTable());
	}
	@Override
	public void deleteTable(String tableName, DBAccessContext dbctx) {
		tableMap.remove(tableName);
	}

	@Override
	public void renameTable(String tableName, String newTableName, DBAccessContext dbctx) {
		MemDBTable tbl = tableMap.get(tableName);
		tbl.name = newTableName;
		tableMap.remove(tableName);
		tableMap.put(newTableName, tbl);
	}

	@Override
	public int executeUpdate(QuerySpec spec, DValue dvalUpdate, DBAccessContext dbctx) {
		int numRowsAffected = 0;

		try {
			numRowsAffected = doExecuteUpdate(spec, dvalUpdate, dbctx);
		} catch (InternalException e) {
			throw new DBException(e.getLastError());
			//				qresp.ok = false;
			//				qresp.err = e.getLastError();
		}
		return numRowsAffected;
	}

	private int doExecuteUpdate(QuerySpec spec, DValue dvalUpdate, DBAccessContext dbctx) {
		RowSelector selector = createSelector(spec, dbctx); //may throw
		MemDBTable tbl = selector.getTbl();
		List<DValue> dvalList = selector.match(tbl.rowL);
		String typeName = spec.queryExp.getTypeName();
		if (selector.wasError()) {
			DeliaError err = et.add("row-selector-error", String.format("xrow selector failed for type '%s'", typeName));
			throw new DBException(err);
		}

		if (CollectionUtils.isEmpty(dvalList)) {
			//nothing to do
			return 0;
		}

		//TODO: also need to validate with list. eq if two rows are setting same value
		for(DValue existing: dvalList) {
			checkUniqueness(dvalUpdate, tbl, typeName, existing, true, dbctx);
		}

		//TODO if dvalUpdate contains the primary key then do uniqueness check

		//update one or more matching dvals
		int numRowsAffected = dvalList.size();
		for(int i = 0; i < tbl.rowL.size(); i++) {
			DValue dd = tbl.rowL.get(i);
			//this is very inefficient if rowL large. TODO fix
			//if dd is one of the matching rows, then clone it and
			//replace it in tbl
			for(DValue tmp: dvalList) {
				if (tmp == dd) {
					DValue clone = DValueHelper.mergeOne(dvalUpdate, tmp);
					dvalList.remove(tmp);
					tbl.rowL.set(i, clone);
					break;
				}
			}

			if (dvalList.isEmpty()) {
				break; //no need to keep searching
			}
		}
		return numRowsAffected;
	}

	@Override
	public boolean isSQLLoggingEnabled() {
		return true;
	}
	@Override
	public void enableSQLLogging(boolean b) {
		//nothing to do
	}

	@Override
	public void createField(String typeName, String field, DBAccessContext dbctx) {
		//nothing to do
	}

	@Override
	public void deleteField(String typeName, String field, DBAccessContext dbctx) {
		//nothing to do
	}

	@Override
	public DBExecutor createExector(DBAccessContext ctx) {
		return new MemDBExecutor(this, ctx);
	}

	@Override
	public void enablePrintStackTrace(boolean b) {
		//nothing to do
	}

	@Override
	public DBType getDBType() {
		return DBType.MEM;
	}

	@Override
	public String getConnectionSummary() {
		return "";
	}

	@Override
	public void renameField(String typeName, String fieldName, String newName, DBAccessContext dbctx) {
		//nothing to do
	}

	@Override
	public void alterFieldType(String typeName, String fieldName, String newFieldType, DBAccessContext dbctx) {
		//nothing to do
	}

	@Override
	public void alterField(String typeName, String fieldName, String deltaFlags, DBAccessContext dbctx) {
		//nothing to do
	}

	@Override
	public void enumerateAllTables(Log logToUse) {
		//not supported
	}

	@Override
	public void enumerateAllConstraints(Log logToUse) {
		//not supported
	}
}