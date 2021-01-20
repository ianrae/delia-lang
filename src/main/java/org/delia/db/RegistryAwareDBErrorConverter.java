package org.delia.db;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.delia.db.sql.table.TableInfo;
import org.delia.error.DeliaError;
import org.delia.type.DTypeRegistry;

/**
 * Parsing errors much easier with the registry to get type info.
 * @author Ian Rae
 *
 */
public class RegistryAwareDBErrorConverter implements DBErrorConverter {
	private DBErrorConverter inner;
	private DTypeRegistry registry;
	
	public RegistryAwareDBErrorConverter(DBErrorConverter inner, DTypeRegistry registry) {
		this.inner = inner;
		this.registry = registry;
	}

	@Override
	public void convertAndRethrowException(SQLException e) {
		inner.convertAndRethrowException(e);
	}

	@Override
	public void convertAndRethrow(DBValidationException e, List<TableInfo> tblinfo) {
		String msg = e.getMessage();
		if (msg.contains("Unique index or primary key violation")) {
			//TODO: we need to parse the error more to figure out which field(s) has the failure.
			DeliaError err = new DeliaError("duplicate-unique-value", e.getLastError().getMsg());
			throw new DBValidationException(err);
		} else if (msg.contains("Referential integrity constraint violation")) {
			//TODO: we need to parse the error more to figure out which field(s) has the failure.
			
			//Referential integrity constraint violation: "CONSTRAINT_D: PUBLIC.CUSTOMERADDRESSDAT1 FOREIGN KEY(LEFTV) REFERENCES PUBLIC.CUSTOMER(ID) (44)"; SQL statement:
			String type = StringUtils.substringAfter(msg, "PUBLIC.");
			type = StringUtils.substringBefore(type, " ");
			
			boolean isManyRule = findTypeOfType(type);
			String errId = isManyRule ? "rule-relationMany" : "rule-relationOne";
			DeliaError err = new DeliaError(errId, e.getLastError().getMsg());
			throw new DBValidationException(err);
		} else {
			inner.convertAndRethrow(e, tblinfo);
		}
	}

	private boolean findTypeOfType(String type) {
		for(String typeName: registry.getAll()) {
			if (typeName.equalsIgnoreCase(type)) {
				return false; 
			}
		}
		
		if (registry.getSchemaVersionType().getName().equalsIgnoreCase(type)) {
			return false;
		}
		
		if (type.contains("DAT")) return true;
		return false;
	}

	@Override
	public boolean isPrintStackTraceEnabled() {
		return inner.isPrintStackTraceEnabled();
	}

	@Override
	public void setPrintStackTraceEnabled(boolean b) {
		inner.setPrintStackTraceEnabled(b);
	}
	
	
}
