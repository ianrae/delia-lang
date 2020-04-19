//package org.delia.db.sql;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.TimeZone;
//
//import org.delia.compiler.ast.BooleanExp;
//import org.delia.compiler.ast.FilterExp;
//import org.delia.compiler.ast.FilterOpFullExp;
//import org.delia.compiler.ast.QueryExp;
//import org.delia.core.DateFormatService;
//import org.delia.core.FactoryService;
//import org.delia.core.ServiceBase;
//import org.delia.core.TimeZoneService;
//import org.delia.db.QuerySpec;
//import org.delia.type.DStructType;
//import org.delia.type.DType;
//import org.delia.type.DTypeRegistry;
//import org.delia.type.Shape;
//import org.delia.type.TypePair;
//import org.delia.type.WrappedDate;
//import org.delia.util.DValueHelper;
//
//public class SqlQueryGenerator extends ServiceBase {
//	private DTypeRegistry registry;
//	private DateFormatService fmtSvc;
//	private QueryTypeDetector queryDetectorSvc;
//
//	public SqlQueryGenerator(FactoryService factorySvc, DTypeRegistry registry) {
//		super(factorySvc);
//		this.registry = registry;
//		this.fmtSvc = factorySvc.getDateFormatService();
//		this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
//	}
//
//	public String generateQuery(QuerySpec spec) {
//		StrCreator sc = new StrCreator();
//		QueryExp exp = spec.queryExp;
//		String typeName = exp.getTypeName();
//		sc.o("SELECT * FROM %s", typeName);
//
//		QueryType queryType = queryDetectorSvc.detectQueryType(spec);
//		switch(queryType) {
//		case ALL_ROWS:
//			break;
//		case OP:
//			addWhereClauseOp(sc, spec, typeName);
//			break;
//		case PRIMARY_KEY:
//		default:
//			addWhereClausePrimaryKey(sc, exp.filter, typeName);
//			break;
//		}
//
//		sc.o(";");
//		return sc.str;
//	}
//	
//	private void addWhereClauseOp(StrCreator sc, QuerySpec spec, String typeName) {
//		queryDetectorSvc.addWhereClauseOp(sc, spec, typeName, null);
//	}
//
//	private void addWhereClausePrimaryKey(StrCreator sc, FilterExp filter, String typeName) {
//		if (filter != null) {
//			DStructType type = (DStructType) registry.findTypeOrSchemaVersionType(typeName);
//			String keyField = DValueHelper.findUniqueField(type);
//			if (keyField == null) {
//				//err!!
//				return;
//			}
//
//			DType inner = findFieldType(type, keyField);
//			String val = valueInSql(inner.getShape(), filter.cond.strValue());
//			sc.o(" WHERE %s=%s", keyField, val);
//		}
//	}
//
//	String valueInSql(Shape shape, Object value) {
//		switch(shape) {
//		case INTEGER:
//			if (value instanceof String) {
//				return (String)value;
//			}
//			return String.format("%d", (Integer)value);
//		case LONG:
//			if (value instanceof String) {
//				return (String)value;
//			}
//			return String.format("%d", (Long)value);
//		case NUMBER:
//			if (value instanceof String) {
//				return (String)value;
//			}
//			return String.format("%g", (Double)value);
//		case BOOLEAN:
//			if (value instanceof String) {
//				return (String)value;
//			}
//			return String.format("%b", (Boolean)value);
//		case STRING:
//			return String.format("'%s'", value.toString());
//		case DATE:
//			if (value instanceof String) {
//				return convertDateStringToSQLTimestamp((String) value);
//			} else if (value instanceof WrappedDate) {
//				WrappedDate wdt = (WrappedDate) value;
//				return convertDateToSQLTimestamp(wdt.getDate());
//				//					return wdt.asString();
//			}
//			return String.format("%s", value.toString());
//		default:
//			return "";
//		}
//	}
//
//	private String convertDateStringToSQLTimestamp(String value) {
//		Date dt = fmtSvc.parse(value);
//		return convertDateToSQLTimestamp(dt);
//	}
//
//	/**
//	 * TODO: this probably needs to become db-specific
//	 * @param wdt
//	 * @return
//	 */
//	private String convertDateToSQLTimestamp(Date dt) {
//		//TIMESTAMP '1999-01-31 10:00:00'
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		TimeZoneService tzSvc = factorySvc.getTimeZoneService();
//		TimeZone tz = tzSvc.getDefaultTimeZone();
//		sdf.setTimeZone(tz);
//
//		String s = sdf.format(dt);
//		return String.format("'%s'", s);
//	}
//
//	DType findFieldType(DStructType dtype, String fieldName) {
//		for(TypePair pair: dtype.getAllFields()) {
//			if (pair.name.equals(fieldName)) {
//				return pair.type;
//			}
//		}
//		return null;
//	}
//
//	private QueryType sfsdf(QuerySpec spec) {
//		if (wantsAllRows(spec)) { 
//			return QueryType.ALL_ROWS;
//		} else if (spec.queryExp.filter.cond instanceof FilterOpFullExp) {
//			return QueryType.OP;
//		} else {
//			return QueryType.PRIMARY_KEY;
//		}
//	}
//
//	private boolean wantsAllRows(QuerySpec spec) {
//		if (spec.queryExp.filter == null) {
//			return true;
//		} else if (spec.queryExp.filter.cond instanceof BooleanExp) {
//			BooleanExp exp = (BooleanExp) spec.queryExp.filter.cond;
//			return exp.val;
//		}
//		return false;
//	}
//}