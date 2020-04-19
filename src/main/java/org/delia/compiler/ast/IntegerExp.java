package org.delia.compiler.ast;

public class IntegerExp extends ExpBase implements ValueExp {
	public Integer val;

	public IntegerExp(int pos, Integer s) {
		super(pos);
		this.val =s;
	}
	public IntegerExp(Integer s) {
		super(99);
		this.val =s;
	}
	@Override
	public String strValue() {
		return val.toString();
	}
}