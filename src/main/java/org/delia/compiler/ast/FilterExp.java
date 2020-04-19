package org.delia.compiler.ast;

public class FilterExp extends ExpBase {
	public Exp cond;

	public FilterExp(int pos, Exp cond) {
		super(pos);
		this.cond = cond;
	}
	@Override
	public String strValue() {
		return cond.strValue();
	}
	
	@Override
	public String toString() {
		String s;
		if (cond instanceof FilterOpExp) {
			s = ((FilterOpExp)cond).toString();
		} else {
			s = formatValue(cond);
		}
		
		return s;
	}
}