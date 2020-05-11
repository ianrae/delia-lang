package org.delia.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.RenderedField;
import org.delia.db.hls.RenderedFieldHelper;
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
	static class ColumnReadInfo {
		public int numColumnsRead;
	}
	
	private ValueHelper valueHelper;
	private DValueConverterService dvalConverter;

	public ResultSetToDValConverter(DBType dbType, FactoryService factorySvc, ConnectionFactory connFactory, SqlHelperFactory sqlhelperFactory) {
		super(factorySvc);
		this.valueHelper = sqlhelperFactory.createValueHelper();
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
		List<DValue> list = null;
		try {
			list = doBuildDValueList(rs, dtype, dbctx, hls);
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

	private List<DValue> doBuildDValueList(ResultSet rs, DStructType dtype, DBAccessContext dbctx, HLSQueryStatement hls) throws Exception {
		List<DValue> list = new ArrayList<>();

		List<RenderedField> rfList = null;
		if (hls != null) {
			RenderedFieldHelper.logRenderedFieldList(hls, log);
			rfList = hls.getRenderedFields();
		}
		
		int index = 0;
		while(rs.next()) {  //get row
			RenderedField rf = rfList == null ? null : rfList.get(index++);
			
			//first columns are the main object
			ColumnReadInfo columnReadInfo = new ColumnReadInfo();
			DValue dval = readStructDValue(rs, dtype, dbctx, rf, columnReadInfo);
			list.add(dval);
			
			//now read sub-objects (if are any)
			if (rf != null) {
				//add column indexes
				int j = 1;
				for(RenderedField rff: rfList) {
					rff.columnIndex = j++;;
				}
				
				//look for sub-objects to the right the main object
				for(int k = columnReadInfo.numColumnsRead; k < rfList.size(); k++) {
					RenderedField rff =  rfList.get(k);
					if (rff.structType != null && rff.structType != dtype) { //TODO: full name compare later
						DValue subDVal= readStructDValueUsingIndex(rs, dbctx, rff, rfList);
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
		
		//TODO: fix for multiple relations. this code only works if Customer has single relation to Address
		
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

	private DValue readStructDValueUsingIndex(ResultSet rs, DBAccessContext dbctx, RenderedField rfTarget, List<RenderedField> rfList) throws SQLException {
		DStructType dtype = rfTarget.structType;
		StructValueBuilder structBuilder = new StructValueBuilder(dtype);
		PrimaryKey pk = dtype.getPrimaryKey();
		
		for(RenderedField rff: rfList) {
			if (rff.structType == rfTarget.structType) {
				if (rff.pair.type.isStructShape()) {
					//FK only goes in child so it may not be here.
					//However if .fks() was used then it is
					String strValue = rs.getString(rff.columnIndex);
					if (rs.wasNull()) {
						structBuilder.addField(rff.pair.name, null);
					} else {
						DValue inner = createRelation(dtype, rff.pair, strValue, dbctx, rff);
						structBuilder.addField(rff.pair.name, inner);
					}
				} else {
					DValue inner = valueHelper.readFieldByColumnIndex(rff.pair, rs, rff.columnIndex, dbctx);
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
			DeliaError err = structBuilder.getValidationErrors().get(0); //TODO: support multiple later
			//TODO: why does the err not have fieldname and typename set? fix.
			throw new ValueException(err); 
		}
		DValue dval = structBuilder.getDValue();
		return dval;
	}
	
	private DValue readStructDValue(ResultSet rs, DStructType dtype, DBAccessContext dbctx, RenderedField rf, ColumnReadInfo rsstuff) throws SQLException {
		StructValueBuilder structBuilder = new StructValueBuilder(dtype);
		for(TypePair pair: dtype.getAllFields()) {
			
			if (pair.type.isStructShape()) {
				//FK only goes in child so it may not be here.
				//However if .fks() was used then it is
				if (ResultSetHelper.hasColumn(rs, pair.name)) {
					rsstuff.numColumnsRead++;
					String strValue = rs.getString(pair.name);
					if (rs.wasNull()) {
						structBuilder.addField(pair.name, null);
					} else {
						DValue inner = createRelation(dtype, pair, strValue, dbctx, rf);
						structBuilder.addField(pair.name, inner);
					}
				}
			} else {
				DValue inner = readField(pair, rs, dbctx);
				structBuilder.addField(pair.name, inner);
				rsstuff.numColumnsRead++;
			}
		}
		boolean b = structBuilder.finish();
		if (! b) {
			DeliaError err = structBuilder.getValidationErrors().get(0); //TODO: support multiple later
			//TODO: why does the err not have fieldname and typename set? fix.
			throw new ValueException(err); 
		}
		DValue dval = structBuilder.getDValue();
		return dval;
	}

	private DValue createRelation(DStructType structType, TypePair targetPair, String strValue, DBAccessContext dbctx, RenderedField rf) throws SQLException {
		//get as string and let builder convert
		String s = strValue;
		
		ScalarValueBuilder xbuilder = factorySvc.createScalarValueBuilder(dbctx.registry);
		DValue keyVal;
		if (rf != null) {
			Shape shape = rf.pair.type.getShape();
			if (rf.pair.type.isStructShape()) {
				PrimaryKey pk = ((DStructType)rf.pair.type).getPrimaryKey();
				shape = pk.getKey().type.getShape();
			}
			
			keyVal = dvalConverter.buildFromObject(s, shape, xbuilder);
		} else {
			keyVal = xbuilder.buildInt(s);
		}
		
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