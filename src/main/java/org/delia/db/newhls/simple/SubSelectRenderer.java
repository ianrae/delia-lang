package org.delia.db.newhls.simple;

import org.delia.db.newhls.SqlParamGenerator;
import org.delia.db.newhls.cond.CustomFilterValueRenderer;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.sql.prepared.SqlStatement;

public class SubSelectRenderer implements CustomFilterValueRenderer {

	private SimpleSelect simple;

	public SubSelectRenderer(SimpleSelect simpleSel) {
		this.simple = simpleSel;
	}

	@Override
	public String render(FilterVal val1, SqlParamGenerator paramGen, SqlStatement stm) {
		return "AAA";
	}
}
