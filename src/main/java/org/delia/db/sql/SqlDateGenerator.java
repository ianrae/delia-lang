package org.delia.db.sql;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.type.WrappedDate;

public class SqlDateGenerator extends ServiceBase {
	private DTypeRegistry registry;
	private DateFormatService fmtSvc;

	public SqlDateGenerator(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.fmtSvc = factorySvc.getDateFormatService();
	}

	String dateValueInSql(Object value) {
		if (value instanceof String) {
			return convertDateStringToSQLTimestamp((String) value);
		} else if (value instanceof WrappedDate) {
			WrappedDate wdt = (WrappedDate) value;
			return convertDateToSQLTimestamp(wdt.getDate());
			//					return wdt.asString();
		}
		return String.format("%s", value.toString());
	}

	public String convertDateStringToSQLTimestamp(String value) {
		ZonedDateTime zdt = fmtSvc.parseDateTime(value);
		return convertDateToSQLTimestamp(zdt);
	}

	/**
	 * FUTURE: this probably needs to become db-specific
	 * @param dt date
	 * @return string representing date in sql format
	 */
	private String convertDateToSQLTimestamp(ZonedDateTime zdt) {
		//TIMESTAMP '1999-01-31 10:00:00'
		DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String s = zdt.format(sdf);
		return String.format("'%s'", s);
	}

	DType findFieldType(DStructType dtype, String fieldName) {
		for(TypePair pair: dtype.getAllFields()) {
			if (pair.name.equals(fieldName)) {
				return pair.type;
			}
		}
		return null;
	}
}