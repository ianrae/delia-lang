package org.delia.lld.processor;

import org.delia.compiler.ast.Exp;
import org.delia.hld.HLD;
import org.delia.hld.dat.AssocSpec;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.tok.Tok;
import org.delia.tok.TokFieldVisitor;
import org.delia.tok.TokVisitorUtils;
import org.delia.type.DStructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliasProcessor implements AliasAssigner {
    private Map<String, String> aliasMap = new HashMap<>();
    private Character nextCh = 'a';
    private DatService datSvc;
    private List<Exp.JoinInfo> childJoins = new ArrayList<>();

    public AliasProcessor(DatService datSvc) {
        this.datSvc = datSvc;
    }

    public void assignAliases(LLD.LLStatement statement, AliasAssigner assigner) {
        if (statement instanceof LLD.LLCreateTable) {
        } else if (statement instanceof LLD.LLCreateSchema) {

        } else if (statement instanceof LLD.LLSelect) {
            doSelectStatement((LLD.LLSelect) statement, assigner);
        } else if (statement instanceof LLD.LLInsert) {

        }
    }

    private void doSelectStatement(LLD.LLSelect statement, AliasAssigner assigner) {
        LLD.LLSelect llSelect = (LLD.LLSelect) statement;

        llSelect.table.alias = assigner.findAlias(llSelect.table.physicalType);

        for (LLD.LLEx llex : llSelect.fields) {
            if (llex instanceof LLD.LLField) {
                LLD.LLField field = (LLD.LLField) llex;
                if (field.joinInfo != null && field.joinInfo.alias != null) {
                    field.physicalTable.alias = field.joinInfo.alias;
                } else {
                    assignAliasToField(field);
                }
            }
        }
        for (LLD.LLJoin join : llSelect.joinL) {
            String otherAlias = findAlias(join.logicalJoin.leftType, join.logicalJoin.throughField);
            String logicalRelAlias = otherAlias;
            boolean isReversed = false;

            if (join.isSelfJoin()) {
                logicalRelAlias = llSelect.table.alias; //main alias. TODO: is self-join always of the main type?
            }
            //Customer[true].addr is other way around. logicalJoin is C.A but table.logicalType is A
            if (!join.logicalJoin.relinfo.nearType.equals(statement.table.logicalType)) {
                otherAlias = llSelect.table.alias;
                isReversed = true;
//                if (logicalRelAlias == null && join.logicalJoin.relinfo.isManyToMany()) { OLD CODE
                if (join.logicalJoin.relinfo.isManyToMany()) {
                    AssocSpec assoc = datSvc.findAssocInfo(join.logicalJoin.relinfo);
                    logicalRelAlias = this.findOrCreateAssocAlias(assoc.assocTblName);
                }
            }

            LLD.LLField ff = join.physicalLeft;
            if (sameType(ff.physicalTable.physicalType, join.logicalJoin.rightType)) {
                ff.physicalTable.alias = isReversed ? otherAlias : logicalRelAlias;
            } else {
                ff.physicalTable.alias = isReversed ? logicalRelAlias : assignAliasToField(ff);
            }

            ff = join.physicalRight;
            if (sameType(ff.physicalTable.physicalType, join.logicalJoin.rightType)) {
                ff.physicalTable.alias = isReversed ? otherAlias : logicalRelAlias;
            } else {
                ff.physicalTable.alias = isReversed ? logicalRelAlias : assignAliasToField(ff);
            }

            //we use the fact that join.fieldL are also in llSelect.fieldL. so set alias here sets in main fieldL
            for (LLD.LLField fff : join.physicalFields) {
                fff.physicalTable.alias = otherAlias;
            }
        }

        //visitor for whereClause to assigner aliases. the joinL should do all alias creation
        TokFieldVisitor visitor = new TokFieldVisitor();
//        MyFieldVisitor visitor = new MyFieldVisitor();
        statement.whereTok.visit(visitor, null);
        for (Tok.FieldTok fld : visitor.allFields) {
            if (fld.joinInfo != null) {
                if (fld.assocPhysicalType != null) {
                    fld.alias = findOrCreateAssocAlias(fld.assocPhysicalType);
                } else {
                    fld.alias = findAlias(fld.joinInfo.leftType, fld.joinInfo.throughField);
                }
            } else if (fld.ownerType != null) {
                fld.alias = findAlias(fld.ownerType, fld.fieldName);
                if (fld.alias == null) {
                    AssocSpec assoc = datSvc.findAssocInfo(fld.ownerFoundInJoinInfo.relinfo);
                    if (assoc == null) { //not M:M
                        //TODO: do we sometimes have to use relinfo.otherSide.fieldName?
                        fld.alias = findAlias(fld.ownerType, fld.ownerFoundInJoinInfo.relinfo.fieldName);
                    } else {
                        String key = String.format("%s.%s", assoc.assocTblName, assoc.leftColumn); //TODO possibly its rightColumn sometimes
                        String alias = aliasMap.get(key);
                        fld.alias = alias;
                    }
                }
            } else {
                fld.alias = llSelect.table.alias;
            }
        }
        doAdditionalWhereAlias(llSelect);

        addAliasForFuncs(llSelect);
    }

    private void doAdditionalWhereAlias(LLD.LLSelect llSelect) {
        Tok.PKWhereTok pktok = TokVisitorUtils.getIfWherePK(llSelect.whereTok.where);
        if (pktok != null) {
            for(LLD.LLJoin join: llSelect.joinL) {
                if (join.logicalJoin.leftType == pktok.pkOwnerType) {
                    if (llSelect.table.physicalType == pktok.pkOwnerType) {
                        //we just use the main from type's alias
                        pktok.alias = llSelect.table.alias;
                    } else {
                        pktok.alias = join.logicalJoin.alias;
                        if (join.logicalJoin.relinfo.isManyToMany()) {
                            AssocSpec assoc = datSvc.findAssocInfo(join.logicalJoin.relinfo);
                            if (pktok.pkOwnerType == assoc.leftType) {
                                pktok.physicalFieldName = assoc.leftColumn;
                            } else {
                                pktok.physicalFieldName = assoc.rightColumn;
                            }
                        }
                    }
                }
            }
        }
//        PkWhereVisitor visitor = new PkWhereVisitor();
//        visitor.top = llSelect.whereTok.where;
//        visitor.stmt = llSelect;
//        llSelect.whereTok.visit(visitor, null);
    }

    private String assignAliasToField(LLD.LLField field) {
        for (Exp.JoinInfo ji : childJoins) {
            DStructType structType = ji.leftType;
            String fieldName = ji.throughField;
            if (structType == field.physicalTable.physicalType) {
                if (field.getFieldName().equals(fieldName)) {
                    field.physicalTable.alias = assignOrCreate(ji.leftType, null);
                    return field.physicalTable.alias;
                }
            }
        }
        field.physicalTable.alias = assignOrCreate(field);
        return field.physicalTable.alias;
    }

    private void addAliasForFuncs(LLD.LLSelect llSelect) {
        for (LLD.LLEx ex : llSelect.finalFieldsL) {
            if (ex instanceof LLD.LLDFuncEx) {
                LLD.LLDFuncEx func = (LLD.LLDFuncEx) ex;
                for (LLD.LLEx arg : func.argsL) {
                    if (arg instanceof LLD.LLFinalFieldEx) {
                        LLD.LLFinalFieldEx field = (LLD.LLFinalFieldEx) arg;
                        //hmm. is this always correct? we're trying to avoid using CustomerAddressDat1 alias on a Customer.id field
                        if (field.physicalTable == llSelect.table) {
                            continue;
                        }

                        for (LLD.LLJoin join : llSelect.joinL) {
                            if (join.logicalJoin.isMatch(field.finalJoin.joinInfo)) {
                                field.physicalTable.alias = join.logicalJoin.alias;
                            }
                        }
                    }
                }
            }
        }

    }

    private boolean sameType(DStructType physicalType, DStructType rightType) {
        return physicalType.getName().equals(rightType.getName());
    }

    private String assignOrCreate(LLD.LLField field) {
        String alias = assignOrCreate(field.physicalTable.physicalType, field.physicalPair.name);
        return alias;
    }

    private String assignOrCreate(DStructType physicalType, String fieldName) {
        String key = makeKey(physicalType, fieldName);
        if (aliasMap.containsKey(key)) {
            return aliasMap.get(key);
        } else if (aliasMap.containsKey(physicalType.getName())) {
            return aliasMap.get(physicalType.getName());
        } else {
            String alias = String.valueOf(nextCh++);
            aliasMap.put(key, alias);
            return alias;
        }
    }

    @Override
    public String findAlias(DStructType structType) {
        String alias = aliasMap.get(structType.getName());
        return alias;
    }

    @Override
    public void dumpAliases() {
        if (aliasMap.isEmpty()) return;
    }

    public String findOrCreateAssocAlias(DStructType structType) {
        String alias = aliasMap.get(structType.getName());
        if (alias == null) {
            alias = String.valueOf(nextCh++);
            aliasMap.put(structType.getName(), alias);
        }
        return alias;
    }

    public String findOrCreateAssocAlias(String assocTypeName) {
        String alias = aliasMap.get(assocTypeName);
        if (alias == null) {
            alias = String.valueOf(nextCh++);
            aliasMap.put(assocTypeName, alias);
        }
        return alias;
    }

    public String findAlias(DStructType structType, String fieldName) {
        String key = makeKey(structType, fieldName);
        String alias = aliasMap.get(key);
        if (alias == null) {
            return findAlias(structType); //TODO: not sure if this is always correct
        } else {
            return alias;
        }
    }

    public void initialAliasAssign(HLD.LetHLDStatement statement) {
        nextCh = 'a';
        String mainKey = String.format("%s", statement.fromType);
        aliasMap.put(mainKey, String.valueOf(nextCh++));

        for (HLD.HLDJoin join : statement.joinL) {
            processJoinInfo(join.joinInfo);
        }
    }

    public void processJoinInfo(Exp.JoinInfo joinInfo) {
        if (joinInfo.relinfo != null && joinInfo.relinfo.isManyToMany()) {
            AssocSpec assoc = datSvc.findAssocInfo(joinInfo.relinfo);
            String key = assoc.assocTblName;
            String alias = null;
            if (!aliasMap.containsKey(key)) {
                alias = String.valueOf(nextCh);
                aliasMap.put(key, alias);
                nextCh++;
            } else {
                alias = aliasMap.get(key);
            }
            joinInfo.alias = alias;

        } else {
            String key = makeKey(joinInfo.leftType, joinInfo.throughField);
            String alias = null;
            if (!aliasMap.containsKey(key)) {
                alias = String.valueOf(nextCh);
                aliasMap.put(key, alias);
                nextCh++;
            } else {
                alias = aliasMap.get(key);
            }
            joinInfo.alias = alias;

            //normal case. Customer.addr joining from parent (Customer) to Address to get fk
            //reverse case: Address.cust joining from child (Address) to Customer (as part of fetch, since we already have the fk)
            if (!joinInfo.relinfo.isParent && !joinInfo.relinfo.isManyToMany()) {
                childJoins.add(joinInfo);
            }
        }
    }

    public void processExtraJoin(Exp.JoinInfo joinInfo) {
        String key = makeKey(joinInfo.leftType, joinInfo.throughField);
        String alias = null;
        if (!aliasMap.containsKey(key)) {
            alias = String.valueOf(nextCh);
            aliasMap.put(key, alias);
            nextCh++;
        } else {
            alias = aliasMap.get(key);
        }
        joinInfo.alias = alias;

        //normal case. Customer.addr joining from parent (Customer) to Address to get fk
        //reverse case: Address.cust joining from child (Address) to Customer (as part of fetch, since we already have the fk)
        if (!joinInfo.relinfo.isParent && !joinInfo.relinfo.isManyToMany()) {
            childJoins.add(joinInfo);
        }
    }

    //transitive joins are not created until LLD layer runs
    public void secondaryAliasAssign(HLD.LetHLDStatement statement) {

        for (HLD.HLDJoin join : statement.joinL) {
            if (join.joinInfo.isTransitive) {
                AssocSpec assoc = datSvc.findAssocInfo(join.joinInfo.relinfo);
                String key = String.format("%s.%s", assoc.assocTblName, assoc.leftColumn); //TODO possibly its rightColumn sometimes
                String alias = null;
                if (!aliasMap.containsKey(key)) {
                    alias = String.valueOf(nextCh);
                    aliasMap.put(key, alias);
                    nextCh++;
                } else {
                    alias = aliasMap.get(key);
                }
                join.joinInfo.alias = alias;
            }
        }
    }

    private String makeKey(DStructType leftType, String throughField) {
        String key = String.format("%s.%s", leftType.getName(), throughField);
        return key;
    }
}
