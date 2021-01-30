package org.delia.zdb.h2;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.delia.db.DBErrorConverterBase;
import org.delia.db.DBException;
import org.delia.db.DBValidationException;
import org.delia.db.sql.table.TableInfo;
import org.delia.error.DeliaError;
import org.delia.type.DTypeRegistry;
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
		if (registry != null) {
			doRegistryAwareConvertAndRethrow(e);
		}
		
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

	private void doRegistryAwareConvertAndRethrow(DBValidationException e) {
		String msg = e.getMessage();
		//if we can't handle an exception here, just return and convertAndRethrow will handle it
		
		if (msg.contains("Referential integrity constraint violation")) {
			//TODO: we need to parse the error more to figure out which field(s) has the failure.
			
			//Referential integrity constraint violation: "CONSTRAINT_D: PUBLIC.CUSTOMERADDRESSDAT1 FOREIGN KEY(LEFTV) REFERENCES PUBLIC.CUSTOMER(ID) (44)"; SQL statement:
			String type = StringUtils.substringAfter(msg, "PUBLIC.");
			type = StringUtils.substringBefore(type, " ");
			
			boolean isManyRule = findTypeOfType(type);
			String errId = isManyRule ? "rule-relationMany" : "rule-relationOne";
			DeliaError err = new DeliaError(errId, e.getLastError().getMsg());
			throw new DBValidationException(err);
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
