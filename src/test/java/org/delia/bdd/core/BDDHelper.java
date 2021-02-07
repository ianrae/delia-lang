package org.delia.bdd.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.delia.api.DeliaSession;
import org.delia.db.DBType;

public class BDDHelper  {
	
	private DBType dbType;
	
	public BDDHelper(DBType dbType) {
		this.dbType = dbType;
	}
	/**
	 * Some DBTypes return a bit extra information. This is not wrong data, just a bit more than you asked for.
	 * So in a bdd "then:" clause you can specify dbtype-specific text
	 *  IF(MEM):vworker:{55}
	 *  ELSE:  vworker:{55:
	 *  ELSE:  {
	 * etc.
	 * @param thenL
	 * @return
	 */
	public List<String> adjustForDBType(List<String> thenL) {
		String dbtype = dbType.name();
		
		List<String> resultL = new ArrayList<>();
		boolean ignoreElse = false;
		for(String line: thenL) {
			String target = String.format("IF(%s):", dbtype);
			if (line.startsWith(target)) {
				line = StringUtils.substringAfter(line, target);
				ignoreElse = true;
			} else if (lineMatchesOtherDBType(line, dbtype)) {
				line = null; //
			}
			
			target = "ELSE:";
			if (line != null && line.startsWith(target)) {
				line = ignoreElse ? null : StringUtils.substringAfter(line, target);
			}
			
			if (line != null) {
				resultL.add(line);
			}
		}
		return resultL;
	}

	private boolean lineMatchesOtherDBType(String line, String currentDBType) {

		for(DBType dbtype: DBType.values()) {
			if (!dbtype.name().equals(currentDBType)) {
				String target = String.format("IF(%s):", dbtype);
				if (line.startsWith(target)) {
					return true;
				}
			}
		}
		
		return false;
	}

}