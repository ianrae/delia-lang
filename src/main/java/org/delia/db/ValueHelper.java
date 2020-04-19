package org.delia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NullExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.core.TimeZoneService;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.type.WrappedDate;
import org.delia.valuebuilder.ScalarValueBuilder;

public class ValueHelper extends ServiceBase {


	private DateFormatService fmtSvc;

	public ValueHelper(FactoryService factorySvc) {
		super(factorySvc);
		this.fmtSvc = factorySvc.getDateFormatService();
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
			if (dval == null) {
				stm.setObject(index++, null);
				continue;
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
				TimeZone tz = tzSvc.getDefaultTimeZone();
				Calendar cal = Calendar.getInstance(tz);
				cal.setTime(dval.asDate());
				Date dt = cal.getTime();
				Timestamp ts = new Timestamp(dt.getTime());
				stm.setTimestamp(index++, ts, cal);
			}
			break;
			//			case RELATION:
			//				//FIX
			//				break;
			}
		}

		return stm;
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
			TimeZone tz = tzSvc.getDefaultTimeZone();
			Calendar cal = Calendar.getInstance(tz);
			Date x = rs.getTimestamp(pair.name, cal);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildDate(x, pair.type);
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
	
	public DValue readIndexedField(DType type, int rsIndex, ResultSet rs, DBAccessContext dbctx) throws SQLException {
		ScalarValueBuilder dvalBuilder = factorySvc.createScalarValueBuilder(dbctx.registry);

		boolean b = rs.next(); //assume rsIndex always 1. TODO fix if needed
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
			TimeZone tz = tzSvc.getDefaultTimeZone();
			Calendar cal = Calendar.getInstance(tz);
			Date x = rs.getTimestamp(rsIndex, cal);
			if (rs.wasNull()) {
				return null;
			}
			return dvalBuilder.buildDate(x, type);
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
	private String convertDateStringToSQLTimestamp(String value) {
		Date dt = fmtSvc.parse(value);
		return convertDateToSQLTimestamp(dt);
	}

	/**
	 * TODO: this probably needs to become db-specific
	 * @param dt date
	 * @return date as string in sql format
	 */
	private String convertDateToSQLTimestamp(Date dt) {
		//TIMESTAMP '1999-01-31 10:00:00'
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZoneService tzSvc = factorySvc.getTimeZoneService();
		TimeZone tz = tzSvc.getDefaultTimeZone();
		sdf.setTimeZone(tz);

		String s = sdf.format(dt);
		return String.format("'%s'", s);
	}

	public Object extractObj(Exp exp) {
		if (exp instanceof NullExp) {
			return null;
		} else if (exp instanceof IntegerExp) {
			Integer n = ((IntegerExp)exp).val;
			return n;
		} else if (exp instanceof LongExp) {
			Long n = ((LongExp)exp).val;
			return n;
		} else if (exp instanceof NumberExp) {
			Double n = ((NumberExp)exp).val;
			return n;
		} else if (exp instanceof BooleanExp) {
			Boolean n = ((BooleanExp)exp).val;
			return n;
		} else if (exp instanceof StringExp) {
			return exp.strValue();
		} else {
			//Do date and relation later: TODO
			return exp.strValue();
		}
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