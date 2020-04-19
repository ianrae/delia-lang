package org.delia.compiler.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ListExp extends ExpBase {
	public List<Exp> valueL = new ArrayList<>();

	public ListExp(int pos, List<List<Exp>> args) {
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
			valueL = list;
		}
	}
	
	
	@Override
	public String strValue() {
		StringJoiner joiner = new StringJoiner(",");
		for(Exp exp: valueL) {
			joiner.add(exp.strValue());
		}
		return String.format("[%s]", joiner.toString());
	}

	@Override
	public String toString() {
		return strValue();
	}
}