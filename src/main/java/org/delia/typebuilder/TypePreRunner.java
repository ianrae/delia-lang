package org.delia.typebuilder;

import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.StructFieldExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.runner.ResultValue;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.OrderedMap;
import org.delia.type.Shape;

public class TypePreRunner extends ServiceBase {
	private PreTypeRegistry preRegistry;
	private DTypeRegistry actualRegistry; //holds builtIn types only.

	public TypePreRunner(FactoryService factorySvc, DTypeRegistry actualRegistry) {
		super(factorySvc);
		this.preRegistry = new PreTypeRegistry();
		this.actualRegistry = actualRegistry;
	}

	public void executeStatements(List<Exp> extL, List<DeliaError> allErrors) {
		for(Exp exp: extL) {
			ResultValue res = executeStatement(exp);
			if (! res.ok) {
				allErrors.addAll(res.errors);
			}
		}
		
		//create errors for undefined types
		for(String typeName: preRegistry.getUndefinedTypes()) {
			MentionContext mention = preRegistry.getMap().get(typeName);
			String msg = String.format("Can't find definition of type '%s' mentioned in type '%s'", typeName, mention.parentType);
			DeliaError err = new DeliaError("undefined-type", msg);
			log.logError(err.toString());
			allErrors.add(err);
		}
	}
	
	private ResultValue executeStatement(Exp exp) {
		ResultValue res = new ResultValue();
		if (exp instanceof TypeStatementExp) {
			DType dtype = createType((TypeStatementExp) exp, res);
			preRegistry.addTypeDefinition(dtype);
		}
		return res;
	}
	
	public DType createType(TypeStatementExp typeStatementExp, ResultValue res) {
		et.clear();
		if (typeStatementExp.structExp == null ) {
			DType dtype = new DType(Shape.INTEGER, typeStatementExp.typeName, null); //not correct values. will fix later
			return dtype;
		}
		
		//build struct type
		OrderedMap omap = new OrderedMap();
		for(StructFieldExp fieldExp: typeStatementExp.structExp.argL) {
			DType fieldType = getTypeForField(fieldExp, typeStatementExp.typeName);
			omap.add(fieldExp.getFieldName(), fieldType, fieldExp.isOptional, fieldExp.isUnique, fieldExp.isPrimaryKey, fieldExp.isSerial);
		}
		
		DType dtype = new DStructType(Shape.STRUCT, typeStatementExp.typeName, null, null, null);
		return dtype;
	}
	
	private DType getTypeForField(StructFieldExp fieldExp, String parentTypeName) {
		DType strType = actualRegistry.getType(BuiltInTypes.STRING_SHAPE);
		DType intType = actualRegistry.getType(BuiltInTypes.INTEGER_SHAPE);
		DType longType = actualRegistry.getType(BuiltInTypes.LONG_SHAPE);
		DType numberType = actualRegistry.getType(BuiltInTypes.NUMBER_SHAPE);
		DType boolType = actualRegistry.getType(BuiltInTypes.BOOLEAN_SHAPE);
		DType dateType = actualRegistry.getType(BuiltInTypes.DATE_SHAPE);
		
		String s = fieldExp.typeName;
		if (s.equals("string")) {
			return strType;
		} else if (s.equals("int")) {
			return intType;
		} else if (s.equals("boolean")) {
			return boolType;
		} else if (s.equals("long")) {
			return longType;
		} else if (s.equals("number")) {
			return numberType;
		} else if (s.equals("date")) {
			return dateType;
		} else {
			DType possibleStruct = preRegistry.getType(fieldExp.typeName);
			if (possibleStruct != null) {
				return possibleStruct;
			} else {
				DType dtype = new DStructType(Shape.STRUCT, fieldExp.typeName, null, null, null);
				preRegistry.addMentionedType(dtype, parentTypeName);
				return dtype;
			}
		}
	}

	public PreTypeRegistry getPreRegistry() {
		return preRegistry;
	}

	public DTypeRegistry getActualRegistry() {
		return actualRegistry;
	}
}