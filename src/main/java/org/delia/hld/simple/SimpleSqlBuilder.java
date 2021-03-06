package org.delia.hld.simple;

import org.delia.hld.HLDField;
import org.delia.hld.HLDQuery;
import org.delia.hld.SqlColumn;
import org.delia.hld.cud.HLDDelete;
import org.delia.hld.cud.HLDInsert;
import org.delia.hld.cud.HLDUpdate;

/**
 * Converts HLD object into a simple SQL statement
 * @author ian
 *
 */
public class SimpleSqlBuilder {
	public SimpleSelect buildFrom(HLDQuery hld) {
		SimpleSelect simple = new SimpleSelect();
		simple.tblFrag = new SqlColumn(hld.fromAlias, hld.fromType.getName());
		for(HLDField fld: hld.fieldL) {
			SqlColumn ff = new SqlColumn(fld.alias, fld.fieldName);
			simple.fieldL.add(ff);
		}
		simple.filter = hld.filter;
		simple.hld = hld;
		return simple;
	}
	public SimpleDelete buildFrom(HLDDelete hld) {
		SimpleDelete simple = new SimpleDelete();
		simple.tblFrag = new SqlColumn(hld.hld.fromAlias, hld.hld.fromType.getName());
		simple.filter = hld.hld.filter;
		simple.hld = hld;
		return simple;
	}
	public SimpleUpdate buildFrom(HLDUpdate hld) {
		SimpleUpdate simple = new SimpleUpdate();
		simple.tblFrag = new SqlColumn(hld.hld.fromAlias, hld.hld.fromType.getName());
		for(HLDField fld: hld.fieldL) {
			SqlColumn ff = new SqlColumn(fld.alias, fld.fieldName);
			simple.fieldL.add(ff);
		}
		simple.filter = hld.hld.filter;
		simple.hld = hld;
		return simple;
	}
	public SimpleInsert buildFrom(HLDInsert hld) {
		SimpleInsert simple = new SimpleInsert();
		simple.tblFrag = new SqlColumn(hld.typeOrTbl.alias, hld.typeOrTbl.getTblName());
		for(HLDField fld: hld.fieldL) {
			SqlColumn ff = new SqlColumn(fld.alias, fld.fieldName);
			simple.fieldL.add(ff);
		}
		simple.hld = hld;
		return simple;
	}
}