package org.delia.hld;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueConverterService;
import org.delia.tok.Tok;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
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
                return createDeferredComposite(tokVisitor, primaryKey, where, ctx);
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

    private Tok.OperandTok createDeferredComposite(HLDTokVisitor tokVisitor, PrimaryKey primaryKey, Tok.OperandTok where, HLDBuilderContext ctx) {
        int n = primaryKey.getKeys().size();

        Tok.CompositeKeyTok compositeKeyTok = null;
        if (where instanceof Tok.PKWhereTok) {
            Tok.PKWhereTok pktok = (Tok.PKWhereTok) where;
            compositeKeyTok = pktok.compositeKeyTok;
        }
        if (compositeKeyTok == null) {
            DeliaExceptionHelper.throwError("bad-composite-key", "Failed composite key!");
            return null;
        }

        List<Tok.ValueTok> list = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            TypePair pkpair = primaryKey.getKeys().get(i);
            Tok.DToken tok = compositeKeyTok.listL.get(i);

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
        Tok.CompositeKeyTok vexp = new Tok.CompositeKeyTok();
        vexp.listL.addAll(list);
        Tok.PKWhereTok pktok = new Tok.PKWhereTok();
        pktok.compositeKeyTok = vexp;
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
