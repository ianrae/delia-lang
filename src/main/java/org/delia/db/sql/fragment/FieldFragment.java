package org.delia.db.sql.fragment;

import org.delia.type.DStructType;
import org.delia.type.DType;

public class FieldFragment extends AliasedFragment {
	public DStructType structType;
	public DType fieldType;
	public boolean isStar;		
	public String fnName;

	@Override
	public String render() {
		if (isStar) {
			if (fnName != null) {
				return String.format("%s(*)", fnName);
			}
			return "*";
		} else if (fnName != null) {
			return String.format("%s(%s)", fnName, super.render());
		}
		
		return super.render();
	}
}