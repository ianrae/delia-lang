package org.delia.db;

import org.delia.type.DType;
import org.delia.type.DValue;

/**
 * Additional parameters for DBInterface.executeInsert.
 * 
 * @author Ian Rae
 *
 */
public class InsertContext { 
	public boolean extractGeneratedKeys;
	public DType genKeytype;
	public DValue actualDValForRawInsert; //zexec.rawInsert must set this
}
