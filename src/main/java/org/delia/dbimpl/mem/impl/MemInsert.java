package org.delia.dbimpl.mem.impl;


import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueExConverter;
import org.delia.dval.compare.DValueCompareService;
import org.delia.error.DetailedError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.type.*;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.StrCreator;
import org.delia.util.StringTrail;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MemInsert extends ServiceBase {
    private final DTypeRegistry registry;
    private final FKResolver fkResolver;
    private DateFormatService fmtSvc;

    public MemInsert(FactoryService factorySvc, DTypeRegistry registry, FKResolver fkResolver) {
        super(factorySvc);
        this.registry = registry;
        this.fmtSvc = factorySvc.getDateFormatService();
        this.fkResolver = fkResolver;
    }

    public DValue executePreInsert(MemDBTable tbl, DStructType structType, List<LLD.LLFieldValue> fieldL, DValue dval, DBStuff stuff) {
        ErrorTracker localET = new SimpleErrorTracker(log);

        DValue generatedId = null;
        if (dval != null) {
            generatedId = addSerialValuesIfNeeded(dval, tbl, stuff, localET);

            //TODO: fldvalue.field is a physical field. will this be a problem if we look it up in DStructType?
            PrimaryKey primaryKey = structType.getPrimaryKey();
            List<String> alreadyChecked = new ArrayList<>();
            if (primaryKey.isMultiple()) {
                for(TypePair pair: primaryKey.getKeys()) {
                    alreadyChecked.add(pair.name);
                }
                checkUniqueness(dval, tbl, alreadyChecked, structType, localET);
            }

            if (!tbl.getList().isEmpty()) { //perf thing. don't bother checking if rowL is empty
                for (LLD.LLFieldValue fldValue : fieldL) {
                    String fieldName = fldValue.field.getFieldName();
                    if (alreadyChecked.contains(fieldName)) {
                        continue;
                    }
                    alreadyChecked.add(fieldName);
                    if (structType.fieldIsUnique(fieldName)) {
                        checkUniqueness(dval, tbl, Collections.singletonList(fieldName), structType, localET);
                    }
                }
            }

            for (String fieldName : dval.asStruct().getFieldNames()) {
                DValue tmp = dval.asStruct().getField(fieldName);
                if (tmp != null && tmp.getType().isRelationShape()) {
                    //tmp is fk into another table (or this table)
                    //validate that fk exists in the target table
                    fkResolver.validateFKs(tmp.asRelation(), dval.asStruct().getType(), fieldName, localET);
                }
            }
        }
        if (!localET.areNoErrors()) {
            et.addAll(localET.getErrors());
        }
        return generatedId;
    }

    public DValue executeInsert(MemDBTable tbl, DStructType structType, List<LLD.LLFieldValue> fieldL, DValue dval, DBStuff stuff) {
        ErrorTracker localET = new SimpleErrorTracker(log);

        DValue generatedId = null; //created in preInsert
        if (dval != null) {
            tbl.getList().add(dval);

            //handle default()
            for(TypePair pair: structType.getAllFields()) {
                Optional<String> opt = structType.fieldHasDefaultValue(pair.name);
                if (opt.isPresent()) {
                    boolean alreadyInDVal = dval.asMap().containsKey(pair.name);
                    if (! alreadyInDVal) {
                        addDefaultValueToDValue(dval, pair, opt.get());
                    }
                }
            }
        }

        //add child side of relation (even if empty relation)
        List<RelationInfo> relationFields = getChildOrMMRelationFields(structType);
        for(RelationInfo relinfo: relationFields) {
            Optional<LLD.LLFieldValue> fieldVal = fieldL.stream().filter(x -> x.field.getFieldName().equals(relinfo.fieldName)).findAny();
            if (!fieldVal.isPresent()) {
                fkResolver.addEmptyRelation(structType, relinfo, dval, registry);
                //don't need to insert on other side, because we will do addEmptyRelation when insert other side dval
                continue;
            }

            if (!relinfo.isParent || relinfo.isManyToMany()) {
                //already added in dval
            } else {
                fkResolver.addOrUpdateRelation(structType, relinfo, dval, fieldVal.get(), registry);
            }
            fkResolver.addRelationOtherSide(structType, relinfo, dval, fieldVal.get(), registry);
        }

        //add parent side too (even if empty)

        if (!localET.areNoErrors()) {
            et.addAll(localET.getErrors());
        }
        return generatedId;
    }

    private void addDefaultValueToDValue(DValue dval, TypePair pair, String s) {
        DValueExConverter converter = new DValueExConverter(factorySvc, registry);
        DValue inner = converter.buildFromObject(s, pair.type);
        dval.asMap().put(pair.name, inner);
    }

    private List<RelationInfo> getChildOrMMRelationFields(DStructType structType) {
//        return fkResolver.findRelationsNeedingFK(structType);
        List<RelationInfo> fields = new ArrayList<>();
        for(TypePair pair: structType.getAllFields()) {
            if (pair.type.isStructShape()) {
                RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
                if (relinfo != null) {
                    fields.add(relinfo);
                }
            }
        }
        return fields;
    }

    /**
     * TODO:This checks all values for fieldName. If there are many rows and we're doing a multi-insert then could
     * improve perf in future by somehow only checking once at the end.
     *
     * @param insertedDVal
     * @param tbl
     * @param fieldName
     * @param structType
     * @param localET
     * @return
     */
    private boolean checkUniqueness(DValue insertedDVal, MemDBTable tbl, List<String> uniqueFields, DStructType structType, ErrorTracker localET) {
        DValueCompareService compareSvc = factorySvc.getDValueCompareService();
        List<DValue> innerVals = new ArrayList<>();
        for(String fieldName: uniqueFields) {
            DValue inner = DValueHelper.getFieldValue(insertedDVal, fieldName);
            if (inner == null) {
                return true; //nothing to do TODO: does uniqueness include null (i.e. 2 null values...)
            }
            innerVals.add(inner);
        }

        boolean b;
        List<DValue> list = tbl.getList();
        if (tbl.needsSynchronizationOnTraverse()) {
            synchronized (list) {
                b = traverseList(list, insertedDVal, uniqueFields, compareSvc, innerVals, localET, structType);
            }
        } else {
            b = traverseList(list, insertedDVal, uniqueFields, compareSvc, innerVals, localET, structType);
        }

        return b;
    }

    private boolean traverseList(List<DValue> list, DValue insertedDVal, List<String> uniqueFields, DValueCompareService compareSvc, List<DValue> innerVals, ErrorTracker localET, DStructType structType) {
        if (uniqueFields.size() == 1) {
            DValue inner = innerVals.get(0);
            return traverseListSingle(list, insertedDVal, uniqueFields.get(0), compareSvc, inner, localET, structType);
        }

        for (DValue existing : list) {
            if (existing != null && existing != insertedDVal) {

                int sameCount = 0;
                StringTrail trail = new StringTrail();
                for(int k = 0; k < uniqueFields.size(); k++) {
                    String uniqueField = uniqueFields.get(k);
                    DValue inner = innerVals.get(k);
                    DValue oldVal = DValueHelper.getFieldValue(existing, uniqueField);
                    if (oldVal != null) {
                        int n = compareSvc.compare(oldVal, inner);
                        if (n == 0) {
                            sameCount++;
                            trail.add(String.format("'%s' = '%s'", uniqueField, inner.asString()));
                        }
                    }
                }
                if (sameCount == uniqueFields.size()) {
                    DetailedError err = new DetailedError("duplicate-unique-value", String.format("%s. row with unique field %s already exists", structType.getName(), trail.getTrail()));
                    err.setFieldName(uniqueFields.get(0)); //there are multiple but we'll just use the first one here
                    localET.add(err);
                    return false;
                }
            }
        }
        return true;
    }
    private boolean traverseListSingle(List<DValue> list, DValue insertedDVal, String uniqueField, DValueCompareService compareSvc, DValue inner, ErrorTracker localET, DStructType structType) {
        for (DValue existing : list) {
            if (existing != null && existing != insertedDVal) {
                DValue oldVal = DValueHelper.getFieldValue(existing, uniqueField);
                if (oldVal != null) {
                    int n = compareSvc.compare(oldVal, inner);
                    if (n == 0) {
                        DetailedError err = new DetailedError("duplicate-unique-value", String.format("%s. row with unique field '%s' = '%s' already exists", structType.getName(), uniqueField, inner.asString()));
                        err.setFieldName(uniqueField);
                        localET.add(err);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private DValue addSerialValuesIfNeeded(DValue dval, MemDBTable tbl, DBStuff stuff, ErrorTracker localET) {
        if (!dval.getType().isStructShape()) {
            return null;
        }
        DValue generatedId = null;
        DStructType structType = (DStructType) dval.getType();
        for (TypePair pair : structType.getAllFields()) {
            if (structType.fieldIsSerial(pair.name)) {
                //this check is now done in Pass4Compiler
//                if (dval.asStruct().getField(pair.name) != null) {
//                    DeliaError err = et.add("serial-value-cannot-be-provided", String.format("serial field '%s' must not have a value specified", pair.name));
//                    localET.add(err);
//                }

                DValue serialVal = stuff.serialProvider.generateSerialValue(structType, pair);
                dval.asMap().put(pair.name, serialVal);
                generatedId = serialVal;
                log.logDebug("serial id generated: %s", serialVal.asString());
            }
        }
        return generatedId;
    }

}