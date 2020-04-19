package org.delia.compiler.ast;

public class BooleanExp extends ExpBase implements ValueExp {
	public Boolean val;

	public BooleanExp(boolean b) {
		super(99);
		this.val = b;
	}
	@Override
	public String strValue() {
		return val.toString();
	}
}