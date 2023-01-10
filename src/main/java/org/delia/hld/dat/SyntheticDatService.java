package org.delia.hld.dat;

import org.delia.compiler.ast.Exp;
import org.delia.relation.RelationInfo;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SyntheticDatService implements DatService {
    private List<AssocSpec> assocL = new ArrayList<>();

    @Override
    public Integer findDat(Exp.JoinInfo joinInfo) {
        List<AssocSpec> matches = assocL.stream().filter(x -> x.leftType == joinInfo.leftType && x.deliaLeftv.equals(joinInfo.throughField)).collect(Collectors.toList());
        if (matches.size() > 1) {
            DeliaExceptionHelper.throwError("too-many-dat-assocs", String.format("more than one match for '%s'", joinInfo.toString()));
        }
        return matches.isEmpty() ? null : matches.get(0).datId;
    }

    @Override
    public Integer findDat(RelationInfo relinfo) {
        return Optional.ofNullable(findAssocInfo(relinfo))
                .map(assoc -> assoc.datId)
                .orElse(null);
    }

    @Override
    public AssocSpec findAssocInfo(RelationInfo relinfo) {
        List<AssocSpec> matches = assocL.stream().filter(x -> x.leftType == relinfo.nearType && x.deliaLeftv.equals(relinfo.fieldName)).collect(Collectors.toList());
        if (relinfo.relationName != null && matches.size() > 1) {
            matches = matches.stream().filter(x -> relinfo.relationName.equals(x.relationName)).collect(Collectors.toList());
        }

        if (matches.size() > 1) {
            DeliaExceptionHelper.throwError("too-many-dat-assocs", String.format("more than one match for '%s'", relinfo.toString()));
        }
        if (matches.isEmpty()) {
            matches = assocL.stream().filter(x -> x.leftType == relinfo.farType && x.deliaLeftv.equals(relinfo.otherSide.fieldName)).collect(Collectors.toList());
        }
        if (matches.size() > 1) {
            DeliaExceptionHelper.throwError("too-many-dat-assocs", String.format("more than one match for '%s'", relinfo.toString()));
        }

        return matches.isEmpty() ? null : matches.get(0);
    }

    @Override
    public AssocSpec findByAssocTableName(String assocTableName) {
        return assocL.stream().filter(spec -> spec.assocTblName.equals(assocTableName)).findAny().orElse(null);
    }

    @Override
    public List<AssocSpec> findAll() {
        return assocL;
    }

    @Override
    public int findMaxDatId() {
        int maxDatId = Integer.MIN_VALUE;
        for (AssocSpec assocSpec : findAll()) {
            if (assocSpec.datId > maxDatId) {
                maxDatId = assocSpec.datId;
            }
        }
        return maxDatId == Integer.MIN_VALUE ? 0 : maxDatId;
    }

    public int buildAssoc(Exp.JoinInfo joinInfo, String targetField) {
        int existing = findExisting(joinInfo, targetField);
        if (existing > 0) {
            return existing;
        }

        AssocSpec assoc = new AssocSpec();
        assoc.datId = assocL.size() + 1;
        assoc.leftType = joinInfo.leftType;
        assoc.rightType = joinInfo.rightType;
        assoc.deliaLeftv = joinInfo.throughField;
        assoc.deliaRightv = targetField;
        assoc.otherSideFieldName = joinInfo.relinfo.otherSide.fieldName;
        assoc.assocTblName = String.format("%s%sDat%s", assoc.leftType.getName(), assoc.rightType.getName(), assoc.datId);
        assoc.leftColumn = "leftv";
        assoc.rightColumn = "rightv";
        assoc.relationName = joinInfo.relinfo.relationName;
        assocL.add(assoc);
        return assoc.datId;
    }

    //we want CustomerAddress and AddressCustomer to be one datId
    private int findExisting(Exp.JoinInfo joinInfo, String targetField) {
        for(AssocSpec spec: assocL) {
            if (spec.leftType == joinInfo.leftType && spec.rightType == joinInfo.rightType) {
                if (spec.deliaLeftv.equals(targetField)) {
                    //if type and field match. i don't think we need to match the right side.
                    //TODO: we currently only support a single relation to a field.
                    //eg. Customer.addr can only have one far side!
//                    String farField = joinInfo.relinfo.otherSide.fieldName;
//                    if (spec.deliaRightv.equals(farField)) {
                        if (joinInfo.relinfo.relationName != null) {
                            if (joinInfo.relinfo.relationName.equals(spec.relationName)) {
                                return spec.datId;
                            }
                        } else {
                            return spec.datId;
                        }
//                    }
                }
            }


            if (spec.leftType == joinInfo.rightType && spec.rightType == joinInfo.leftType) {
                if (spec.otherSideFieldName.equals(targetField)) {
//                    String farField = joinInfo.relinfo.otherSide.fieldName;
//                    if (spec.deliaLeftv.equals(farField)) {
                        if (joinInfo.relinfo.relationName != null) {
                            if (joinInfo.relinfo.relationName.equals(spec.relationName)) {
                                return spec.datId;
                            }
                        } else {
                            return spec.datId;
                        }
//                    }
                }
            }
        }
        return 0; //not found
    }
}
