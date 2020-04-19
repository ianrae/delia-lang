package org.delia.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class DsonExp extends ExpBase {
	public List<Exp> argL = new ArrayList<>();

	public DsonExp(int pos, List<List<Exp>> args) {
		super(pos);
		
		if (args != null) {
			List<Exp> list = new ArrayList<>();
			if (! args.isEmpty()) {
				for(List<Exp> sublist : args) {
					for(Exp inner: sublist) {
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
				ss += "," + exp.strValue();
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
				ss += "," + formatValue(exp);
			} else {
				ss += formatValue(exp);
			}
			i++;
		}
		ss = String.format("%s }", ss);
		return ss;
	}
}