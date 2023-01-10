package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.rule.RuleBuilder;
import org.delia.rule.rules.IsDateOnlyRule;
import org.delia.rule.rules.IsTimeOnlyRule;
import org.delia.rule.rules.RulePostProcessor;
import org.delia.rule.rules.SizeofRule;
import org.delia.type.*;
import org.delia.typebuilder.FutureDeclError;
import org.delia.typebuilder.PreTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

import java.util.List;

/**
 * A sub-runner that executes Delia type statements.
 *
 * @author Ian Rae
 */
public class NewTypeRunner extends ServiceBase {
    private final RuleBuilder ruleBuilder;
    private DTypeRegistry registry;
    private PreTypeRegistry preRegistry;
    private boolean haveAddedSizeofTypes;
    private boolean haveAddedDateOnlyTypes;

    public NewTypeRunner(FactoryService factorySvc, DTypeRegistry registry) {
        super(factorySvc);
        this.registry = registry;
        this.ruleBuilder = new RuleBuilder(factorySvc, registry);
    }

    public void executeStatements(List<AST.StatementAst> extL, List<DeliaError> allErrors, boolean runRulePostProcessor) {
        for (AST.StatementAst exp : extL) {
            executeStatement(exp, allErrors);
        }

        if (allErrors.isEmpty() && runRulePostProcessor) {
            executeRulePostProcessor(allErrors);
        }
    }

    public void executeRulePostProcessor(List<DeliaError> allErrors) {
        //TODO: this is needed for relations
        if (allErrors.isEmpty()) {
            RulePostProcessor postProcessor = new RulePostProcessor(factorySvc);
            postProcessor.process(registry, allErrors);
        }
    }

    private void executeStatement(AST.StatementAst exp, List<DeliaError> allErrors) {
        if (exp instanceof AST.TypeAst) {
            //log.log("exec: " + exp.toString());
            executeTypeStatement((AST.TypeAst) exp, allErrors);
        }

    }

    private void executeTypeStatement(AST.TypeAst exp, List<DeliaError> allErrors) {
        NewTypeBuilder typeBuilder = new NewTypeBuilder(factorySvc, registry, preRegistry);

        DType dtype = typeBuilder.createType(exp);
        if (dtype == null) {
            allErrors.addAll(typeBuilder.getErrorTracker().getErrors());
        }
        ruleBuilder.addRules(dtype, exp);
        adjustForSizeof(dtype);
        adjustForIsDateOnly(dtype);
        adjustForIsTimeOnly(dtype);
    }

    //handle sizeof rule for int fields (or int scalar types). set EffectiveShape.LONG when needed
    private void adjustForSizeof(DType dtype) {
        List<SizeofRule> sizeofRules = ruleBuilder.findSizeofRules(dtype);
        for(SizeofRule sizeofRule: sizeofRules) {
            //Note. sizeof can also be used for strings. we only care about integers here
            if (dtype.isStructShape()) {
                DStructTypeImpl structType = (DStructTypeImpl) dtype;
                String fieldName = sizeofRule.getSubject();
                TypePair pair = structType.findField(fieldName);
                if (pair != null && pair.type.isShape(Shape.INTEGER)) {
                    DType intType = getEffectiveIntType(sizeofRule);
                    structType.setEffectiveShapeType(fieldName, intType);
                }
            } else {
                if (dtype.isShape(Shape.INTEGER)) {
                    DType intType = getEffectiveIntType(sizeofRule);
                    DTypeImpl typeImpl = (DTypeImpl) dtype;
                    typeImpl.setEffectiveShape(intType.getEffectiveShape());
                }
            }
        }
    }
    private void adjustForIsDateOnly(DType dtype) {
        List<IsDateOnlyRule> rules = ruleBuilder.findIsDateOnlyRules(dtype);
        for(IsDateOnlyRule isDateOnlyRule: rules) {
            //Note. sizeof can also be used for strings. we only care about integers here
            if (dtype.isStructShape()) {
                DStructTypeImpl structType = (DStructTypeImpl) dtype;
                String fieldName = isDateOnlyRule.getSubject();
                TypePair pair = structType.findField(fieldName);
                if (pair != null && pair.type.isShape(Shape.DATE)) {
                    DType intType = getEffectiveDateType(isDateOnlyRule);
                    structType.setEffectiveShapeType(fieldName, intType);
                }
            } else {
                if (dtype.isShape(Shape.DATE)) {
                    DType intType = getEffectiveDateType(isDateOnlyRule);
                    DTypeImpl typeImpl = (DTypeImpl) dtype;
                    typeImpl.setEffectiveShape(intType.getEffectiveShape());
                }
            }
        }
    }

    private void adjustForIsTimeOnly(DType dtype) {
        List<IsTimeOnlyRule> rules = ruleBuilder.findIsTimeOnlyRules(dtype);
        for(IsTimeOnlyRule isTimeOnlyRule: rules) {
            //Note. sizeof can also be used for strings. we only care about integers here
            if (dtype.isStructShape()) {
                DStructTypeImpl structType = (DStructTypeImpl) dtype;
                String fieldName = isTimeOnlyRule.getSubject();
                TypePair pair = structType.findField(fieldName);
                if (pair != null && pair.type.isShape(Shape.DATE)) {
                    DType intType = getEffectiveTimeType(isTimeOnlyRule);
                    structType.setEffectiveShapeType(fieldName, intType);
                }
            } else {
                if (dtype.isShape(Shape.DATE)) {
                    DType intType = getEffectiveTimeType(isTimeOnlyRule);
                    DTypeImpl typeImpl = (DTypeImpl) dtype;
                    typeImpl.setEffectiveShape(intType.getEffectiveShape());
                }
            }
        }
    }


    private DType getEffectiveIntType(SizeofRule sizeofRule) {
        if (! haveAddedSizeofTypes) {
            DTypeRegistryBuilder regBuilder = new DTypeRegistryBuilder(registry);
            regBuilder.registerSizeOfInts();
            haveAddedSizeofTypes = true;
        }

        String sizeofTypeName = String.format("INTEGER_%d", sizeofRule.getSizeofAmount());
        DTypeName typeName = new DTypeName(null, sizeofTypeName);
        DType intType = registry.getType(typeName);
        if (intType == null) {
            DeliaExceptionHelper.throwError("sizeof-wrong-amount", "Can't find type '%s'", sizeofTypeName);
        }
        return intType;
    }
    private DType getEffectiveDateType(IsDateOnlyRule rule) {
        if (! haveAddedDateOnlyTypes) {
            DTypeRegistryBuilder regBuilder = new DTypeRegistryBuilder(registry);
            regBuilder.registerDateAndTimeOnly();
            haveAddedDateOnlyTypes = true;
        }

        String sizeofTypeName = String.format("DATE_DATE_ONLY");
        DTypeName typeName = new DTypeName(null, sizeofTypeName);
        DType intType = registry.getType(typeName);
        if (intType == null) {
            DeliaExceptionHelper.throwError("isDateOnly-wrong-amount", "Can't find type '%s'", sizeofTypeName);
        }
        return intType;
    }
    private DType getEffectiveTimeType(IsTimeOnlyRule rule) {
        if (! haveAddedDateOnlyTypes) {
            DTypeRegistryBuilder regBuilder = new DTypeRegistryBuilder(registry);
            regBuilder.registerDateAndTimeOnly();
            haveAddedDateOnlyTypes = true;
        }

        String sizeofTypeName = String.format("DATE_TIME_ONLY");
        DTypeName typeName = new DTypeName(null, sizeofTypeName);
        DType intType = registry.getType(typeName);
        if (intType == null) {
            DeliaExceptionHelper.throwError("isDateOnly-wrong-amount", "Can't find type '%s'", sizeofTypeName);
        }
        return intType;
    }


    public DTypeRegistry getRegistry() {
        return registry;
    }

    public boolean hasFutureDeclErrors() {
        for (DeliaError err : et.getErrors()) {
            if (err instanceof FutureDeclError) {
                return true;
            }
        }
        return false;
    }


    public PreTypeRegistry getPreRegistry() {
        return preRegistry;
    }

    public void setPreRegistry(PreTypeRegistry preRegistry) {
        this.preRegistry = preRegistry;
    }

}