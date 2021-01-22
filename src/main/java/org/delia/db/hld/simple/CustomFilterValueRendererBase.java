package org.delia.db.hld.simple;

import org.delia.db.hld.HLDAliasBuilderAdapter;
import org.delia.db.hld.StructField;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;

public class CustomFilterValueRendererBase {

	protected void assignAliasesToFilter(SimpleSelect simple, HLDAliasBuilderAdapter aliasBuilder) {
		if (simple.filter instanceof SingleFilterCond) {
			doAddInnerAliases(simple, null, aliasBuilder);
		} else if (simple.filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) simple.filter;
			doAddInnerAliases(simple, ofc.val1, aliasBuilder);
		}
	}

	private void doAddInnerAliases(SimpleSelect simple, FilterVal val1, HLDAliasBuilderAdapter aliasBuilder) {
		if (val1 != null) {
			String fieldName = val1.asSymbol();
			val1.structField = new StructField(simple.hld.fromType, fieldName, null);
		}
		
		aliasBuilder.pushAliasScope("CUSTOM");
		aliasBuilder.assignAliases(simple);
		aliasBuilder.popAliasScope();
	}
}
