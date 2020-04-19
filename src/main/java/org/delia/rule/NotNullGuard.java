package org.delia.rule;

import java.util.List;

import org.delia.type.DValue;

/**
 * Only run rule if the dval (or the dependent fields) are not-null.
 * 
 * @author Ian Rae
 *
 */
public class NotNullGuard implements RuleGuard {
	private List<String> fieldL;
	
	public NotNullGuard(List<String> fieldList) {
		this.fieldL = fieldList;
	}
	@Override
	public boolean shouldExecRule(DValue dval) {
		if (dval.getType().isStructShape()) {
			return fieldsNotNull(dval);
		}
		return dval != null;
	}
	private boolean fieldsNotNull(DValue dval) {
		for(String fieldName: fieldL) {
			DValue inner = dval.asStruct().getField(fieldName);
			if (inner == null) {
				return false;
			}
		}
		return true;
	}

}
