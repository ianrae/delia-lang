package org.delia.compiler.ast;

public class OptionExp extends ExpBase {
	public IdentExp option;

	public OptionExp(int pos, IdentExp option) {
		super(pos);
		this.option = option;
	}
	@Override
	public String strValue() {
		return option.strValue();
	}
	
	@Override
	public String toString() {
		return "-" + option.strValue();
	}
}