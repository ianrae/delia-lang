package org.delia.db.sql;

import org.delia.type.DType;

public class SimpleSqlNameFormatter implements SqlNameFormatter {

	private boolean convertToLowerCase;
	
	public SimpleSqlNameFormatter() {
		this(false);
	}
	public SimpleSqlNameFormatter(boolean convertToLowerCase) {
		this.convertToLowerCase = convertToLowerCase;
	}
	
	@Override
	public String convert(String tblName) {
		if (convertToLowerCase) {
			String s = tblName.toLowerCase();
			return s;
		} else {
			return tblName;
		}
	}
	@Override
	public String convert(DType dtype) {
		return convert(dtype.getName());
	}

}
