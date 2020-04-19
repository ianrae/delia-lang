package org.delia.db.memdb.filter;


public interface OpEvaluator {
	boolean match(Object left);
	void setRightVar(Object rightVar);
	void setNegFlag(boolean negFlag);
}