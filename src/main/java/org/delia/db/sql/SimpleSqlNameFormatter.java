package org.delia.db.sql;

import org.delia.type.DType;

public class SimpleSqlNameFormatter implements SqlNameFormatter {

	private boolean convertToLowerCase;
	private String schema;
	
	public SimpleSqlNameFormatter(String schema) {
		this(schema, false);
	}
	public SimpleSqlNameFormatter(String schema, boolean convertToLowerCase) {
		this.convertToLowerCase = convertToLowerCase;
		this.schema = schema;
	}
	
	@Override
	public String convert(String tblName) {
		String prefix = schema == null ? "" : String.format("%s.", schema);
		if (convertToLowerCase) {
			String s = tblName.toLowerCase();
			return prefix + s;
		} else {
			return prefix + tblName;
		}
	}
	@Override
	public String convert(DType dtype) {
		return convert(dtype.getName());
	}

}
