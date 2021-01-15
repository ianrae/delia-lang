package org.delia.db.newhls.simple;

import org.delia.core.FactoryService;
import org.delia.db.newhls.HLDAliasBuilderAdapter;
import org.delia.db.newhls.HLDQuery;
import org.delia.db.newhls.SqlParamGenerator;
import org.delia.db.newhls.StructField;
import org.delia.db.newhls.cond.CustomFilterValueRenderer;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DTypeRegistry;

public class SubSelectRenderer implements CustomFilterValueRenderer {

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

		if (simple.filter instanceof SingleFilterCond) {
			addInnerAliases(null, aliasBuilder);
		} else if (simple.filter instanceof OpFilterCond) {
			ofc = (OpFilterCond) simple.filter;
			addInnerAliases(ofc.val1, aliasBuilder);
		}
	}

	private void addInnerAliases(FilterVal val1, HLDAliasBuilderAdapter aliasBuilder) {
		if (val1 != null) {
			String fieldName = val1.asSymbol();
			val1.structField = new StructField(simple.hld.fromType, fieldName, null);
		}
		
		aliasBuilder.pushAliasScope("CUSTOM");
		aliasBuilder.assignAliases(simple);
		aliasBuilder.popAliasScope();
	}
}
