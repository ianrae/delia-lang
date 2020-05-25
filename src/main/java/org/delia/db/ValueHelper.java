package org.delia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.delia.compiler.ast.Exp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.core.TimeZoneService;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.dval.DValueConverterService;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.type.WrappedDate;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

public class ValueHelper extends ServiceBase {
	private DateFormatService fmtSvc;
	private DValueConverterService dvalConverter;

	public ValueHelper(FactoryService factorySvc) {
		super(factorySvc);
		this.fmtSvc = factorySvc.getDateFormatService();
		this.dvalConverter = new DValueConverterService(factorySvc);
	}
	
	public PreparedStatement createPrepStatement(SqlStatement statement, Connection conn) throws SQLException {
		PreparedStatement stm = conn.prepareStatement(statement.sql);
		return xcreatePrepStatement(stm, statement, conn);
	}
	public PreparedStatement createPrepStatementWithGenKey(SqlStatement statement, Connection conn) throws SQLException {
		PreparedStatement stm = conn.prepareStatement(statement.sql, Statement.RETURN_GENERATED_KEYS);
		return xcreatePrepStatement(stm, statement, conn);
	}
	private PreparedStatement xcreatePrepStatement(PreparedStatement stm, SqlStatement statement, Connection conn) throws SQLException {
		int index = 1;
		for(DValue dval: statement.paramL) {
			index = doCreatePrepStatement(stm, dval, index);
		}

		return stm;
	}
	private int doCreatePrepStatement(PreparedStatement stm, DValue dval, int index) throws SQLException {
		if (dval == null) {
			stm.setObject(index++, null);
			return index;
		}

		switch(dval.getType().getShape()) {
		case INTEGER:
			stm.setInt(index++, dval.asInt());
			break;
		case LONG:
			stm.setLong(index++, dval.asLong());
			break;
		case NUMBER:
			stm.setDouble(index++, dval.asNumber());
			break;
		case BOOLEAN:
			stm.setBoolean(index++, dval.asBoolean());
			break;
		case STRING:
			stm.setString(index++, dval.asString());
			break;
		case DATE:
		{
			TimeZoneService tzSvc = factorySvc.getTimeZoneService();
			ZoneId zoneId = tzSvc.getDefaultTimeZone();
			TimeZone tz = TimeZone.getTimeZone(zoneId);
			Calendar cal = Calendar.getInstance(tz);
			cal.setTime(dval.asLegacyDate());
			Date dt = dval.asLegacyDate();
			Timestamp ts = new Timestamp(dt.getTime()); //TODO find way that doesn't lose nano seconds
			stm.setTimestamp(index++, ts, cal);
		}
		break;
		case STRUCT:
		{
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
			DValue key = DValueHelper.getFieldValue(dval, pair.name);
			return doCreatePrepStatement(stm, key, index);
		}
		//			case RELATION:
		//				//FIX
		//				break;
		default:
			break;
		}
		return index;
	}
	
	protected DValue extractGeneratedKey(ResultSet rs, Shape keyShape, DTypeRegistry registry) throws SQLException {
		ScalarValueBuilder dvalBuilder = factorySvc.createScalarValueBuilder(registry);

		DValue genVal = null;
		while(rs.next()) {
			switch(keyShape) {
			case INTEGER:
			{
				Integer x = rs.getInt(1);
				log.log("DB: gennKey(int): %d", x);
				if (!rs.wasNull()) {
					genVal = dvalBuilder.buildInt(x);
				}
			}
			break;
			case LONG:
			{
				Long x = rs.getLong(1);
				log.log("DB: gennKey(long): %d", x);
				if (!rs.wasNull()) {
					genVal = dvalBuilder.buildLong(x);
				}
			}
			break;
			case STRING:
			{
				String x = rs.getString(1);
				log.log("DB: gennKey(string): %s", x);
				if (!rs.wasNull()) {
					genVal = dvalBuilder.buildString(x);
				}
			}
			break;
			default:
				break; //error!
			}
		}
		return genVal;
	}
	
	
	public DValue readField(TypePair pair, ResultSet rs, DBAccessContext dbctx) throws SQLException {
		ScalarValueBuilder dvalBuilder = factorySvc.createScalarValueBuilder(dbctx.registry);

		switch(pair.type.getShape()) {
		case INTEGER:
		{
			Integer x = rs.getInt(pair.name);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildInt(x, pair.type);
		}
		case LONG:
		{
			Long x = rs.getLong(pair.name);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildLong(x, pair.type);
		}
		case NUMBER:
		{
			Double x = rs.getDouble(pair.name);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildNumber(x, pair.type);
		}
		case DATE:
		{
			TimeZoneService tzSvc = factorySvc.getTimeZoneService();
			ZoneId zoneId = tzSvc.getDefaultTimeZone();
			TimeZone tz = TimeZone.getTimeZone(zoneId);
			Calendar cal = Calendar.getInstance(tz);
			Date x = rs.getTimestamp(pair.name, cal);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildLegacyDate(x, pair.type);
			//				DValue tmp = dvalBuilder.buildDate(x, pair.type);;
			//				this.log.log("x: %s", tmp.asString());
			//				return tmp;
		}
		case BOOLEAN:
		{
			Boolean x = rs.getBoolean(pair.name);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildBoolean(x, pair.type);
		}
		case STRING:
		{
			String s = rs.getString(pair.name);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildString(s, pair.type);
		}
		default:
			return null;
		}
	}
	
	public DValue readFieldByColumnIndex(TypePair pair, ResultSet rs, int index, DBAccessContext dbctx) throws SQLException {
		ScalarValueBuilder dvalBuilder = factorySvc.createScalarValueBuilder(dbctx.registry);

		switch(pair.type.getShape()) {
		case INTEGER:
		{
			Integer x = rs.getInt(index);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildInt(x, pair.type);
		}
		case LONG:
		{
			Long x = rs.getLong(index);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildLong(x, pair.type);
		}
		case NUMBER:
		{
			Double x = rs.getDouble(index);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildNumber(x, pair.type);
		}
		case DATE:
		{
			TimeZoneService tzSvc = factorySvc.getTimeZoneService();
			ZoneId zoneId = tzSvc.getDefaultTimeZone();
			TimeZone tz = TimeZone.getTimeZone(zoneId);
			Calendar cal = Calendar.getInstance(tz);
			Date x = rs.getTimestamp(index, cal);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildLegacyDate(x, pair.type);
			//				DValue tmp = dvalBuilder.buildDate(x, pair.type);;
			//				this.log.log("x: %s", tmp.asString());
			//				return tmp;
		}
		case BOOLEAN:
		{
			Boolean x = rs.getBoolean(index);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildBoolean(x, pair.type);
		}
		case STRING:
		{
			String s = rs.getString(index);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildString(s, pair.type);
		}
		default:
			return null;
		}
	}
	
	public DValue readIndexedField(DType type, int rsIndex, ResultSet rs, DBAccessContext dbctx) throws SQLException {
		ScalarValueBuilder dvalBuilder = factorySvc.createScalarValueBuilder(dbctx.registry);

		boolean b = rs.next(); //assume we're reading 1st record
		if (!b) {
			return null;
		}
		
		switch(type.getShape()) {
		case INTEGER:
		{
			Integer x = rs.getInt(rsIndex);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildInt(x, type);
		}
		case LONG:
		{
			Long x = rs.getLong(rsIndex);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildLong(x, type);
		}
		case NUMBER:
		{
			Double x = rs.getDouble(rsIndex);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildNumber(x, type);
		}
		case DATE:
		{
			TimeZoneService tzSvc = factorySvc.getTimeZoneService();
			ZoneId zoneId = tzSvc.getDefaultTimeZone();
			TimeZone tz = TimeZone.getTimeZone(zoneId);
			Calendar cal = Calendar.getInstance(tz);
			Date x = rs.getTimestamp(rsIndex, cal);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildLegacyDate(x, type);
		}
		case BOOLEAN:
		{
			Boolean x = rs.getBoolean(rsIndex);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildBoolean(x, type);
		}
		case STRING:
		{
			String s = rs.getString(rsIndex);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildString(s, type);
		}
		default:
			return null;
		}
	}
	
	public DValue valueInSql(Shape shape, Object value, DTypeRegistry registry) {
		ScalarValueBuilder dvalBuilder = factorySvc.createScalarValueBuilder(registry);
		switch(shape) {
		case INTEGER:
			if (value instanceof String) {
				return dvalBuilder.buildInt((String)value);
			}
			return dvalBuilder.buildInt((Integer)value);
		case LONG:
			if (value instanceof String) {
				return dvalBuilder.buildLong((String)value);
			} else if (value instanceof Integer) {
				Integer n = (Integer) value;
				return dvalBuilder.buildLong(n.longValue());
			}
			return dvalBuilder.buildLong((Long)value);
		case NUMBER:
			if (value instanceof String) {
				return dvalBuilder.buildNumber((String)value);
			} else if (value instanceof Integer) {
				Integer n = (Integer) value;
				return dvalBuilder.buildNumber(n.doubleValue());
			} else if (value instanceof Long) {
				Long n = (Long) value;
				return dvalBuilder.buildNumber(n.doubleValue());
			}
			return dvalBuilder.buildNumber((Double)value);
		case BOOLEAN:
			if (value instanceof String) {
				return dvalBuilder.buildBoolean((String)value);
			}
			return dvalBuilder.buildBoolean((Boolean)value);
		case STRING:
			return dvalBuilder.buildString(value.toString());
		case DATE:
			if (value instanceof String) {
//				String s = convertDateStringToSQLTimestamp((String) value);
				return dvalBuilder.buildDate((String)value);
			} else if (value instanceof WrappedDate) {
				WrappedDate wdt = (WrappedDate) value;
				String s = convertDateToSQLTimestamp(wdt.getDate());
				return dvalBuilder.buildString(s);
			}
			return dvalBuilder.buildString(value.toString());
		case STRUCT:
			if (value instanceof Integer) {
				Integer n = (Integer) value;
				return dvalBuilder.buildInt(n);
			} else if (value instanceof Long) {
				Long n = (Long) value;
				return dvalBuilder.buildLong(n);
			} else {
				return dvalBuilder.buildString(value.toString());
			}
		default:
			return dvalBuilder.buildString("");
		}
	}

	/**
	 * FUTURE: this probably needs to become db-specific
	 * @param dt date
	 * @return date as string in sql format
	 */
	private String convertDateToSQLTimestamp(ZonedDateTime zdt) {
		//TIMESTAMP '1999-01-31 10:00:00'
		DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String s = zdt.format(sdf);
		
		return String.format("'%s'", s);
	}

	public Object extractObj(Exp exp) {
		return dvalConverter.extractObj(exp);
	}
	
	public void fixupForExist(List<DValue> dvalList, DBAccessContext dbctx) {
		ScalarValueBuilder dvalBuilder = factorySvc.createScalarValueBuilder(dbctx.registry);

		DValue dval = dvalList.get(0);
		dvalList.clear();
		
		long n = dval.asLong();
		dval = dvalBuilder.buildBoolean(n != 0);
		dvalList.add(dval);
	}

	
}