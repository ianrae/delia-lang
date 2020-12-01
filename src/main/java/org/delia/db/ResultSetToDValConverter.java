package org.delia.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.RenderedField;
import org.delia.db.hls.RenderedFieldHelper;
import org.delia.db.hls.ResultTypeInfo;
import org.delia.db.sql.ConnectionFactory;
import org.delia.dval.DRelationHelper;
import org.delia.dval.DValueConverterService;
import org.delia.error.DeliaError;
import org.delia.runner.ValueException;
import org.delia.type.BuiltInTypes;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.RelationValueBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;

/**
 * @author Ian Rae
 *
 */
public class ResultSetToDValConverter extends ServiceBase {
	public static boolean logResultSetDetails = false;
	
	static class ColumnReadInfo {
		public int numColumnsRead;
	}
	
	protected ValueHelper valueHelper;
	private DValueConverterService dvalConverter;

	public ResultSetToDValConverter(DBType dbType, FactoryService factorySvc, ConnectionFactory connFactory, SqlHelperFactory sqlhelperFactory) {
		super(factorySvc);
		this.valueHelper = sqlhelperFactory.createValueHelper();
	}
	public ResultSetToDValConverter(FactoryService factorySvc, ValueHelper valueHelper) {
		super(factorySvc);
		this.valueHelper = valueHelper;
	}

	public void init(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
		this.log = factorySvc.getLog();
		this.et = factorySvc.getErrorTracker();
		this.dvalConverter = new DValueConverterService(factorySvc);
	}

	/**
	 * we can only have generated one key even if did several inserts because
	 * the additional inserts are just the assoc table.
	 * @param ctx
	 * @param sqlctx
	 * @return
	 * @throws SQLException
	 */
	public DValue extractGeneratedKey(InsertContext ctx, SqlExecuteContext sqlctx) throws SQLException {
		for(ResultSet rs: sqlctx.genKeysL) {
			DValue genVal = valueHelper.extractGeneratedKey(rs, ctx.genKeytype.getShape(), sqlctx.registry);
			if (genVal != null) {
				return genVal;
			}
		}
		return null;
	}

	public List<DValue> buildDValueList(ResultSet rs, DStructType dtype, QueryDetails details, DBAccessContext dbctx, HLSQueryStatement hls) {
		ResultSetWrapper rsw = new ResultSetWrapper(rs, valueHelper, logResultSetDetails, log);
		List<DValue> list = null;
		try {
			list = doBuildDValueList(rsw, dtype, dbctx, hls);
			if (details.mergeRows) {
				if (details.isManyToMany) {
					list = mergeRowsOneToMany(list, dtype, details, dbctx);
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
		return list;
	}
	
	
	public List<DValue> buildScalarResult(ResultSet rs, ResultTypeInfo selectResultType, QueryDetails details, DBAccessContext dbctx) {
		List<DValue> list = new ArrayList<>();
		try {
			DValue dval = valueHelper.readIndexedField(selectResultType.physicalType, 1, rs, dbctx);
			if (selectResultType.needPhysicalToLogicalMapping()) {
				ScalarValueBuilder builder = new ScalarValueBuilder(factorySvc, dbctx.registry);
				dval = selectResultType.mapPhysicalToLogicalValue(dval, builder);
			}
			
			if (dval != null) {
				list.add(dval);
			}
		} catch (ValueException e) {
			ValueException ve = (ValueException)e;
			throw new DBException(ve.errL);
		} catch (Exception e) {
			//			e.printStackTrace();
			DeliaError err = new DeliaError("db-resultset-error2", e.getMessage());
			throw new DBException(err);
		}
		return list;
	}
	

	/**
	 * On a One-to-many relation our query returns multiple rows in order to get all
	 * the 'many' ids. Merge into a single row.
	 * @param rawList list of dvalues to merge
	 * @param dtype of values
	 * @param details query details
	 * @param dbctx 
	 * @return merged rows
	 */
	public List<DValue> mergeRowsOneToMany(List<DValue> rawList, DStructType dtype, QueryDetails details, DBAccessContext dbctx) {
		Map<Object,DValue> pkMap = new HashMap<>(); //pk,dval
		PrimaryKey pkType = dtype.getPrimaryKey();
		String pkField = pkType.getFieldName();
		
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
						if (! alreadyExist(inner1, drel2.getForeignKey())) {
							inner1.asRelation().addKey(drel2.getForeignKey());
							DRelationHelper.addToFetchedItemsFromRelation(inner1, drel2);
							
							//TODO: add config flag for this. it's good for tests but slows perf
							DRelationHelper.sortFKs(inner1.asRelation());
						}
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
		
		return list;
	}
	//TODO: fix. very inefficient when many fks
	private boolean alreadyExist(DValue inner1, DValue foreignKey) {
		Object obj2 = foreignKey.getObject();
		DRelation drel = inner1.asRelation();
		for(DValue keyval: drel.getMultipleKeys()) {
			Object obj1 = keyval.getObject();
			if (obj1.equals(obj2)) {
				return true;
			}
		}
		return false;
	}
	
	protected DValue createEmptyRelation(DBAccessContext dbctx, DStructType structType, String mergeOnField) {
		DType relType = dbctx.registry.getType(BuiltInTypes.RELATION_SHAPE);
		TypePair pair = DValueHelper.findField(structType, mergeOnField);
		RelationValueBuilder builder = new RelationValueBuilder(relType, pair.type, dbctx.registry);
		builder.buildEmptyRelation();
		boolean b = builder.finish();
		if (!b) {
			DeliaExceptionHelper.throwError("relation-create-failed-assocCrud", "Type '%s': Failed to create empty relation", pair.type);
		} 
		return builder.getDValue();
	}

	private List<DValue> doBuildDValueList(ResultSetWrapper rsw, DStructType dtype, DBAccessContext dbctx, HLSQueryStatement hls) throws Exception {
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
		
		while(rsw.next()) {  //get row
			
			//first columns are the main object
			ColumnReadInfo columnReadInfo = new ColumnReadInfo();
			DValue dval = readStructDValue(rsw, dtype, dbctx, rf, columnReadInfo);
			list.add(dval);
			
			//now read sub-objects (if are any)
			if (rf != null) {
				//look for sub-objects to the right the main object
				for(int k = columnReadInfo.numColumnsRead; k < rfList.size(); k++) {
					RenderedField rff =  rfList.get(k);
					if (rff.structType != null && rff.structType != dtype) { //FUTURE: full name compare later
						DValue subDVal= readStructDValueUsingIndex(rsw, dbctx, rff, rfList);
						if (subDVal != null) {
							addAsSubOjbect(dval, subDVal, rff, rfList);
						}
					}
				}
			}
		}

		return list;
	}
	
	private void addAsSubOjbect(DValue dval, DValue subDVal, RenderedField rff, List<RenderedField> rfList) {
		//rff is something like b.id as addr
		String targetAlias = getAlias(rff.field);
		
		for(RenderedField rf: rfList) {
			String alias = getAlias(rf.field);
			if (alias != null && alias.equals(targetAlias)) {
				String fieldName = StringUtils.substringAfter(rf.field, " as ");
				DValue inner = dval.asStruct().getField(fieldName);
				DRelation drel = inner.asRelation();
				DRelationHelper.addToFetchedItems(drel, subDVal);
				return;
			}
		}
	}

	private String getAlias(String field) {
		return StringUtils.substringBefore(field, ".");
	}

	protected DValue readStructDValueUsingIndex(ResultSetWrapper rsw, DBAccessContext dbctx, RenderedField rfTarget, List<RenderedField> rfList) throws SQLException {
		DStructType dtype = rfTarget.structType;
		StructValueBuilder structBuilder = new StructValueBuilder(dtype);
		PrimaryKey pk = dtype.getPrimaryKey();
		
		for(RenderedField rff: rfList) {
			if (rff.structType == rfTarget.structType) {
				if (rff.pair.type.isStructShape()) {
					//FK only goes in child so it may not be here.
					//However if .fks() was used then it is
					String strValue = rsw.getString(rff.columnIndex);
					if (strValue == null) {
						structBuilder.addField(rff.pair.name, null);
					} else {
						DValue inner = createRelation(dtype, rff.pair, strValue, dbctx, rff);
						structBuilder.addField(rff.pair.name, inner);
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
		}
		
		boolean b = structBuilder.finish();
		if (! b) {
			throw new ValueException(structBuilder.getValidationErrors()); 
		}
		DValue dval = structBuilder.getDValue();
		return dval;
	}
	
	private DValue readStructDValue(ResultSetWrapper rsw, DStructType dtype, DBAccessContext dbctx, RenderedField rf, ColumnReadInfo rsstuff) throws SQLException {
		StructValueBuilder structBuilder = new StructValueBuilder(dtype);
		for(TypePair pair: dtype.getAllFields()) {
			
			if (pair.type.isStructShape()) {
				//FK only goes in child so it may not be here.
				//However if .fks() was used then it is
				if (rsw.hasColumn(pair.name)) {
					rsstuff.numColumnsRead++;
					String strValue = rsw.getString(pair.name);
					if (strValue == null) {
						structBuilder.addField(pair.name, null);
					} else {
						DValue inner = createRelation(dtype, pair, strValue, dbctx, rf);
						structBuilder.addField(pair.name, inner);
					}
				}
			} else {
				DValue inner = readField(pair, rsw, dbctx);
				structBuilder.addField(pair.name, inner);
				rsstuff.numColumnsRead++;
			}
		}
		boolean b = structBuilder.finish();
		if (! b) {
			throw new ValueException(structBuilder.getValidationErrors()); 
		}
		DValue dval = structBuilder.getDValue();
		return dval;
	}

	protected DValue createRelation(DStructType structType, TypePair targetPair, String strValue, DBAccessContext dbctx, RenderedField rf) throws SQLException {
		//get as string and let builder convert
		String s = strValue;
		
		ScalarValueBuilder xbuilder = factorySvc.createScalarValueBuilder(dbctx.registry);
		Shape shape = null;
		if (rf != null && rf.pair != null) {
			shape = rf.pair.type.getShape();
			if (rf.pair.type.isStructShape()) {
				PrimaryKey pk = ((DStructType)rf.pair.type).getPrimaryKey();
				shape = pk.getKey().type.getShape();
			}
		} else {
			DStructType otherSideType = (DStructType) targetPair.type;
			PrimaryKey pk = otherSideType.getPrimaryKey();
			shape = pk.getKey().type.getShape();
		}			
			
		DValue keyVal;
		keyVal = dvalConverter.buildFromObject(s, shape, xbuilder);
		
		DType relType = dbctx.registry.getType(BuiltInTypes.RELATION_SHAPE);
		String typeName = targetPair.type.getName();
		RelationValueBuilder builder = new RelationValueBuilder(relType, typeName, dbctx.registry);
		builder.buildFromString(keyVal.asString());
		boolean b = builder.finish();
		if (!b) {
			//err
			return null;
		} else {
			return builder.getDValue();
		}
	}

	private DValue readField(TypePair pair, ResultSetWrapper rsw, DBAccessContext dbctx) throws SQLException {
		return rsw.readField(pair, dbctx);
	}
}