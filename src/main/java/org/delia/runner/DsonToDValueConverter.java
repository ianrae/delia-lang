package org.delia.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.DsonFieldExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.ListExp;
import org.delia.compiler.ast.NullExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueConverterService;
import org.delia.error.DetailedError;
import org.delia.error.ErrorTracker;
import org.delia.sprig.SprigService;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.valuebuilder.PartialStructValueBuilder;
import org.delia.valuebuilder.RelationValueBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;

public class DsonToDValueConverter extends ServiceBase {
		private DTypeRegistry registry;
		private VarEvaluator varEvaluator;
		private ScalarValueBuilder builder;
		private SprigService sprigSvc;
		private Map<String,String> assocCrudMap;
		private DValueConverterService dvalConverter;
		
		public DsonToDValueConverter(FactoryService factorySvc, ErrorTracker localET, DTypeRegistry registry, VarEvaluator varEvaluator, SprigService sprigSvc) {
			super(factorySvc);
			this.registry = registry;
			this.varEvaluator = varEvaluator;
			this.et = localET;
			this.builder = new ScalarValueBuilder(factorySvc, registry);
			this.dvalConverter = new DValueConverterService(factorySvc);
			this.sprigSvc = sprigSvc;
		}

		public DValue convertOne(String typeName, DsonExp dsonExp, ConversionResult cres) {
			return doConvertOne(typeName, dsonExp, false, cres);
		}
		public DValue convertOnePartial(String typeName, DsonExp dsonExp) {
			this.assocCrudMap = new HashMap<>();
			return doConvertOne(typeName, dsonExp, true, null);
		}
		private DValue doConvertOne(String typeName, DsonExp dsonExp, boolean isPartial, ConversionResult cres) {
			DType dtype = registry.getType(typeName);
			if (dtype == null) {
//				res.error = et.add("type.not.found", String.format("can't find type '%s'", exp.getTypeName()));
//				res.ok = false;
				return null;
			}
			
			DStructType structType = (DStructType) dtype;
			if (structType.getAllFields().isEmpty()) {
				et.add("cant-insert-empty-type", String.format("type '%s' has no fields. Can't execute insert.", dtype.getName()));
				return null;
			}
			
			StructValueBuilder structBuilder;
			if (isPartial) {
				structBuilder = new PartialStructValueBuilder(structType);
			} else {
				structBuilder = new StructValueBuilder(structType);
			}
			DValue dval = buildValue(structType, dsonExp, structBuilder, cres);
			return dval;
		}
		private DValue buildValue(DStructType dtype, DsonExp dsonExp, StructValueBuilder structBuilder, ConversionResult cres) {
			if (cres != null) {
				cres.extraMap = new HashMap<>(); //ok for thread safety
			}
			DValue dval = doBuildValue(dtype, dsonExp, structBuilder, cres);
			return dval;
		}
		private DValue doBuildValue(DStructType structType, DsonExp dsonExp, StructValueBuilder structBuilder, ConversionResult cres) {
			for(Exp exp: dsonExp.argL) {
				DsonFieldExp fieldExp = (DsonFieldExp) exp;
				String fieldName = fieldExp.getFieldName();
				DType fieldType = findFieldType(structType, fieldName); 
				if (assocCrudMap != null && fieldExp.assocCrudAction != null) {
					assocCrudMap.put(fieldName, fieldExp.assocCrudAction.strValue());
				}
				
				//Customer.sid
				if (sprigSvc.haveEnabledFor(structType.getName(), fieldName) && cres != null) {
					cres.extraMap.put(fieldName, builder.buildInt(fieldExp.exp.strValue()));
					continue;
				} 
				
				if (fieldType == null) {
					et.add("field-not-found", String.format("type '%s': can't find field '%s'", structType.getName(), fieldName));
					//throw new ValueException(err);
					return null;
				}
				
				DValue inner = buildInner(fieldExp, fieldType);
				structBuilder.addField(fieldName, inner);
			}
			
			boolean b = structBuilder.finish();
			if (! b) {
				for(DetailedError err: structBuilder.getValidationErrors()) {
					et.add(err);
				}
//				throw new ValueException(err);
				return null;
			}
			DValue dval = structBuilder.getDValue();
			return dval;
		}

		private DType findFieldType(DStructType dtype, String fieldName) {
			for(TypePair pair: dtype.getAllFields()) {
				if (pair.name.equals(fieldName)) {
					return pair.type;
				}
			}
			return null;
		}

		private DValue buildInner(DsonFieldExp fieldExp, DType fieldType) {
			if (fieldExp.exp instanceof NullExp) {
				return null;
			}
			
			String input = evalFieldExp(fieldExp, fieldType);
			if (input == null) {
				return null;
			}
			
			if (Shape.STRING.equals(fieldType.getShape())) {
				DValue dval = builder.buildString(input, fieldType); 
				return dval;
			} else if (Shape.INTEGER.equals(fieldType.getShape())) {
				DValue dval = builder.buildInt(input, fieldType); 
				return dval;
			} else if (Shape.LONG.equals(fieldType.getShape())) {
				DValue dval = builder.buildLong(input, fieldType); 
				return dval;
			} else if (Shape.NUMBER.equals(fieldType.getShape())) {
				DValue dval = builder.buildNumber(input, fieldType); 
				return dval;
			} else if (Shape.BOOLEAN.equals(fieldType.getShape())) {
				DValue dval = builder.buildBoolean(input, fieldType); 
				return dval;
			} else if (Shape.DATE.equals(fieldType.getShape())) {
				DValue dval = builder.buildDate(input, fieldType); 
				return dval;
			} else if (Shape.STRUCT.equals(fieldType.getShape())) {
				DType relType = registry.getType(BuiltInTypes.RELATION_SHAPE);
				RelationValueBuilder rbuilder = new RelationValueBuilder(relType, fieldType.getName(), registry);
				if (fieldExp.exp instanceof ListExp) {
					DStructType relStructType = (DStructType) registry.getType(fieldType.getName());
					PrimaryKey pk = relStructType.getPrimaryKey();
					//TODO should use input here not fieldExp???
					List<DValue> elementL = buildList((ListExp)fieldExp.exp, fieldType.getName(), pk.getKeyType());
					rbuilder.buildFromList(elementL);
				} else {
					rbuilder.buildFromString(input); 
				}
				boolean b = rbuilder.finish();
				if (!b) {
					for(DetailedError err: rbuilder.getValidationErrors()) {
						this.et.add(err);
					}
					return null;
				}
				DValue dval = rbuilder.getDValue();
				return dval;
			} else {
				return null;
			}
		}

		private List<DValue> buildList(ListExp listExp, String typeName, DType dtype) {
			List<DValue> resultL = new ArrayList<>();
			
			for(Exp exp: listExp.valueL) {
				Object rawValue = dvalConverter.extractObj(exp);
				DValue dval = dvalConverter.buildFromObject(rawValue, dtype.getShape(), this.builder);
				resultL.add(dval);
			}
			return resultL;
		}

		private String evalFieldExp(DsonFieldExp fieldExp, DType fieldType) {
			boolean b = sprigSvc.haveEnabledFor(fieldType.getName());
			if (b || fieldExp.exp instanceof IdentExp) {
				return varEvaluator.evalVarAsString(fieldExp.exp.strValue(), fieldType.getName());
			}
			
			return fieldExp.exp.strValue();
		}

		public Map<String, String> getAssocCrudMap() {
			return assocCrudMap;
		}

	}