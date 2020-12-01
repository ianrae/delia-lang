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

		public ColumnRun(int i, DStructType dtype) {
			this.iStart = i;
			this.dtype = dtype;
		}
	}
	
	public ResultSetConverter(DBType dbType, FactoryService factorySvc, ConnectionFactory connFactory, SqlHelperFactory sqlhelperFactory) {
		super(dbType, factorySvc, connFactory, sqlhelperFactory);
	}
	public ResultSetConverter(FactoryService factorySvc, ValueHelper valueHelper) {
		super(factorySvc, valueHelper);
	}

	public void init(FactoryService factorySvc) {
		super.init(factorySvc);;
	}

	@Override
	public List<DValue> buildDValueList(ResultSet rs, DStructType dtype, QueryDetails details, DBAccessContext dbctx, HLSQueryStatement hls) {
		if (hls == null) {
			return super.buildDValueList(rs, dtype, details, dbctx, hls);
		}
		
		ResultSetWrapper rsw = new ResultSetWrapper(rs, valueHelper, logResultSetDetails, log);
		List<DValue> list = null;
		try {
			list = newBuildDValueList(rsw, dtype, dbctx, hls);
			if (details.mergeRows) {
				if (details.isManyToMany) {
					list = mergeRowsManyToMany(list, dtype, details, dbctx);
				} else {
					list = mergeRowsOneToMany(list, dtype, details, dbctx);
				}
			}
		} catch (ValueException e) {
			ValueException ve = (ValueException)e;
			throw new DBException(ve.errL);
		} catch (Exception e) {
						e.printStackTrace();
			DeliaError err = new DeliaError("db-resultset-error", e.getMessage());
			throw new DBException(err);
		}
		
		chkObjects(list, "addr");
		return list;
	}
	
	
	private List<DValue> newBuildDValueList(ResultSetWrapper rsw, DStructType dtype, DBAccessContext dbctx, HLSQueryStatement hls) throws Exception {
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
					addAsSubOjbectX(dval, subDVal, columnRun, dbctx);
				}
			}
		}

		return list;
	}
	
	private List<ColumnRun> buildColumnRuns(DStructType dtype, List<RenderedField> rfList) {
		List<ColumnRun> resultL = new ArrayList<>();
		ColumnRun run = new ColumnRun(0, dtype);
		resultL.add(run);
		
		DStructType currentType = dtype;
		int iEnd = 0;
		for(int i = 0; i < rfList.size(); i++) {
			RenderedField rff = rfList.get(i);
			
			DStructType tmp = getFieldStructType(rff, currentType);
			if (tmp == currentType) {
				iEnd = i;
			} else {
				copyToRunList(run, iEnd, rfList);
				
				run = new ColumnRun(i, tmp);
				resultL.add(run);
				currentType = tmp;
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
					structBuilder.addField(rff.pair.name, null);
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
				structBuilder.addField(rff.pair.name, inner);
			}
		}
		
		boolean b = structBuilder.finish();
		if (! b) {
			throw new ValueException(structBuilder.getValidationErrors()); 
		}
		DValue dval = structBuilder.getDValue();
		return dval;
	}
	protected void addAsSubOjbectX(DValue dval, DValue subDVal, ColumnRun columnRun, DBAccessContext dbctx) {
		//rff is something like b.id as addr
		RenderedField rff = columnRun.runList.get(0);
		String fieldName = RenderedFieldHelper.getAssocFieldName(rff);
		
		//setting dval's relation (fieldName) to have subDVal
		DValue inner = dval.asStruct().getField(fieldName);
		DRelation drel = inner.asRelation();
		DRelationHelper.addToFetchedItems(drel, subDVal);
		
//		TypePair tp = new TypePair(fieldName, null); //type part not needed;
//		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo((DStructType) dval.getType(), tp);
//		if (relinfo.isManyToMany()) {
//			//do the inverse. setting subDVal's relation to have dval
//			String otherField = relinfo.otherSide.fieldName;
//			DValue inner2 = subDVal.asStruct().getField(otherField);
//			if (inner2 == null) {
//				inner2 = this.createEmptyRelation(dbctx, (DStructType) subDVal.getType(), otherField);
//				subDVal.asMap().put(otherField, inner2);
//			}
//			drel = inner2.asRelation();
//			
//			DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
//			this.log.log("xx %s", pkval.asString());
//			drel.addKey(pkval);
//			DRelationHelper.addToFetchedItems(drel, dval);
//		}
	}

	/**
	 * On a Many-to-many relation our query returns multiple rows in order to get all
	 * the 'many' ids.
	 * @param rawList list of dvalues to merge
	 * @param dtype of values
	 * @param details query details
	 * @param dbctx 
	 * @return merged rows
	 */
	private List<DValue> mergeRowsManyToMany(List<DValue> rawList, DStructType dtype, QueryDetails details, DBAccessContext dbctx) {
		Map<Object,DValue> pkMap = new HashMap<>(); //pk,dval
		PrimaryKey pkType = dtype.getPrimaryKey();
		String pkField = pkType.getFieldName();
		
		Map<Object,DValue> subMap = new HashMap<>(); //pk,dval
		
		for(DValue dval: rawList) {
			DValue pkval = dval.asStruct().getField(pkField); 
			Object key = pkval.getObject();
			if (! pkMap.containsKey(key)) {
				pkMap.put(key, dval);
			} else {
				DValue mergeToVal = pkMap.get(key);
				for(String mergeOnField: details.mergeOnFieldL) {
					DValue inner1 = mergeToVal.asStruct().getField(mergeOnField);
					DValue inner2 = dval.asStruct().getField(mergeOnField);
					if (inner2 != null) {
						if (inner1 == null) {
							inner1 = this.createEmptyRelation(dbctx, dtype, mergeOnField);
							mergeToVal.asMap().put(mergeOnField, inner1);
						}
						DRelation drel2 = inner2.asRelation();
						addToSubMap(drel2, subMap);
						
						for(DValue fkval: drel2.getMultipleKeys()) {
							if (! alreadyExist(inner1, fkval)) {
								inner1.asRelation().addKey(fkval);
								addToFetchedItemsFromRelationX(inner1, drel2, subMap);
							}
						}
						//TODO: add config flag for this. it's good for tests but slows perf
						DRelationHelper.sortFKs(inner1.asRelation());
					}
				}
			}
		}
		
		//build output list. keep same order
		List<DValue> list = new ArrayList<>();
		for(DValue dval: rawList) {
			DValue pkval = dval.asStruct().getField(pkField); 
			Object key = pkval.getObject();
			if (pkMap.containsKey(key)) {
				list.add(dval);
				pkMap.remove(key);
			}
		}
		
		for(DValue dval: list) {
			for(String relField: details.mergeOnFieldL) {
				fillInParentSideRelation(dval, relField, dbctx);
			}
		}		
		return list;
	}

	private void addToSubMap(DRelation drel2, Map<Object, DValue> subMap) {
		for(DValue dd: drel2.getFetchedItems()) {
			DValue pkval = DValueHelper.findPrimaryKeyValue(dd);
			Object key = pkval.getObject();
			if (! subMap.containsKey(key)) {
				subMap.put(key, dd);
			}
		}
	}
	public void addToFetchedItemsFromRelationX(DValue inner1, DRelation drel2, Map<Object, DValue> subMap) {
		if (! drel2.haveFetched()) {
			return;
		}
		
		for(DValue dval: drel2.getFetchedItems()) {
			DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
			Object key = pkval.getObject();
			DValue subObj = subMap.get(key);
			if (subObj == null) {
				System.out.println("ssssssssssssss");
			}
			DRelation drel1 = inner1.asRelation();
			DRelationHelper.addToFetchedItems(drel1, subObj);
		}
	}
	
	
	protected void fillInParentSideRelation(DValue dval, String relField, DBAccessContext dbctx) {
		String fieldName = relField;
		
		TypePair tp = DValueHelper.findField(dval.getType(), relField);
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo((DStructType) dval.getType(), tp);
		if (relinfo.isManyToMany()) {
			DValue inner = dval.asStruct().getField(fieldName);
			DRelation drel = inner.asRelation();

			String otherField = relinfo.otherSide.fieldName;
			for(DValue subval: drel.getFetchedItems()) {
				DValue inner2 = subval.asStruct().getField(otherField);
				if (inner2 == null) {
					inner2 = this.createEmptyRelation(dbctx, (DStructType) subval.getType(), otherField);
					subval.asMap().put(otherField, inner2);
				}
				drel = inner2.asRelation();
				
				DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
				this.log.log("xx %s", pkval.asString());
				drel.addKey(pkval);
				DRelationHelper.addToFetchedItems(drel, dval);
			}
		}
	}
	
	
	protected void chkObjects(List<DValue> list, String relField, String backField) {
		int id = 100;
		for(DValue dval: list) {
			DValueImpl impl = (DValueImpl) dval;
			if (impl.getPersistenceId() == null) {
				impl.setPersistenceId(id++);
			}
			log.log("%s: %d", dval.getType().getName(), dval.getPersistenceId());

			DValue inner = dval.asStruct().getField(relField);
			DRelation rel = inner.asRelation();
			for(DValue xx: rel.getFetchedItems()) {
				impl = (DValueImpl) xx;
				if (impl.getPersistenceId() == null) {
					impl.setPersistenceId(id++);
				}
				log.log("  %s: %d", impl.getType().getName(), impl.getPersistenceId());
			}
		}
	}
	
	
}