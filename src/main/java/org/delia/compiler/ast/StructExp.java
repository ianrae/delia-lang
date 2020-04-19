package org.delia.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class StructExp extends ExpBase {
	public List<StructFieldExp> argL = new ArrayList<>();

	public StructExp(int pos, List<List<StructFieldExp>> args) {
		super(pos);
		
		if (args != null) {
			List<StructFieldExp> list = new ArrayList<>();
			if (! args.isEmpty()) {
				for(List<StructFieldExp> sublist : args) {
					for(StructFieldExp inner: sublist) {
						list.add(inner);
					}
				}
			}
			argL = list;
		}
	}
	
	@Override
	public String strValue() {
		String ss = String.format("{");
		int i = 0;
		for(Exp exp : argL) {
			if (i > 0) {
				ss += ", " + exp.strValue();
			} else {
				ss += exp.strValue();
			}
			i++;
		}
		ss = String.format("%s }", ss);
		return ss;
	}

	@Override
	public String toString() {
		String ss = String.format("{");
		int i = 0;
		for(Exp exp : argL) {
			if (i > 0) {
				ss += ", " + formatValue(exp);
			} else {
				ss += formatValue(exp);
			}
			i++;
		}
		ss = String.format("%s }", ss);
		return ss;
	}
}