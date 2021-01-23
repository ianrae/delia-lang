package org.delia.hld.simple;

import org.delia.hld.HLDAliasBuilderAdapter;
import org.delia.hld.StructField;
import org.delia.hld.cond.FilterVal;
import org.delia.hld.cond.OpFilterCond;
import org.delia.hld.cond.SingleFilterCond;

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
