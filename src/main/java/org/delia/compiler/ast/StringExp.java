package org.delia.compiler.ast;

public class StringExp extends ExpBase implements ValueExp {
	public String val;

	public StringExp(String s) {
		super(99);
		this.val = s;
	}
	public StringExp(int pos, String s) {
		super(pos);
		this.val = s;
	}
	@Override
	public String strValue() {
		return val.toString();
	}
}