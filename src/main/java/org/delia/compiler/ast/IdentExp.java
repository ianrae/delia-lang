package org.delia.compiler.ast;

public class IdentExp extends ExpBase {
	public String val;

	public IdentExp(int pos, String s) {
		super(pos);
		this.val =s;
	}
	public IdentExp(String s) {
		super(99);
		this.val =s;
	}
	
	public String name() {
		return val;
	}

	@Override
	public String strValue() {
		return val;
	}
}