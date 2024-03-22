package org.delia.migration;

import org.delia.DeliaSession;
import org.delia.log.DeliaLog;
import org.delia.migration.action.*;
import org.delia.relation.RelationInfo;
import org.delia.type.*;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

import java.util.*;

import static java.util.Objects.isNull;

public class AssocActionBuilder {

    private final DeliaLog log;
//        private DatService datSvc;
//        private int nextDatId;

    public AssocActionBuilder(DeliaLog log) {
        this.log = log;
    }

    public void addAssocActions(SchemaMigration schemaMigration, DeliaSession finalSess) {
        Map<String, ManyToManyInfo> assocMap = new HashMap<>();

        List<MigrationActionBase> initialActions = new ArrayList<>(schemaMigration.actions);
        for (MigrationActionBase action : initialActions) {
            if (action instanceof CreateTableAction) {
                for (ManyToManyInfo mminfo : findManyToManyFields(action)) {
                    String key = isInAssocMap(mminfo, assocMap);
                    if (isNull(key)) {
                        createAssocTable(mminfo, schemaMigration);
                        assocMap.put(mminfo.makeKey(), mminfo);
                    }
                }
            } else if (action instanceof DeleteTableAction) {
                for (ManyToManyInfo mminfo : findManyToManyFields(action)) {
                    String key = isInAssocMap(mminfo, assocMap);
                    if (isNull(key)) {
                        deleteAssocTable(mminfo, schemaMigration);
                        assocMap.put(mminfo.makeKey(), mminfo);
//                        assocMap.remove(key);
                    }
                }
            } else if (action instanceof RenameTableAction) {
                for (ManyToManyInfo mminfo : findManyToManyFields(action)) {
                    String key = isInAssocMap(mminfo, assocMap);
                    if (isNull(key)) {
                        renameAssocTable(mminfo, schemaMigration, action.structType);
                        assocMap.put(mminfo.makeKey(), mminfo);
                    }
                }
            } else if (action instanceof AlterFieldAction) {
                AlterFieldAction afAction = (AlterFieldAction) action;
                Optional<ManyToManyInfo> mminfo = buildManyToManyFieldIfNeeded(action, afAction.fieldName);
                if (mminfo.isPresent()) {
                    String key = isInAssocMap(mminfo.get(), assocMap);
                    if (isNull(key)) {
                        DStructType assocType = createAssocTableType(mminfo.get(), schemaMigration);
                        if (createAssocAlterFieldActionIfNeeded(mminfo.get(), assocType, schemaMigration, afAction)) {
                            assocMap.put(mminfo.get().makeKey(), mminfo.get());
                        }
                    }
                }
            } else if (action instanceof AddFieldAction) {
                AddFieldAction afAction = (AddFieldAction) action;
                Optional<ManyToManyInfo> mminfo = buildManyToManyFieldIfNeeded(action, afAction.fieldName, afAction.type);
                if (mminfo.isPresent()) {
                    String key = isInAssocMap(mminfo.get(), assocMap);
                    if (isNull(key)) {
                        createAssocTable(mminfo.get(), schemaMigration);
                        assocMap.put(mminfo.get().makeKey(), mminfo.get());
                    }
                } else { //need to check finalSess too. TODO: do we also need to check finalSess in alterField?
                    mminfo = buildManyToManyFieldIfNeeded(action, finalSess, afAction.fieldName, afAction.type);
                    if (mminfo.isPresent()) {
                        String key = isInAssocMap(mminfo.get(), assocMap);
                        if (isNull(key)) {
                            createAssocTable(mminfo.get(), schemaMigration);
                            assocMap.put(mminfo.get().makeKey(), mminfo.get());
                        }
                    }
                }
            } else if (action instanceof RemoveFieldAction) {
                RemoveFieldAction afAction = (RemoveFieldAction) action;
                Optional<ManyToManyInfo> mminfo = buildManyToManyFieldIfNeeded(action, afAction.fieldName);
                if (mminfo.isPresent()) {
                    String key = isInAssocMap(mminfo.get(), assocMap);
                    if (isNull(key)) {
                        deleteAssocTable(mminfo.get(), schemaMigration);
                        assocMap.put(mminfo.get().makeKey(), mminfo.get());
//                        assocMap.remove(key);
                    }
                }
            } else if (action instanceof RenameFieldAction) {
                RenameFieldAction afAction = (RenameFieldAction) action;
                Optional<ManyToManyInfo> mminfo = buildManyToManyFieldIfNeeded(action, afAction.fieldName);
                if (mminfo.isPresent()) {
                    String key = isInAssocMap(mminfo.get(), assocMap);
                    //No. if Customer.addr becomes .addr2 the assoc table is still CustomerAddressDat<datId>
//                    if (isNull(key)) {
//                        renameAssocTable(mminfo.get(), schemaMigration, action.structType);
//                        assocMap.put(mminfo.get().makeKey(), mminfo.get());
//                    }
                }
            } else if (action instanceof AlterUniqueFieldsConstraint) {
                //nothing to do
            } else {
                DeliaExceptionHelper.throwError("unknown-migration-action", String.format("Unknown action type '%s'", action.getClass().getName()));
            }
        }
    }

    private void renameAssocTable(ManyToManyInfo mminfo, SchemaMigration schemaMigration, DStructType renameActionType) {
        String newAssocTableName = null;
        if (mminfo.isLeftTable(renameActionType)) {
            newAssocTableName = createAssocTableType(mminfo, schemaMigration, Optional.of(renameActionType), Optional.empty()).getName();
        } else {
            newAssocTableName = createAssocTableType(mminfo, schemaMigration, Optional.empty(), Optional.of(renameActionType)).getName();
        }
        DStructType assocType = createAssocTableType(mminfo, schemaMigration);
        RenameTableAction action = new RenameTableAction(assocType);
        action.isAssocTbl = true;
        action.newName = newAssocTableName;
        schemaMigration.addAction(action);
    }

    private void deleteAssocTable(ManyToManyInfo mminfo, SchemaMigration schemaMigration) {
        DStructType assocType = createAssocTableType(mminfo, schemaMigration);
        DeleteTableAction action = new DeleteTableAction(assocType);
        action.isAssocTbl = true;
        schemaMigration.addAction(action);
    }

    private void createAssocTable(ManyToManyInfo mminfo, SchemaMigration schemaMigration) {
        DStructType assocType = createAssocTableType(mminfo, schemaMigration);
        CreateTableAction action = new CreateTableAction(assocType);
        action.isAssocTbl = true;
        schemaMigration.addAction(action);
    }

    private DStructType createAssocTableType(ManyToManyInfo mminfo, SchemaMigration schemaMigration) {
        return createAssocTableType(mminfo, schemaMigration, Optional.empty(), Optional.empty());
    }

    private DStructType createAssocTableType(ManyToManyInfo mminfo, SchemaMigration schemaMigration, Optional<DStructType> newLeftType, Optional<DStructType> newRightType) {
        //TODO: handle is flipped!
        DType leftPK = getPKType(mminfo.relinfo.nearType);
        DType rightPK = getPKType(mminfo.relinfo.farType);
        boolean leftOptional = mminfo.relinfo.nearType.fieldIsOptional(mminfo.relinfo.fieldName);
        //TODO this only works if relinfo is not one-sided!
        boolean rightOptional = mminfo.relinfo.otherSide.nearType.fieldIsOptional(mminfo.relinfo.otherSide.fieldName);

        OrderedMap omap = new OrderedMap();
        omap.add("leftv", leftPK, leftOptional, false, false, false, null);
        omap.add("rightv", rightPK, rightOptional, false, false, false, null);
        PrimaryKey prikey = null; //no pk

        Integer datId = mminfo.relinfo.getDatId(); //Integer.valueOf(nextDatId++);
        String name1 = newLeftType.orElse(mminfo.relinfo.nearType).getName();
        String name2 = newRightType.orElse(mminfo.relinfo.farType).getName();
        String assocTableName = String.format("%s%sDat%s", name1, name2, datId.toString());

        String schema = mminfo.relinfo.nearType.getSchema();
        DStructType dtype = new DStructTypeImpl(Shape.STRUCT, schema, assocTableName, null, omap, null);
        return dtype;
    }

    private boolean createAssocAlterFieldActionIfNeeded(ManyToManyInfo mminfo, DStructType assocType, SchemaMigration schemaMigration, AlterFieldAction afAction) {
        DType leftFieldType = DValueHelper.findFieldType(assocType, "leftv");
        DType rightFieldType = DValueHelper.findFieldType(assocType, "rightv");
        boolean leftOptional = assocType.fieldIsOptional("leftv");
        boolean rightOptional = assocType.fieldIsOptional("rightv");

        AlterFieldAction assocAlterFieldAction = new AlterFieldAction(assocType);
        if (mminfo.isLeftTable(afAction.structType)) {
            addChangeFlagIfDifferent(assocAlterFieldAction, leftOptional, afAction.changeFlags);
            if (!areTypesEqual(leftFieldType, afAction.type)) {
                assocAlterFieldAction.type = afAction.type;
                //TODO handle change of sizeof later!!
            }
        } else {
            addChangeFlagIfDifferent(assocAlterFieldAction, rightOptional, afAction.changeFlags);
            if (!areTypesEqual(rightFieldType, afAction.type)) {
                assocAlterFieldAction.type = afAction.type;
                //TODO handle change of sizeof later!!
            }
        }

        if (!assocAlterFieldAction.changeFlags.isEmpty() || assocAlterFieldAction.type != null) {
            assocAlterFieldAction.isAssocTbl = true;
            schemaMigration.addAction(assocAlterFieldAction);
            return true;
        }
        return false;
    }

    private boolean areTypesEqual(DType dtype1, DType dtype2) {
        return MigrationHelper.areTypesEqual(dtype1, dtype2);
    }

    private void addChangeFlagIfDifferent(AlterFieldAction assocAlterFieldAction, boolean isOptional, String changeFlags) {
        if (isOptional && changeFlags.contains("-O")) {
            assocAlterFieldAction.changeFlags = "-O";
        } else if (!isOptional && changeFlags.contains("+O")) {
            assocAlterFieldAction.changeFlags = "+O";
        }
    }


    private DType getPKType(DStructType structType) {
        TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
        return pkpair.type;
    }

    private String isInAssocMap(ManyToManyInfo mminfo, Map<String, ManyToManyInfo> assocMap) {
        //need to check both sides of relation
        String key = mminfo.makeKey();
        if (assocMap.containsKey(key)) {
            return key;
        }

        String key2 = mminfo.makeKey2(); //careful. relation may not have otherside
        if (key2 != null && assocMap.containsKey(key2)) {
            return key2;
        }
        return null;
    }

    private List<ManyToManyInfo> findManyToManyFields(MigrationActionBase action) {
        List<ManyToManyInfo> mminfos = new ArrayList<>();
        for (TypePair pair : action.structType.getAllFields()) {
            if (!pair.type.isStructShape()) {
                continue;
            }
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(action.structType, pair);
            if (relinfo != null && relinfo.isManyToMany()) {
                ManyToManyInfo mminfo = new ManyToManyInfo(pair, relinfo);
                mminfos.add(mminfo);
            }
        }
        return mminfos;
    }

    private Optional<ManyToManyInfo> buildManyToManyFieldIfNeeded(MigrationActionBase action, String fieldName) {
        TypePair pair = DValueHelper.findField(action.structType, fieldName);
        if (pair != null && pair.type.isStructShape()) {
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(action.structType, pair);
            if (relinfo != null && relinfo.isManyToMany()) {
                ManyToManyInfo mminfo = new ManyToManyInfo(pair, relinfo);
                return Optional.of(mminfo);
            }
        }
        return Optional.empty();
    }

    private Optional<ManyToManyInfo> buildManyToManyFieldIfNeeded(MigrationActionBase action, String fieldName, DType fieldType) {
        TypePair pair = new TypePair(fieldName, fieldType);
        if (pair.type.isStructShape()) {
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(action.structType, pair);
            if (relinfo != null && relinfo.isManyToMany()) {
                ManyToManyInfo mminfo = new ManyToManyInfo(pair, relinfo);
                return Optional.of(mminfo);
            }
        }
        return Optional.empty();
    }
    private Optional<ManyToManyInfo> buildManyToManyFieldIfNeeded(MigrationActionBase action, DeliaSession finalSess, String fieldName, DType fieldType) {
        DStructType structType = finalSess.getRegistry().getStructType(action.structType.getTypeName());
        TypePair pair = new TypePair(fieldName, fieldType);
        if (pair.type.isStructShape()) {
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
            if (relinfo != null && relinfo.isManyToMany()) {
                ManyToManyInfo mminfo = new ManyToManyInfo(pair, relinfo);
                return Optional.of(mminfo);
            }
        }
        return Optional.empty();
    }

}
