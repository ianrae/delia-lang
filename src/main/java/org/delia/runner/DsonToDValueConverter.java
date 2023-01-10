package org.delia.runner;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueConverterService;
import org.delia.error.DetailedError;
import org.delia.error.ErrorTracker;
import org.delia.lld.LLD;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.PartialStructValueBuilder;
import org.delia.valuebuilder.RelationValueBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;
import org.delia.varevaluator.VarEvaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DsonToDValueConverter extends ServiceBase {
    private DTypeRegistry registry;
    private VarEvaluator varEvaluator;
    private ScalarValueBuilder builder;
    //		private SprigService sprigSvc;
    private Map<String, String> assocCrudMap;
    private DValueConverterService dvalConverter;

    public DsonToDValueConverter(FactoryService factorySvc, ErrorTracker localET, DTypeRegistry registry, VarEvaluator varEvaluator) { //}, SprigService sprigSvc) {
        super(factorySvc);
        this.registry = registry;
//			this.varEvaluator = varEvaluator;
        this.et = localET;
        this.builder = new ScalarValueBuilder(factorySvc, registry);
        this.dvalConverter = new DValueConverterService(factorySvc);
//			this.sprigSvc = sprigSvc;
        this.varEvaluator = varEvaluator;
    }

    public DValue convertOne(DTypeName typeName, DsonExp dsonExp, ConversionResult cres) {
        return doConvertOne(typeName, dsonExp, false, false, cres);
    }

    public DValue convertOneUpsert(DTypeName typeName, DsonExp dsonExp, ConversionResult cres) {
        return doConvertOne(typeName, dsonExp, false, true, cres);
    }

    public DValue convertOnePartial(DTypeName typeName, DsonExp dsonExp, ConversionResult cres) {
        this.assocCrudMap = new HashMap<>();
        return doConvertOne(typeName, dsonExp, true, false, cres);
    }

    private DValue doConvertOne(DTypeName typeName, DsonExp dsonExp, boolean isPartial, boolean ignoreMissingPK, ConversionResult cres) {
        cres.localET = et;
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
            if (ignoreMissingPK) {
                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
                if (pkpair != null) {
                    structBuilder.setIgnoreThisField(pkpair.name);
                }
            }
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
        for (LLD.LLFieldValue fieldExp : dsonExp.fieldL) {
            String fieldName = fieldExp.field.getFieldName();
            DType fieldType = findFieldType(structType, fieldName);
//				if (assocCrudMap != null && fieldExp.assocCrudAction != null) {
//					assocCrudMap.put(fieldName, fieldExp.assocCrudAction.strValue());
//				}

//				//Customer.sid
//				if (sprigSvc.haveEnabledFor(structType.getName(), fieldName) && cres != null) {
//					cres.extraMap.put(fieldName, builder.buildInt(fieldExp.exp.strValue()));
//					continue;
//				}

            if (fieldType == null) {
                et.add("field-not-found", String.format("type '%s': can't find field '%s'", structType.getName(), fieldName));
                //throw new ValueException(err);
                return null;
            }

            DValue inner = buildInner(fieldExp, fieldType);
            structBuilder.addField(fieldName, inner);
        }

        boolean b = structBuilder.finish();
        if (!b) {
            for (DetailedError err : structBuilder.getValidationErrors()) {
                et.add(err);
            }
//				throw new ValueException(err);
            return null;
        }
        DValue dval = structBuilder.getDValue();
        return dval;
    }

    private DType findFieldType(DStructType dtype, String fieldName) {
        for (TypePair pair : dtype.getAllFields()) {
            if (pair.name.equals(fieldName)) {
                return pair.type;
            }
        }
        return null;
    }

    private DValue buildInner(LLD.LLFieldValue fieldExp, DType fieldType) {
//			if (fieldExp.exp instanceof NullExp) {
//				return null;
//			}

        //TODO we already have normalized DValues. Why build again?

        DValue input = null;
        List<DValue> inputList = null;
        if (fieldType.isStructShape() && fieldExp.dvalList != null) {
            //insert Address {id: 100, cust: [55,57] }
            for (DValue dval : fieldExp.dvalList) {
                resolveSingleDeferredVar(dval);
            }
            inputList = fieldExp.dvalList; //TODO later eval variables

        } else {
            resolveSingleDeferredVar(fieldExp.dval);
            input = evalFieldExp(fieldExp, fieldType);
            if (input == null) {
                return null;
            }
        }

        switch (fieldType.getShape()) {
            case STRING: {
//				DValue dval = builder.buildString(input, fieldType);
                return input;
            }
            case INTEGER: {
                if (fieldType.getEffectiveShape().equals(EffectiveShape.EFFECTIVE_LONG)) {
                    Long nval = input.asLong();
                    DValue dval = builder.buildInt(nval);
                    return dval;
                } else {
                    Integer nval = input.asInt();
                    DValue dval = builder.buildInt(nval);
                    return dval;
                }
            }
//			case LONG: {
////				DValue dval = builder.buildLong(input, fieldType);
//				return input;
//			}
            case NUMBER: {
//				DValue dval = builder.buildNumber(input, fieldType);
                return input;
            }
            case BOOLEAN: {
//				DValue dval = builder.buildBoolean(input, fieldType);
                return input;
            }
            case DATE: {
                DValue dval = builder.buildDate(input.asString(), fieldType);
                return dval;
            }
            case BLOB:
                //Note we don't use BlobLoader any more. Apps can generate HLD statements programmatically instead.
                return input;
            case STRUCT: {
                DType relType = registry.getType(BuiltInTypes.RELATION_SHAPE);
                RelationValueBuilder rbuilder = new RelationValueBuilder(relType, fieldType.getTypeName(), registry);
                //TODO fix this later
//				if (fieldExp.exp instanceof ListExp) {
//					DStructType relStructType = (DStructType) registry.getType(fieldType.getName());
//					PrimaryKey pk = relStructType.getPrimaryKey();
//					List<DValue> elementL = buildList((ListExp)fieldExp.exp, fieldType.getName(), pk.getKeyType());
//					rbuilder.buildFromList(elementL);
//				} else {
//					rbuilder.buildFromString(input);
//				}
                rbuilder.buildFromList(input == null ? inputList : Collections.singletonList(input));
                boolean b = rbuilder.finish();
                if (!b) {
                    for (DetailedError err : rbuilder.getValidationErrors()) {
                        this.et.add(err);
                    }
                    return null;
                }
                DValue dval = rbuilder.getDValue();
                return dval;
            }
            default:
                return null;
        }
    }

    private void resolveSingleDeferredVar(DValue dval) {
        if (dval != null) {
            DValue realVal = DeferredDValueHelper.preResolveDeferredDval(dval, varEvaluator);
            realVal = dvalConverter.normalizeValue(realVal, dval.getType(), builder);
            DeferredDValueHelper.resolveTo(dval, realVal); //note. realVal can be null
        }
    }

//		private List<DValue> buildList(ListExp listExp, String typeName, DType dtype) {
//			List<DValue> resultL = new ArrayList<>();
//
//			for(Exp exp: listExp.valueL) {
//				Object rawValue = dvalConverter.extractObj(exp);
//				DValue dval = dvalConverter.buildFromObject(rawValue, dtype.getShape(), this.builder);
//				resultL.add(dval);
//			}
//			return resultL;
//		}

    private DValue evalFieldExp(LLD.LLFieldValue fieldExp, DType fieldType) {
//			boolean b = sprigSvc.haveEnabledFor(fieldType.getName());
//			if (b || fieldExp.exp instanceof IdentExp) {
//				return varEvaluator.evalVarAsString(fieldExp.exp.strValue(), fieldType.getName());
//			}

        return fieldExp.dval;
    }

    public Map<String, String> getAssocCrudMap() {
        return assocCrudMap;
    }

}