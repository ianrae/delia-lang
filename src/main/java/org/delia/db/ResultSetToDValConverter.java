package org.delia.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.sql.ConnectionFactory;
import org.delia.error.DeliaError;
import org.delia.runner.ValueException;
import org.delia.type.BuiltInTypes;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;
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
	private ValueHelper valueHelper;

	public ResultSetToDValConverter(DBType dbType, FactoryService factorySvc, ConnectionFactory connFactory, SqlHelperFactory sqlhelperFactory) {
		super(factorySvc);
		this.valueHelper = sqlhelperFactory.createValueHelper();
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

	public List<DValue> buildDValueList(ResultSet rs, DStructType dtype, QueryDetails details, DBAccessContext dbctx) {
		List<DValue> list = null;
		try {
			list = doBuildDValueList(rs, dtype, dbctx);
			if (details.mergeRows) {
				if (details.isManyToMany) {
					list = mergeRowsManyToMany(list, dtype, details, dbctx);
				} else {
					list = mergeRowsOneToMany(list, dtype, details);
				}
			}
		} catch (ValueException e) {
			//				e.printStackTrace();
			DeliaError err = ((ValueException)e).errL.get(0);
			throw new DBException(err);
		} catch (Exception e) {
			//			e.printStackTrace();
			DeliaError err = new DeliaError("db-resultset-error", e.getMessage());
			throw new DBException(err);
		}
		return list;
	}
	
	
	public List<DValue> buildScalarResult(ResultSet rs, DType selectResultType, QueryDetails details, DBAccessContext dbctx) {
		List<DValue> list = new ArrayList<>();
		try {
			DValue dval = valueHelper.readIndexedField(selectResultType, 1, rs, dbctx);
			if (dval != null) {
				list.add(dval);
			}
		} catch (ValueException e) {
			//				e.printStackTrace();
			DeliaError err = ((ValueException)e).errL.get(0);
			throw new DBException(err);
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
	 * @return merged rows
	 */
	public List<DValue> mergeRowsOneToMany(List<DValue> rawList, DStructType dtype, QueryDetails details) {
		List<DValue> list = new ArrayList<>();
		List<DValue> foreignKeyL = new ArrayList<>();
		DValue firstVal = null;
		int i = 0;
		for(DValue dval: rawList) {
			DValue inner = dval.asStruct().getField(details.mergeOnField);
			if (inner != null) {
				if (i == 0) {
					firstVal = dval;
				}
				DRelation drel = inner.asRelation();
				foreignKeyL.add(drel.getForeignKey());
			}
			i++;
		}

		if (firstVal != null) {
			DValue inner = firstVal.asStruct().getField(details.mergeOnField);
			if (inner != null) {
				DRelation drel = inner.asRelation();
				drel.getMultipleKeys().clear();
				drel.getMultipleKeys().addAll(foreignKeyL);
				list.add(firstVal);
			}
		} else if (! rawList.isEmpty()) {
			//if all the parents were null then just use raw list
			list.addAll(rawList);
		}

		return list;
	}
	/**
	 * On a Many-to-many relation our query returns multiple rows in order to get all
	 * the 'many' ids. Merge into a single row.
	 * @param rawList list of dvalues to merge
	 * @param dtype of values
	 * @param details query details
	 * @param dbctx 
	 * @return merged rows
	 */
	public List<DValue> mergeRowsManyToMany(List<DValue> rawList, DStructType dtype, QueryDetails details, DBAccessContext dbctx) {
		List<DValue> list = new ArrayList<>();
		int i = 0;
		List<DValue> subL = new ArrayList<>();
		Map<Object,String> alreadyHandledL = new HashMap<>();
		for(DValue dval: rawList) {
			DValue keyVal = DValueHelper.findPrimaryKeyValue(dval);
			if (alreadyHandledL.containsKey(keyVal.getObject())) {
				continue;
			}
			
			fillSubL(rawList, keyVal, dval, subL); //other values with same primary key
			if (subL.isEmpty()) {
				list.add(dval);
			} else {
				alreadyHandledL.put(keyVal.getObject(), "");
				List<DValue> toMergeL = new ArrayList<>();
				for(DValue subVal: subL) {
					DValue inner = subVal.asStruct().getField(details.mergeOnField);
					if (inner != null) {
						DRelation drel = inner.asRelation();
						toMergeL.addAll(drel.getMultipleKeys());
					}
				}
				
				if (!toMergeL.isEmpty()) {
					//and add back into dval
					DValue inner2 = dval.asStruct().getField(details.mergeOnField);
					if (inner2 == null) {
						//fix later!! TODO
						DType relType = dbctx.registry.getType(BuiltInTypes.RELATION_SHAPE);
						TypePair pair = DValueHelper.findField(dval.getType(), details.mergeOnField);
						RelationValueBuilder builder = new RelationValueBuilder(relType, pair.type, dbctx.registry);
						builder.buildEmptyRelation();
						boolean b = builder.finish();
						if (!b) {
							DeliaExceptionHelper.throwError("relation-create-failed-assocCrud", "Type '%s': Failed to create empty relation", pair.type);
						} else {
							inner2 = builder.getDValue();
							dval.asMap().put(details.mergeOnField, inner2);
						}
					}
					DRelation drel2 = inner2.asRelation();
					drel2.getMultipleKeys().addAll(toMergeL);
					list.add(dval);
				}
			}
			
			i++;
		}

		return list;
	}

	private void fillSubL(List<DValue> rawList, DValue targetKeyVal, DValue skip, List<DValue> subL) {
		subL.clear();
		String s2 = targetKeyVal.asString();
		for(DValue tmp: rawList) {
			if (tmp == skip) {
				continue;
			}
			DValue keyVal = DValueHelper.findPrimaryKeyValue(tmp);
			String s1 = keyVal.asString(); //TODO: need better way to compare dval
			if (s1.equals(s2)) {
				subL.add(tmp);
			}
		}
	}

	private List<DValue> doBuildDValueList(ResultSet rs, DStructType dtype, DBAccessContext dbctx) throws Exception {
		List<DValue> list = new ArrayList<>();

		while(rs.next()) {
			StructValueBuilder structBuilder = new StructValueBuilder(dtype);
			for(TypePair pair: dtype.getAllFields()) {
//				//key goes in child only
//				if (DRuleHelper.isParentRelation(dtype, pair)) {
//					continue;
//				}
				
				if (pair.type.isStructShape()) {
					//FK only goes in child so it may not be here.
					//However if .fks() was used then it is
					if (ResultSetHelper.hasColumn(rs, pair.name)) {
						DValue inner = createRelation(dtype, pair, rs, dbctx);
						structBuilder.addField(pair.name, inner);
					}
				} else {
					DValue inner = readField(pair, rs, dbctx);
					structBuilder.addField(pair.name, inner);
				}
				//					log.log(": " + pair.name);
			}
			boolean b = structBuilder.finish();
			if (! b) {
				DeliaError err = structBuilder.getValidationErrors().get(0); //TODO: support multiple later
				//TODO: why does the err not have fieldname and typename set? fix.
				throw new ValueException(err); 
			}
			DValue dval = structBuilder.getDValue();
			list.add(dval);
		}

		return list;
	}
	
	private DValue createRelation(DStructType structType, TypePair targetPair, ResultSet rs, DBAccessContext dbctx) throws SQLException {
		//get as string and let builder convert
		String s = rs.getString(targetPair.name);
		if (rs.wasNull()) {
			return null;
		}
		ScalarValueBuilder xbuilder = factorySvc.createScalarValueBuilder(dbctx.registry);
		//			RelationInfo info = DRuleHelper.findMatchingRuleInfo(structType, targetPair);
		//			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(targetPair.type);
		DValue keyVal = xbuilder.buildInt(s);

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

	private DValue readField(TypePair pair, ResultSet rs, DBAccessContext dbctx) throws SQLException {
		return valueHelper.readField(pair, rs, dbctx);
	}
}