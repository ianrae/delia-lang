package org.delia.sql;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBException;
import org.delia.db.DBType;
import org.delia.db.ValueHelper;
import org.delia.db.sql.ConnectionFactory;
import org.delia.dval.compare.DValueCompareService;
import org.delia.error.DeliaError;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.RelationValueBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
	protected DValueCompareService compareSvc;

	public HLDResultSetConverterBase(DBType dbType, FactoryService factorySvc, ConnectionFactory connFactory) {
		super(factorySvc);
		//TODO fix null on next line
		this.valueHelper =  new ValueHelper(factorySvc); //, null); //sqlhelperFactory.createValueHelper();
	}
	public HLDResultSetConverterBase(FactoryService factorySvc, ValueHelper valueHelper) {
		super(factorySvc);
		this.valueHelper = valueHelper;
	}

	/**
	 * we can only have generated one key even if did several inserts because
	 * the additional inserts are just the assoc table.
	 */
	public DValue extractGeneratedKey(List<ResultSet> genKeysL, TypePair pkpair, int pkFieldIndex, DBAccessContext dbactx) throws SQLException {
		for(ResultSet rs: genKeysL) {
			DValue genVal = valueHelper.extractGeneratedKey(rs, pkFieldIndex, pkpair.type.getShape(), pkpair.type.getEffectiveShape(), dbactx.registry);
			if (genVal != null) {
				return genVal;
			}
		}
		return null;
	}

	public List<DValue> buildScalarResult(ResultSet rs, ResultTypeInfo selectResultType, DBAccessContext dbctx) {
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


}