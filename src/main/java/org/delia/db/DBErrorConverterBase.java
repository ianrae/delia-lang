package org.delia.db;

import java.sql.SQLException;
import java.util.List;

import org.delia.db.sql.table.TableInfo;

/**
 * It's important that db errors get converted into standard delia errors.
 * @author Ian Rae
 *
 */
public abstract class DBErrorConverterBase implements DBErrorConverter {
	public boolean printStackTraceFlag;

	
	@Override
	public abstract void convertAndRethrowException(SQLException e);
	
	protected void printStackTraceIfEnabled(SQLException e) {
		if (printStackTraceFlag) {
			e.printStackTrace();
		}
	}
	

	@Override
	public abstract void convertAndRethrow(DBValidationException e, List<TableInfo> tblinfo);

	@Override
	public boolean isPrintStackTraceEnabled() {
		return printStackTraceFlag;
	}

	@Override
	public void setPrintStackTraceEnabled(boolean b) {
		printStackTraceFlag = b;
	}

}
