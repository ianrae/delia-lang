package org.delia.db.newhls.simple;

import org.delia.db.newhls.HLDField;
import org.delia.db.newhls.HLDQuery;
import org.delia.db.newhls.SqlColumn;
import org.delia.db.newhls.cud.HLDDelete;
import org.delia.db.newhls.cud.HLDUpdate;

/**
 * Converts HLD object into a simple SQL statement
 * @author ian
 *
 */
public class SimpleSqlBuilder {
	public SimpleSelect buildFrom(HLDQuery hld) {
		SimpleSelect sel = new SimpleSelect();
		sel.tblFrag = new SqlColumn(hld.fromAlias, hld.fromType.getName());
		for(HLDField fld: hld.fieldL) {
			SqlColumn ff = new SqlColumn(fld.alias, fld.fieldName);
			sel.fieldL.add(ff);
		}
		sel.filter = hld.filter;
		return sel;
	}
	public SimpleDelete buildFrom(HLDDelete hld) {
		SimpleDelete sel = new SimpleDelete();
		sel.tblFrag = new SqlColumn(hld.hld.fromAlias, hld.hld.fromType.getName());
		sel.filter = hld.hld.filter;
		return sel;
	}
	public SimpleUpdate buildFrom(HLDUpdate hld) {
		SimpleUpdate sel = new SimpleUpdate();
		sel.tblFrag = new SqlColumn(hld.hld.fromAlias, hld.hld.fromType.getName());
		for(HLDField fld: hld.fieldL) {
			SqlColumn ff = new SqlColumn(fld.alias, fld.fieldName);
			sel.fieldL.add(ff);
		}
		sel.filter = hld.hld.filter;
		return sel;
	}
}