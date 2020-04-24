package org.delia.db.sql.fragment;

import org.delia.type.DStructType;
import org.delia.type.DType;

public class FieldFragment extends AliasedFragment {
	public DStructType structType;
	public DType fieldType;
	public boolean isStar;		
	public String fnName;
	public String asName;

	@Override
	public String render() {
		if (isStar) {
			return renderField("*");
		}
		
		return renderField(super.render());
	}
	
	private String renderField(String arg) {
		String suffix = asName == null ? "" : " as " + asName;
		if (fnName != null) {
			return String.format("%s(%s)%s", fnName, arg, suffix);
		} else {
			return String.format("%s%s", arg, suffix);
		}
	}
	
}