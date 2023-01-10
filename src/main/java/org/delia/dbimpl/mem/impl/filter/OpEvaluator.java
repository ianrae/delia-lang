package org.delia.dbimpl.mem.impl.filter;


import org.delia.tok.Tok;

import java.util.List;

public interface OpEvaluator {
	boolean match(Object left);
	void setRightVar(Object rightVar);
	void setNegFlag(boolean negFlag);
	void setFuncs(List<Tok.FunctionTok> funcs);
}