package org.delia.db;

import java.sql.SQLException;
import java.util.List;

import org.delia.db.sql.table.TableInfo;
import org.delia.type.DTypeRegistry;

/**
 * It's important that db errors get converted into standard delia errors.
 * @author Ian Rae
 *
 */
public abstract class DBErrorConverterBase implements DBErrorConverter {
	public boolean printStackTraceFlag;
	protected DTypeRegistry registry;

	
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

	@Override
	public void setRegistry(DTypeRegistry registry) {
		this.registry = registry;
	}
	
	protected boolean findTypeOfType(String type) {
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
	

}
