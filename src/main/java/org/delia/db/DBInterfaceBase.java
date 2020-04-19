package org.delia.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.h2.SqlHelperFactory;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionString;
import org.delia.db.sql.prepared.FKSqlGenerator;
import org.delia.db.sql.prepared.InsertStatementGenerator;
import org.delia.db.sql.prepared.PreparedStatementGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.TableCreator;
import org.delia.db.sql.table.TableInfo;
import org.delia.error.DeliaError;
import org.delia.log.Log;
import org.delia.log.LogLevel;
import org.delia.log.SimpleLog;
import org.delia.runner.ValueException;
import org.delia.type.BuiltInTypes;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.valuebuilder.RelationValueBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;

/**
 * Represents db access to a single db (conn)
 * @author Ian Rae
 *
 */
public abstract class DBInterfaceBase extends ServiceBase implements DBInterface {
	protected DBCapabilties capabilities;
	protected Log sqlLog;
	protected TableCreator tableCreator;
	protected ValueHelper valueHelper;
	protected SqlHelperFactory sqlHelperFactory;
	protected ConnectionFactory connFactory;
	protected DBType dbType;
	protected DBErrorConverter errorConverter;
	protected Random random = new Random();

	public DBInterfaceBase(DBType dbType, FactoryService factorySvc, ConnectionFactory connFactory, SqlHelperFactory sqlhelperFactory) {
		super(factorySvc);
		this.dbType = dbType;
		this.capabilities = new DBCapabilties(true, true, true, true);
		this.sqlLog = new SimpleLog();
		this.connFactory = connFactory;
		this.sqlHelperFactory = sqlhelperFactory;
		this.valueHelper = sqlHelperFactory.createValueHelper();
	}

	@Override
	public DBCapabilties getCapabilities() {
		return capabilities;
	}

	@Override
	public abstract DBExecutor createExector(DBAccessContext ctx);

	@Override
	public void init(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
		this.log = factorySvc.getLog();
		this.et = factorySvc.getErrorTracker();
	}

	protected DValue extractGeneratedKey(InsertContext ctx, SqlExecuteContext sqlctx) throws SQLException {
		ResultSet rs = sqlctx.genKeys;
		DValue genVal = valueHelper.extractGeneratedKey(rs, ctx.genKeytype.getShape(), sqlctx.registry);
		return genVal;
	}

	protected void logSql(String sql) {
		sqlLog.log("SQL: " + sql);
	}

	protected List<DValue> buildDValueList(ResultSet rs, DStructType dtype, QueryDetails details, DBAccessContext dbctx) {
		List<DValue> list = null;
		try {
			list = doBuildDValueList(rs, dtype, dbctx);
			if (details.mergeRows) {
				list = mergeRows(list, dtype, details);
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
	
	
	protected List<DValue> buildScalarResult(ResultSet rs, DType selectResultType, QueryDetails details, DBAccessContext dbctx) {
		List<DValue> list = new ArrayList<>();
		try {
			DValue dval = valueHelper.readIndexedField(selectResultType, 1, rs, dbctx);
			list.add(dval);
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
	

	/**
	 * On a One-to-many relation our query returns multiple rows in order to get all
	 * the 'many' ids. Merge into a single row.
	 */
	protected List<DValue> mergeRows(List<DValue> rawList, DStructType dtype, QueryDetails details) {
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

	protected List<DValue> doBuildDValueList(ResultSet rs, DStructType dtype, DBAccessContext dbctx) throws Exception {
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
	
	protected DValue createRelation(DStructType structType, TypePair targetPair, ResultSet rs, DBAccessContext dbctx) throws SQLException {
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

	protected DValue readField(TypePair pair, ResultSet rs, DBAccessContext dbctx) throws SQLException {
		return valueHelper.readField(pair, rs, dbctx);
	}

	protected PreparedStatementGenerator createPrepSqlGen(DBAccessContext dbctx) {
		return sqlHelperFactory.createPrepSqlGen(dbctx);
	}
	protected InsertStatementGenerator createPrepInsertSqlGen(DBAccessContext dbctx) {
		return sqlHelperFactory.createPrepInsertSqlGen(dbctx);
	}
	protected synchronized TableCreator createTableCreator(DBAccessContext dbctx) {
		if (tableCreator == null) {
			this.tableCreator = sqlHelperFactory.createTableCreator(dbctx);
		}
		return tableCreator;
	}
	protected FKSqlGenerator createFKSqlGen(List<TableInfo> tblinfoL, DBAccessContext dbctx) {
		return sqlHelperFactory.createFKSqlGen(tblinfoL, dbctx);
	}
	
	@Override
	public boolean isSQLLoggingEnabled() {
		return !LogLevel.OFF.equals(sqlLog.getLevel());
	}
	
	@Override
	public void enableSQLLogging(boolean b) {
		if (b) {
			sqlLog.setLevel(LogLevel.INFO);
		} else {
			sqlLog.setLevel(LogLevel.OFF);
		}
	}
	protected void logSql(SqlStatement statement) {
		StringJoiner joiner = new StringJoiner(",");
		for(DValue dval: statement.paramL) {
			if (dval.getType().isShape(Shape.STRING)) {
				joiner.add(String.format("'%s'", dval.asString()));
			} else {
				joiner.add(dval == null ? "null" : dval.asString());
			}
		}
		
		String s = String.format("%s  -- (%s)", statement.sql, joiner.toString());
		logSql(s);
	}
	@Override
	public DBType getDBType() {
		return dbType;
	}
	
	protected String generateUniqueConstraintName() {
		int n = random.nextInt(Integer.MAX_VALUE - 10);
		return String.format("DConstraint_%d", n);
	}
}