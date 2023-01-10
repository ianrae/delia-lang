package org.delia.lld.processor;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.hld.HLD;
import org.delia.hld.dat.AssocSpec;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.sql.LLFieldHelper;
import org.delia.tok.Tok;
import org.delia.tok.TokVisitorUtils;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LetLLDProcessor extends ServiceBase implements LLDProcessor, LLTableResolver {

    private final LLWhereAdjuster whereAdjuster;
    private final LLTokWhereAdjuster tokWhereAdjuster;
    private final LLJoinHelper joinHelper;

    //avoid creating multiple instances of LLTable for same table
    //However key is log.phys name because Customer.Customer can exist (main select part)
    //and Customer.Address can also exist (in a join)
    private Map<String, LLD.LLTable> tableMap = new HashMap<>(); //logicalTypeName.physicalTypeName,it's table

    public LetLLDProcessor(FactoryService factorySvc, AliasProcessor aliasProc) {
        super(factorySvc);
        this.whereAdjuster = new LLWhereAdjuster(factorySvc);
        this.tokWhereAdjuster = new LLTokWhereAdjuster(factorySvc);
        this.joinHelper = new LLJoinHelper(tableMap, this, aliasProc);
    }

    @Override
    public void build(HLD.HLDStatement hldStatementParam, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx) {
        HLD.LetHLDStatement statement = (HLD.LetHLDStatement) hldStatementParam;
        LLD.LLTable llTable = createLLTable(statement.fromType);
//            public int datId; //0 means not an assoc table

        LLD.LLSelect selectLL = new LLD.LLSelect(hldStatementParam.getLoc());
        selectLL.table = llTable;
//        selectLL.whereClause = adjustWhereClause(statement, ctx);
        selectLL.whereTok = adjustWhereClause(statement, ctx);
        selectLL.resultType = statement.resultType;
        selectLL.varName = statement.varName;
        selectLL.finalFieldsL = statement.finalFieldsL.stream().map(x -> {
            if (x instanceof HLD.HLDField) {
                HLD.HLDField hldField = (HLD.HLDField) x;
//                LLD.LLFinalFieldEx field = new LLD.LLFinalFieldEx(llTable, hldField.pair.name);
                LLD.LLTable llTableForField = createLLTable(hldField.hldTable);
                LLD.LLFinalFieldEx field = new LLD.LLFinalFieldEx(llTableForField, hldField.pair.name);
                adjustForFinalField(field, hldField, statement.finalField);
                return field;
            } else if (x instanceof HLD.HLDFuncEx) {
                HLD.HLDFuncEx hldFunc = (HLD.HLDFuncEx) x;
                LLD.LLDFuncEx func = new LLD.LLDFuncEx(hldFunc.fnName);
                func.argsL = convertFnArgs(selectLL, hldFunc.argsL, statement);
                return func;
            } else if (x instanceof HLD.HLDFuncArg) {
                HLD.HLDFuncArg funcArgHLD = (HLD.HLDFuncArg) x;
                LLD.LLFuncArg funcArg = new LLD.LLFuncArg(funcArgHLD.argVal);
                return funcArg;
            } else {
                DeliaExceptionHelper.throwNotImplementedError("final fields in lld");
                return null;
            }
        }).collect(Collectors.toList());

        for (HLD.HLDEx hldex : statement.fields) {
            if (hldex instanceof HLD.HLDField) {
                HLD.HLDField hldField = (HLD.HLDField) hldex;
                //in LL we only want fields that will appear in SQL statement
                if (hldField.relinfo != null && hldField.relinfo.isParent && !hldField.relinfo.isManyToMany()) {
                    //ignore
                } else if (hldField.relinfo != null && hldField.relinfo.isManyToMany()) {
                    if (isNeededManyToManyField(hldField, statement)) {
                        AssocSpec assoc = ctx.datSvc.findAssocInfo(hldField.relinfo);
                        String fieldName = hldField.pair.name;
                        TypePair pp = null;
                        if (fieldName.equals(assoc.deliaLeftv)) {
                            pp = new TypePair(assoc.rightColumn, hldField.pair.type); //TODO what about type??
//                            fld.assocPhysicalType = createDATType(ctx.registry, assoc);
                        } else {
                            pp = new TypePair(assoc.leftColumn, hldField.pair.type); //TODO what about type??
                        }
                        LLD.LLField field = new LLD.LLField(pp, createLLTable(createDATType(ctx.registry, assoc)), new LLD.DefaultLLNameFormatter());
                        //TODO: public String asName; //can be null
                        for (HLD.HLDJoin jj : statement.joinL) {
                            if (jj.joinInfo.relinfo == hldField.relinfo) {
                                field.joinInfo = jj.joinInfo;
                            }
                        }

                        field.isAssocField = true;
                        selectLL.fields.add(field);
                    }
                } else {
                    LLD.LLField field = new LLD.LLField(hldField.pair, llTable, new LLD.DefaultLLNameFormatter());
                    //TODO: public String asName; //can be null
                    selectLL.fields.add(field);
                }
            } else if (hldex instanceof HLD.HLDFuncEx) {
                HLD.HLDFuncEx ff = (HLD.HLDFuncEx) hldex;
                LLD.LLDFuncEx llff = new LLD.LLDFuncEx(ff.fnName);
                selectLL.fields.add(llff);
            }
        }
        addJoins(selectLL, statement, ctx);
        addJoinFields(selectLL);
        fillInWhereExpHints(selectLL);
        adjustWhereClauseForTrue(statement, selectLL, ctx);
//        fillInWhereFieldHints(selectLL);
        adjustFinalFields(selectLL, ctx);
        lldStatements.add(selectLL);
    }

    private void adjustForFinalField(LLD.LLFinalFieldEx field, HLD.HLDField hldField, HLD.HLDField finalField) {
        //TODO fix this. for now we add it to all final fields but later there may be multiple joins...
        field.finalJoin = finalField.finalJoin;
    }

    private void adjustFinalFields(LLD.LLSelect selectLL, LLDBuilderContext ctx) {
        for (LLD.LLEx ex : selectLL.finalFieldsL) {
            if (ex instanceof LLD.LLDFuncEx) {
                LLD.LLDFuncEx func = (LLD.LLDFuncEx) ex;
                if (func.fnName.equals("orderBy") && isAfterDistinct(selectLL.finalFieldsL, func)) {
                    continue; //avoid: SQL Error [42P10]: ERROR: for SELECT DISTINCT, ORDER BY expressions must appear in select list
                }

                for (LLD.LLEx arg : func.argsL) {
                    if (arg instanceof LLD.LLFinalFieldEx) {
                        LLD.LLFinalFieldEx field = (LLD.LLFinalFieldEx) arg;
                        if (field.finalJoin == null) {
                            continue;
                        }
                        Exp.FieldExp fld = new Exp.FieldExp(field.fieldName, field.finalJoin.joinInfo);
                        adjustWhereField(fld, ctx);
                        if (!fld.fieldName.equals(field.fieldName)) { //changed?
                            field.fieldName = fld.fieldName;
                            field.physicalTable = createOrGetLLTableAssoc(fld.assocPhysicalType);
                            //TODO: may need to fix for 1:1 and N:1 as well??
                        }
                    }
                }
            }
        }
    }

    //distinct.orderBy('id') -- is orderBy occuring within context of a distinct
    private boolean isAfterDistinct(List<LLD.LLEx> finalFieldsL, LLD.LLDFuncEx func) {
        LLD.LLDFuncEx distinctFn = LLFieldHelper.findFunc(finalFieldsL, "distinct");
        return LLFieldHelper.isFuncAfter(finalFieldsL, distinctFn, func);
    }

    private List<LLD.LLEx> convertFnArgs(LLD.LLSelect selectLL, List<HLD.HLDEx> argsL, HLD.LetHLDStatement statement) {
        List<LLD.LLEx> list = new ArrayList<>();
        for (HLD.HLDEx exp : argsL) {
            if (exp instanceof HLD.HLDFuncEx) {
                HLD.HLDFuncEx fexp = (HLD.HLDFuncEx) exp;
                LLD.LLDFuncEx funcEx = new LLD.LLDFuncEx(fexp.fnName);
                funcEx.argsL = convertFnArgs(selectLL, fexp.argsL, statement); //**recursion**
                list.add(funcEx);
            } else if (exp instanceof HLD.HLDFieldValue) {
                HLD.HLDFieldValue vexp = (HLD.HLDFieldValue) exp;
                LLD.LLTable llTable = createLLTable(vexp.hldField.hldTable);
                LLD.LLFinalFieldEx fieldEx = new LLD.LLFinalFieldEx(llTable, vexp.hldField.pair.name);
                fieldEx.finalJoin = vexp.hldField.finalJoin;
                list.add(fieldEx);
            } else if (exp instanceof HLD.HLDField) {
                HLD.HLDField fexp = (HLD.HLDField) exp;
                LLD.LLTable llTable = createLLTable(fexp.hldTable);
                LLD.LLFinalFieldEx fieldEx = new LLD.LLFinalFieldEx(llTable, fexp.pair.name);
                fieldEx.finalJoin = fexp.finalJoin;
                list.add(fieldEx);
            } else if (exp instanceof HLD.HLDFuncArg) {
                HLD.HLDFuncArg funcArgHLD = (HLD.HLDFuncArg) exp;
                LLD.LLFuncArg funcArg = new LLD.LLFuncArg(funcArgHLD.argVal);
                funcArg.dval = funcArgHLD.dval;
                list.add(funcArg);
            }
        }
        return list;
    }

    private void fillInWhereExpHints(LLD.LLSelect selectLL) {
        tokWhereAdjuster.fillInWhereExpHints(selectLL.table, selectLL.whereTok);
    }


    //hld includes MM leftv,rightv fields even if doing .fks()
    private boolean isNeededManyToManyField(HLD.HLDField hldField, HLD.LetHLDStatement statement) {
        if (hldField.relinfo != null && hldField.relinfo.isManyToMany()) {
            for (HLD.HLDJoin jj : statement.joinL) {
                for (HLD.HLDField fld : jj.fields) {
                    if (sameField(hldField, fld)) {
                        return jj.joinInfo.isFKOnly;
                    }
                }
            }
        }
        return false;
    }

    private boolean sameField(HLD.HLDField hldField, HLD.HLDField fld) {
        if (hldField.pair.type == fld.pair.type) {
            if (hldField.pair.name.equals(fld.pair.name)) {
                return true;
            }
        }
        return false;
    }

    private Tok.WhereTok adjustWhereClause(HLD.LetHLDStatement statement, LLDBuilderContext ctx) {
        tokWhereAdjuster.adjustWhereClause(statement.whereTok, statement.hldTable, ctx);
//        whereAdjuster.adjustWhereClause(statement.whereClause, ctx);
        return statement.whereTok;
    }

    private void adjustWhereClauseForTrue(HLD.LetHLDStatement statement, LLD.LLSelect selectLL, LLDBuilderContext ctx) {
//        selectLL.whereAllOrPKType = whereAdjuster.adjustWhereClauseForTrue(statement.whereClause, statement.hldTable, ctx);
//        selectLL.whereAllOrPKType = tokWhereAdjuster.adjustWhereClauseForTrue(statement.whereClause, statement.hldTable, ctx);

        if (TokVisitorUtils.isWhereAll(selectLL.whereTok.where) || TokVisitorUtils.isWherePK(selectLL.whereTok.where)) {
            selectLL.whereAllOrPKType = statement.hldTable;
        }
    }


    private void adjustWhereField(Exp.FieldExp fld, LLDBuilderContext ctx) {
        whereAdjuster.adjustWhereField(fld, ctx);
    }

//    private Tok.WhereTok adjustTokWhereClause(HLD.LetHLDStatement statement, LLDBuilderContext ctx) {
//        tokWhereAdjuster.adjustWhereClause(statement.whereTok, statement.hldTable, ctx);
//        return statement.whereTok;
//    }



    private LLD.LLTable createLLTable(DStructType hldTable) {
        String key = String.format("%s.%s", hldTable.getName(), hldTable.getName()); //doesn't includes schema. careful!
        if (tableMap.containsKey(key)) {
            return tableMap.get(key);
        }
        //TODO: fix schema. later will be inside DStructType
        LLD.LLTable llTable = new LLD.LLTable(hldTable, hldTable, new LLD.DefaultLLNameFormatter());
        tableMap.put(key, llTable);
        return llTable;
    }

    //LLTableResolver
    @Override
    public LLD.LLTable createOrGetLLTable(DStructType logicalType, LLD.LLTable llTable) {
        String key = String.format("%s.%s", logicalType.getName(), llTable.physicalType.getName()); //doesn't includes schema. careful!
        if (tableMap.containsKey(key)) {
            return tableMap.get(key);
        }
        tableMap.put(key, llTable);
        return llTable;
    }

    private LLD.LLTable createOrGetLLTableAssoc(DStructType datType) {
        return joinHelper.createOrGetLLTableAssoc(datType);
    }

    private void addJoins(LLD.LLSelect lld, HLD.LetHLDStatement hldStatement, LLDBuilderContext ctx) {
        //for now just copy over
        //but this will change when we have different physical table vs logical one
        for (HLD.HLDJoin hldJoin : hldStatement.joinL) {
            LLD.LLJoin join = new LLD.LLJoin();
            join.logicalJoin = hldJoin.joinInfo;
            LLD.LLJoin extraJoin = null;

            RelationInfo relinfo = hldJoin.joinInfo.relinfo; //Customer.addr
//            if (relinfo != null && relinfo.isParent && !relinfo.isManyToMany()) {
            if (relinfo != null && !relinfo.isManyToMany()) {
                if (relinfo.nearType.equals(hldStatement.fromType)) {
                    if (relinfo.isParent) {
                        //near is Customer.id
                        DStructType structType = relinfo.nearType;
                        join.physicalLeft = createFieldPKSide(structType, hldJoin);

                        //far is Address.cust
                        structType = relinfo.farType;
                        TypePair pp = DValueHelper.findField(structType, relinfo.otherSide.fieldName);
                        LLD.LLTable llOther = createOrGetLLTable(structType, new LLD.LLTable(structType, structType, new LLD.DefaultLLNameFormatter()));
                        join.physicalRight = createField(pp, llOther, hldJoin);
                    } else {
                        //far is Customer.addr
                        DStructType structType = relinfo.nearType;
                        TypePair pp = DValueHelper.findField(structType, relinfo.fieldName);
                        LLD.LLTable llOther = createOrGetLLTable(structType, new LLD.LLTable(structType, structType, new LLD.DefaultLLNameFormatter()));
                        join.physicalLeft = createField(pp, llOther, hldJoin);

                        join.physicalRight = createFieldPKSide(relinfo.farType, hldJoin);
                    }
                } else { //Customer[true].addr
                    //far is Address.cust
                    if (relinfo.isParent) {
                        DStructType structType = relinfo.farType;
                        TypePair pp = DValueHelper.findField(structType, relinfo.otherSide.fieldName);
                        LLD.LLTable llOther = createOrGetLLTable(structType, new LLD.LLTable(structType, structType, new LLD.DefaultLLNameFormatter()));
                        join.physicalLeft = createField(pp, llOther, hldJoin);

                        //near is Customer.id
                        join.physicalRight = createFieldPKSide(relinfo.nearType, hldJoin);
                    } else {
                        //near is Customer.id
                        join.physicalLeft = createFieldPKSide(relinfo.farType, hldJoin);

                        DStructType structType = relinfo.nearType;
                        TypePair pp = DValueHelper.findField(structType, relinfo.fieldName);
                        LLD.LLTable llOther = createOrGetLLTable(structType, new LLD.LLTable(structType, structType, new LLD.DefaultLLNameFormatter()));
                        join.physicalRight = createField(pp, llOther, hldJoin);
                    }
                }
            } else if (relinfo != null && relinfo.isManyToMany()) {
                if (join.logicalJoin.isTransitive) {
                    createTransitiveJoin(join, relinfo, hldJoin, ctx);
                } else {
                    AssocSpec assoc = ctx.datSvc.findAssocInfo(relinfo);
                    DStructType datStructType = createDATType(ctx.registry, assoc); //CustomerAddressDat1
                    joinHelper.addAssocJoin(hldJoin, join, relinfo, hldStatement.fromType, assoc, datStructType);

                    //Customer[55].fetch('addr'). add join from DAT to Address
                    if (hldJoin.joinInfo.isFetch) {
                        extraJoin = new LLD.LLJoin();
                        extraJoin.logicalJoin = hldJoin.joinInfo;
                        joinHelper.addAssocJoin(hldJoin, extraJoin, relinfo, relinfo.farType, assoc, datStructType);
                        joinHelper.flipJoin(extraJoin);
                        addAllFarFields(extraJoin, relinfo);
                    }
                }
            }

            //copy fields
            if (!hldJoin.joinInfo.isTransitive) {
                for (HLD.HLDField hf : hldJoin.fields) {
                    LLD.LLField ff = null;
                    if (hf.relinfo != null && hf.relinfo.isParent && !hf.relinfo.isManyToMany()) {
                        TypePair pair = DValueHelper.findPrimaryKeyFieldPair(relinfo.farType);
                        LLD.LLTable llOther = createOrGetLLTable(relinfo.nearType, new LLD.LLTable(relinfo.nearType, relinfo.farType, new LLD.DefaultLLNameFormatter()));
                        ff = createField(pair, llOther, hldJoin);
                    } else if (hf.relinfo != null && hf.relinfo.isManyToMany()) {
                        //do nothing. we already added a field
                    } else {
//                        ff = createField(hf.pair, lld.table, hldJoin); OLD CODE
                        LLD.LLTable llOther = createOrGetLLTable(hf.hldTable, new LLD.LLTable(hf.hldTable, hf.hldTable, new LLD.DefaultLLNameFormatter()));
                        ff = createField(hf.pair, llOther, hldJoin);
                    }

                    if (ff != null) {
                        join.physicalFields.add(ff);
                    }
                }
            }

            lld.joinL.add(join);
            if (extraJoin != null) {
                lld.joinL.add(extraJoin);
            }
        }
    }

    private void addAllFarFields(LLD.LLJoin join, RelationInfo relinfo) {
        DStructType structType = relinfo.farType;
        for(TypePair pp: structType.getAllFields()) {
            if (pp.type.isStructShape()) {
                RelationInfo relinfox = DRuleHelper.findMatchingRuleInfo(structType, pp);
                if (relinfox != null && relinfox.containsFK()) {
                    LLD.LLTable llOther = createOrGetLLTable(structType, new LLD.LLTable(structType, structType, new LLD.DefaultLLNameFormatter()));
                    LLD.LLField ff = joinHelper.createField(pp, llOther, join.logicalJoin);
                    join.physicalFields.add(ff);
                }
            } else {
                LLD.LLTable llOther = createOrGetLLTable(structType, new LLD.LLTable(structType, structType, new LLD.DefaultLLNameFormatter()));
                LLD.LLField ff = joinHelper.createField(pp, llOther, join.logicalJoin);
                join.physicalFields.add(ff);
            }
        }
    }

    private LLD.LLField createFieldPKSide(DStructType structType, HLD.HLDJoin hldJoin) {
        return joinHelper.createFieldPKSide(structType, hldJoin);
    }

    private void createTransitiveJoin(LLD.LLJoin join, RelationInfo relinfo, HLD.HLDJoin hldJoin, LLDBuilderContext ctx) {
        AssocSpec assoc = ctx.datSvc.findAssocInfo(relinfo);

        DStructType datStructType = createDATType(ctx.registry, assoc); //CustomerAddressDat1
        LLD.LLTable llOther = createOrGetLLTableAssoc(datStructType);

        if (relinfo.nearType.equals(join.logicalJoin.leftType)) {
            boolean flipped = assoc.isFlipped(relinfo); //TODO is this correct?
            if (!flipped) {
                DStructType structType = relinfo.nearType;
                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
                LLD.LLTable llTmp = createOrGetLLTable(structType, new LLD.LLTable(structType, structType, new LLD.DefaultLLNameFormatter()));
                join.physicalLeft = createField(pkpair, llTmp, hldJoin);

                TypePair pp = new TypePair(assoc.leftColumn, datStructType);
                join.physicalRight = createField(pp, llOther, hldJoin);
            } else {
                DStructType structType = relinfo.nearType;
                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
                LLD.LLTable llTmp = createOrGetLLTable(structType, new LLD.LLTable(structType, structType, new LLD.DefaultLLNameFormatter()));
                join.physicalLeft = createField(pkpair, llTmp, hldJoin);

                TypePair pp = new TypePair(assoc.rightColumn, datStructType);
                join.physicalRight = createField(pp, llOther, hldJoin);
            }
        } else { //fromType different from main type
//            boolean flipped = assoc.isFlipped(relinfo); //TODO is this correct?
//            if (!flipped) {
//                DStructType structType = relinfo.farType;
//                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
//                LLD.LLTable llTmp = createOrGetLLTable(structType, new LLD.LLTable(structType, structType, new LLD.DefaultLLNameFormatter()));
//                join.physicalLeft = createField(pkpair, llTmp, hldJoin);
//
//                TypePair pp = new TypePair(assoc.rightColumn, datStructType);
//                join.physicalRight = createField(pp, llOther, hldJoin);
//            } else {
//                DStructType structType = relinfo.farType;
//                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
//                LLD.LLTable llTmp = createOrGetLLTable(structType, new LLD.LLTable(structType, structType, new LLD.DefaultLLNameFormatter()));
//                join.physicalLeft = createField(pkpair, llTmp, hldJoin);
//
//                TypePair pp = new TypePair(assoc.leftColumn, datStructType);
//                join.physicalRight = createField(pp, llOther, hldJoin);
//            }
        }
    }

    private LLD.LLField createField(TypePair pkpair, LLD.LLTable llOther, HLD.HLDJoin hldJoin) {
        return joinHelper.createField(pkpair, llOther, hldJoin);
    }

    public DStructType createDATType(DTypeRegistry registry, AssocSpec assoc) {
        return LLDUtils.createDATType(registry, assoc);
    }


    private void addJoinFields(LLD.LLSelect lld) {
        for (LLD.LLJoin join : lld.joinL) {
            for (LLD.LLEx llex : join.physicalFields) {
                lld.fields.add(llex);
            }
        }
    }

}
