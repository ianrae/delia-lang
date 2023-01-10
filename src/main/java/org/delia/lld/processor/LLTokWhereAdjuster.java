package org.delia.lld.processor;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.hld.dat.AssocSpec;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.tok.MyTokHintVisitor;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class LLTokWhereAdjuster extends ServiceBase {

    public LLTokWhereAdjuster(FactoryService factorySvc) {
        super(factorySvc);
    }

    public void fillInWhereExpHints(LLD.LLTable table, Tok.WhereTok whereClause) {
        MyTokHintVisitor hintVisitor = new MyTokHintVisitor();
        hintVisitor.top = whereClause.where;
        hintVisitor.pkpair = DValueHelper.findPrimaryKeyFieldPair(table.physicalType);
        hintVisitor.structType = table.physicalType;
        whereClause.visit(hintVisitor, null);
    }

    public Tok.WhereTok adjustWhereClause(Tok.WhereTok whereClause, DStructType structType, LLDBuilderContext ctx) {
        TokFieldHintVisitor visitor = new TokFieldHintVisitor(structType, true);
        whereClause.visit(visitor, null);


        if (!ctx.isMEMDb()) {
            for (Tok.FieldTok fld : visitor.allFields) {
                if (fld.joinInfo != null) {
                    adjustWhereField(fld, ctx);
                }
            }
        }

        return whereClause;
    }


    public void adjustWhereField(Tok.FieldTok fld, LLDBuilderContext ctx) {
        RelationInfo relinfo = fld.joinInfo.relinfo;
        if (relinfo != null && relinfo.isParent && !relinfo.isManyToMany()) {
            //TODO: is this safe. we rewrite the where expression (which is kind of a logical thing)
            TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(relinfo.farType);
            fld.fieldName = pkpair.name;
        } else if (relinfo != null && relinfo.isManyToMany()) {
            AssocSpec assoc = ctx.datSvc.findAssocInfo(relinfo);
            if (fld.fieldName.equals(assoc.deliaLeftv)) {
                fld.fieldName = assoc.rightColumn; //TODO what about type??
                fld.assocPhysicalType = createDATType(ctx.registry, assoc);
            } else {
                fld.fieldName = assoc.leftColumn; //TODO what about type??
                fld.assocPhysicalType = createDATType(ctx.registry, assoc);
            }
        }
    }


    private DStructType createDATType(DTypeRegistry registry, AssocSpec assoc) {
        return LLDUtils.createDATType(registry, assoc);
    }

}
