package org.delia.compiler.ast;

public class NullExp extends ExpBase implements ValueExp {

	public NullExp() {
		super(99);
	}
	@Override
	public String strValue() {
		return "null";
	}
}