package org.delia.db;

import org.delia.type.DType;

/**
 * Additional parameters for DBInterface.executeInsert.
 * 
 * @author Ian Rae
 *
 */
public class InsertContext { 
	public boolean extractGeneratedKeys;
	public DType genKeytype;
}
