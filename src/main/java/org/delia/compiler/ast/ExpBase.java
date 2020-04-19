package org.delia.compiler.ast;

import org.delia.compiler.astx.XNAFMultiExp;

public abstract class ExpBase implements Exp {
	public int pos = 0;
	
	public ExpBase(int pos) {
		this.pos = pos;
	}

	public int getPos() {
		return pos;
	}
	
	protected String formatValue(Exp value) {
		if (value != null) {
			if (value instanceof LongExp) {
				return value.strValue();
			} else if (value instanceof IntegerExp) {
				return value.strValue();
			} else if (value instanceof StringExp) {
				return String.format("'%s'", value.strValue());
			} else if (value instanceof XNAFMultiExp) {
				return String.format("%s", value.strValue());
			} else {
				return value.strValue();
			}
		} else 
			return null;
	}
}