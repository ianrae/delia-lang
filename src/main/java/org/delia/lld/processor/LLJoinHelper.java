package org.delia.lld.processor;

import org.delia.compiler.ast.Exp;
import org.delia.hld.HLD;
import org.delia.hld.dat.AssocSpec;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

import java.util.Map;

public class LLJoinHelper {

    private final LLTableResolver tableResolver;
    private final AliasProcessor aliasProc;
    private Map<String, LLD.LLTable> tableMap;

    public LLJoinHelper(Map<String, LLD.LLTable> tableMap, LLTableResolver tableResolver, AliasProcessor aliasProc) {
        this.tableMap = tableMap;
        this.tableResolver = tableResolver;
        this.aliasProc = aliasProc;
    }

    //add join from leftType to DAT table. The physicalRight table is joined in
    public void addAssocJoin(HLD.HLDJoin hldJoin, LLD.LLJoin join, RelationInfo relinfo, DStructType leftType, AssocSpec assoc, DStructType datStructType) {
        LLD.LLTable llOther = createOrGetLLTableAssoc(datStructType);

        if (relinfo.nearType.equals(leftType)) {
            boolean flipped = assoc.isFlipped(relinfo); //TODO is this correct?
            if (!flipped) {
                join.physicalLeft = createFieldPKSide(relinfo.nearType, hldJoin);

                TypePair pp = new TypePair(assoc.leftColumn, datStructType);
                join.physicalRight = createField(pp, llOther, hldJoin);
            } else {
                join.physicalLeft = createFieldPKSide(relinfo.nearType, hldJoin);

                TypePair pp = new TypePair(assoc.rightColumn, datStructType);
                join.physicalRight = createField(pp, llOther, hldJoin);
            }
        } else { //fromType different from main type
            boolean flipped = assoc.isFlipped(relinfo); //TODO is this correct?
            if (!flipped) {
                join.physicalLeft = createFieldPKSide(relinfo.farType, hldJoin);

                TypePair pp = new TypePair(assoc.rightColumn, datStructType);
                join.physicalRight = createField(pp, llOther, hldJoin);
            } else {
                join.physicalLeft = createFieldPKSide(relinfo.farType, hldJoin);

                TypePair pp = new TypePair(assoc.leftColumn, datStructType);
                join.physicalRight = createField(pp, llOther, hldJoin);
            }
        }
    }

    public void flipJoin(LLD.LLJoin join) {
        LLD.LLField tmp = join.physicalRight;
        join.physicalRight = join.physicalLeft;
        join.physicalLeft = tmp;

        RelationInfo otherinfo = join.logicalJoin.relinfo.otherSide;
        //TODO: why don't we reverse the throughField. It works when we don't, but why?
        Exp.JoinInfo ji = new Exp.JoinInfo(join.logicalJoin.rightTypeName, join.logicalJoin.leftTypeName, join.logicalJoin.relinfo.fieldName); //why otherinfo.fieldName);
        ji.leftType = join.logicalJoin.rightType;
        ji.rightType = join.logicalJoin.leftType;
        ji.isFKOnly = join.logicalJoin.isFKOnly;
        ji.isFetch = join.logicalJoin.isFetch;
        ji.isTransitive = join.logicalJoin.isTransitive;
        ji.relinfo = otherinfo;

        join.logicalJoin = ji;
        aliasProc.processExtraJoin(ji); //needs alias
    }

    public LLD.LLField createField(TypePair pkpair, LLD.LLTable llOther, HLD.HLDJoin hldJoin) {
        LLD.LLField field = new LLD.LLField(pkpair, llOther, new LLD.DefaultLLNameFormatter());
        field.joinInfo = hldJoin.joinInfo;
        return field;
    }
    public LLD.LLField createField(TypePair pkpair, LLD.LLTable llOther, Exp.JoinInfo joinInfo) {
        LLD.LLField field = new LLD.LLField(pkpair, llOther, new LLD.DefaultLLNameFormatter());
        field.joinInfo = joinInfo;
        return field;
    }
    public LLD.LLField createFieldPKSide(DStructType structType, HLD.HLDJoin hldJoin) {
        TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
        LLD.LLTable llOther = tableResolver.createOrGetLLTable(structType, new LLD.LLTable(structType, structType, new LLD.DefaultLLNameFormatter()));
        return createField(pkpair, llOther, hldJoin);
    }

    public LLD.LLTable createOrGetLLTableAssoc(DStructType datType) {
        String key = String.format("%s", datType.getName());
        if (tableMap.containsKey(key)) {
            return tableMap.get(key);
        }
        LLD.LLTable llTable = new LLD.LLTable(datType, datType, new LLD.DefaultLLNameFormatter());
        tableMap.put(key, llTable);
        return llTable;
    }
}
