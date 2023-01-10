package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.impl.CompileScalarValueBuilder;
import org.delia.core.FactoryService;
import org.delia.dval.DValueConverterService;
import org.delia.relation.RelationInfo;
import org.delia.tok.Tok;
import org.delia.tok.TokFieldVisitor;
import org.delia.tok.TokFuncVisitor;
import org.delia.type.*;
import org.delia.util.DRuleHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LetHLDStatementBuilder extends StatementBuilderBase {

    private final LetFinalFieldBuilder finalFieldBuilder;

    public LetHLDStatementBuilder(FactoryService factorySvc) {
        super(factorySvc);
        this.finalFieldBuilder = new LetFinalFieldBuilder(factorySvc);
    }

    @Override
    public void build(AST.StatementAst statementParam, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        AST.LetStatementAst statement = (AST.LetStatementAst) statementParam;
        if (statement.scalarElem != null) {
            buildScalarLet(hldStatements, statement, ctx);
            return;
        }
        HLDJoinHelper joinHelper = new HLDJoinHelper(factorySvc);
        DTypeName dtypeName = new DTypeName(statement.schemaName, statement.typeName);
        DType dtype = ctx.registry.getType(dtypeName);
        HLD.LetHLDStatement hld = new HLD.LetHLDStatement((DStructType) dtype, statement.getLoc()); //TODO check is structType and log error
        hld.varName = statement.varName;
//        hld.whereClause = statement.whereClause;
        hld.whereTok = tokBuilder.buildWhere(statement.whereClause);
        adjustWhereClauseForVars(hld, ctx);

        //TODO: later we need to honour the order of things in fieldAndFuncs!!
        boolean hasFKS = false;
        List<String> fetches = new ArrayList<>();
        boolean hasCount = false;
        if (statement.fieldAndFuncs != null) {
            hld.fieldAndFuncs = tokBuilder.buildFieldsAndFuncs(statement.fieldAndFuncs);
            TokFuncVisitor funcVisitor = new TokFuncVisitor();
            hld.fieldAndFuncs.visit(funcVisitor, null);
            for (Tok.FunctionTok exp : funcVisitor.allFuncs) {
                Tok.FunctionTok fexp = (Tok.FunctionTok) exp;
                if (fexp.fnName.equals("fks")) {
                    hasFKS = true;
                } else if (fexp.fnName.equals("fetch")) {
                    fetches.add(fexp.argsL.get(0).strValue());
                } else if (fexp.fnName.equals("count")) {
                    hasCount = true;
                }
            }
        }

        List<AddedFieldInfo> infos = buildFinalFields(statement, hld, ctx);
        AddedFieldInfo info = infos.isEmpty() ? null : infos.get(infos.size() - 1);
        hld.fromType = infos.isEmpty() ? hld.hldTable : info.getFromType();
        if (hld.finalField != null) {
            if (hld.finalField.relinfo == null) {
                addSingleField(hld, hld.finalField);
                hld.resultType = hld.finalField.pair.type;
                if (hld.fromType != hld.hldTable) {
                    AddedFieldInfo infoWithJoin = null;
                    for (AddedFieldInfo x : infos) {
                        if (x.joinField != null) {
                            infoWithJoin = x;
                        }
                    }
                    addSingleJoin(hld, hld.fromType, hld.hldTable, infoWithJoin, joinHelper);
                }
            } else {
                hld.resultType = hld.fromType;
                addAllFieldsOfType(hld, hld.finalField.relinfo.farType);
                joinHelper.addSingleJoin(hld, hld.finalField.relinfo);
            }
        } else if (!hasCount) {
            addAllFieldsOfType(hld, hld.hldTable); //later in cases we won't add all fields (eg. [true].addr)
            joinHelper.addJoins(hld);
        } else if (hasCount) {
            HLD.HLDFuncEx funcEx = new HLD.HLDFuncEx("count");
            hld.fields.add(funcEx);
            hld.resultType = ctx.registry.getType(BuiltInTypes.INTEGER_SHAPE);
        }

        //TODO. we need better granularity. some parts may have fks or fetch. fix later
        if (hasFKS || !fetches.isEmpty()) {
            joinHelper.addFKJoins(hld, hasFKS, fetches, false);
        } else {
            joinHelper.addFKJoins(hld, false, Collections.emptyList(), true);
        }

        if (!hasCount) {
            joinHelper.addWhereJoins(hld);
        }

        hldStatements.add(hld);
    }

    private void adjustWhereClauseForVars(HLD.LetHLDStatement hld, HLDBuilderContext ctx) {
        hld.whereTok.where = whereHelper.adjustWhereClauseForPKAndVars(hld.whereTok.where, hld.hldTable, ctx);
    }

    private void buildScalarLet(List<HLD.HLDStatement> hldStatements, AST.LetStatementAst statement, HLDBuilderContext ctx) {
        HLD.LetAssignHLDStatement stmt = new HLD.LetAssignHLDStatement(statement.getLoc());
        stmt.varName = statement.varName;

        if (statement.scalarElem instanceof Exp.FieldExp) {
            Exp.FieldExp fexp = (Exp.FieldExp) statement.scalarElem;
            stmt.rhsExpr = fexp.fieldName;
            hldStatements.add(stmt);
            return;
        }

        //allow let x = 10. infer scalar type from the value
        if (statement.typeName == null && statement.scalarElem instanceof Exp.ValueExp) {
            Exp.ValueExp vexp = (Exp.ValueExp) statement.scalarElem;
            if (vexp.value != null) {
                statement.typeName = vexp.value.getType().getName();
            }
        }

        BuiltInTypes bit = null;
        if (statement.typeName != null) {
            bit = BuiltInTypes.fromDeliaTypeName(statement.typeName); //let x int = 5
            if (bit == null) {
                bit = BuiltInTypes.getAsBuiltInScalarTypeName(statement.typeName); //let x = 5
            }
        }

        if (bit == null) {
            DTypeName dtypeName = new DTypeName(statement.schemaName, statement.typeName);
            stmt.dtype = ctx.registry.getType(dtypeName);
            //TODO: handle if null
        } else {
            stmt.dtype = ctx.registry.getType(bit);
        }
        DValue alreadyParsedValue = null;
        if (statement.scalarElem instanceof Exp.ValueExp) {
            Exp.ValueExp vexp = (Exp.ValueExp) statement.scalarElem;
            alreadyParsedValue = vexp.value;
        }
        stmt.dvalue = createScalarValue(stmt.dtype, statement.scalarElem.strValue(), ctx.registry, alreadyParsedValue, ctx.valueBuilder, ctx);
        hldStatements.add(stmt);
    }

    private DValue createScalarValue(DType dtype, String valueStr, DTypeRegistry registry, DValue alreadyParsedValue, ScalarValueBuilder valueBuilder, HLDBuilderContext ctx) {
        //TODO: do we treat delia null as java null? or as a DValue whose object is null?
        if ("null".equals(valueStr)) {
            return null;
        }

        if (alreadyParsedValue != null && isNumeric(alreadyParsedValue)) {
            if (!isNumericCompatible(dtype)) {
                String typeName = BuiltInTypes.convertDTypeNameToDeliaName(dtype.getName());
                String msg = String.format("Expecting %s value but got %s", typeName, alreadyParsedValue.asString());
                ctx.localET.add("incompatible-assignment", msg);
                return alreadyParsedValue;
            }
            DValue dvalx = normalizeValue(alreadyParsedValue, dtype, valueBuilder);
            return dvalx; //new DValueImpl(dtypeNum, alreadyParsedValue.getObject());
        }

        DValueConverterService converterSvc = new DValueConverterService(factorySvc);
        ScalarValueBuilder svBuilder = new CompileScalarValueBuilder(factorySvc, registry);
        Shape shape = dtype.getShape(); //TODO how do we build custom types with type Grade??
        DType typeToUse = converterSvc.getType(dtype, shape, registry);
        DValue dval = converterSvc.buildFromObject(valueStr, shape, svBuilder, typeToUse);
        return dval;
    }

    private boolean isNumeric(DValue alreadyParsedValue) {
        switch (alreadyParsedValue.getType().getShape()) {
//            case LONG:
            case INTEGER:
            case NUMBER:
                return true;
            default:
                return false;
        }
    }
    private boolean isNumericCompatible(DType dtype) {
        switch (dtype.getShape()) {
//            case LONG:
            case INTEGER:
            case NUMBER: //TODO do we still support dates as longs
                return true;
            default:
                return false;
        }
    }

    //TODO i think final field can cause joins. implement this!
    //the whole chain of final funcs cannot cause joins i think
    private List<AddedFieldInfo> buildFinalFields(AST.LetStatementAst statement, HLD.LetHLDStatement hld, HLDBuilderContext ctx) {
        return finalFieldBuilder.buildFinalFields(statement, hld, ctx);
    }


    private HLD.HLDJoin addSingleJoin(HLD.LetHLDStatement hld, DStructType nearType, DStructType farType, AddedFieldInfo afInfo, HLDJoinHelper joinHelper) {
        String fieldName = afInfo.joinField;
        RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(farType, fieldName);

        HLD.HLDJoin join = joinHelper.addSingleJoin(hld, relinfo);
        this.finalFieldBuilder.addJoinInfo(hld, join);
        return hld.finalField.finalJoin;
    }

    private void addAllFieldsOfType(HLD.LetHLDStatement hld, DStructType structType) {
        //includes serial id.
        //does include parent side of 11 or N1 relations (eg. Customer.addr)
        for (TypePair pair : structType.getAllFields()) {
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
            HLD.HLDField field = new HLD.HLDField(hld.hldTable, pair, relinfo);
            hld.fields.add(field);
        }
    }

    private void addSingleField(HLD.LetHLDStatement hld, HLD.HLDField hldField) {
        HLD.HLDField field = new HLD.HLDField(hld.hldTable, hldField.pair, hldField.relinfo);
        hld.fields.add(field);
        return;
    }

    @Override
    public void assignDATs(HLD.HLDStatement statement, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        HLD.LetHLDStatement hld = (HLD.LetHLDStatement) statement;

        //fields
        for (HLD.HLDField field : makeFieldList(hld)) {
            fieldAssignDatIfNeeded(field, ctx);
        }

        //whereClause
        TokFieldVisitor visitor = new TokFieldVisitor();
        visitor.onlyJoinFields = true;
        hld.whereTok.visit(visitor, null);
        for (Tok.FieldTok field : visitor.allFields) {
            Exp.JoinInfo jinfo = field.joinInfo;
            if (jinfo.relinfo != null && jinfo.relinfo.getDatId() == null) {
                jinfo.relinfo.forceDatId(ctx.datSvc.findDat(jinfo.relinfo));
            }
        }

        //joinL
        for (HLD.HLDJoin join : hld.joinL) {
            Exp.JoinInfo jinfo = join.joinInfo;
            if (jinfo.relinfo != null && jinfo.relinfo.getDatId() == null) {
                jinfo.relinfo.forceDatId(ctx.datSvc.findDat(jinfo.relinfo));
            }
            for (HLD.HLDField field : join.fields) {
                fieldAssignDatIfNeeded(field, ctx);
            }
        }

        //finalField
        if (hld.finalField != null) {
            fieldAssignDatIfNeeded(hld.finalField, ctx);
        }

    }

    private void fieldAssignDatIfNeeded(HLD.HLDField field, HLDBuilderContext ctx) {
        if (field.relinfo != null && field.relinfo.getDatId() == null) {
            field.relinfo.forceDatId(ctx.datSvc.findDat(field.relinfo));
        }
    }

    protected List<HLD.HLDField> makeFieldList(HLD.LetHLDStatement hld) {
        List<HLD.HLDField> list = hld.fields.stream().filter(x -> x instanceof HLD.HLDField).map(x -> (HLD.HLDField) x).collect(Collectors.toList());
        return list;
    }

}
