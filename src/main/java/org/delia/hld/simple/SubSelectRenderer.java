package org.delia.hld.simple;

import org.delia.core.FactoryService;
import org.delia.db.SqlStatement;
import org.delia.hld.HLDAliasBuilderAdapter;
import org.delia.hld.HLDQuery;
import org.delia.hld.SqlParamGenerator;
import org.delia.hld.StructField;
import org.delia.hld.cond.CustomFilterValueRenderer;
import org.delia.hld.cond.OpFilterCond;
import org.delia.type.DTypeRegistry;

public class SubSelectRenderer extends CustomFilterValueRendererBase implements CustomFilterValueRenderer {

	private SimpleSelect simple;
	private SimpleSqlGenerator sqlgen;
	
	public SubSelectRenderer(FactoryService factorySvc, DTypeRegistry registry, SimpleSelect simpleSel) {
		this.sqlgen = new SimpleSqlGenerator(registry, factorySvc);
		this.simple = simpleSel;
	}

	@Override
	public String render(Object obj, SqlParamGenerator paramGen, SqlStatement stm) {
		////			WHERE t1.cust IN (SELECT t2.cid FROM Customer as t2 WHERE t2.x > ?", "10");
		OpFilterCond ofc = (OpFilterCond) obj;
		String s1 = String.format("%s.%s", ofc.val1.alias, ofc.val1.structField.fieldName);
		String s2 = sqlgen.genAny(simple, stm);
		String sql = String.format("%s IN (%s)", s1, s2);

		return sql;
	}

	@Override
	public void assignAliases(Object obj, HLDQuery hld, HLDAliasBuilderAdapter aliasBuilder) {
		OpFilterCond ofc = (OpFilterCond) obj;
		String fieldName = ofc.val1.asSymbol();
		ofc.val1.structField = new StructField(null, fieldName, null);

		assignAliasesToFilter(simple, aliasBuilder);
	}
}
