package org.delia.runner;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NullExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueConverterService;
import org.delia.type.BuiltInTypes;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.valuebuilder.ScalarValueBuilder;

public class ScalarBuilder extends ServiceBase {
	private static class TypeInfo {
		DType type;
	}
	
	private DTypeRegistry registry;
	private ScalarValueBuilder builder;
	private DValueConverterService dvalConverter;
	
	public ScalarBuilder(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.builder = factorySvc.createScalarValueBuilder(registry);
		this.dvalConverter = new DValueConverterService(factorySvc);
	}
	
	public DValue buildDValue(Exp valueExp, String typeName) {
		if (valueExp instanceof NullExp) {
			return null;
		}
		
		DValue dval;
		if (typeName == null) {
			dval = createDVal(valueExp);
		} else {
			dval = createDValExplicit(valueExp, typeName);
		}
//		DValueInternal dvi = (DValueInternal) dval;
//		dvi.setValidationState(ValidationState.VALID);
		return dval;
	}
	
	private DValue createDValExplicit(Exp valueExp, String typeName) {
		TypeInfo info = new TypeInfo();
		
		if (isType("int", typeName, info)) {
			if (! (valueExp instanceof IntegerExp)) {
				wrongTypeError(valueExp, typeName, "int");
				return null;
			}
			IntegerExp exp = (IntegerExp) valueExp;
			return builder.buildInt(exp.val, info.type);
		} else if (isType("long", typeName, info)) {
			if (valueExp instanceof LongExp) {
				LongExp exp = (LongExp) valueExp;
				return builder.buildLong(exp.val, info.type);
			} else if (valueExp instanceof IntegerExp) {
				IntegerExp exp = (IntegerExp) valueExp;
				Long n = exp.val.longValue();
				return builder.buildLong(n, info.type);
			} else {
				wrongTypeError(valueExp, typeName, "long");
				return null;
			}
		} else if (isType("boolean", typeName, info)) {
			if (! (valueExp instanceof BooleanExp)) {
				wrongTypeError(valueExp, typeName, "boolean");
				return null;
			}
			BooleanExp exp = (BooleanExp) valueExp;
			return builder.buildBoolean(exp.val, info.type);
		} else if (isType("number", typeName, info)) {
			if (valueExp instanceof NumberExp) {
				NumberExp exp = (NumberExp) valueExp;
				return builder.buildNumber(exp.val, info.type);
			} else if (valueExp instanceof LongExp) {
				LongExp exp = (LongExp) valueExp;
				return builder.buildNumber(exp.val.doubleValue(), info.type);
			} else if (valueExp instanceof IntegerExp) {
				IntegerExp exp = (IntegerExp) valueExp;
				return builder.buildNumber(exp.val.doubleValue(), info.type);
			} else {
				wrongTypeError(valueExp, typeName, "number");
				return null;
			}
		} else if (isType("date", typeName, info)) {
			return builder.buildDate(valueExp.strValue(), info.type);
		} else if (isType("string", typeName, info)) {
			return builder.buildString(valueExp.strValue(), info.type);
		} else if (valueExp instanceof NullExp) {
			return null; 
		} else { //treat as string
			return builder.buildString(valueExp.strValue(), info.type);
		}
	}
	
	private void wrongTypeError(Exp valueExp, String typeName, String expectedType) {
		String msg = String.format("%s value is not an %s - %s", typeName, expectedType, valueExp.strValue());
		et.add("wrong-type", msg);
	}

	private DValue createDVal(Exp valueExp) {
		return dvalConverter.createDValFromExp(valueExp, builder);
	}

	private boolean isType(String s, String typeName, TypeInfo info) {
		if (typeName == null) {
			return false;
		} else if (s.equals(typeName)) {
			BuiltInTypes bitype = BuiltInTypes.fromDeliaTypeName(s); //int
			info.type = registry.getType(bitype);
			return true;
		} else if (BuiltInTypes.isBuiltInScalarType(typeName)) {
			BuiltInTypes bitype = BuiltInTypes.fromDeliaTypeName(s); //int
			info.type = registry.getType(bitype);
			return false; //since s not eq typeName
		} else {
			DType type = registry.getType(typeName);
			info.type = type;
			BuiltInTypes bitype = BuiltInTypes.fromDeliaTypeName(s); //int -> INTEGER_SHAPE
			Shape shape = BuiltInTypes.getShapeOf(bitype);
			return (type.isShape(shape));
		}
	}
}
