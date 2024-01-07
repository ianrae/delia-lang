package org.delia.dbimpl.mysql;

import org.apache.commons.lang3.StringUtils;
import org.delia.db.DBErrorConverterBase;
import org.delia.db.DBException;
import org.delia.db.DBExecuteContext;
import org.delia.db.DBValidationException;
import org.delia.error.DeliaError;
import org.delia.lld.LLD;
import org.delia.rule.rules.UniqueFieldsRule;
import org.delia.sql.UniqueFieldsRuleHelper;

import java.sql.SQLException;
import java.util.List;
//import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
//import org.postgresql.util.PSQLException;

public class MySqlErrorConverter extends DBErrorConverterBase {
	
//	private SqlNameFormatter nameFormatter;
//
//	public PostgresErrorConverter(SqlNameFormatter nameFormatter) {
//		this.nameFormatter = nameFormatter;
//	}

	@Override
	public void convertAndRethrowException(SQLException e, DBExecuteContext dbctx) {
		printStackTraceIfEnabled(e);
		
		if (registry != null) {
			doRegistryAwareConvertAndRethrow(e);
		}
		
		if (isPSQLExceptionWith(e, "duplicate key value violates unique")) {
			if (dbctx != null && dbctx.currentStatement instanceof LLD.HasLLTable) {
				//we're doing insert,update,upsert,delete,select. i think this error only occurs in insert,update,upsert
				LLD.HasLLTable hasLLTable = (LLD.HasLLTable) dbctx.currentStatement;
				//TODO actual parse exception to determine type and fields
				List<UniqueFieldsRule> rules = UniqueFieldsRuleHelper.buildUniqueFields(hasLLTable.getTable().physicalType);
				if (! rules.isEmpty()) {
					throw new DBValidationException(makeError("rule-uniqueFields", e));
				}
			}
			throw new DBValidationException(makeError("duplicate-unique-value", e));
		} else if (isPSQLExceptionWith(e, "violates foreign key constraint")) {
			throw new DBValidationException(makeError("db-validation-fail", e));
		} else if (isClass(e, "JdbcSQLIntegrityConstraintViolationException")) {
			throw new DBValidationException(makeError("db-validation-fail", e));
		} else if (isClass(e, "JdbcSQLIntegrityConstraintViolationException")) {
			throw new DBValidationException(makeError("db-validation-fail", e));
		} else {
			DeliaError err = new DeliaError("db-unexpected-exception", "UNEXPECTED error: " + e.getMessage());
			throw new DBException(err);
		}
	}
	
	private void doRegistryAwareConvertAndRethrow(SQLException e) {
		String msg = e.getMessage();
		//if we can't handle an exception here, just return and convertAndRethrow will handle it
		
		
		//rg.postgresql.util.PSQLException: ERROR: insert or update on table "customeraddressdat1" violates foreign key constraint "customeraddressdat1_leftv_fkey"
		//Detail: Key (leftv)=(44) is not present in table "customer".
		
		if (msg.contains("violates foreign key constraint")) {
			
			//Referential integrity constraint violation: "CONSTRAINT_D: PUBLIC.CUSTOMERADDRESSDAT1 FOREIGN KEY(LEFTV) REFERENCES PUBLIC.CUSTOMER(ID) (44)"; SQL statement:
			String type = StringUtils.substringAfter(msg, "on table \"");
			type = StringUtils.substringBefore(type, "\"");
			
			boolean b = msg.contains("_leftv") || msg.contains("_rightv");
			
			boolean isManyRule = b; //findTypeOfType(type);
			String errId = isManyRule ? "rule-relationMany" : "rule-relationOne";
			DeliaError err = new DeliaError(errId, msg);
			throw new DBValidationException(err);
		}
	}
	
	
	private DeliaError makeError(String id, Exception e) {
		DeliaError err = new DeliaError(id, e.getMessage());
		return err;
	}
	
	private boolean isPSQLExceptionWith(SQLException e, String string) {
//		if (e instanceof PSQLException && e.getMessage().contains(string)) {
//			return true;
//		}
		if (isClass(e, "PSQLException") && e.getMessage().contains(string)) {
			return true;
		}
		return false;
	}


//	@Override
//	public void convertAndRethrow(DBValidationException e, List<TableInfo> tblinfoL) {
//		String msg = e.getMessage();
//		throw e;
////		if (msg.contains("Unique index or primary key violation")) {
////			//TODO: we need to parse the error more to figure out which field(s) has the failure.
////			DeliaError err = new DeliaError("duplicate-unique-value", e.getLastError().getMsg());
////			throw new DBValidationException(err);
////		} else if (msg.contains("violates foreign key constraint") || isRelationError(msg)) {
////			//TODO: we need to parse the error more to figure out which field(s) has the failure.
////			boolean isManyRule = findTypeOfViolation(e.getMessage(), tblinfoL);
////			String errId = isManyRule ? "rule-relationMany" : "rule-relationOne";
////			DeliaError err = new DeliaError(errId, e.getLastError().getMsg());
////			throw new DBValidationException(err);
////		} else {
////			throw e;
////		}
//	}
//
//	//org.postgresql.util.PSQLException: ERROR: insert or update on table "addresscustomerassoc" violates foreign key constraint "addresscustomerassoc_rightv_fkey"
////	  Detail: Key (rightv)=(44) is not present in table "customer".
//	private boolean isRelationError(String msg) {
//		if (msg.contains("insert or update on table") && msg.contains("violates foreign key constraint")) {
//			return true;
//		}
//		return false;
//	}
//
//	private boolean findTypeOfViolation(String message, List<TableInfo> tblinfoL) {
//		for(TableInfo info: tblinfoL) {
//			if (info.assocTblName != null && message.contains(nameFormatter.convert(info.assocTblName))) {
//				return true;
//			}
//		}
//		return false;
//	}

}
