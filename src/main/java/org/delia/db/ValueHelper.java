package org.delia.db;

import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.core.TimeZoneService;
import org.delia.dval.DValueConverterService;
import org.delia.sql.DBAccessContext;
import org.delia.type.*;
import org.delia.util.BlobUtils;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ValueHelper extends ServiceBase {
    protected DateFormatService fmtSvc;
    protected DValueConverterService dvalConverter;
//	protected BlobCreator blobCreator;

    public ValueHelper(FactoryService factorySvc) { //, BlobCreator blobCreator) {
        super(factorySvc);
        this.fmtSvc = factorySvc.getDateFormatService();
        this.dvalConverter = new DValueConverterService(factorySvc);
//		this.blobCreator = blobCreator;
    }

    public PreparedStatement createPrepStatement(SqlStatement statement, Connection conn) throws SQLException {
        PreparedStatement stm = conn.prepareStatement(statement.sql);
        return xcreatePrepStatement(stm, statement, conn);
    }

    public PreparedStatement createPrepStatementWithGenKey(SqlStatement statement, Connection conn) throws SQLException {
        PreparedStatement stm = conn.prepareStatement(statement.sql, Statement.RETURN_GENERATED_KEYS);
        return xcreatePrepStatement(stm, statement, conn);
    }

    protected PreparedStatement xcreatePrepStatement(PreparedStatement stm, SqlStatement statement, Connection conn) throws SQLException {
        int index = 1;
        for (DValue dval : statement.paramL) {
            index = doCreatePrepStatement(stm, dval, index);
        }

        return stm;
    }

    protected int doCreatePrepStatement(PreparedStatement stm, DValue dval, int index) throws SQLException {
        if (dval == null) {
            stm.setObject(index++, null);
            return index;
        }

        switch (dval.getType().getShape()) {
            case INTEGER:
                if (EffectiveShape.EFFECTIVE_LONG.equals(dval.getType().getEffectiveShape())) {
                    stm.setLong(index++, dval.asLong());
                } else {
                    stm.setInt(index++, dval.asInt());
                }
                break;
//            case LONG:
//                stm.setLong(index++, dval.asLong());
//                break;
            case NUMBER:
                stm.setDouble(index++, dval.asNumber());
                break;
            case BOOLEAN:
                stm.setBoolean(index++, dval.asBoolean());
                break;
            case STRING:
                stm.setString(index++, dval.asString());
                break;
            case DATE: {
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
            case BLOB: {
                //h2 and postgres both use hex format
                String base64Str = dval.asString();
                String hex = BlobUtils.base64ToHexString(base64Str);
                stm.setString(index++, hex);
                //TODO: use stm.setBlob later
            }
            break;
            case STRUCT: {
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

    public DValue extractGeneratedKey(ResultSet rs, int pkFieldIndex, Shape keyShape, EffectiveShape effectiveShape, DTypeRegistry registry) throws SQLException {
        ScalarValueBuilder dvalBuilder = factorySvc.createScalarValueBuilder(registry);
        pkFieldIndex++; //result sets start at 1 for indexs

        DValue genVal = null;
        while (rs.next()) {
            switch (keyShape) {
                case INTEGER: {
                    if (EffectiveShape.EFFECTIVE_LONG.equals(effectiveShape)) {
                        Long x = rs.getLong(pkFieldIndex);
                        log.log("DB: gennKey(long): %d", x);
                        if (!rs.wasNull()) {
                            genVal = dvalBuilder.buildInt(x);
                        }
                    } else {
                        Integer x = rs.getInt(pkFieldIndex);
                        log.log("DB: gennKey(int): %d", x);
                        if (!rs.wasNull()) {
                            genVal = dvalBuilder.buildInt(x);
                        }
                    }
                }
                break;
//                case LONG: {
//                    Long x = rs.getLong(1);
//                    log.log("DB: gennKey(long): %d", x);
//                    if (!rs.wasNull()) {
//                        genVal = dvalBuilder.buildLong(x);
//                    }
//                }
//                break;
                case STRING: {
                    String x = rs.getString(pkFieldIndex);
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

        switch (pair.type.getShape()) {
            case INTEGER: {
                if (EffectiveShape.EFFECTIVE_LONG.equals(pair.type.getEffectiveShape())) {
                    Long x = rs.getLong(pair.name);
                    if (rs.wasNull()) {
                        return null;
                    }
                    return dvalBuilder.buildInt(x, pair.type);
                } else {
                    Integer x = rs.getInt(pair.name);
                    if (rs.wasNull()) {
                        return null;
                    }
                    return dvalBuilder.buildInt(x, pair.type);
                }
            }
//		case LONG:
//		{
//			Long x = rs.getLong(pair.name);
//			if (rs.wasNull()) {
//				return null;
//			}
//			return dvalBuilder.buildLong(x, pair.type);
//		}
            case NUMBER: {
                Double x = rs.getDouble(pair.name);
                if (rs.wasNull()) {
                    return null;
                }
                return dvalBuilder.buildNumber(x, pair.type);
            }
            case DATE: {
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
            case BLOB: {
                byte[] byteArr = rs.getBytes(pair.name);
                if (rs.wasNull()) {
                    return null;
                }
                return dvalBuilder.buildBlob(byteArr, pair.type);
            }
            case BOOLEAN: {
                Boolean x = rs.getBoolean(pair.name);
                if (rs.wasNull()) {
                    return null;
                }
                return dvalBuilder.buildBoolean(x, pair.type);
            }
            case STRING: {
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

        if (pair.type.isStructShape()) {
            pair = DValueHelper.findPrimaryKeyFieldPair(pair.type);
            //read the primary key value
        }

        switch (pair.type.getShape()) {
            case INTEGER: {
                if (EffectiveShape.EFFECTIVE_LONG.equals(pair.type.getEffectiveShape())) {
                    Long x = rs.getLong(index);
                    if (rs.wasNull()) {
                        return null;
                    }
                    return dvalBuilder.buildInt(x, pair.type);
                } else {
                    Integer x = rs.getInt(index);
                    if (rs.wasNull()) {
                        return null;
                    }
                    return dvalBuilder.buildInt(x, pair.type);
                }
            }
//		case LONG:
//		{
//			Long x = rs.getLong(index);
//			if (rs.wasNull()) {
//				return null;
//			}
//			return dvalBuilder.buildLong(x, pair.type);
//		}
            case NUMBER: {
                Double x = rs.getDouble(index);
                if (rs.wasNull()) {
                    return null;
                }
                return dvalBuilder.buildNumber(x, pair.type);
            }
            case DATE: {
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
            case BLOB: {
                byte[] byteArr = rs.getBytes(index);
                if (rs.wasNull()) {
                    return null;
                }
                return dvalBuilder.buildBlob(byteArr, pair.type);
            }
            case BOOLEAN: {
                Boolean x = rs.getBoolean(index);
                if (rs.wasNull()) {
                    return null;
                }
                return dvalBuilder.buildBoolean(x, pair.type);
            }
            case STRING: {
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

//		boolean b = rs.next(); //assume we're reading 1st record
//		if (!b) {
//			return null;
//		}

        switch (type.getShape()) {
            case INTEGER: {
                if (EffectiveShape.EFFECTIVE_LONG.equals(type.getEffectiveShape())) {
                    Long x = rs.getLong(rsIndex);
                    if (rs.wasNull()) {
                        return null;
                    }
                    return dvalBuilder.buildInt(x, type);
                } else {
                    Integer x = rs.getInt(rsIndex);
                    if (rs.wasNull()) {
                        return null;
                    }
                    return dvalBuilder.buildInt(x, type);
                }
            }
//		case LONG:
//		{
//			Long x = rs.getLong(rsIndex);
//			if (rs.wasNull()) {
//				return null;
//			}
//			return dvalBuilder.buildLong(x, type);
//		}
            case NUMBER: {
                Double x = rs.getDouble(rsIndex);
                if (rs.wasNull()) {
                    return null;
                }
                return dvalBuilder.buildNumber(x, type);
            }
            case DATE: {
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
            case BLOB: {
                byte[] byteArr = rs.getBytes(rsIndex);
                if (rs.wasNull()) {
                    return null;
                }
                return dvalBuilder.buildBlob(byteArr, type);
            }
            case BOOLEAN: {
                Boolean x = rs.getBoolean(rsIndex);
                if (rs.wasNull()) {
                    return null;
                }
                return dvalBuilder.buildBoolean(x, type);
            }
            case STRING: {
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

//    public DValue valueInSql(Shape shape, Object value, DTypeRegistry registry) {
//        ScalarValueBuilder dvalBuilder = factorySvc.createScalarValueBuilder(registry);
//        switch (shape) {
//            case INTEGER:
//                if (value instanceof String) {
//                    return dvalBuilder.buildInt((String) value);
//                }
//                return dvalBuilder.buildInt((Integer) value);
////		case LONG:
////			if (value instanceof String) {
////				return dvalBuilder.buildLong((String)value);
////			} else if (value instanceof Integer) {
////				Integer n = (Integer) value;
////				return dvalBuilder.buildLong(n.longValue());
////			}
////			return dvalBuilder.buildLong((Long)value);
//            case NUMBER:
//                if (value instanceof String) {
//                    return dvalBuilder.buildNumber((String) value);
//                } else if (value instanceof Integer) {
//                    Integer n = (Integer) value;
//                    return dvalBuilder.buildNumber(n.doubleValue());
//                } else if (value instanceof Long) {
//                    Long n = (Long) value;
//                    return dvalBuilder.buildNumber(n.doubleValue());
//                }
//                return dvalBuilder.buildNumber((Double) value);
//            case BOOLEAN:
//                if (value instanceof String) {
//                    return dvalBuilder.buildBoolean((String) value);
//                }
//                return dvalBuilder.buildBoolean((Boolean) value);
//            case STRING:
//                return dvalBuilder.buildString(value.toString());
//            case DATE:
//                if (value instanceof String) {
////				String s = convertDateStringToSQLTimestamp((String) value);
//                    return dvalBuilder.buildDate((String) value);
//                } else if (value instanceof WrappedDate) {
//                    WrappedDate wdt = (WrappedDate) value;
//                    String s = convertDateToSQLTimestamp(wdt.getDate());
//                    return dvalBuilder.buildString(s);
//                }
//                return dvalBuilder.buildString(value.toString());
//            //TODO: blob
//            case STRUCT:
//                if (value instanceof Integer) {
//                    Integer n = (Integer) value;
//                    return dvalBuilder.buildInt(n);
////			} else if (value instanceof Long) {
////				Long n = (Long) value;
////				return dvalBuilder.buildLong(n);
//                } else {
//                    return dvalBuilder.buildString(value.toString());
//                }
//            default:
//                return dvalBuilder.buildString("");
//        }
//    }

//    /**
//     * FUTURE: this probably needs to become db-specific
//     *
//     * @param zdt date
//     * @return date as string in sql format
//     */
//    protected String convertDateToSQLTimestamp(ZonedDateTime zdt) {
//        //TIMESTAMP '1999-01-31 10:00:00'
//        DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String s = zdt.format(sdf);
//
//        return String.format("'%s'", s);
//    }

}