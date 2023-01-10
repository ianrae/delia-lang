package org.delia.hld;

import org.delia.DeliaOptions;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.log.DeliaLog;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeName;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;

import java.util.List;

public class TypeHLDStatementBuilder implements HLDStatementBuilder {

    private final DeliaOptions options;
    private final DeliaLog log;

    public TypeHLDStatementBuilder(DeliaOptions options, DeliaLog log) {
        this.options = options;
        this.log = log;
    }

    @Override
    public void build(AST.StatementAst statementParam, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        AST.TypeAst statement = (AST.TypeAst) statementParam;
        HLD.TypeHLDStatement hld = new HLD.TypeHLDStatement(statementParam.getLoc());
        DTypeName dtypeName = new DTypeName(statement.schemaName, statement.typeName);
        DType dtype = ctx.registry.getType(dtypeName); //registry populated in first pass
        if (!dtype.isStructShape()) {
            return; //a scalar type. do nothing
        }
        DStructType structType = (DStructType) dtype;
        hld.hldTable = structType;
        for (TypePair pair : structType.getAllFields()) {
            String fieldName = pair.name;
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
            HLD.HLDField field = new HLD.HLDField(hld.hldTable, pair, relinfo);
            hld.fields.add(field);
        }
        hldStatements.add(hld);
    }

    @Override
    public void assignDATs(HLD.HLDStatement statementParam, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        HLD.TypeHLDStatement hld = (HLD.TypeHLDStatement) statementParam;
        for (TypePair pair : hld.hldTable.getAllFields()) {
            if (pair.type.isScalarShape()) {
                continue;
            }
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(hld.hldTable, pair);
            if (relinfo != null && relinfo.isManyToMany()) {
                relinfo = findUsingAssocHints(relinfo, ctx);
                Exp.JoinInfo joinInfo = new Exp.JoinInfo(relinfo.nearType.getTypeName(), relinfo.farType.getTypeName(), relinfo.fieldName);
                joinInfo.leftType = relinfo.nearType;
                joinInfo.rightType = relinfo.farType;
                joinInfo.relinfo = relinfo; //DRuleHelper.findMatchingRuleInfo(joinInfo.leftType, joinInfo.throughField);
                SyntheticDatService synthDatSvc = (SyntheticDatService) ctx.datSvc; //TODO: why do we need to do this?
                int datId = synthDatSvc.buildAssoc(joinInfo, pair.name);
                relinfo.forceDatId(datId);
            }
        }
    }

    private RelationInfo findUsingAssocHints(RelationInfo relinfo, HLDBuilderContext ctx) {
        if (relinfo.otherSide == null) return relinfo; //single-sided relations can't use assoc hints

        for(String key: options.assocHints) {
            boolean polarity = true;

            String s1 = relinfo.nearType.getTypeName().getTypeName();
            String s2 = relinfo.farType.getTypeName().getTypeName();
            String possibleKey = String.format("%s:%s:%s", s1, relinfo.fieldName, s2);
            if (key.equals(possibleKey)) {
                return polarity ? relinfo : relinfo.otherSide;
            }

            possibleKey = String.format("%s:%s:%s", s2, relinfo.otherSide.fieldName, s1);
            if (key.equals(possibleKey)) {
                log.log("using hint: %s", key);
                return polarity ? relinfo.otherSide : relinfo;
            }
        }
        return relinfo;
    }
}