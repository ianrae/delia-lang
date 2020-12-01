package org.delia.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
	
	
	private DValue createEmptyRelation(DBAccessContext dbctx, DStructType structType, String mergeOnField) {
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
			
			//first columns are the main object
			ColumnReadInfo columnReadInfo = new ColumnReadInfo();
			DValue dval = null; //readStructDValue(rsw, dtype, dbctx, rf, columnReadInfo);
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
				
				run = new ColumnRun(i, dtype);
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
			String fieldName = StringUtils.substringAfter(rff.field, " as ");
			DType type = DValueHelper.findFieldType(currentType, fieldName);
			if (type != null) {
				return currentType;
			}
			return (DStructType)type; //is this right?
		}
	}
}