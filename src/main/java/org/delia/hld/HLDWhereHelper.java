package org.delia.hld;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueConverterService;
import org.delia.tok.Tok;
import org.delia.type.*;
import org.delia.util.DValueHelper;

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
            TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
            DValue dval = createDeferredDValue(pkpair, tokVisitor.deferredFieldTok, ctx);

            Tok.ValueTok vexp = new Tok.ValueTok();
            vexp.value = dval;
            Tok.PKWhereTok pktok = new Tok.PKWhereTok();
            pktok.value = vexp;
            pktok.pkOwnerType = structType;
            pktok.physicalFieldName = pkpair.name;
            return pktok;
        } else {
            return where;
        }
    }

    protected DValue createDeferredDValue(TypePair pair, Tok.FieldTok varExp, HLDBuilderContext ctx) {
        Shape shape = pair.type.getShape();
        DValue dval = dvalConverterService.buildDefaultValue(shape, ctx.valueBuilder);
        DeferredDValue deferredDValue = new DeferredDValue(dval.getType(), dval.getObject(), varExp.fieldName);
        return deferredDValue;
    }

}
