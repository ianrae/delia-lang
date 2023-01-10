package org.delia.hld;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.relation.RelationInfo;
import org.delia.tok.Tok;
import org.delia.tok.TokFieldVisitor;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HLDJoinHelper extends ServiceBase {
    private Map<String, RelationInfo> relMap = new HashMap<>();


    public HLDJoinHelper(FactoryService factorySvc) {
        super(factorySvc);
    }


    public void addJoins(HLD.LetHLDStatement hld) {
//        MyFieldVisitor visitor = new MyFieldVisitor();
//        visitor.onlyJoinFields = true;
//        hld.whereClause.visit(visitor);
        TokFieldVisitor visitor = new TokFieldVisitor();
        visitor.onlyJoinFields = false;
        hld.whereTok.visit(visitor, null);

        DStructType structType = hld.hldTable;
        for (Tok.FieldTok fexp : visitor.allFields) {
            TypePair pair = DValueHelper.findField(structType, fexp.fieldName);
            if (pair != null && pair.type.isStructShape()) {
                RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
                if (relinfo == null) return;
                Exp.JoinInfo joinInfo = new Exp.JoinInfo(relinfo.nearType.getTypeName(), relinfo.farType.getTypeName(), relinfo.fieldName);
                joinInfo.leftType = relinfo.nearType;
                joinInfo.rightType = relinfo.farType;
                joinInfo.relinfo = DRuleHelper.findMatchingRuleInfo(joinInfo.leftType, joinInfo.throughField);
                fexp.joinInfo = joinInfo;
            }
        }

        for (Tok.FieldTok fieldExp : visitor.allFields) {
            TypePair pair = new TypePair(fieldExp.fieldName, null); //2nd arg not used
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
            if (relinfo != null && relMap.containsKey(makeRelMapKey(relinfo))) {
                continue;
            }

            if (relinfo != null && (relinfo.isParent || relinfo.isManyToMany())) {
                HLD.HLDJoin join = new HLD.HLDJoin();
                join.joinInfo = fieldExp.joinInfo;
                join.joinInfo.relinfo = relinfo;
                relMap.put(makeRelMapKey(relinfo), relinfo);
                hld.joinL.add(join);
                //TODO:public List<LLDTests.LLField> fields = new ArrayList<>(); //fields in the joined table that we need to get
            }
        }

        //
//        MyJoinInfoVisitor jiVisitor = new MyJoinInfoVisitor();
//        jiVisitor.structType = structType;
//        hld.whereTok.visit(jiVisitor);
    }

    private String makeRelMapKey(RelationInfo relinfo) {
        return String.format("%s:%s", relinfo.nearType, relinfo.fieldName);
    }

    public void addFKJoins(HLD.LetHLDStatement hld, boolean isFKOnly, List<String> fetches, boolean implicitMMFk) {
        List<HLD.HLDEx> list = hld.fields.stream().filter(x -> (x instanceof HLD.HLDField) && (((HLD.HLDField) x).relinfo != null)).collect(Collectors.toList());

        for (HLD.HLDEx hldEx : list) {
            if (hldEx instanceof HLD.HLDField) {
                HLD.HLDField hldField = (HLD.HLDField) hldEx;
                if (hldField.relinfo == null) {
                    continue;
                }

                if (!hldField.relinfo.isManyToMany()) {
                    if (implicitMMFk) {
                        continue;
                    }
                    boolean isFetch = findFetch(fetches, hldField.relinfo.fieldName);
                    if (hldField.relinfo.isParent) {
                        RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(hldField.hldTable, hldField.pair);
                        if (relinfo != null) {
                            HLD.HLDJoin join = addSingleJoin(hld, relinfo);
                            join.joinInfo.isFKOnly = isFKOnly;
                            join.joinInfo.isFetch = isFetch;

                            //add fk as a field (even though it's a parent field)
                            if (isFetch) {
                                addAllFarFields(join, relinfo);
                            } else {
                                HLD.HLDField ff = new HLD.HLDField(hldField.hldTable, new TypePair(hldField.pair.name, hldField.pair.type), relinfo);
                                join.fields.add(ff);
                            }
                        }
                    } else if (isFetch) {
                        RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(hldField.hldTable, hldField.pair);
                        if (relinfo != null) {
                            if (relMap.containsKey(makeRelMapKey(relinfo))) {
                                continue;
                            }
                            HLD.HLDJoin join = addSingleJoin(hld, relinfo);
                            join.joinInfo.isFKOnly = isFKOnly;
                            join.joinInfo.isFetch = isFetch;

                            addAllFarFields(join, relinfo);
                        }
                    }
                } else if (hldField.relinfo.isManyToMany()) {
                    if (implicitMMFk && hldField.hldTable.fieldIsOptional(hldField.pair.name)) {
                        continue;
                    }

                    RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(hldField.hldTable, hldField.pair);
                    if (relinfo != null) {
                        boolean isFetch = findFetch(fetches, hldField.relinfo.fieldName);
                        HLD.HLDJoin join = addSingleJoin(hld, relinfo);
                        join.joinInfo.isFKOnly = isFKOnly || implicitMMFk;
                        join.joinInfo.isFetch = isFetch;

                        //add fk as a field (even though it's a parent field)
                        HLD.HLDField ff = new HLD.HLDField(hldField.hldTable, new TypePair(hldField.pair.name, hldField.pair.type), relinfo);
                        join.fields.add(ff);
                    }
                }
            }
        }
    }

    private boolean findFetch(List<String> fetches, String fieldName) {
        return fetches.stream().filter(x -> x.equals(fieldName)).findAny().isPresent();
    }

    private void addAllFarFields(HLD.HLDJoin join, RelationInfo relinfo) {
        for (TypePair pp : relinfo.farType.getAllFields()) {
            if (pp.type.isStructShape()) {
                RelationInfo relinfox = DRuleHelper.findMatchingRuleInfo(relinfo.farType, pp);
                if (relinfox != null && relinfox.containsFK()) {
                    HLD.HLDField ff = new HLD.HLDField(relinfo.farType, pp, relinfox);
                    join.fields.add(ff);
                }
            } else {
                HLD.HLDField ff = new HLD.HLDField(relinfo.farType, pp, null);
                join.fields.add(ff);
            }
        }
    }

    public void addWhereJoins(HLD.LetHLDStatement hld) {
        //Customer[wid < 30].addr  we may need to add Customer back in to where can refer to wid
        MyHLDFieldHintVisitor hintVisitor = new MyHLDFieldHintVisitor();
        hintVisitor.top = hld.whereTok.where;
        hintVisitor.structType = hld.hldTable; //where is based on Customer[], not final fields
        hintVisitor.joinL = hld.joinL;
        hld.whereTok.visit(hintVisitor, null);

        //joins of actual entity types (not assoc tabls)
        List<HLD.HLDJoin> entityJoins = hld.joinL.stream().filter(x -> !x.joinInfo.relinfo.isManyToMany()).collect(Collectors.toList());

        for (Tok.FieldTok field : hintVisitor.allFields) {
            if (field.ownerType == null) {
                DeliaExceptionHelper.throwNotImplementedError("unbound field in where issue");
            } else if (field.ownerType == hld.fromType) {
                //do nothing. we're already getting this table
            } else {
                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(field.ownerType);
                if (pkpair != null && field.fieldName.equals(pkpair.name)) {
                    //field is a pkfield, which will be handled by existing joins
                } else {
                    Optional<HLD.HLDJoin> join1 = entityJoins.stream().filter(x -> x.joinInfo.relinfo.nearType == field.ownerType).findAny();
                    Optional<HLD.HLDJoin> join2 = entityJoins.stream().filter(x -> x.joinInfo.relinfo.farType == field.ownerType).findAny();
                    if (!join1.isPresent() && !join2.isPresent()) {
                        //need to add an additional join
                        boolean hack = false;
                        if (hack && field.ownerFoundInJoinInfo != null) {
                            HLD.HLDJoin join = addSingleJoin(hld, field.ownerFoundInJoinInfo.relinfo);
                        } else {
                            MyHLDFieldHintVisitor hintVisitor2 = new MyHLDFieldHintVisitor();
                            hintVisitor2.top = hld.whereTok.where;
                            hintVisitor2.structType = hld.hldTable;
                            hintVisitor2.joinL = hld.joinL;
                            hintVisitor2.onlyLookInJoins = true;
                            hld.whereTok.visit(hintVisitor2, null);

                            for (Tok.FieldTok ff : hintVisitor2.allFields) {
                                if (ff == field) {
                                    HLD.HLDJoin join = addSingleJoin(hld, field.ownerFoundInJoinInfo.relinfo);
                                    join.joinInfo.isTransitive = true;
                                } else {
                                    DeliaExceptionHelper.throwNotImplementedError("oop33");
                                }
                            }
                        }
                    }
                }
            }

            if (field.ownerFoundInJoinInfo == null) {
                for (HLD.HLDJoin join : hld.joinL) {
                    if (join.joinInfo.relinfo.nearType == field.ownerType) {
                        field.ownerFoundInJoinInfo = join.joinInfo;
                        break;
                    }
                }
            }
        }

        //tok
        TokHLDFieldHintVisitor tokVisitor = new TokHLDFieldHintVisitor();
        tokVisitor.top = hld.whereTok.where;
        tokVisitor.structType = hld.hldTable; //where is based on Customer[], not final fields
        tokVisitor.joinL = hld.joinL;
        hld.whereTok.visit(tokVisitor, null);
    }

    public HLD.HLDJoin addSingleJoin(HLD.LetHLDStatement hld, RelationInfo relinfo) {
        if (relinfo == null) return null;

        if (relinfo != null && relMap.containsKey(makeRelMapKey(relinfo))) {
            for (HLD.HLDJoin join : hld.joinL) {
                if (join.joinInfo != null && join.joinInfo.relinfo == relinfo) {
                    return join;
                }
            }
        }

        HLD.HLDJoin join = new HLD.HLDJoin();

        Exp.JoinInfo info = new Exp.JoinInfo(relinfo.nearType.getTypeName(), relinfo.farType.getTypeName(), relinfo.fieldName);
        info.leftType = relinfo.nearType;
        info.rightType = relinfo.farType;
        info.relinfo = relinfo;

        join.joinInfo = info;
        join.joinInfo.relinfo = relinfo;
        hld.joinL.add(join);
        return join;
    }


}
