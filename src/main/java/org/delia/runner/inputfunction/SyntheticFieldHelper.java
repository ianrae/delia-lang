package org.delia.runner.inputfunction;

import java.util.Map;
import java.util.Random;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.dval.DValueConverterService;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;

public class SyntheticFieldHelper {

	public static DValue buildSyntheticValue(InputFuncMappingExp mappingExp, DValueConverterService dvalConverter, ScalarValueBuilder builder) {
		XNAFSingleExp sexp = mappingExp.getSingleExp();
		Exp arg1 = sexp.argL.get(0);
		
		return dvalConverter.createDValFromExp(arg1, builder);
	}
	
	public static String generateSyntheticFieldName(Map<String, ProgramSpec> map, Random rand) {
		for(int n = 0; n < 1000; n++) {
			int k = rand.nextInt(999999);
			String fieldName = String.format("synthetic%d", k);
			if (! map.containsKey(fieldName)) {
				return fieldName;
			}
		}
		//give up and with fail
		return null;
	}
	
	
	
}
