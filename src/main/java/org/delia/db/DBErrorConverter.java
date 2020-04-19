package org.delia.db;

import java.sql.SQLException;
import java.util.List;

import org.delia.db.sql.table.TableInfo;

/**
 * It's important that db errors get converted into standard delia errors.
 * @author Ian Rae
 *
 */
public interface DBErrorConverter {
	void convertAndRethrowException(SQLException e);
	void convertAndRethrow(DBValidationException e,  List<TableInfo> tblinfo);
	boolean isPrintStackTraceEnabled();
	void setPrintStackTraceEnabled(boolean b);
}
