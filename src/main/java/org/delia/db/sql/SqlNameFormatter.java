package org.delia.db.sql;

import org.delia.type.DType;

/**
 * Databases have various naming conventions.
 * @author Ian Rae
 *
 */
public interface SqlNameFormatter {

	String convert(String tblName);
	String convert(DType dtype);
	
}
