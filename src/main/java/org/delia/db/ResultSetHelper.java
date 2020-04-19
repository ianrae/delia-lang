package org.delia.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author Ian Rae
 *
 */
public abstract class ResultSetHelper {

	public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int columns = rsmd.getColumnCount();
	    for (int x = 1; x <= columns; x++) {
	    	String name = rsmd.getColumnName(x);
	        if (columnName.equalsIgnoreCase(name)) {
	            return true;
	        }
	        
	        //on H2 we need to explicitly check for the alias as well (b.id as addr)
	        String aliasOnH2 = rsmd.getColumnLabel(x);
	        if (columnName.equalsIgnoreCase(aliasOnH2)) {
	        	return true;
	        }
	    }
	    return false;
	}
	public static int getColumnCount(ResultSet rs) throws SQLException {
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int columns = rsmd.getColumnCount();
	    return columns;
	}
}