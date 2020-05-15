package org.delia.zdb;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.delia.api.Delia;
import org.delia.assoc.DatIdMap;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.db.DBAccessContext;
import org.delia.db.DBCapabilties;
import org.delia.db.DBException;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.InsertContext;
import org.delia.db.InternalException;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.memdb.AllRowSelector;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.memdb.MemDBTable;
import org.delia.db.memdb.MemInsert;
import org.delia.db.memdb.MemUpdate;
import org.delia.db.memdb.MemUpsert;
import org.delia.db.memdb.OpRowSelector;
import org.delia.db.memdb.PrimaryKeyRowSelector;
import org.delia.db.memdb.RowSelector;
import org.delia.db.memdb.SerialProvider;
import org.delia.db.memdb.MemDBInterface.Stuff;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.dval.DValueExConverter;
import org.delia.error.DeliaError;
import org.delia.error.DetailedError;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.FetchRunner;
import org.delia.runner.FilterEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.BuiltInTypes;
import org.delia.type.DRelation;
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
import org.delia.valuebuilder.RelationValueBuilder;
import org.delia.zdb.core.ZDBConnection;
import org.delia.zdb.core.ZDBExecutor;
import org.delia.zdb.core.ZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class ZDBTests  extends NewBDDBase {
	
	public static class MemZDBInterfaceFactory extends ServiceBase implements ZDBInterfaceFactory {
		private DBCapabilties capabilities;

		public MemZDBInterfaceFactory(FactoryService factorySvc) {
			super(factorySvc);
			this.capabilities = new DBCapabilties(false, false, false, false);
			this.capabilities.setRequiresTypeReplacementProcessing(true);
		}

		@Override
		public DBType getDBType() {
			return DBType.MEM;
		}

		@Override
		public DBCapabilties getCapabilities() {
			return capabilities;
		}

		@Override
		public ZDBConnection openConnection() {
			return null; //no connection for MEM
		}

		@Override
		public boolean isSQLLoggingEnabled() {
			return false;
		}

		@Override
		public void enableSQLLogging(boolean b) {
			//not supported
		}
	}
	
	public static class ZMemInsert extends ServiceBase {

		DateFormatService fmtSvc;

		public ZMemInsert(FactoryService factorySvc) {
			super(factorySvc);
			this.fmtSvc = factorySvc.getDateFormatService();
		}

		public DValue doExecuteInsert(MemDBTable tbl, DValue dval, InsertContext ctx, MemZDBExecutor executor, ZStuff stuff) {
			String typeName = dval.getType().getName();

			DValue generatedId = addSerialValuesIfNeeded(dval, tbl, stuff);
			executor.checkUniqueness(dval, tbl, typeName, null, false);

			tbl.rowL.add(dval);
			return generatedId;
		}

		private DValue addSerialValuesIfNeeded(DValue dval, MemDBTable tbl, ZStuff stuff) {
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

					DValue serialVal = stuff.serialProvider.generateSerialValue(structType, pair);
					dval.asMap().put(pair.name, serialVal);
					generatedId = serialVal;
					log.logDebug("serial id generated: %s", serialVal.asString());
				}
			}
			return generatedId;
		}
		
	}	
	
	
	//per database stuff
	public static class ZStuff {
		public SerialProvider serialProvider;
		public QueryTypeDetector queryDetectorSvc;

		public void init(FactoryService factorySvc, DTypeRegistry registry) {
			this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
			if (serialProvider == null) {
				this.serialProvider = new SerialProvider(factorySvc, registry);
			} else {
				//we want to keep the serial providers so don't generate ids already used
				this.serialProvider.setRegistry(registry);
			}
		}

	}

	public static class MemZDBExecutor extends ServiceBase implements ZDBExecutor {

		private DTypeRegistry registry;
		private DatIdMap datIdMap;
		private VarEvaluator varEvaluator;
		
		private Map<String,MemDBTable> tableMap = new ConcurrentHashMap<>();
		private ZStuff stuff; //created lazily
		DateFormatService fmtSvc;
		public boolean createTablesAsNeededFlag = false;
		private MemZDBInterfaceFactory dbInterface;
		

		public MemZDBExecutor(FactoryService factorySvc, MemZDBInterfaceFactory dbInterface) {
			super(factorySvc);
			this.dbInterface = dbInterface;
			this.log = factorySvc.getLog();
			this.et = factorySvc.getErrorTracker();
			this.fmtSvc = factorySvc.getDateFormatService();
		}

		@Override
		public ZDBConnection getDBConnection() {
			return null; //none for MEM
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
			return null; //TODO fix
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
			// TODO Auto-generated method stub
			
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
			// TODO Auto-generated method stub
			return null;
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteField(String typeName, String field, int datId) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void renameField(String typeName, String fieldName, String newName) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void alterFieldType(String typeName, String fieldName, String newFieldType) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void alterField(String typeName, String fieldName, String deltaFlags) {
			// TODO Auto-generated method stub
			
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
		
	}
	
	
	//========
	public static class ZMemUpdate extends ServiceBase {

		DateFormatService fmtSvc;
		private DTypeRegistry registry;

		public ZMemUpdate(FactoryService factorySvc, DTypeRegistry registry) {
			super(factorySvc);
			this.registry = registry;
			this.fmtSvc = factorySvc.getDateFormatService();
		}

		public int doExecuteUpdate(QuerySpec spec, DValue dvalUpdate, Map<String, String> assocCrudMap, RowSelector selector, MemZDBExecutor memDBInterface) {
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
				memDBInterface.checkUniqueness(dvalUpdate, tbl, typeName, existing, true);
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
						DValue clone = DValueHelper.mergeOne(dvalUpdate, tmp, assocCrudMap);
						if (assocCrudMap != null) {
							doAssocCrud(dvalUpdate, clone, assocCrudMap);
						}
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

		private void doAssocCrud(DValue dvalUpdate, DValue clone, Map<String, String> assocCrudMap) {
			for(String fieldName: assocCrudMap.keySet()) {
				DRelation src = dvalUpdate.asStruct().getField(fieldName).asRelation();
				DRelation dest = getOrCreateRelation(clone, fieldName); 
				
				String action = assocCrudMap.get(fieldName);
				switch(action) {
				case "insert":
					dest.getMultipleKeys().addAll(src.getMultipleKeys());
				break;
				case "update":
					doUpdate(src, dest);
				break;
				case "delete":
				{
					for(DValue fk: src.getMultipleKeys()) {
						DValue srcFK = findIn(dest, fk);
						if (srcFK != null) {
							dest.getMultipleKeys().remove(srcFK);
						}
					}
					//empty relation not allowed, so delete entire relation if mepty
					if (dest.getMultipleKeys().isEmpty()) {
						clone.asMap().remove(fieldName);
					}
				}
				break;
				default:
				break;
				}
			}
		}
		
		private DRelation getOrCreateRelation(DValue clone, String fieldName) {
			DValue tmp = clone.asStruct().getField(fieldName);
			if (tmp == null) {
				DValue dval = createRelation(clone, fieldName);
				clone.asMap().put(fieldName, dval);
				return dval.asRelation();
			} else {
				return tmp.asRelation();
			}
		}

		//create empty relation
		private DValue createRelation(DValue clone, String fieldName) {
			DType relType = registry.getType(BuiltInTypes.RELATION_SHAPE);
			DType farEndType = DValueHelper.findFieldType(clone.getType(), fieldName);
			String typeName = farEndType.getName();
			RelationValueBuilder builder = new RelationValueBuilder(relType, typeName, registry);
			builder.buildEmptyRelation();
			boolean b = builder.finish();
			if (!b) {
				DeliaExceptionHelper.throwError("relation-create-failed-assocCrud", "Type '%s': Failed to create empty relation", typeName);
				return null;
			} else {
				DValue dval = builder.getDValue();
				return dval;
			}
		}

		private DValue findIn(DRelation drelDest, DValue target) {
			String s2 = target.asString();
			for(DValue dval: drelDest.getMultipleKeys()) {
				//str compare for now
				String s1 = dval.asString();
				if (s1.equals(s2)) {
					return dval;
				}
			}
			return null;
		}

		private void doUpdate(DRelation drelSrc, DRelation drelDest) {
			//process pairs (old,new)
			for(int i = 0; i < drelSrc.getMultipleKeys().size(); i += 2) {
				DValue currentVal = drelSrc.getMultipleKeys().get(i);
				DValue newVal = drelSrc.getMultipleKeys().get(i+1);
				
				DValue existingVal = findIn(drelDest, currentVal);
				if (existingVal != null) {	
					drelDest.getMultipleKeys().remove(existingVal);
					drelDest.getMultipleKeys().add(newVal);
				}
			}
		}
	}	
	public static class ZMemUpsert extends ServiceBase {

		DateFormatService fmtSvc;
		private DTypeRegistry registry;

		public ZMemUpsert(FactoryService factorySvc, DTypeRegistry registry) {
			super(factorySvc);
			this.registry = registry;
		}

		public int doExecuteUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap, boolean noUpdateFlag, RowSelector selector, 
					MemZDBExecutor memDBInterface, ZStuff stuff) {
			MemDBTable tbl = selector.getTbl();
			List<DValue> dvalList = selector.match(tbl.rowL);
			String typeName = spec.queryExp.getTypeName();
			if (selector.wasError()) {
				DeliaError err = et.add("row-selector-error", String.format("row selector failed for type '%s'", typeName));
				throw new DBException(err);
			}
			if (dvalList.size() > 1) {
				DeliaError err = et.add("upsert-unique-violation", String.format("upsert filter must specify one row (at most). %d rows matched for type '%s'", dvalList.size(), typeName));
				throw new DBException(err);
			}
			if (spec.queryExp.filter.cond instanceof BooleanExp) {
				DeliaExceptionHelper.throwError("upsert-filter-error", "[true] not supported");
			} else if (spec.queryExp.filter.cond instanceof FilterOpFullExp) {
				DeliaExceptionHelper.throwError("upsert-filter-error", "only primary key filters are supported");
			}

			if (CollectionUtils.isEmpty(dvalList)) {
				//add primary key to dvalFull
				addPrimaryKey(spec, dvalFull, selector);
				
				ZMemInsert memInsert = new ZMemInsert(factorySvc);
				InsertContext ctx = new InsertContext(); //upsert not supported for serial primaryKey
				memInsert.doExecuteInsert(tbl, dvalFull, ctx, memDBInterface, stuff);
				return 1;
			} else if (noUpdateFlag) {
				return 0; //don't update
			}

			//TODO: also need to validate with list. eq if two rows are setting same value
			for(DValue existing: dvalList) {
				memDBInterface.checkUniqueness(dvalFull, tbl, typeName, existing, true);
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
						DValue clone = DValueHelper.mergeOne(dvalFull, tmp);
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

		private void addPrimaryKey(QuerySpec spec, DValue dvalFull, RowSelector selector) {
			TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(dvalFull.getType());
			if (dvalFull.asStruct().getField(keyPair.name) != null) {
				return; //already has primary key
			}
			
			FilterEvaluator evaluator = spec.evaluator;
			DValueExConverter dvalConverter = new DValueExConverter(factorySvc, registry);
			String keyField = evaluator.getRawValue(); //we assume primary key. eg Customer[55]
			DValue inner = dvalConverter.buildFromObject(keyField, keyPair.type);

			Map<String, DValue> map = dvalFull.asMap();
			map.put(keyPair.name, inner);
		}
	}	
	
	
	
	
	@Test
	public void testTool() {
		assertEquals(1,2);
	}
	
	// --
	private DeliaDao dao;
	private Delia delia;

	@Before
	public void init() {
		this.dao = createDao();
		this.delia = dao.getDelia();
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
}
