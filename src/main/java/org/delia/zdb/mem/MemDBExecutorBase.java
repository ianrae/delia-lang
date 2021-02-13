package org.delia.zdb.mem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBException;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.memdb.AllRowSelector;
import org.delia.db.memdb.MemDBTable;
import org.delia.db.memdb.OpRowSelector;
import org.delia.db.memdb.PrimaryKeyRowSelector;
import org.delia.db.memdb.RowSelector;
import org.delia.db.sql.QueryType;
import org.delia.error.DeliaError;
import org.delia.hld.HLDFactory;
import org.delia.hld.HLDQueryStatement;
import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.DValueImpl;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.validation.ValidationRunner;

public abstract class MemDBExecutorBase extends ServiceBase implements DBInternal {

	protected DTypeRegistry registry;
	protected Map<String,MemDBTable> tableMap;
	protected DBStuff stuff; //created lazily
	DateFormatService fmtSvc;
	public boolean createTablesAsNeededFlag = true;
	protected MemDBInterfaceFactory dbInterface;
	private PreSpecService preSpecSvc;
	protected HLDFactory hldFactory;

	public MemDBExecutorBase(FactoryService factorySvc, MemDBInterfaceFactory dbInterface, HLDFactory hldFactory) {
		super(factorySvc);
		this.dbInterface = dbInterface;
		this.hldFactory = hldFactory;
		this.tableMap = dbInterface.createSingleMemDB();
		this.log = factorySvc.getLog();
		this.et = factorySvc.getErrorTracker();
		this.fmtSvc = factorySvc.getDateFormatService();
		this.preSpecSvc = new PreSpecService(factorySvc, dbInterface);
	}

	protected QueryResponse doExecuteQuery(HLDQueryStatement hld, QueryContext qtx) {
		if (qtx.existingQResp != null) {
			return qtx.existingQResp;
		}
		QuerySpec spec = hld.querySpec;
		if (!hld.hldquery.throughChain.isEmpty()) {
			System.out.println("iiiii");
		}
		
		
		//TODO: delete this prequery. not needed now i think
		//avoid infinite loop
		int maxPreQueries = 10;
		QuerySpec preQuerySpec = null;
		QueryResponse qresp0 = null;
		for(int i = 0; i < maxPreQueries; i++) {
			preQuerySpec = preSpecSvc.getPreQuery(spec, preQuerySpec, qresp0);
			if (preQuerySpec == null) {
				break;
			}
			qresp0 = doSingleQuery(preQuerySpec, qtx);
		}
		
		QueryResponse qresp = doSingleQuery(spec, qtx);
		return qresp;
	}

	private QueryResponse doSingleQuery(QuerySpec spec, QueryContext qtx) {
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
//			if (qtx.loadFKs) {
//				for(DValue dval: dvalList) {
//					addAnyFKs(dval);
//				}
			if (qtx.pruneParentRelationFlag) {
				dvalList = removeParentSideRelations(dvalList);
			}

			qresp.dvalList = dvalList;
			qresp.ok = true;
			return qresp;
		}
	}
	protected List<DValue> removeParentSideRelations(List<DValue> dvalList) {
		List<DValue> list = new ArrayList<>();
		for(DValue dval: dvalList) {
			dval = removeParentSideRelationsOne(dval);
			list.add(dval);
		}

		return list;
	}
	protected DValue removeParentSideRelationsOne(DValue dval) {
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
					//MM have no parent so don't check isParent flag
//					if (manyRule.relInfo != null && manyRule.relInfo.isParent) {
					if (manyRule.relInfo != null) {
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

	protected void addAnyFKs(DValue dval) {
		FetchRunner fetchRunner = doCreateFetchRunner();
		ValidationRunner ruleRunner = factorySvc.createValidationRunner(dbInterface, registry, fetchRunner);
		ruleRunner.enableRelationModifier(true);
		ruleRunner.setPopulateFKsFlag(true);
		ruleRunner.validateRelationRules(dval);
	}

	protected RowSelector createSelector(QuerySpec spec) {
		String typeName = spec.queryExp.getTypeName();
		MemDBTable tbl = tableMap.get(typeName);
		if (tbl == null) {
			tbl = handleUnknownTable(typeName);
		}

		DBStuff stuff = findOrCreateStuff();
		RowSelector selector;
		QueryType queryType = stuff.queryDetectorSvc.detectQueryType(spec);
		switch(queryType) {
		case ALL_ROWS:
			selector = new AllRowSelector();
			break;
		case OP:
			selector = new OpRowSelector(fmtSvc, factorySvc, spec.evaluator);
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

	protected DStructType findType(String typeName) {
		DStructType structType = registry.findTypeOrSchemaVersionType(typeName);
		return structType;
	}
	
	protected void removeFetchedItems(DValue dval, String typeName, String fieldName) {
		DStructType targetStructType = (DStructType) registry.getType(typeName);
		if (targetStructType == null) {
			return;
		}
		
		for(DRule rule: dval.getType().getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				removeFromRelationFetchedItems(dval, rr.relInfo, targetStructType, fieldName);
			} else if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				removeFromRelationFetchedItems(dval, rr.relInfo, targetStructType, fieldName);
			}
		}
	}
	
	private void removeFromRelationFetchedItems(DValue dval, RelationInfo relInfo, DStructType structType, String fieldName) {
		if (DValueHelper.typesAreSame(relInfo.farType, structType)) {
			DValue inner = dval.asStruct().getField(relInfo.fieldName);
			if (inner != null) {
				DRelation drel = inner.asRelation();
				if (drel.haveFetched()) {
					for(DValue x: drel.getFetchedItems()) {
						removeFieldFromSingleDVal(x, fieldName, structType);
					}
				}
			}
		}
	}


	protected void removeFieldFromSingleDVal(DValue dval, String fieldName, DStructType structType) {
		for(TypePair pair: structType.getAllFields()) {
			if (pair.name.equals(fieldName)) {
				dval.asMap().remove(pair.name);
			}
		}
		DValueImpl dvalimpl = (DValueImpl) dval;
		dvalimpl.forceType(structType);
	}

	/**
	 * Ugly. we need a serial provider per registry (really per runner i thinkg)
	 * TODO fix later
	 * @param ctx db context
	 * @return stuff
	 */
	protected DBStuff findOrCreateStuff() {
		if (stuff == null) {
			stuff = new DBStuff();
			stuff.init(factorySvc, registry, dbInterface.getSerialMap());
		}
		return stuff;
	}

	public MemDBTable getTbl(String typeName) {
		MemDBTable tbl = tableMap.get(typeName);
		return tbl;
	}
}