package org.delia.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class UserFnCallExp extends ExpBase {
		public String funcName;
		public List<Exp> argL = new ArrayList<>();

		public UserFnCallExp(int pos, IdentExp nameExp,  List<List<Exp>> args) {
			super(pos);
			this.funcName = nameExp.name();
			
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
			String ss = String.format("%s(", funcName);
//			int i = 0;
//			for(Exp exp : argL) {
//				if (i > 0) {
//					ss += "," + exp.strValue();
//				} else {
//					ss += exp.strValue();
//				}
//				i++;
//			}
			ss = String.format("%s)", ss);
			return ss;
		}

		@Override
		public String toString() {
			return strValue();
		}
	}