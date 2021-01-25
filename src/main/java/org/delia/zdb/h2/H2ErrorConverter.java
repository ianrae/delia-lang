package org.delia.zdb.h2;

import java.sql.SQLException;
import java.util.List;

import org.delia.db.DBErrorConverterBase;
import org.delia.db.DBException;
import org.delia.db.DBValidationException;
import org.delia.db.sql.table.TableInfo;
import org.delia.error.DeliaError;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;

public class H2ErrorConverter extends DBErrorConverterBase {

	
	@Override
	public void convertAndRethrowException(SQLException e) {
		printStackTraceIfEnabled(e);
		if (e instanceof JdbcSQLIntegrityConstraintViolationException) {
			//				JdbcSQLIntegrityConstraintViolationException ex = (JdbcSQLIntegrityConstraintViolationException) e;

			DeliaError err = new DeliaError("db-validation-fail", e.getMessage());
			throw new DBValidationException(err);
		} else {
			DeliaError err = new DeliaError("db-unexpected-exception", "UNEXPECTED error: " + e.getMessage());
			throw new DBException(err);
		}
	}
	
	
	@Override
	public void convertAndRethrow(DBValidationException e, List<TableInfo> tblinfoL) {
		String msg = e.getMessage();
		if (msg.contains("Unique index or primary key violation")) {
			//TODO: we need to parse the error more to figure out which field(s) has the failure.
			DeliaError err = new DeliaError("duplicate-unique-value", e.getLastError().getMsg());
			throw new DBValidationException(err);
		} else if (msg.contains("Referential integrity constraint violation")) {
			//TODO: we need to parse the error more to figure out which field(s) has the failure.
			boolean isManyRule = findTypeOfViolation(e.getMessage(), tblinfoL);
			String errId = isManyRule ? "rule-relationMany" : "rule-relationOne";
			DeliaError err = new DeliaError(errId, e.getLastError().getMsg());
			throw new DBValidationException(err);
		} else {
			throw e;
		}
	}

	private boolean findTypeOfViolation(String message, List<TableInfo> tblinfoL) {
		for(TableInfo info: tblinfoL) {
			if (info.assocTblName != null && message.contains(info.assocTblName.toUpperCase())) {
				return true;
			}
		}
		return false;
	}

}
