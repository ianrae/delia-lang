package org.delia.db;

import org.delia.type.DTypeRegistry;

import java.sql.SQLException;

/**
 * It's important that db errors get converted into standard delia errors.
 * The registry is not available during delia startup, so setRegistry
 * will be called when it is available.
 * 
 * @author Ian Rae
 *
 */
public interface DBErrorConverter {
	void convertAndRethrowException(SQLException e, DBExecuteContext dbctx);
//	void convertAndRethrow(DBValidationException e,  List<TableInfo> tblinfo);
	boolean isPrintStackTraceEnabled();
	void setPrintStackTraceEnabled(boolean b);
	void setRegistry(DTypeRegistry registry);
}
