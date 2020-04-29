package org.delia.compiler.ast.inputfunction;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.ExpBase;
import org.delia.compiler.ast.IdentExp;

public class InputFuncHeaderExp extends ExpBase {
	public String fnName;
	public List<IdentPairExp> argsL = new ArrayList<>();

	public InputFuncHeaderExp(IdentExp fnNameExp, List<List<IdentPairExp>> args) {
		super(fnNameExp.pos);
		this.fnName = fnNameExp.name();
		
		if (args != null) {
			List<IdentPairExp> list = new ArrayList<>();
			if (! args.isEmpty()) {
				for(List<IdentPairExp> sublist : args) {
					for(IdentPairExp inner: sublist) {
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