package org.delia.db.hld;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBException;
import org.delia.db.DBType;
import org.delia.db.ResultSetWrapper;
import org.delia.db.SqlHelperFactory;
import org.delia.db.ValueHelper;
import org.delia.db.newhls.HLDField;
import org.delia.db.newhls.HLDQueryStatement;
import org.delia.db.newhls.JoinElement;
import org.delia.db.sql.ConnectionFactory;
import org.delia.dval.DRelationHelper;
import org.delia.error.DeliaError;
import org.delia.relation.RelationInfo;
import org.delia.runner.ValueException;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.StructValueBuilder;

/**
 * @author Ian Rae
 *
 * A newer algorithm using HLS and Rendered fields. We read fields by column index in the order specified in SQL statement.
 *
 */
public class HLDResultSetConverter extends HLDResultSetConverterBase {
	
	static class ColumnRun {
		public DStructType dtype;
		public List<HLDField> runList = new ArrayList<>();
		public int iStart;
//		public FieldGroup fieldGroup;

		public ColumnRun(int i, DStructType dtype) {
			this.iStart = i;
			this.dtype = dtype;
		}
		
	}

	private DTypeRegistry registry;
	
	public HLDResultSetConverter(DBType dbType, FactoryService factorySvc, ConnectionFactory connFactory, SqlHelperFactory sqlhelperFactory) {
		super(dbType, factorySvc, connFactory, sqlhelperFactory);
	}
	public HLDResultSetConverter(FactoryService factorySvc, ValueHelper valueHelper, DTypeRegistry registry) {
		super(factorySvc, valueHelper);
		this.registry = registry;
	}

	public void init(FactoryService factorySvc) {
		super.init(factorySvc);
	}

	public List<DValue> buildDValueList(ResultSet rs, DBAccessContext dbctx, HLDQueryStatement hld) {
//		if (hls == null) { //TODO: or if is select *
//			return super.buildDValueList(rs, dtype, details, dbctx, hls);
//		} else if (!hls.hlspanL.isEmpty() && !hls.hlspanL.get(0).renderedFieldL.isEmpty()) {
//			boolean isStarQuery = hls.hlspanL.get(0).renderedFieldL.get(0).field.equals("*");
//			if (isStarQuery) {
//				return super.buildDValueList(rs, dtype, details, dbctx, hls);
//			}
//		}
		
		ResultSetWrapper rsw = new ResultSetWrapper(rs, valueHelper, logResultSetDetails, log);
		List<DValue> list = null;
		ObjectPool pool = new ObjectPool(factorySvc, registry);
		try {
			list = doBuildDValueList(rsw, dbctx, hld, pool);
		} catch (ValueException e) {
			ValueException ve = (ValueException)e;
			throw new DBException(ve.errL);
		} catch (Exception e) {
						e.printStackTrace();
			DeliaError err = new DeliaError("db-resultset-error", e.getMessage());
			throw new DBException(err);
		}
		
		return list;
	}
	
	
	private List<DValue> doBuildDValueList(ResultSetWrapper rsw, DBAccessContext dbctx, HLDQueryStatement hld, ObjectPool pool) throws Exception {
		List<DValue> list = new ArrayList<>();

		HLDFieldHelper.logRenderedFieldList(hld, log);
		List<HLDField> rfList = hld.hldquery.fieldL; //.getRenderedFields();
		
		//add column indexes
		int j = 1;
		for(HLDField rff: rfList) {
			rff.columnIndex = j++;;
		}
		
		List<ColumnRun> columnRunL = buildColumnRuns(hld, rfList);
		
		while(rsw.next()) {  //get row
			//do main type
			ColumnRun mainRun = columnRunL.get(0);
			DValue dval = readStructDValueX(mainRun, rsw, dbctx);
			if (dval == null) {
				DeliaExceptionHelper.throwError("unexpected-null-db-results", "%s: unexpected null dval", hld.hldquery.fromType.getName());
			}
			list.add(dval);
			
			//do remaining column runs
			for(int i = 1; i < columnRunL.size(); i++) {
				ColumnRun columnRun = columnRunL.get(i);
				DValue subDVal = readStructDValueX(columnRun, rsw, dbctx);
				if (subDVal != null) {
					if (addAsSubObjectX(dval, subDVal, columnRun, dbctx) != null) {
						pool.add(subDVal);
					}
				}
			}
			pool.add(dval);
		}

		list = mergeRows(list, pool, columnRunL);
		return list;
	}
	
	private List<ColumnRun> buildColumnRuns(HLDQueryStatement hld, List<HLDField> rfList) {
		DStructType dtype = hld.hldquery.fromType;
		
		List<ColumnRun> resultL = new ArrayList<>();
		ColumnRun run = new ColumnRun(0, dtype);
		resultL.add(run);
		
		DStructType currentType = dtype;
		String currentKey = "t0"; //TODO: is the main type always t0???
		int iEnd = 0;
		for(int i = 0; i < rfList.size(); i++) {
			HLDField rff = rfList.get(i);
			
			DStructType tmp = getFieldStructType(rff, currentType);
			String tmpKey = rff.alias; 
			if (tmpKey.equals(currentKey)) {
				iEnd = i;
			} else {
				copyToRunList(run, iEnd, rfList);
				
				run = new ColumnRun(i, tmp);
				resultL.add(run);
				currentType = tmp;
				currentKey = tmpKey;
				iEnd = i;
			}
		}
		copyToRunList(run, iEnd, rfList);
		
		return resultL;
	}
	private void copyToRunList(ColumnRun run, int iEnd, List<HLDField> rfList) {
		List<HLDField> tmpL = rfList.subList(run.iStart, iEnd+1);
		run.runList.addAll(tmpL);
	}
	private DStructType getFieldStructType(HLDField rff, DStructType currentType) {
		if (rff.structType != null) {
			return rff.structType;
		} else {
//			//alias
//			String fieldName = HLDFieldHelper.getAssocFieldName(rff);
//			DType type = DValueHelper.findFieldType(currentType, fieldName);
//			if (type != null) {
//				return currentType;
//			}
//			return (DStructType)type; //is this right?
			DeliaExceptionHelper.throwNotImplementedError("wtf!");
			return null;
		}
	}
	
	private DValue readStructDValueX(ColumnRun columnRun, ResultSetWrapper rsw, DBAccessContext dbctx) throws Exception {
		DStructType dtype = columnRun.dtype;
		StructValueBuilder structBuilder = new StructValueBuilder(dtype);
		PrimaryKey pk = dtype.getPrimaryKey();
		
		for(HLDField rff: columnRun.runList) {
//			if (rff.pair.type == null) {
//				String fld = HLDFieldHelper.getAssocFieldName(rff);
//				DType ddd = DValueHelper.findFieldType(dtype, fld);
//				if (! ddd.isStructShape()) {
//					DeliaError err = new DeliaError("db-resultset-error-assoc", String.format("type %s has no relation field %s",  dtype.getName(), fld));
//					throw new DBException(err);
//				}
//				HLDField copyrff = new HLDField(rff);
//				copyrff.pair.name = fld;
//				copyrff.pair.type = ddd;
//				
//				//FK only goes in child so it may not be here.
//				//However if .fks() was used then it is
//				String strValue = rsw.getString(rff.columnIndex);
//				if (strValue == null) {
//					structBuilder.addField(fld, null);
//				} else {
//					TypePair fieldPair = new TypePair(fld, ddd);
//					DValue inner = createRelation(dtype, fieldPair, strValue, dbctx, copyrff);
//					structBuilder.addField(fieldPair.name, inner);
//				}
//			} else {

			TypePair pair = rff.getAsPair();
			DValue inner = rsw.readFieldByColumnIndex(rff.getAsPair(), rff.columnIndex, dbctx);
			if (inner == null && pair.name.equals(pk.getFieldName())) {
				//is optional relation and is null
				return null;
			}

			//handle relation. inner is the pkval
			if (inner != null && pair.type.isStructShape()) {
				DValue pkval = inner;
				//					inner = this.createEmptyRelation(dbctx, (DStructType) rff.pair.type, rff.pair.name);
				inner = this.createEmptyRelation(dbctx, (DStructType) rff.structType, pair.name);
				DRelation drel = inner.asRelation();
				drel.addKey(pkval);
			}
			structBuilder.addField(pair.name, inner);
		}
//		}
		
		boolean b = structBuilder.finish();
		if (! b) {
			//JTElement el = columnRun.getJTElementIfExist();
			boolean needAllColumns = !areAnyJoinsFKJoins(columnRun);
			//if we're doing .fks() then are only getting pk, not all the columns
			//TODO: only ignore missing field errors. other types of validation errors should still be thrown!
			if (needAllColumns) {
				throw new ValueException(structBuilder.getValidationErrors()); 
			}
		}
		DValue dval = structBuilder.getDValue();
		return dval;
	}
	private boolean areAnyJoinsFKJoins(ColumnRun columnRun) {
		for(HLDField field: columnRun.runList) {
			if (field.source instanceof JoinElement) {
				JoinElement jel = (JoinElement) field.source;
				if (jel.usedForFK()) {
					return true;
				}
			}
		}
		return false;
	}
	private DRelation addAsSubObjectX(DValue dval, DValue subDVal, ColumnRun columnRun, DBAccessContext dbctx) {
		if (DValueHelper.findPrimaryKeyValue(subDVal) == null) {
			return null;
		}
		
		//rff is something like b.id as addr
		HLDField rff = columnRun.runList.get(0);
		JoinElement jel = (JoinElement) rff.source;
		String fieldName = jel.relationField.fieldName;
		
		//setting dval's relation (fieldName) to have subDVal
		DRelation drel = getOrCreateRelation(dval, fieldName, subDVal, dbctx);
		
		RelationInfo relinfo = jel.relinfo;
//		if (relinfo.isManyToMany()) {
			//do the inverse. setting subDVal's relation to have dval
			String otherField = relinfo.otherSide.fieldName;
			return getOrCreateRelation(subDVal, otherField, dval, dbctx);
//		}
	}

	private DRelation getOrCreateRelation(DValue dval, String relField, DValue subDVal, DBAccessContext dbctx) {
		DValue inner2 = dval.asStruct().getField(relField);
		if (inner2 == null) {
			inner2 = createEmptyRelation(dbctx, (DStructType) dval.getType(), relField);
			dval.asMap().put(relField, inner2);
			DRelation drel = inner2.asRelation();
			
			DValue pkval = DValueHelper.findPrimaryKeyValue(subDVal);
			if (pkval == null) {
				return null; //happens in self.join c.leftv as workers
			}
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
			sortFKsIfNeeded(dval);
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
	private void sortFKsIfNeeded(DValue dval) {
		DStructType dtype = (DStructType) dval.getType();
		for(TypePair pair: dtype.getAllFields()) {
			if (pair.type.isStructShape()) {
				DValue inner = dval.asStruct().getField(pair.name);
				if (inner != null) {
					DRelation drel = inner.asRelation();
					DRelationHelper.sortFKs(drel); //this is not needed, but simplifies unit tests
				}
			}
		}
	}
	
	private boolean isAFetchedColumn(DStructType dtype, TypePair pair, List<ColumnRun> columnRunL) {
		for(ColumnRun run: columnRunL) {
			if (run.dtype != dtype) {
				continue;
			}
			
			for(HLDField fld: run.runList) {
				if (fld.fieldName.equals(pair.name)) {
					if (fld.source instanceof JoinElement) {
						JoinElement el = (JoinElement) fld.source;
						return el.usedForFetch();
					}
				}
			}
			
		}
		return false;
	}
//	private void chkObjects(List<DValue> list, String relField, String backField) {
//		int id = 100;
//		for(DValue dval: list) {
//			DValueImpl impl = (DValueImpl) dval;
//			if (impl.getPersistenceId() == null) {
//				impl.setPersistenceId(id++);
//			}
//			log.log("%s: %d", dval.getType().getName(), dval.getPersistenceId());
//
//			id = chkSubObj(dval, relField, id, backField);
//		}
//	}
//	private int chkSubObj(DValue dval, String relField, int id, String backField) {
//		DValue inner = dval.asStruct().getField(relField);
//		if (inner == null) {
//			return -1;
//		}
//		DRelation rel = inner.asRelation();
//		if (!rel.haveFetched()) {
//			return -1;
//		}
//		for(DValue xx: rel.getFetchedItems()) {
//			DValueImpl impl = (DValueImpl) xx;
//			if (impl.getPersistenceId() == null) {
//				impl.setPersistenceId(id++);
//			}
//			log.log("  %s: %d", impl.getType().getName(), impl.getPersistenceId());
//			
//			if (backField != null) {
//				id = chkSubObj(xx, backField, id, null);
//			}
//		}
//		return id;
//	}
	
	
}