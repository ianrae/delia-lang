package org.delia.compiler.ast;

import java.util.ArrayList;
import java.util.List;

//only used during parsing
public class UserFuncHeaderExp extends ExpBase {
	public String fnName;
	public List<IdentExp> argsL = new ArrayList<>();

	public UserFuncHeaderExp(IdentExp fnNameExp, List<List<IdentExp>> args) {
		super(fnNameExp.pos);
		this.fnName = fnNameExp.name();
		
		if (args != null) {
			List<IdentExp> list = new ArrayList<>();
			if (! args.isEmpty()) {
				for(List<IdentExp> sublist : args) {
					for(IdentExp inner: sublist) {
						list.add(inner);
					}
				}
			}
			argsL = list;
		}
	}
	
	@Override
	public String strValue() {
		return null;
	}

	@Override
	public String toString() {
		return null;
	}
}