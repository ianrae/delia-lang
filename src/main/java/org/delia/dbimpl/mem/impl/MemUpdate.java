package org.delia.dbimpl.mem.impl;


import org.delia.core.FactoryService;
import org.delia.dbimpl.mem.MemTableFinder;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.runner.QueryResponse;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemUpdate extends MemFilterBase {

    private final FKResolver fkResolver;

    public MemUpdate(FactoryService factorySvc, DTypeRegistry registry, FKResolver fkResolver, MemTableFinder tableFinder) {
        super(factorySvc, registry, tableFinder);
        this.fkResolver = fkResolver;
    }

    //TODO should this be void? how to handle errors
    public QueryResponse executeUpdate(MemDBTable tbl, LLD.LLUpdate stmt) {
        QueryResponse qresp = new QueryResponse();
        RowSelector selector = createSelector(tbl, stmt.table.physicalType.getTypeName(), stmt.whereTok);
        if (selector == null) {
            //err!!
            return qresp;
        } else {
            List<DValue> dvalList = selector.match(selector.getTbl().rowL);
            if (selector.wasError()) {
                //err!!
                qresp.ok = false;
                return qresp;
            }

            /*
            update pk:
                 -search for uniqueness
                 -would have to find all other tbls references the id in DRelation and update them
                update scalar
                 -simply update
                 -rerun validation (in case rules reference several fields; eg. x + y < 10)
                update relation
                 a) 1N,NN
                  parent: do nothing. will support this later
                  child:
                   first get other side of all fks in relinfo and remove sourceValPK
                   replace DRel's current fk list with new value(s)
                   update other side to add new sourceValPK
                 b) MN
                  two-step. eg update Address { cust: 57,58 }
                   first get other side of all fks in relinfo and remove sourceValPK
                   replace DRel's current fk list with new value(s)
                   update other side to add new sourceValPK
             */
            processMatches(qresp, stmt.table, stmt.fieldL, dvalList);
            return qresp;
        }
    }

    public void processMatches(QueryResponse qresp, LLD.LLTable table, List<LLD.LLFieldValue> fieldL, List<DValue> dvalList) {
        DStructType structType = table.physicalType;
        for (DValue dval : dvalList) {
            for (TypePair pair : structType.getAllFields()) {
                String fieldName = pair.name;
                Optional<LLD.LLFieldValue> fieldVal = fieldL.stream().filter(x -> x.field.getFieldName().equals(fieldName)).findAny();

                if (structType.fieldIsPrimaryKey(fieldName)) {
                    if (fieldVal.isPresent()) {
                        DeliaExceptionHelper.throwNotImplementedError("update pk not supported"); //TODO fix later
                    }
                } else if (!pair.type.isStructShape()) {
                    //scalar
                    if (!fieldVal.isPresent()) {
                        continue;
                    }

                    putVal(dval, fieldName, fieldVal.get());
                } else { //relation field
                    if (!fieldVal.isPresent()) {
                        continue;
                    }

                    RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, fieldName);
                    if (relinfo == null) {
                        //err!!
                    } else if (relinfo.isManyToMany()) {
                        //           first get other side of all fks in relinfo and remove sourceValPK
                        //           replace DRel's current fk list with new value(s)
                        //           update other side to add new sourceValPK
                        if (isCrudAction(fieldVal)) {
                            doCrudActionManyToMany(dval, relinfo, fieldVal, structType);
                        } else {
                            fkResolver.removeFromFarSide(dval, relinfo);
                            fkResolver.clearRelation(dval, relinfo);
                            fkResolver.addOrUpdateRelation(structType, relinfo, dval, fieldVal.get(), registry);
                            fkResolver.addRelationOtherSide(structType, relinfo, dval, fieldVal.get(), registry);
                        }
                    } else if (relinfo.isParent) {
                        DeliaExceptionHelper.throwNotImplementedError("update parent side of relation not supported"); //TODO fix later
                    } else { //child
                        //           first get other side of all fks in relinfo and remove sourceValPK
                        //           replace DRel's current fk list with new value(s)
                        //           update other side to add new sourceValPK
                        if (isCrudAction(fieldVal)) {
                            doCrudAction(dval, relinfo, fieldVal, structType);
                        } else {
                            fkResolver.removeFromFarSide(dval, relinfo);
                            fkResolver.clearRelation(dval, relinfo);
                            fkResolver.addOrUpdateRelation(structType, relinfo, dval, fieldVal.get(), registry);
                            fkResolver.addRelationOtherSide(structType, relinfo, dval, fieldVal.get(), registry);
                        }
                    }
                }
            }
        }

        qresp.ok = true;
    }

    private void doCrudAction(DValue dval, RelationInfo relinfo, Optional<LLD.LLFieldValue> optFieldVal, DStructType structType) {
        LLD.LLFieldValue fieldVal = optFieldVal.get();
        LLD.LLFieldValue fieldValToUse = fieldVal;
        switch (fieldVal.field.crudAction) {
            case UPDATE:
                fieldValToUse = new LLD.LLFieldValue(fieldVal.field, null);
                fieldValToUse.dval = fieldVal.dvalList.get(1); //second one of the pair
                break;
            case INSERT: //basically ignore INSERT AND DELETE
            case DELETE: {
                if (!structType.fieldIsOptional(fieldVal.field.getFieldName())) {
                    DeliaExceptionHelper.throwError("crud-action-not-allowed", "crudAction delete not allowed for mandatory field %s.%s", structType.getName(), fieldVal.field.getFieldName());
                }
            }
            default:
                break;
        }

        fkResolver.removeFromFarSide(dval, relinfo);
        fkResolver.clearRelation(dval, relinfo);
        fkResolver.addOrUpdateRelation(structType, relinfo, dval, fieldValToUse, registry);
        fkResolver.addRelationOtherSide(structType, relinfo, dval, fieldValToUse, registry);

    }

    private void doCrudActionManyToMany(DValue dval, RelationInfo relinfo, Optional<LLD.LLFieldValue> optFieldVal, DStructType structType) {
        LLD.LLFieldValue fieldVal = optFieldVal.get();
        LLD.LLFieldValue fieldValToUse = fieldVal;

        switch (fieldVal.field.crudAction) {
            case UPDATE: {
                fieldValToUse = new LLD.LLFieldValue(fieldVal.field, null);
                fieldValToUse.dvalList = new ArrayList<>();

                for (int i = 0; i < fieldVal.dvalList.size(); i += 2) { //increment by 2
                    DValue oldVal = fieldVal.dvalList.get(i);
                    fkResolver.removeFK(dval, relinfo, oldVal);
                    DValue newVal = fieldVal.dvalList.get(i + 1);
                    fieldValToUse.dvalList.add(newVal);
                }

                fkResolver.addOrUpdateRelation(structType, relinfo, dval, fieldValToUse, registry);
                fkResolver.addRelationOtherSide(structType, relinfo, dval, fieldValToUse, registry);
            }
            break;
            case INSERT:
                fkResolver.addOrUpdateRelation(structType, relinfo, dval, fieldValToUse, registry);
                fkResolver.addRelationOtherSide(structType, relinfo, dval, fieldValToUse, registry);
                break;
            case DELETE:
                if (fieldVal.dval != null) {
                    fkResolver.removeFK(dval, relinfo, fieldVal.dval);
                } else if (fieldVal.dvalList != null) {
                    fieldVal.dvalList.stream().forEach(fv -> fkResolver.removeFK(dval, relinfo, fv));
                }
                break;
            default:
                break;
        }
    }

    private boolean isCrudAction(Optional<LLD.LLFieldValue> optFieldVal) {
        LLD.LLFieldValue fieldVal = optFieldVal.get();
        if (fieldVal.field.crudAction != null) {
            return true;
        }
        return false;
    }

    /*
    CRUD ACTION
-can be used on update only
-can be used for 1:1 1:N and M:N relations

  update Address[100] { insert cust:56}
1:1 ignore 'insert'. relation only has one value, so we set it
1:N ignore 'insert'. child side of N:1 only has one value, so we set it
M:N add one row in DAT table 56,100

  update Address[100] { update cust:[55,56]}
1:1 ignore 'update'. relation only has one value, so we set it to 56. should fail if current value not 55
1:N ignore 'update'. child side of N:1 only has one value, so we set it to 56. ""
M:N updates one row in DAT table 55,100 -> 56,100 (returns 0 if row not exist)

  update Address[100] { delete cust:56}
1:1 ignore 'delete'. same as cust:null. error if relation not optional
1:N ignore 'delete'. same as cust:null. ""
M:N deletes one row in DAT table 56,100 (does nothing if this row doesn't exist)

     */

    private void putVal(DValue dval, String fieldName, LLD.LLFieldValue fieldVal) {
        if (fieldVal.dval == null) {
            dval.asMap().put(fieldName, null);
        } else {
            dval.asMap().put(fieldName, fieldVal.dval);
        }
    }

}