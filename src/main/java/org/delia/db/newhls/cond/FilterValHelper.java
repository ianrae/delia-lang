package org.delia.db.newhls.cond;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NullExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.db.newhls.ValType;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

public class FilterValHelper {

	public static FilterCond singleFilterFromDVal(DValue dval, String varName) {
		if (dval == null) {
			return new NullFilterCond(new NullExp());
		}
		
		switch(dval.getType().getShape()) {
		case BOOLEAN:
			return new BooleanFilterCond(new BooleanExp(dval.asBoolean()));
		case INTEGER:
			return new IntegerFilterCond(new IntegerExp(dval.asInt()));
		case LONG:
			return new LongFilterCond(new LongExp(dval.asLong()));
		case STRING:
			return new StringFilterCond(new StringExp(dval.asString()));
			default:
				DeliaExceptionHelper.throwError("bad-filter-var-type", "Var '%s' used in filter has type '%s'. Not allowed", varName, dval.getType().getName());
		}
		return null;
	}

	public static ValType createValType(Exp exp) {
		if (exp instanceof BooleanExp) {
			return ValType.BOOLEAN;
		} else if (exp instanceof IntegerExp) {
			return ValType.INT;
		} else if (exp instanceof LongExp) {
			return ValType.LONG;
		} else if (exp instanceof NumberExp) {
			return ValType.NUMBER;
		} else if (exp instanceof StringExp) {
			return ValType.STRING;
		} else if (exp instanceof NullExp) {
			return ValType.NULL;
		} else {
			return null; //TODO: error
		}
	}

	public static FilterVal createFromExp(Exp exp) {
		//special code for asc and desc
		//TODO: arg might be a varname. add code for this. need to resolve vars before calling here
		if (exp instanceof IdentExp) {
			StringExp sexp = new StringExp(exp.strValue());
			return new FilterVal(ValType.STRING, sexp);
		}
		
		ValType valType = createValType(exp);
		return new FilterVal(valType, exp);
	}
}