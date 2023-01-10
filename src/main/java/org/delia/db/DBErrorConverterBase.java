package org.delia.db;

import org.delia.type.DTypeName;
import org.delia.type.DTypeRegistry;

import java.sql.SQLException;

/**
 * It's important that db errors get converted into standard delia errors.
 * @author Ian Rae
 *
 */
public abstract class DBErrorConverterBase implements DBErrorConverter {
	public boolean printStackTraceFlag;
	protected DTypeRegistry registry;

	
	@Override
	public abstract void convertAndRethrowException(SQLException e, DBExecuteContext dbctx);
	
	protected void printStackTraceIfEnabled(SQLException e) {
		if (printStackTraceFlag) {
			e.printStackTrace();
		}
	}
	

//	@Override
//	public abstract void convertAndRethrow(DBValidationException e, List<TableInfo> tblinfo);

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
	
	protected boolean findTypeOfType(DTypeName type) {
		for(DTypeName typeName: registry.getAll()) {
			if (typeName.equals(type)) {
				return false; 
			}
		}
		
		if (type.isEqual(registry.getSchemaVersionType().getName())) {
			return false;
		}
		
		if (type.getTypeName().contains("DAT")) return true;
		return false;
	}
	protected boolean isClass(SQLException e, String className) {
		if (e.getClass().getSimpleName().equals(className)) {
			return true;
		}
		return false;
	}
	
	


}
