package org.delia.db;

import java.sql.SQLException;
import java.util.List;

import org.delia.db.sql.table.TableInfo;
import org.delia.error.DeliaError;

/**
 * Parsing errors much easier with the registry to get type info.
 * @author Ian Rae
 *
 */
public class RegistryAwareDBErrorConverter implements DBErrorConverter {
	private DBErrorConverter inner;
	
	public RegistryAwareDBErrorConverter(DBErrorConverter inner) {
		this.inner = inner;
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
			boolean isManyRule = false; //findTypeOfViolation(e.getMessage(), tblinfoL);
			String errId = isManyRule ? "rule-relationMany" : "rule-relationOne";
			DeliaError err = new DeliaError(errId, e.getLastError().getMsg());
			throw new DBValidationException(err);
		} else {
			inner.convertAndRethrow(e, tblinfo);
		}
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
