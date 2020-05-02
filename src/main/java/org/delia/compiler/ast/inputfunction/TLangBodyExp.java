package org.delia.compiler.ast.inputfunction;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.ExpBase;

public class TLangBodyExp extends ExpBase {
	public List<Exp> statementL = new ArrayList<>();

	public TLangBodyExp(int pos, List<List<Exp>> args) {
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
			statementL = list;
		}
	}
	
	@Override
	public String strValue() {
		String ss = "";
		int i = 0;
		for(Exp exp : statementL) {
			if (i > 0) {
				ss += "," + exp.strValue();
			} else {
				ss += exp.strValue();
			}
			i++;
		}
		return ss;
	}

	@Override
	public String toString() {
		String ss = "";
		int i = 0;
		for(Exp exp : statementL) {
			if (i > 0) {
				ss += "," + exp.toString();
			} else {
				ss += exp.toString();
			}
			i++;
		}
		return ss;
	}
}