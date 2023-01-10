package org.delia.sql;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.hld.dat.AssocSpec;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.RelationValueBuilder;

public class AssocPoolMerger extends ServiceBase {
    private final DTypeRegistry registry;
    private final DatService datSvc;

    public AssocPoolMerger(FactoryService factorySvc, DTypeRegistry registry, DatService datSvc) {
        super(factorySvc);
        this.registry = registry;
        this.datSvc = datSvc;
    }

    public void xmergeAssocItems(TypePair pair, Exp.JoinInfo logicalJoin, DValue dval, HLDResultSetConverter.AssocColInfo aci, LLD.LLSelect hld, DBAccessContext dbctx) {
//        Map<String, DValue> alreadyMap = new HashMap<>();
        //TODO: check by type not just pair.name (two types might have field with same name)
        if (logicalJoin.throughField.equals(pair.name)) {
            if (logicalJoin.rightType != pair.type) {
                return; //continue; //extra check to make sure its the same relation
            }

            LLD.LLField fld = aci.columnRun.runList.get(0);
//            if (fld.joinInfo != logicalJoin) {
//                return;
//            }
            DValue fkval = aci.dval.asStruct().getField(fld.getFieldName());
            if (fkval == null) { //MM relations are optional
                return; //continue;
            }
//            if (!samePK(aci.parentVal, dval)) {
//                return; //continue;
//            }
//                    dumpACI(aci, fkval);
            AssocSpec assocSpec = datSvc.findAssocInfo(logicalJoin.relinfo);

//            isPKValue(dval, "3");

            //TODO: need better compare than asString!
            String fkvalStr = fkval.asString();
//            if (alreadyMap.containsKey(fkvalStr)) {
//                return; //continue;
//            }
//            alreadyMap.put(fkvalStr, fkval);

            String firstRelFieldName = assocSpec.getReverseRelationFieldForAssocField(fld.getFieldName());
            String relFieldName = null;
            if (firstRelFieldName.equals(pair.name)) {
                relFieldName = assocSpec.getReverseRelationFieldForAssocField(fld.getFieldName());
            } else {
                relFieldName = assocSpec.otherSideFieldName;
            }

            DStructType relStructType = assocSpec.getReverseTypeForField(fld.getFieldName());
            if (relStructType.getTypeName().equals(dval.getType().getTypeName())) {
                if (relFieldName.equals(pair.name)) {
                    TypePair relPair = new TypePair(relFieldName, relStructType);
                    DValue inner = dval.asStruct().getField(pair.name);
                    if (inner == null) {
                        inner = this.createEmptyRelation(dbctx, relStructType, relPair.name);
                    }
                    DRelation drel = inner.asRelation();
                    drel.addKey(fkval);
                    dval.asMap().put(relPair.name, inner);
                }
            }
        }
    }

    protected DValue createEmptyRelation(DBAccessContext dbctx, DStructType structType, String mergeOnField) {
        DType relType = dbctx.registry.getType(BuiltInTypes.RELATION_SHAPE);
        TypePair pair = DValueHelper.findField(structType, mergeOnField);
        RelationValueBuilder builder = new RelationValueBuilder(relType, pair.type, dbctx.registry);
        builder.buildEmptyRelation();
        boolean b = builder.finish();
        if (!b) {
            DeliaExceptionHelper.throwError("relation-create-failed-assocCrud", "Type '%s': Failed to create empty relation", pair.type);
        }
        return builder.getDValue();
    }
}
