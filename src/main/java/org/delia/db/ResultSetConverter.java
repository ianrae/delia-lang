package org.delia.db;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.collections.CollectionUtils;
import org.delia.core.FactoryService;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.RenderedField;
import org.delia.db.hls.RenderedFieldHelper;
import org.delia.db.hls.join.FieldGroup;
import org.delia.db.hls.join.JTElement;
import org.delia.db.sql.ConnectionFactory;
import org.delia.dval.DRelationHelper;
import org.delia.error.DeliaError;
import org.delia.relation.RelationInfo;
import org.delia.runner.ValueException;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.DValueImpl;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.StructValueBuilder;

/**
 * @author Ian Rae
 *
 * A newer algorithm using HLS and Rendered fields. We read fields by column index in the order specified in SQL statement.
 *
 */
public class ResultSetConverter extends ResultSetToDValConverter {
	
	static class ColumnRun {
		public DStructType dtype;
		public List<RenderedField> runList = new ArrayList<>();
		public int iStart;
		public FieldGroup fieldGroup;

		public ColumnRun(int i, DStructType dtype) {
			this.iStart = i;
			this.dtype = dtype;
		}
		
		public JTElement getJTElementIfExist() {
			if (fieldGroup != null) {
				return fieldGroup.el;
			}
			return null;
		}
	}
	
	static class ObjectPool {
		private Map<String,DValue> map = new HashMap<>(); //Customer.55 is key
		
		public void add(DValue dval) {
			String key = makeKey(dval);
			if (map.containsKey(key)) {
				//harvest the fks
				DStructType dtype = (DStructType) dval.getType();
				for(TypePair pair: dtype.getAllFields()) {
					if (pair.type.isStructShape()) {
						DValue inner = dval.asStruct().getField(pair.name);
						if (inner != null) {
							DRelation drel = inner.asRelation();
							addForeignKeys(key, pair, drel);
						}
					}
				}
			} else {
				map.put(key, dval);
			}
		}

		private void addForeignKeys(String key, TypePair pair, DRelation drelSrc) {
			DValue current = map.get(key);
			DValue inner = current.asStruct().getField(pair.name);
			
			//when doing fks() and are multiple relations then we load each one at a time, and one might be missing
			if (inner != null) {
				DRelation drelTarget = inner.asRelation();
				
				for(DValue srcval: drelSrc.getMultipleKeys()) {
					if (drelTarget.findMatchingKey(srcval) == null) { //avoid duplicates
						drelTarget.addKey(srcval);
					}
				}
			}
		}

		private String makeKey(DValue dval) {
			DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
			return makeKey(dval.getType(), pkval);
		}
		private String makeKey(DType dtype, DValue pkval) {
			String key = String.format("%s.%s", dtype.getName(), pkval.asString());
			return key;
		}

		public boolean contains(DValue dval) {
			String key = makeKey(dval);
			DValue current = map.get(key);
			return current == dval;
		}

		//should always be a match
		public DValue findMatch(DType dtype, DValue pkval) {
			String key = makeKey(dtype, pkval);
			return map.get(key);
		}
	}
	
	public ResultSetConverter(DBType dbType, FactoryService factorySvc, ConnectionFactory connFactory, SqlHelperFactory sqlhelperFactory) {
		super(dbType, factorySvc, connFactory, sqlhelperFactory);
	}
	public ResultSetConverter(FactoryService factorySvc, ValueHelper valueHelper) {
		super(factorySvc, valueHelper);
	}

	public void init(FactoryService factorySvc) {
		super.init(factorySvc);
	}

	@Override
	public List<DValue> buildDValueList(ResultSet rs, DStructType dtype, QueryDetails details, DBAccessContext dbctx, HLSQueryStatement hls) {
		if (hls == null) { //TODO: or if is select *
			return super.buildDValueList(rs, dtype, details, dbctx, hls);
		} else if (!hls.hlspanL.isEmpty() && !hls.hlspanL.get(0).renderedFieldL.isEmpty()) {
			boolean isStarQuery = hls.hlspanL.get(0).renderedFieldL.get(0).field.equals("*");
			if (isStarQuery) {
				return super.buildDValueList(rs, dtype, details, dbctx, hls);
			}
		}
		
		ResultSetWrapper rsw = new ResultSetWrapper(rs, valueHelper, logResultSetDetails, log);
		List<DValue> list = null;
		ObjectPool pool = new ObjectPool();
		try {
			list = doBuildDValueList(rsw, dtype, dbctx, hls, pool);
		} catch (ValueException e) {
			ValueException ve = (ValueException)e;
			throw new DBException(ve.errL);
		} catch (Exception e) {
						e.printStackTrace();
			DeliaError err = new DeliaError("db-resultset-error", e.getMessage());
			throw new DBException(err);
		}
		
		chkObjects(list, "addr", "cust"); //TODO remove
		return list;
	}
	
	
	private List<DValue> doBuildDValueList(ResultSetWrapper rsw, DStructType dtype, DBAccessContext dbctx, HLSQueryStatement hls, ObjectPool pool) throws Exception {
		List<DValue> list = new ArrayList<>();

		List<RenderedField> rfList = null;
		if (hls != null) {
			RenderedFieldHelper.logRenderedFieldList(hls, log);
			rfList = hls.getRenderedFields();
		}
		
		RenderedField rf = CollectionUtils.isEmpty(rfList) ? null : rfList.get(0);
		if (rf != null) {
			//add column indexes
			int j = 1;
			for(RenderedField rff: rfList) {
				rff.columnIndex = j++;;
			}
		}		
		
		List<ColumnRun> columnRunL = buildColumnRuns(dtype, rfList);
		
		while(rsw.next()) {  //get row
			//do main type
			ColumnRun mainRun = columnRunL.get(0);
			DValue dval = readStructDValueX(mainRun, rsw, dbctx);
			list.add(dval);
			
			//do remaining column runs
			for(int i = 1; i < columnRunL.size(); i++) {
				ColumnRun columnRun = columnRunL.get(i);
				DValue subDVal = readStructDValueX(columnRun, rsw, dbctx);
				if (subDVal != null) {
					addAsSubObjectX(dval, subDVal, columnRun, dbctx);
					pool.add(subDVal);
				}
			}
			pool.add(dval);
		}

		list = mergeRows(list, pool, columnRunL);
		return list;
	}
	
	private List<ColumnRun> buildColumnRuns(DStructType dtype, List<RenderedField> rfList) {
		List<ColumnRun> resultL = new ArrayList<>();
		ColumnRun run = new ColumnRun(0, dtype);
		run.fieldGroup = new FieldGroup(true, null); //main group
		resultL.add(run);
		
		DStructType currentType = dtype;
		String currentKey = run.fieldGroup.getUniqueKey();
		int iEnd = 0;
		for(int i = 0; i < rfList.size(); i++) {
			RenderedField rff = rfList.get(i);
			
			DStructType tmp = getFieldStructType(rff, currentType);
			String tmpKey = rff.fieldGroup.getUniqueKey();
			if (tmpKey.equals(currentKey)) {
				iEnd = i;
			} else {
				copyToRunList(run, iEnd, rfList);
				
				run = new ColumnRun(i, tmp);
				run.fieldGroup = rff.fieldGroup;
				resultL.add(run);
				currentType = tmp;
				currentKey = tmpKey;
				iEnd = i;
			}
		}
		copyToRunList(run, iEnd, rfList);
		
		return resultL;
	}
	private void copyToRunList(ColumnRun run, int iEnd, List<RenderedField> rfList) {
		List<RenderedField> tmpL = rfList.subList(run.iStart, iEnd+1);
		run.runList.addAll(tmpL);
	}
	private DStructType getFieldStructType(RenderedField rff, DStructType currentType) {
		if (rff.structType != null) {
			return rff.structType;
		} else {
			//alias
			String fieldName = RenderedFieldHelper.getAssocFieldName(rff);
			DType type = DValueHelper.findFieldType(currentType, fieldName);
			if (type != null) {
				return currentType;
			}
			return (DStructType)type; //is this right?
		}
	}
	
	private DValue readStructDValueX(ColumnRun columnRun, ResultSetWrapper rsw, DBAccessContext dbctx) throws Exception {
		DStructType dtype = columnRun.dtype;
		StructValueBuilder structBuilder = new StructValueBuilder(dtype);
		PrimaryKey pk = dtype.getPrimaryKey();
		
		for(RenderedField rff: columnRun.runList) {
			if (rff.pair.type == null) {
				String fld = RenderedFieldHelper.getAssocFieldName(rff);
				DType ddd = DValueHelper.findFieldType(dtype, fld);
				if (! ddd.isStructShape()) {
					DeliaError err = new DeliaError("db-resultset-error-assoc", String.format("type %s has no relation field %s",  dtype.getName(), fld));
					throw new DBException(err);
				}
				
				RenderedField copyrff = new RenderedField(rff);
				copyrff.pair.name = fld;
				copyrff.pair.type = ddd;
				
				//FK only goes in child so it may not be here.
				//However if .fks() was used then it is
				String strValue = rsw.getString(rff.columnIndex);
				if (strValue == null) {
					structBuilder.addField(fld, null);
				} else {
					TypePair fieldPair = new TypePair(fld, ddd);
					DValue inner = createRelation(dtype, fieldPair, strValue, dbctx, copyrff);
					structBuilder.addField(fieldPair.name, inner);
				}
			} else {
				DValue inner = rsw.readFieldByColumnIndex(rff.pair, rff.columnIndex, dbctx);
				if (inner == null && rff.pair.name.equals(pk.getFieldName())) {
					//is optional relation and is null
					return null;
				}
				
				//handle relation. inner is the pkval
				if (inner != null && rff.pair.type.isStructShape()) {
					DValue pkval = inner;
//					inner = this.createEmptyRelation(dbctx, (DStructType) rff.pair.type, rff.pair.name);
					inner = this.createEmptyRelation(dbctx, (DStructType) rff.structType, rff.pair.name);
					DRelation drel = inner.asRelation();
					drel.addKey(pkval);
				}
				structBuilder.addField(rff.pair.name, inner);
			}
		}
		
		boolean b = structBuilder.finish();
		if (! b) {
			JTElement el = columnRun.getJTElementIfExist();
			boolean needAllColumns = el == null ? true : !el.usedForFK;
			//if we're doing .fks() then are only getting pk, not all the columns
			//TODO: only ignore missing field errors. other types of validation errors should still be thrown!
			if (needAllColumns) {
				throw new ValueException(structBuilder.getValidationErrors()); 
			}
		}
		DValue dval = structBuilder.getDValue();
		return dval;
	}
	protected void addAsSubObjectX(DValue dval, DValue subDVal, ColumnRun columnRun, DBAccessContext dbctx) {
		//rff is something like b.id as addr
		RenderedField rff = columnRun.runList.get(0);
		String fieldName = RenderedFieldHelper.getAssocFieldName(rff);
		
		//setting dval's relation (fieldName) to have subDVal
		DRelation drel = getOrCreateRelation(dval, fieldName, subDVal, dbctx);
		
		TypePair tp = new TypePair(fieldName, null); //type part not needed;
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo((DStructType) dval.getType(), tp);
//		if (relinfo.isManyToMany()) {
			//do the inverse. setting subDVal's relation to have dval
			String otherField = relinfo.otherSide.fieldName;
			drel = getOrCreateRelation(subDVal, otherField, dval, dbctx);
//		}
	}

	private DRelation getOrCreateRelation(DValue subDVal, String otherField, DValue parentDVal, DBAccessContext dbctx) {
		DValue inner2 = subDVal.asStruct().getField(otherField);
		if (inner2 == null) {
			inner2 = this.createEmptyRelation(dbctx, (DStructType) subDVal.getType(), otherField);
			subDVal.asMap().put(otherField, inner2);
			DRelation drel = inner2.asRelation();
			
			DValue pkval = DValueHelper.findPrimaryKeyValue(parentDVal);
			this.log.log("xx %s", pkval.asString());
			drel.addKey(pkval);
		}
		return inner2.asRelation();
	}
	/**
	 * On a Many-to-many relation our query returns multiple rows in order to get all
	 * the 'many' ids.
	 * @param rawList list of dvalues to merge
	 * @param columnRunL 
	 * @param dtype 
	 * @param dtype of values
	 * @param details query details
	 * @param dbctx 
	 * @return merged rows
	 */
	private List<DValue> mergeRows(List<DValue> rawList, ObjectPool pool, List<ColumnRun> columnRunL) {
		//build output list. keep same order
		List<DValue> resultList = new ArrayList<>();
		for(DValue dval: rawList) {
			if (! pool.contains(dval)) {
				continue;
			}
			resultList.add(dval);
			fillInFetchedItems(dval, pool, true, columnRunL);
		}
		
		return resultList;
	}

	private void fillInFetchedItems(DValue dval, ObjectPool pool, boolean doInner, List<ColumnRun> columnRunL) {
		DStructType dtype = (DStructType) dval.getType();
		for(TypePair pair: dtype.getAllFields()) {
			if (pair.type.isStructShape()) {
				DValue inner = dval.asStruct().getField(pair.name);
				if (inner != null) {
					DRelation drel = inner.asRelation();
					if (!isAFetchedColumn(dtype, pair, columnRunL)) {
						continue;
					}
					
					for(DValue pkval: drel.getMultipleKeys()) {
						DValue foreignVal = pool.findMatch(pair.type, pkval);
						if (foreignVal != null) { //can be null if only doing fks()
							DRelationHelper.addToFetchedItems(drel, foreignVal);
							if (doInner) {
								fillInFetchedItems(foreignVal, pool, false, columnRunL); //** recursion **
							}
						}
					}
				}
			}
		}
	}
	
	private boolean isAFetchedColumn(DStructType dtype, TypePair pair, List<ColumnRun> columnRunL) {
		for(ColumnRun run: columnRunL) {
			if (run.fieldGroup != null && run.fieldGroup.el != null) {
				JTElement el = run.fieldGroup.el;
				if (el.dtype == dtype && el.fieldName.equals(pair.name)) {
					return el.usedForFetch;
				}
			}
		}
		return false;
	}
	protected void chkObjects(List<DValue> list, String relField, String backField) {
		int id = 100;
		for(DValue dval: list) {
			DValueImpl impl = (DValueImpl) dval;
			if (impl.getPersistenceId() == null) {
				impl.setPersistenceId(id++);
			}
			log.log("%s: %d", dval.getType().getName(), dval.getPersistenceId());

			id = chkSubObj(dval, relField, id, backField);
		}
	}
	private int chkSubObj(DValue dval, String relField, int id, String backField) {
		DValue inner = dval.asStruct().getField(relField);
		if (inner == null) {
			return -1;
		}
		DRelation rel = inner.asRelation();
		if (!rel.haveFetched()) {
			return -1;
		}
		for(DValue xx: rel.getFetchedItems()) {
			DValueImpl impl = (DValueImpl) xx;
			if (impl.getPersistenceId() == null) {
				impl.setPersistenceId(id++);
			}
			log.log("  %s: %d", impl.getType().getName(), impl.getPersistenceId());
			
			if (backField != null) {
				id = chkSubObj(xx, backField, id, null);
			}
		}
		return id;
	}
	
	
}