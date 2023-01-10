package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.dval.DValueConverterService;
import org.delia.relation.RelationInfo;
import org.delia.tok.Tok;
import org.delia.tok.TokClauseBuilder;
import org.delia.type.*;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public abstract class StatementBuilderBase implements HLDStatementBuilder {
    protected final FactoryService factorySvc;
    protected final DValueConverterService dvalConverterService;
    protected final TokClauseBuilder tokBuilder;
    protected final HLDWhereHelper whereHelper;

    public StatementBuilderBase(FactoryService factorySvc) {
        this.factorySvc = factorySvc;
        this.dvalConverterService = new DValueConverterService(factorySvc);
        this.tokBuilder = new TokClauseBuilder(factorySvc);
        this.whereHelper = new HLDWhereHelper(factorySvc);
    }


    protected List<HLD.HLDFieldValue> buildFields(List<AST.InsertFieldStatementAst> fields, DStructType structType, HLDBuilderContext ctx, boolean isInsert) {
        List<HLD.HLDFieldValue> hldFields = new ArrayList<>();

        for (TypePair pair : structType.getAllFields()) {
            int index = findInStatement(pair.name, fields);
            if (index < 0) {
                if (isInsert && isValidSyntheticField(structType, pair, ctx)) {
                    //do nothing
                } else if (isInsert && !structType.fieldIsOptional(pair.name) && !structType.fieldIsSerial(pair.name)) {
                    String msg = String.format("Type %s: field '%s' is not optional. A value must be provided in the insert statement", structType.getName(), pair.name);
                    ctx.localET.add("value-missing-for-non-optional-field", msg);
                }
                continue; //probably an optional field and no value was provided
            }

            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
            HLD.HLDField field = new HLD.HLDField(structType, pair, relinfo);
            AST.InsertFieldStatementAst fieldAST = fields.get(index);

            if (fieldAST.crudAction != null) {
                field.crudAction = CrudAction.valueOf(fieldAST.crudAction.toUpperCase(Locale.ROOT)); //already checked in Pass2Compiler
            }

            if (fieldAST.varExp != null) {
                DValue dval = createDeferredDValue(pair, fieldAST.varExp, ctx);
//                List<DValue> dvallist = ctx.varEvaluator.lookupVar(fieldAST.varExp.fieldName);
//                if (CollectionUtils.isEmpty(dvallist)) {
//                    DeliaExceptionHelper.throwError("var-eval-failed", "insert contained '%s' that failed", fieldAST.fieldName);
//                }
                HLD.HLDFieldValue fieldVal = new HLD.HLDFieldValue(field, dval);
                hldFields.add(fieldVal);
            } else if (fieldAST.valueExp != null) {
                DValue dval = fieldAST.valueExp.value;
                dval = normalizeValue(dval, field, ctx);
                HLD.HLDFieldValue fieldVal = new HLD.HLDFieldValue(field, dval);
                hldFields.add(fieldVal);
            } else {
                List<DValue> dvalList = new ArrayList<>();
                for (Exp.ElementExp exp : fieldAST.listExp.listL) {
                    Exp.ValueExp vexp = (Exp.ValueExp) exp;
                    DValue dval = normalizeValue(vexp.value, field, ctx);
                    dvalList.add(dval);
                }
                HLD.HLDFieldValue fieldVal = new HLD.HLDFieldValue(field, null);
                fieldVal.dvalueList = dvalList;
                hldFields.add(fieldVal);
            }
        }
        return hldFields;
    }

    protected boolean isValidSyntheticField(DStructType structType, TypePair pair, HLDBuilderContext ctx) {
        if (structType.fieldIsSerial(pair.name) && ctx.syntheticIdMap != null) {
            String synthField = ctx.syntheticIdMap.get(structType.getName());
            return synthField != null;
        }
        return false;
    }
    protected String getSyntheticField(DStructType structType, HLDBuilderContext ctx) {
        if (ctx.syntheticIdMap != null) {
            String synthField = ctx.syntheticIdMap.get(structType.getName());
            return synthField;
        }
        return null;
    }

    protected HLD.HLDFieldValue buildSingleField(TypePair pair, DValue dval, DStructType structType, HLDBuilderContext ctx) {
        //TODO: do we need to check that pair is in structType?
        RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
        HLD.HLDField field = new HLD.HLDField(structType, pair, relinfo);
        //TODO: support defered value: upsert Customer[x]
        dval = normalizeValue(dval, field, ctx);
        HLD.HLDFieldValue fieldVal = new HLD.HLDFieldValue(field, dval);
        return fieldVal;
    }

    //convert dval to actual field type (eg. dval might be LONG and want NUMBER)
    protected DValue normalizeValue(DValue dval, HLD.HLDField field, HLDBuilderContext ctx) {
        if (field.pair.type.isStructShape()) {
            TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(field.pair.type);
            return dvalConverterService.normalizeValue(dval, pkpair.type, ctx.valueBuilder);
        }
        return dvalConverterService.normalizeValue(dval, field.pair.type, ctx.valueBuilder);
    }
    protected DValue normalizeValue(DValue dval, DType dtype, ScalarValueBuilder valueBuilder) {
        return dvalConverterService.normalizeValue(dval, dtype, valueBuilder);
    }

    protected DValue createDeferredDValue(TypePair pair, Exp.FieldExp varExp, HLDBuilderContext ctx) {
        Shape shape = pair.type.getShape();
        DValue dval = dvalConverterService.buildDefaultValue(shape, ctx.valueBuilder);
        DeferredDValue deferredDValue = new DeferredDValue(dval.getType(), dval.getObject(), varExp.fieldName);
        return deferredDValue;
    }

    private int findInStatement(String fieldName, List<AST.InsertFieldStatementAst> fields) {
        int index = 0;
        for (AST.InsertFieldStatementAst field : fields) {
            if (field.fieldName.equals(fieldName)) {
                return index;
            }
            index++;
        }
        return -1;
    }


}
