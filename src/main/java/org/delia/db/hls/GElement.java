package org.delia.db.hls;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.util.DeliaExceptionHelper;

public class GElement implements HLSElement {
	public QueryFuncExp qfe;

	public GElement(QueryFuncExp qfe) {
		this.qfe = qfe;
	}
	
	int getIntArg(int i) {
		if (i + 1 > qfe.argL.size()) {
			DeliaExceptionHelper.throwError("queryfn-bad-index", "Bad index on fn '%s'", qfe.funcName);
		}
		Exp exp = qfe.argL.get(i);
		IntegerExp iexp = (IntegerExp) exp;
		return iexp.val;
	}
	
	public String getFuncName() {
		return qfe.funcName;
	}

	@Override
	public String toString() {
		String s = String.format("%s", qfe.funcName);
		return s;
	}
	
}