package org.delia.dbimpl.mem.impl;


import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dbimpl.mem.MemTableMap;
import org.delia.dval.DRelationHelper;
import org.delia.dval.compare.DValueCompareService;
import org.delia.error.ErrorTracker;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.type.*;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

public class FKResolver extends ServiceBase {
    private MemTableMap tableMap;
    private DValueCompareService compareSvc;

    public FKResolver(FactoryService factorySvc, MemTableMap tableMap) {
        super(factorySvc);
        this.tableMap = tableMap;
        this.compareSvc = new DValueCompareService(factorySvc);
    }

    public List<DValue> cloneValues(List<DValue> list) {
        return null; //use DValueCopyService
    }

    public List<RelationInfo> findRelationsNeedingFK(LLD.LLSelect stmt) {
        return findRelationsNeedingFK(stmt.table.physicalType);
    }

    public List<RelationInfo> findRelationsNeedingFK(DStructType structType) {
        List<RelationInfo> relationsNeedingFK = new ArrayList<>();
        for (TypePair pair : structType.getAllFields()) {
            if (pair.type.isStructShape()) {
                RelationInfo info = DRuleHelper.findMatchingRuleInfo(structType, pair);
                if (info != null && (info.isParent || info.isManyToMany())) {
                    relationsNeedingFK.add(info);
                }
            }
        }
        return relationsNeedingFK;
    }

    public List<DValue> findFKs(DValue sourceVal, RelationInfo relinfo) {
        String targetFieldName = relinfo.otherSide.fieldName;
        DStructType targetType = relinfo.farType;
        MemDBTable tbl = tableMap.getTable(targetType);

        RowSelector selector = new AllRowsSelector();
        selector.init(null, null, null, null); //null is ok

        List<DValue> matches = new ArrayList<>();
        DValue pkval = DValueHelper.findPrimaryKeyValue(sourceVal);
        List<DValue> dvalList = selector.match(tbl.rowL);
        for (DValue dval : dvalList) {
            DValue inner = dval.asStruct().getField(targetFieldName);
            if (inner != null) {
                DRelation drel = inner.asRelation();
                for (DValue fkval : drel.getMultipleKeys()) {
                    if (compareSvc.compare(fkval, pkval) == 0) {
                        DValue foreignPKVal = DValueHelper.findPrimaryKeyValue(dval);
                        matches.add(foreignPKVal);
                    }
                }
            }
        }

        return matches;
    }

    public void addRelations(DValue sourceVal, LLD.LLSelect stmt, RelationInfo relinfo, List<DValue> pkvals, DTypeRegistry registry) {
        DStructType structType = stmt.table.physicalType;

        //build relation value sourceVal.relation.fieldName = matches
        if (!pkvals.isEmpty()) {
            DValue tmp = DValueHelper.getFieldValue(sourceVal, relinfo.fieldName);
            if (tmp == null) {
                DValue dvalRelation = DRelationHelper.createEmptyRelation(structType, relinfo.fieldName, registry);
                dvalRelation.asRelation().getMultipleKeys().addAll(pkvals);
                sourceVal.asMap().put(relinfo.fieldName, dvalRelation);
            }
        }
    }

    public boolean validateFKs(DRelation drel, DStructType structType, String fieldName, ErrorTracker localET) {
        RelationInfo info = DRuleHelper.findMatchingRuleInfo(structType, fieldName);
        MemDBTable tbl = tableMap.getTable(info.farType);

        RowSelector selector = new AllRowsSelector();
        selector.init(null, null, null, null); //null is ok

        int failCount = 0;
        List<DValue> dvalList = selector.match(tbl.rowL);
        for (DValue fkval : drel.getMultipleKeys()) {
            if (!findInList(fkval, dvalList)) {
                String msg = String.format("relation field '%s' one - no value found for foreign key '%s'", fieldName, fkval.asString());
                String errId = info.isManyToMany() ? "rule-relationMany" : "rule-relationOne";
                localET.add(errId, msg);
                failCount++;
            }
        }
        return failCount == 0;
    }

    private boolean findInList(DValue fkval, List<DValue> dvalList) {
        DValue dval = findDValInList(fkval, dvalList);
        return (dval != null);
    }

    private DValue findDValInList(DValue fkval, List<DValue> dvalList) {
        for (DValue dval : dvalList) {
            if (dval != null) {
                DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
                if (compareSvc.compare(fkval, pkval) == 0) {
                    return dval;
                }
            }
        }
        return null;
    }

    public void addOrUpdateRelation(DStructType structType, RelationInfo relinfo, DValue sourceVal, LLD.LLFieldValue fieldVal, DTypeRegistry registry) {
        DValue dvalRelation = sourceVal.asStruct().getField(relinfo.fieldName);
//        if (dvalRelation == null) {
//            dvalRelation = DRelationHelper.createEmptyRelation(structType, relinfo.fieldName, registry);
//        }
        //dval can be null, in which case we just create empty relation
        if (fieldVal.dval != null) {
            dvalRelation.asRelation().getMultipleKeys().add(fieldVal.dval);
        } else if (fieldVal.dvalList != null) {
            dvalRelation.asRelation().getMultipleKeys().addAll(fieldVal.dvalList);
        }
//        sourceVal.asMap().put(relinfo.fieldName, dvalRelation);
    }

    public void addEmptyRelation(DStructType structType, RelationInfo relinfo, DValue sourceVal, DTypeRegistry registry) {
        DValue dvalRelation = DRelationHelper.createEmptyRelation(structType, relinfo.fieldName, registry);
        sourceVal.asMap().put(relinfo.fieldName, dvalRelation);
    }

    public void addRelationOtherSide(DStructType structType, RelationInfo relinfo, DValue sourceVal, LLD.LLFieldValue fieldVal, DTypeRegistry registry) {
        MemDBTable tbl = tableMap.getTable(relinfo.farType);

        RowSelector selector = new AllRowsSelector();
        selector.init(null, null, null, null); //null is ok
        //example: insert Address 100  cust:[55,57]
        //sourceVal is Address.100 and fieldVal.dvalList is [55,57]
        //otherSideDvalList is all the Customer objects

        boolean isOneWay = relinfo.otherSide == null;
        DValue sourcePK = DValueHelper.findPrimaryKeyValue(sourceVal);
        List<DValue> fks = fieldVal.dval != null ? Collections.singletonList(fieldVal.dval) : fieldVal.dvalList;
        if (fks == null) {
            return;
        }

        List<DValue> otherSideDvalList = selector.match(tbl.rowL);
        for (DValue fkval : fks) {
            if (fkval == null || isOneWay) {
                continue;
            }

            DValue otherSideDVal = findDValInList(fkval, otherSideDvalList);
            if (otherSideDVal != null) {
                updateRelation(otherSideDVal, relinfo.otherSide.fieldName, sourcePK, structType, registry);
            }
        }
    }

    //should not have to create relation since we always create DRel (even if empty)
    private void updateRelation(DValue otherSideDVal, String fieldName, DValue sourcePK, DStructType structType, DTypeRegistry registry) {
        DValue inner = otherSideDVal.asStruct().getField(fieldName);
        if (isNull(inner)) {
            DStructType otherSideType = otherSideDVal.asStruct().getType();
            DValue fieldVal = DRelationHelper.createEmptyRelation(otherSideType, fieldName, registry);
            DRelation drel = fieldVal.asRelation();
            drel.getMultipleKeys().add(sourcePK); //TODO: make thread-safe. use thread-safe list inside DRelation
            otherSideDVal.asMap().put(fieldName, fieldVal);
        } else {
            DRelation drel = inner.asRelation();
            drel.getMultipleKeys().add(sourcePK); //TODO: make thread-safe. use thread-safe list inside DRelation
        }
    }

    public void clearRelations(DValue dval, List<RelationInfo> relinfos) {
        for (RelationInfo relinfo : relinfos) {
            clearRelation(dval, relinfo);
        }
    }

    public void clearRelation(DValue dval, RelationInfo relinfo) {
        DValue inner;
        if (dval.asStruct().hasField(relinfo.fieldName)) {
            inner = dval.asStruct().getField(relinfo.fieldName);
        } else {
            inner = dval.asStruct().getField(relinfo.otherSide.fieldName);
        }

        //Note. DValue map for structType can be empty in 3 ways
        //a) no entry for fieldname in the map
        //b) entry in map for fieldname, whose value is null
        //c) entry in map for fieldname, whose value is an empy DRelation
        if (inner != null) {
            DRelation drel = inner.asRelation();
            drel.clearKeys();
        }
    }

    public void removeFK(DValue dval, RelationInfo relinfo, DValue fkvalToRemove) {
        DValue inner = dval.asStruct().getField(relinfo.fieldName);
        DRelation drel = inner.asRelation();
        int index = 0;
        for (DValue fk : drel.getMultipleKeys()) {
            if (compareSvc.compare(fk, fkvalToRemove) == 0) {
                drel.getMultipleKeys().remove(index);
                return;
            }
            index++;
        }
    }

    public void removeFromFarSide(DValue sourceVal, RelationInfo relinfo) {
        if (relinfo.otherSide == null) return;
        MemDBTable tbl = tableMap.getTable(relinfo.farType);

        RowSelector selector = new AllRowsSelector();
        selector.init(null, null, null, null); //null is ok
        //example: delete Address 100
        //sourceVal is Address.100 and two Customer objects refer to it [55,57]
        //otherSideDvalList is all the Customer objects

        DValue sourcePK = DValueHelper.findPrimaryKeyValue(sourceVal);
//        List<DValue> fks = fieldVal.dval != null ? Collections.singletonList(fieldVal.dval) : fieldVal.dvalList;
        List<DValue> otherSideDvalList = selector.match(tbl.rowL);
        for (DValue otherSideDVal : otherSideDvalList) {
            DValue inner = otherSideDVal.asStruct().getField(relinfo.otherSide.fieldName);
            DRelation drelOther = inner.asRelation();
            if (relationContainsFK(drelOther, sourcePK)) {
                drelOther.removeKeyIfPresent(sourcePK);
            }
        }
    }

    private boolean relationContainsFK(DRelation drelOther, DValue sourcePK) {
        for (DValue fk : drelOther.getMultipleKeys()) {
            if (compareSvc.compare(fk, sourcePK) == 0) {
                return true;
            }
        }
        return false;
    }
}