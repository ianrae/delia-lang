package org.delia.db.postgres;

import java.sql.SQLException;
import java.util.List;

import org.delia.db.DBErrorConverterBase;
import org.delia.db.DBException;
import org.delia.db.DBValidationException;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.table.TableInfo;
import org.delia.error.DeliaError;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.postgresql.util.PSQLException;

public class PostgresErrorConverter extends DBErrorConverterBase {
	
	private SqlNameFormatter nameFormatter;

	public PostgresErrorConverter(SqlNameFormatter nameFormatter) {
		this.nameFormatter = nameFormatter;
	}

	@Override
	public void convertAndRethrowException(SQLException e) {
		printStackTraceIfEnabled(e);
		if (isPSQLExceptionWith(e, "duplicate key value violates unique")) {
			throw new DBValidationException(makeError("duplicate-unique-value", e));
		} else 		if (isPSQLExceptionWith(e, "violates foreign key constraint")) {
			throw new DBValidationException(makeError("db-validation-fail", e));
		} else if (e instanceof JdbcSQLIntegrityConstraintViolationException) {
			throw new DBValidationException(makeError("db-validation-fail", e));
		} else if (e instanceof JdbcSQLIntegrityConstraintViolationException) {
			throw new DBValidationException(makeError("db-validation-fail", e));
		} else {
			DeliaError err = new DeliaError("db-unexpected-exception", "UNEXPECTED error: " + e.getMessage());
			throw new DBException(err);
		}
	}
	
	private DeliaError makeError(String id, Exception e) {
		DeliaError err = new DeliaError(id, e.getMessage());
		return err;
	}
	
	
	private boolean isPSQLExceptionWith(SQLException e, String string) {
		if (e instanceof PSQLException && e.getMessage().contains(string)) {
			return true;
		}
		return false;
	}


	@Override
	public void convertAndRethrow(DBValidationException e, List<TableInfo> tblinfoL) {
		String msg = e.getMessage();
		if (msg.contains("Unique index or primary key violation")) {
			//TODO: we need to parse the error more to figure out which field(s) has the failure.
			DeliaError err = new DeliaError("duplicate-unique-value", e.getLastError().getMsg());
			throw new DBValidationException(err);
		} else if (msg.contains("violates foreign key constraint") || isRelationError(msg)) {
			//TODO: we need to parse the error more to figure out which field(s) has the failure.
			boolean isManyRule = findTypeOfViolation(e.getMessage(), tblinfoL);
			String errId = isManyRule ? "rule-relationMany" : "rule-relationOne";
			DeliaError err = new DeliaError(errId, e.getLastError().getMsg());
			throw new DBValidationException(err);
		} else {
			throw e;
		}
	}

	//org.postgresql.util.PSQLException: ERROR: insert or update on table "addresscustomerassoc" violates foreign key constraint "addresscustomerassoc_rightv_fkey"
//	  Detail: Key (rightv)=(44) is not present in table "customer".
	private boolean isRelationError(String msg) {
		if (msg.contains("insert or update on table") && msg.contains("violates foreign key constraint")) {
			return true;
		}
		return false;
	}

	private boolean findTypeOfViolation(String message, List<TableInfo> tblinfoL) {
		for(TableInfo info: tblinfoL) {
			if (info.assocTblName != null && message.contains(nameFormatter.convert(info.assocTblName))) {
				return true;
			}
		}
		return false;
	}

}
