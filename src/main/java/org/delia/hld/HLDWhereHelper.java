package org.delia.hld;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueConverterService;
import org.delia.tok.Tok;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.ArrayList;
import java.util.List;

public class HLDWhereHelper extends ServiceBase {

    private final DValueConverterService dvalConverterService;

    public HLDWhereHelper(FactoryService factorySvc) {
        super(factorySvc);
        this.dvalConverterService = new DValueConverterService(factorySvc);
    }

    public Tok.OperandTok adjustWhereClauseForPKAndVars(Tok.OperandTok where, DStructType ownerType, HLDBuilderContext ctx) {
        HLDTokVisitor tokVisitor = new HLDTokVisitor(where, ownerType);
        where.visit(tokVisitor, null);
        if (tokVisitor.deferredFieldTok != null) {
            DStructType structType = ownerType;
            PrimaryKey primaryKey = DValueHelper.findPrimaryKeyField(structType);
            if (primaryKey.isMultiple()) {
                return createDeferredComposite(tokVisitor, primaryKey, ctx);
            } else {
                TypePair pkpair = primaryKey.getKey();
                DValue dval = createDeferredDValue(pkpair, tokVisitor.deferredFieldTok.fieldName, ctx);

                Tok.ValueTok vexp = new Tok.ValueTok();
                vexp.value = dval;
                Tok.PKWhereTok pktok = new Tok.PKWhereTok();
                pktok.value = vexp;
                pktok.pkOwnerType = structType;
                pktok.physicalFieldName = pkpair.name;
                return pktok;
            }
        } else {
            return where;
        }
    }

    private Tok.OperandTok createDeferredComposite(HLDTokVisitor tokVisitor, PrimaryKey primaryKey, HLDBuilderContext ctx) {
        int n = primaryKey.getKeys().size();

        List<Tok.ValueTok> list = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            TypePair pkpair = primaryKey.getKeys().get(i);
            Tok.DToken tok = tokVisitor.deferredFieldTok.funcL.get(0).argsL.get(i);

            if (tok instanceof Tok.FieldTok) {
                Tok.FieldTok ftok = (Tok.FieldTok) tok;
                DValue dval = createDeferredDValue(pkpair, ftok.fieldName, ctx);
                Tok.ValueTok valueTok = new Tok.ValueTok();
                valueTok.value = dval;
                list.add(valueTok);
            } else {
                //ValueTok
                ScalarValueBuilder valueBuilder = factorySvc.createScalarValueBuilder(ctx.registry);
                DType typeToUse = dvalConverterService.getType(null, pkpair.type.getShape(), ctx.registry);
                DValue dval = dvalConverterService.buildFromObject(tok.strValue(), pkpair.type.getShape(), valueBuilder,typeToUse);
                Tok.ValueTok valueTok = new Tok.ValueTok();
                valueTok.value = dval;
                list.add(valueTok);
            }
        }
        Tok.ListTok vexp = new Tok.ListTok();
        vexp.listL.addAll(list);
        Tok.PKWhereTok pktok = new Tok.PKWhereTok();
        pktok.listValue = vexp;
        pktok.pkOwnerType = null;
        pktok.physicalFieldName = null;
        pktok .primaryKey = primaryKey;
        return pktok;
    }

    protected DValue createDeferredDValue(TypePair pair, String fieldName, HLDBuilderContext ctx) {
        Shape shape = pair.type.getShape();
        DValue dval = dvalConverterService.buildDefaultValue(shape, ctx.valueBuilder);
        DeferredDValue deferredDValue = new DeferredDValue(dval.getType(), dval.getObject(), fieldName);
        return deferredDValue;
    }

}
