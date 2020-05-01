package org.delia.runner.inputfunction;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;

public class SyntheticFieldHelper {

	public static DValue buildSyntheticValue(InputFuncMappingExp mappingExp, ScalarValueBuilder builder) {
		XNAFSingleExp sexp = mappingExp.getSingleExp();
		Exp arg1 = sexp.argL.get(0);
		if (arg1 instanceof IntegerExp) {
			IntegerExp nexp = (IntegerExp)arg1;
			return builder.buildInt(nexp.val);
		} else if (arg1 instanceof LongExp) {
			LongExp nexp = (LongExp)arg1;
			return builder.buildLong(nexp.val);
		} else if (arg1 instanceof NumberExp) {
			NumberExp nexp = (NumberExp)arg1;
			return builder.buildNumber(nexp.val);
		} else if (arg1 instanceof BooleanExp) {
			BooleanExp nexp = (BooleanExp)arg1;
			return builder.buildBoolean(nexp.val);
		} else if (arg1 instanceof StringExp) {
			StringExp nexp = (StringExp)arg1;
			return builder.buildString(nexp.val);
		}
		
		// TODO do date!!
		return null;
	}
	
	
}
