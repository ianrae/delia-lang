package org.delia.tlang.runner;

import java.util.Date;

import org.delia.db.memdb.filter.OP;
import org.delia.util.DeliaExceptionHelper;

public class EvalSpec {
	public OP op;
	public Object left;
	public Object right;
	
	public boolean execute() {
		if (left instanceof Integer) {
			return doInteger((Integer)left, (Integer)right);
		} else if (left instanceof Long) {
			return doLong((Long)left, (Long)right);
		} else if (left instanceof Double) {
			return doDouble((Double)left, (Double) right);
		} else if (left instanceof String) {
			return doString((String)left, (String) right);
		} else if (left instanceof Boolean) {
			return doBoolean((Boolean)left, (Boolean)right);
		} else if (left instanceof Date) {
			return doDate((Date)left, (Date)right);
		} else {
			DeliaExceptionHelper.throwError("tlang-unknown-type", "TLANG unkown type: %s", left.getClass().getSimpleName());
			return false;
		}
	} 
	
	private boolean doBoolean(Boolean b1, Boolean b2) {
		switch(op) {
		case EQ:
			return b1 == b2; 
		case NEQ:
			return b1 != b2; 
		default:
			DeliaExceptionHelper.throwError("tlang-unsupported-op", "TLANG unsupported op: %s (boolean)", op.name());
			return false;
		}
	}

	protected boolean doInteger(Integer n1, Integer n2) {
		switch(op) {
		case LT:
			return n1.compareTo(n2) < 0; 
		case LE:
			return n1.compareTo(n2) <= 0; 
		case GT:
			return n1.compareTo(n2) > 0; 
		case GE:
			return n1.compareTo(n2) >= 0; 
		case EQ:
			return n1.compareTo(n2) == 0; 
		case NEQ:
			return n1.compareTo(n2) != 0; 
		default:
			DeliaExceptionHelper.throwError("tlang-unsupported-op", "TLANG unsupported op: %s (int)", op.name());
			return false;
		}
	}
	protected boolean doLong(Long n1, Long n2) {
		switch(op) {
		case LT:
			return n1.compareTo(n2) < 0; 
		case LE:
			return n1.compareTo(n2) <= 0; 
		case GT:
			return n1.compareTo(n2) > 0; 
		case GE:
			return n1.compareTo(n2) >= 0; 
		case EQ:
			return n1.compareTo(n2) == 0; 
		case NEQ:
			return n1.compareTo(n2) != 0; 
		default:
			DeliaExceptionHelper.throwError("tlang-unsupported-op", "TLANG unsupported op: %s (long)", op.name());
			return false;
		}
	}
	protected boolean doDouble(Double n1, Double n2) {
		switch(op) {
		case LT:
			return n1.compareTo(n2) < 0; 
		case LE:
			return n1.compareTo(n2) <= 0; 
		case GT:
			return n1.compareTo(n2) > 0; 
		case GE:
			return n1.compareTo(n2) >= 0; 
		case EQ:
			return n1.compareTo(n2) == 0; 
		case NEQ:
			return n1.compareTo(n2) != 0; 
		default:
			DeliaExceptionHelper.throwError("tlang-unsupported-op", "TLANG unsupported op: %s (number)", op.name());
			return false;
		}
	}
	protected boolean doString(String s1, String s2) {
		switch(op) {
		case LT:
			return s1.compareTo(s2) < 0; 
		case LE:
			return s1.compareTo(s2) <= 0; 
		case GT:
			return s1.compareTo(s2) > 0; 
		case GE:
			return s1.compareTo(s2) >= 0; 
		case EQ:
			return s1.compareTo(s2) == 0; 
		case NEQ:
			return s1.compareTo(s2) != 0; 
		default:
			DeliaExceptionHelper.throwError("tlang-unsupported-op", "TLANG unsupported op: %s (string)", op.name());
			return false;
		}
	}
	public boolean doDate(Date left, Date right) {
		long n1 = left.getTime();
		long n2 = right.getTime();
		return doLong(n1, n2);
	}
}