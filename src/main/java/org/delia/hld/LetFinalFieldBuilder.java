package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.lld.processor.TokFieldHintVisitor;
import org.delia.relation.RelationInfo;
import org.delia.tok.Tok;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LetFinalFieldBuilder extends StatementBuilderBase {

    private static class FFInfo {
        DStructType currentType;
        HLD.HLDFuncEx mostRecentOrderBy;
        HLD.HLDField mostRecentField;
    }

    public LetFinalFieldBuilder(FactoryService factorySvc) {
        super(factorySvc);
    }

    @Override
    public void build(AST.StatementAst statementParam, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        DeliaExceptionHelper.throwError("dont-call-this", "not supported by this class");
    }

    @Override
    public void assignDATs(HLD.HLDStatement statement, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        DeliaExceptionHelper.throwError("dont-call-this", "not supported by this class");
    }


    //TODO i think final field can cause joins. implement this!
    //the whole chain of final funcs cannot cause joins i think
    public List<AddedFieldInfo> buildFinalFields(AST.LetStatementAst statement, HLD.LetHLDStatement hld, HLDBuilderContext ctx) {
        if (statement.fieldAndFuncs == null) {
            AddedFieldInfo info = new AddedFieldInfo();
            info.structType = hld.fromType;
            return Collections.singletonList(info);
        }

        TokFieldHintVisitor visitor = new TokFieldHintVisitor(hld.fromType, true);
        hld.fieldAndFuncs.visit(visitor, null);

        List<AddedFieldInfo> resultL = new ArrayList<>();
        AddedFieldInfo info = null;
        FFInfo ffInfo = new FFInfo();
        ffInfo.currentType = hld.hldTable;
        ffInfo.mostRecentOrderBy = null;
        ffInfo.mostRecentField = null;
        for (Tok.DToken tok : hld.fieldAndFuncs.chainL) {
            if (tok instanceof Tok.FieldTok) {
                Tok.FieldTok fexp = (Tok.FieldTok) tok;
                info = createField(ffInfo.currentType, fexp.fieldName, hld.finalFieldsL, ctx);
                if (info == null) {
                    return null; //an error was detected
                }
                ffInfo.currentType = info.structType;
                ffInfo.mostRecentField = info.hldField;

                for (Tok.FunctionTok func : fexp.funcL) {
                    zzFunc(func, hld, ffInfo, ctx);
                }

                TypePair pair = DValueHelper.findField(ffInfo.currentType, fexp.fieldName);
                if (pair != null && pair.type.isStructShape()) {
                    ffInfo.currentType = (DStructType) pair.type;
                }

                resultL.add(info);
            } else if (tok instanceof Tok.FunctionTok) {
                Tok.FunctionTok func = (Tok.FunctionTok) tok;
                zzFunc(func, hld, ffInfo, ctx);
            } else {
                DeliaExceptionHelper.throwNotImplementedError("rtuu");
            }
        }

        if (! resultL.isEmpty()) {
            hld.finalField = resultL.get(resultL.size() - 1).hldField;
        }
        return resultL;
    }

    private void zzFunc(Tok.FunctionTok func, HLD.LetHLDStatement hld, FFInfo ffInfo, HLDBuilderContext ctx) {
        Tok.FunctionTok ffexp = (Tok.FunctionTok) func;
//                    if (fexp.prefix != null) {
//                        info = createField(currentType, fexp.prefix, hld.finalFieldsL, ctx);
//                        if (info == null) {
//                            return null;
//                        }
//                        mostRecentField = info.hldField;
//                        currentType = info.structType;
//                    }
        HLD.HLDFuncEx hldFunc = new HLD.HLDFuncEx(ffexp.fnName);
        if (ffexp.isName("ith") || ffexp.isName("offset") || ffexp.isName("limit")) {
            hldFunc.argsL = convertSimpleFnArgs(ffexp.argsL);
        } else {
            hldFunc.argsL = convertFnArgs(ffInfo.currentType, ffexp.argsL, ctx);
        }
        hld.finalFieldsL.add(hldFunc);
        if (ffexp.isName("exists")) {
            hld.resultType = ctx.registry.getType(BuiltInTypes.BOOLEAN_SHAPE);
        } else if (ffexp.isName("orderBy")) {
            ffInfo.mostRecentOrderBy = hldFunc;
        } else if (ffexp.isName("last")) {
            doLast(hldFunc, ffInfo.mostRecentOrderBy, ctx);
        } else if (ffexp.isName("min") || ffexp.isName("max")) {
            doMinOrMax(hldFunc, ffInfo.mostRecentField, ffInfo.currentType, ctx);
        }

    }

    private void doMinOrMax(HLD.HLDFuncEx hldFunc, HLD.HLDField mostRecentField, DStructType structType, HLDBuilderContext ctx) {
        String fnName = hldFunc.fnName;
        if (mostRecentField == null) {
            DeliaExceptionHelper.throwNotImplementedError(fnName + " needs a field");
        } else {
            String fieldName = mostRecentField.pair.name;

            TypePair pair = DValueHelper.findField(structType, fieldName);
            if (pair == null) {
                String msg = String.format("let '%s': %s() on unknown field '%s'", structType.getName(), fnName, fieldName);
                ctx.localET.add("unknown-field", msg).setLoc(ctx.loc);
                return;
            }

            HLD.HLDFuncArg funcArg = new HLD.HLDFuncArg(mostRecentField.pair.name);
            hldFunc.argsL.add(funcArg);
        }
    }

    private void doLast(HLD.HLDFuncEx hldFunc, HLD.HLDFuncEx mostRecentOrderBy, HLDBuilderContext ctx) {
        if (ctx.isMEMDb()) {
            return; //don't ask 'desc'. The mem function will implement 'last'
        }
        if (mostRecentOrderBy == null) {
            //DeliaExceptionHelper.throwNotImplementedError("last needs an orderBy");
            //OK we'll just let postgres return whatever order it chooses and do limit 1
        } else {
            List<HLD.HLDEx> newList = new ArrayList<>();
            //remove 'asc' if present
            //TODO this won't work if multiple sorts and multiple asc/desc
            for (HLD.HLDEx ex : mostRecentOrderBy.argsL) {
                boolean doCopy = true;
                if (ex instanceof HLD.HLDFuncArg) {
                    HLD.HLDFuncArg fnArg = (HLD.HLDFuncArg) ex;
                    if (fnArg.argVal.equals("asc")) {
                        doCopy = false;
                    }
                }

                if (doCopy) {
                    newList.add(ex);
                }
            }

            HLD.HLDFuncArg fnArg = new HLD.HLDFuncArg("desc");
            mostRecentOrderBy.argsL.add(fnArg);
        }
    }

    private List<HLD.HLDEx> convertFnArgs(DStructType currentType, List<Tok.DToken> argsL, HLDBuilderContext ctx) {
        List<HLD.HLDEx> list = new ArrayList<>();
        for (Tok.DToken exp : argsL) {
            if (exp instanceof Tok.FunctionTok) {
                Tok.FunctionTok fexp = (Tok.FunctionTok) exp;
                HLD.HLDFuncEx funcEx = new HLD.HLDFuncEx(fexp.fnName);
                funcEx.argsL = convertFnArgs(currentType, fexp.argsL, ctx); //**recursion**
                list.add(funcEx);
            } else if (exp instanceof Tok.ValueTok) {
                Tok.ValueTok vexp = (Tok.ValueTok) exp;
                List<HLD.HLDEx> unusedL = new ArrayList<>();
                AddedFieldInfo info = createField(currentType, vexp.strValue(), unusedL, ctx);
                if (info == null) {
                    HLD.HLDFieldValue fieldValue = new HLD.HLDFieldValue(info.hldField, vexp.value);
                    list.add(fieldValue);
                } else {
                    HLD.HLDField field = info.hldField;
                    list.add(field);
                }
            } else if (exp instanceof Tok.FieldTok) {
                Tok.FieldTok fexp = (Tok.FieldTok) exp;
                HLD.HLDFuncArg funcArg = new HLD.HLDFuncArg(fexp.fieldName);
                list.add(funcArg);

            }
        }
        return list;
    }

    //some fns like ith(0) never have fields as args, so can parse in simpler way
    //TODO: need to do var lookup. eg ith(z)
    private List<HLD.HLDEx> convertSimpleFnArgs(List<Tok.DToken> argsL) {
        List<HLD.HLDEx> list = new ArrayList<>();
        for (Tok.DToken exp : argsL) {
            if (exp instanceof Tok.ValueTok) {
                Tok.ValueTok vexp = (Tok.ValueTok) exp;
                HLD.HLDFuncArg funcArg = new HLD.HLDFuncArg(vexp.value == null ? null : vexp.value.asString());
                funcArg.dval = vexp.value;
                list.add(funcArg);
            } else if (exp instanceof Tok.FieldTok) {
                Tok.FieldTok fexp = (Tok.FieldTok) exp;
                HLD.HLDFuncArg funcArg = new HLD.HLDFuncArg(fexp.fieldName);
                list.add(funcArg);
            } else {
                DeliaExceptionHelper.throwError("convertSimpleFnArgs unsupported exp..", exp.strValue());
            }
        }
        return list;
    }


    //This supports contiguous subfields like Customer[true].addr.region.id
    //TODO support non-contiguous subfields like Customer[true].addr.distinct().region.first().id
    private AddedFieldInfo createField(DStructType currentType, String fieldNames, List<HLD.HLDEx> finalFieldsL, HLDBuilderContext ctx) {
        //
        String[] ar = fieldNames.split("\\.");
        String joinField = null;
        TypePair matchPair = null;
        DStructType potentialFromType = null;
        HLD.HLDField hldField = null;
        for (String fieldName : ar) {
            if (potentialFromType != null) {
                currentType = potentialFromType;  //.addr should not change currentType, but .addr.city should
                potentialFromType = null;
            }

            matchPair = DValueHelper.findField(currentType, fieldName);
            if (matchPair == null) {
                //err? perhaps not, eg "asc" and "desc". no is always an error
                String msg = String.format("let '%s': unknown field '%s' in '%s'", currentType.getName(), fieldName, fieldNames);
                ctx.localET.add("unknown-field", msg);
                return null;
            } else if (matchPair.type.isStructShape()) {
                potentialFromType = (DStructType) matchPair.type;
                joinField = fieldName;
                hldField = createHLDField(currentType, fieldName);
                finalFieldsL.add(hldField);
            } else {
                hldField = createHLDField(currentType, fieldName);
                finalFieldsL.add(hldField);
            }
        }

        if (matchPair == null) {
            String msg = String.format("let '%s': unknown field '%s'", currentType.getName(), fieldNames);
            ctx.localET.add("unknown-field", msg);
            return null;
        }

        AddedFieldInfo info = new AddedFieldInfo();
        info.hldField = hldField;
        info.structType = currentType;
        info.joinField = joinField;
        return info;
    }

    private HLD.HLDField createHLDField(DStructType structType, String fieldName) {
        TypePair pair1 = DValueHelper.findField(structType, fieldName);
        RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, fieldName);
        TypePair pair = new TypePair(fieldName, pair1.type);
        HLD.HLDField hldField = new HLD.HLDField(structType, pair, relinfo);
        return hldField;
    }

    public void addJoinInfo(HLD.LetHLDStatement hld, HLD.HLDJoin join) {
        if (hld.finalFieldsL.isEmpty()) {
            return;
        }

        for (HLD.HLDEx x : hld.finalFieldsL) {
            if (x instanceof HLD.HLDField) {
                HLD.HLDField hldField = (HLD.HLDField) x;
                if (hldField.hldTable == join.joinInfo.leftType) {
                    hldField.finalJoin = join;
                }
            } else if (x instanceof HLD.HLDFuncEx) {
                HLD.HLDFuncEx hldFunc = (HLD.HLDFuncEx) x;
                addJoinToFnArgs(hldFunc.argsL, join);
            } else {
            }
        }
    }

    private void addJoinToFnArgs(List<HLD.HLDEx> argsL, HLD.HLDJoin join) {
        for (HLD.HLDEx x : argsL) {
            if (x instanceof HLD.HLDField) {
                HLD.HLDField hldField = (HLD.HLDField) x;
                if (hldField.hldTable == join.joinInfo.leftType) {
                    hldField.finalJoin = join;
                }
            } else if (x instanceof HLD.HLDFuncEx) {
                HLD.HLDFuncEx hldFunc = (HLD.HLDFuncEx) x;
                LLD.LLDFuncEx func = new LLD.LLDFuncEx(hldFunc.fnName);
                addJoinToFnArgs(hldFunc.argsL, join); //**recursion**
            } else {
            }
        }
    }
}

