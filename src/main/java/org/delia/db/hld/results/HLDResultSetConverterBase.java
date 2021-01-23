package org.delia.db.hld.results;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBException;
import org.delia.db.DBType;
import org.delia.db.InsertContext;
import org.delia.db.QueryDetails;
import org.delia.db.SqlExecuteContext;
import org.delia.db.ValueHelper;
import org.delia.db.hls.ResultTypeInfo;
import org.delia.db.sql.ConnectionFactory;
import org.delia.dval.DValueConverterService;
import org.delia.error.DeliaError;
import org.delia.runner.ValueException;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.RelationValueBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;

/**
 * @author Ian Rae
 *
 */
public class HLDResultSetConverterBase extends ServiceBase {
	public static boolean logResultSetDetails = false;

	static class ColumnReadInfo {
		public int numColumnsRead;
	}

	protected ValueHelper valueHelper;

	public HLDResultSetConverterBase(DBType dbType, FactoryService factorySvc, ConnectionFactory connFactory) {
		super(factorySvc);
		this.valueHelper =  new ValueHelper(factorySvc); //sqlhelperFactory.createValueHelper();
	}
	public HLDResultSetConverterBase(FactoryService factorySvc, ValueHelper valueHelper) {
		super(factorySvc);
		this.valueHelper = valueHelper;
	}

	public void init(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
		this.log = factorySvc.getLog();
		this.et = factorySvc.getErrorTracker();
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

	public List<DValue> buildScalarResult(ResultSet rs, ResultTypeInfo selectResultType, QueryDetails details, DBAccessContext dbctx) {
		List<DValue> list = new ArrayList<>();
		try {
			while (rs.next()) {
				DValue dval = valueHelper.readIndexedField(selectResultType.physicalType, 1, rs, dbctx);
				if (selectResultType.needPhysicalToLogicalMapping()) {
					ScalarValueBuilder builder = new ScalarValueBuilder(factorySvc, dbctx.registry);
					dval = selectResultType.mapPhysicalToLogicalValue(dval, builder);
				}

				if (dval != null) {
					list.add(dval);
				}
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


////	/**
////	 * On a One-to-many relation our query returns multiple rows in order to get all
////	 * the 'many' ids. Merge into a single row.
////	 * @param rawList list of dvalues to merge
////	 * @param dtype of values
////	 * @param details query details
////	 * @param dbctx 
////	 * @return merged rows
////	 */
////	public List<DValue> mergeRowsOneToMany(List<DValue> rawList, DStructType dtype, QueryDetails details, DBAccessContext dbctx) {
////		Map<Object,DValue> pkMap = new HashMap<>(); //pk,dval
////		PrimaryKey pkType = dtype.getPrimaryKey();
////		String pkField = pkType.getFieldName();
////
////		for(DValue dval: rawList) {
////			DValue pkval = dval.asStruct().getField(pkField); 
////			Object key = pkval.getObject();
////			if (! pkMap.containsKey(key)) {
////				pkMap.put(key, dval);
////			} else {
////				DValue mergeToVal = pkMap.get(key);
////				for(String mergeOnField: details.mergeOnFieldL) {
////					DValue inner1 = mergeToVal.asStruct().getField(mergeOnField);
////					DValue inner2 = dval.asStruct().getField(mergeOnField);
////					if (inner2 != null) {
////						if (inner1 == null) {
////							inner1 = this.createEmptyRelation(dbctx, dtype, mergeOnField);
////							mergeToVal.asMap().put(mergeOnField, inner1);
////						}
////						DRelation drel2 = inner2.asRelation();
////						if (! alreadyExist(inner1, drel2.getForeignKey())) {
////							inner1.asRelation().addKey(drel2.getForeignKey());
////							DRelationHelper.addToFetchedItemsFromRelation(inner1, drel2);
////
////							//TODO: add config flag for this. it's good for tests but slows perf
////							DRelationHelper.sortFKs(inner1.asRelation());
////						}
////					}
////				}
////			}
////		}
////
////		//build output list. keep same order
////		List<DValue> list = new ArrayList<>();
////		for(DValue dval: rawList) {
////			DValue pkval = dval.asStruct().getField(pkField); 
////			Object key = pkval.getObject();
////			if (pkMap.containsKey(key)) {
////				list.add(dval);
////				pkMap.remove(key);
////			}
////		}
////
////		return list;
////	}
//	//TODO: fix. very inefficient when many fks
//	private boolean alreadyExist(DValue inner1, DValue foreignKey) {
//		Object obj2 = foreignKey.getObject();
//		DRelation drel = inner1.asRelation();
//		for(DValue keyval: drel.getMultipleKeys()) {
//			Object obj1 = keyval.getObject();
//			if (obj1.equals(obj2)) {
//				return true;
//			}
//		}
//		return false;
//	}

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

//	protected DValue createRelation(DStructType structType, TypePair targetPair, String strValue, DBAccessContext dbctx, HLDField rf) throws SQLException {
//		//get as string and let builder convert
//		String s = strValue;
//
//		ScalarValueBuilder xbuilder = factorySvc.createScalarValueBuilder(dbctx.registry);
//		Shape shape = null;
//		//		if (rf != null && rf.pair != null) {
//		//			shape = rf.pair.type.getShape();
//		//			if (rf.pair.type.isStructShape()) {
//		//				PrimaryKey pk = ((DStructType)rf.pair.type).getPrimaryKey();
//		//				shape = pk.getKey().type.getShape();
//		//			}
//		//		} else {
//		DStructType otherSideType = (DStructType) targetPair.type;
//		PrimaryKey pk = otherSideType.getPrimaryKey();
//		shape = pk.getKey().type.getShape();
//		//		}			
//
//		DValue keyVal;
//		keyVal = dvalConverter.buildFromObject(s, shape, xbuilder);
//
//		DType relType = dbctx.registry.getType(BuiltInTypes.RELATION_SHAPE);
//		String typeName = targetPair.type.getName();
//		RelationValueBuilder builder = new RelationValueBuilder(relType, typeName, dbctx.registry);
//		builder.buildFromString(keyVal.asString());
//		boolean b = builder.finish();
//		if (!b) {
//			//err
//			return null;
//		} else {
//			return builder.getDValue();
//		}
//	}

}