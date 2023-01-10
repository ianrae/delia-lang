package org.delia.lld.processor;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.hld.MyFieldVisitor;
import org.delia.hld.dat.AssocSpec;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class LLWhereAdjuster extends ServiceBase {

    public LLWhereAdjuster(FactoryService factorySvc) {
        super(factorySvc);
    }

    public void fillInWhereExpHints(LLD.LLTable table, Exp.WhereClause whereClause) {
        MyHintVisitor hintVisitor = new MyHintVisitor();
        hintVisitor.top = whereClause.where;
        hintVisitor.pkpair = DValueHelper.findPrimaryKeyFieldPair(table.physicalType);
        hintVisitor.structType = table.physicalType;
        whereClause.visit(hintVisitor);
    }

    public Exp.WhereClause adjustWhereClause(Exp.WhereClause whereClause, LLDBuilderContext ctx) {
        MyFieldVisitor visitor = new MyFieldVisitor();
        whereClause.where.visit(visitor);

        if (!ctx.isMEMDb()) {
            for (Exp.FieldExp fld : visitor.allFields) {
                if (fld.joinInfo != null) {
                    adjustWhereField(fld, ctx);
                }
            }
        }

        return whereClause;
    }

    public DStructType adjustWhereClauseForTrue(Exp.WhereClause whereClause, DStructType hldTable, LLDBuilderContext ctx) {
        MyFieldVisitor visitor = new MyFieldVisitor();
        whereClause.visit(visitor);

        if (whereClause != null && whereClause.where instanceof Exp.DottedExp) {
            Exp.DottedExp dexp = (Exp.DottedExp) whereClause.where;
            //handle the Flight[z] case.
            //TODO need to resolve all other vars in where expr!
            if (dexp.chainL.size() == 1 && visitor.allFields.size() == 0) {
                Exp.ValueExp vexp = (Exp.ValueExp) dexp.chainL.get(0);
                //either 'true' or a pk val
//                return selectLL.whereAllOrPKType = statement.hldTable; //if statements is Customer[true] then is Customer, even if we do .addr
                return hldTable; //if statements is Customer[true] then is Customer, even if we do .addr
            }
        }
        return null;
    }


    public void adjustWhereField(Exp.FieldExp fld, LLDBuilderContext ctx) {
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
