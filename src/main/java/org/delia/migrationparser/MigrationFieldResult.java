package org.delia.migrationparser;

import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.delia.type.TypePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Note. the proper order for actions is
 * -deletes
 * -alters
 * -renames
 * -adds
 * <p>
 * TODO: we may need to sort actions. For now we assume migration actions are in a sensible order
 */
public class MigrationFieldResult {
    private Map<String, TypePair> addMap = new HashMap<>(); //MigrationField.key, TypePair  field being added
    private Map<String, TypePair> renameMap = new HashMap<>(); //MigrationField.key, TypePair  field with new name
    private Map<String, TypePair> deleteMap = new HashMap<>(); //MigrationField.key, TypePair  field being deleted
    private Map<String, TypePair> alterMap = new HashMap<>(); //MigrationField.key, TypePair  field being altered
    public Map<String, TypePair> finalMap = new HashMap<>(); //MigrationField.key, TypePair  result of all delete,alter,rename,add
    //Note. finalMap does not include fields that were not migrated

    public void applyAdd(MigrationField mf, TypePair pair) {
        addMap.put(mf.makeKey(), pair);
        finalMap.put(mf.makeKey(), pair);
    }

    public void applyRename(MigrationField mf, TypePair pair) {
        renameMap.put(mf.makeKey(), pair);
        finalMap.put(mf.makeKey(), pair);
    }

    public void applyDelete(MigrationField mf, TypePair pair) {
        deleteMap.put(mf.makeKey(), pair);
        finalMap.remove(mf.makeKey());
    }

    public void applyAlter(MigrationField mf, TypePair pair) {
        alterMap.put(mf.makeKey(), pair);
        finalMap.put(mf.makeKey(), pair);
    }

    public boolean hasAdd(MigrationField mf) {
        return addMap.containsKey(mf.makeKey());
    }

    public boolean hasRename(MigrationField mf) {
        return renameMap.containsKey(mf.makeKey());
    }
    public String findRenameByNewName(DStructType structType, String newFieldName) {
        for(String key: renameMap.keySet()) {
            DTypeName dTypeName = MigrationField.getTypeNameFromKey(key);
            if (dTypeName.equals(structType.getTypeName())) {
                TypePair pair = renameMap.get(key);
                if (pair != null && pair.name.equals(newFieldName)) {
                    String oldName = MigrationField.getFieldNameFromKey(key);
                    return oldName;
                }
            }
        }
        return null;
    }

    public boolean hasDelete(MigrationField mf) {
        return deleteMap.containsKey(mf.makeKey());
    }

    public boolean hasAlter(MigrationField mf) {
        return alterMap.containsKey(mf.makeKey());
    }

    public boolean hasFinal(MigrationField mf) {
        return finalMap.containsKey(mf.makeKey());
    }

    public TypePair getAdd(MigrationField mf) {
        return addMap.get(mf.makeKey());
    }

    public TypePair getRename(MigrationField mf) {
        return renameMap.get(mf.makeKey());
    }

    public TypePair getDelete(MigrationField mf) {
        return deleteMap.get(mf.makeKey());
    }

    public TypePair getAlter(MigrationField mf) {
        return alterMap.get(mf.makeKey());
    }

    public Map<String, String> buildNameReplacementMap(DStructType structType) {
        Map<String, String> replacementMap = new HashMap<>();

        for (String key : renameMap.keySet()) {
            DTypeName dTypeName = MigrationField.getTypeNameFromKey(key);
            if (dTypeName.equals(structType.getTypeName())) {
                String oldName = MigrationField.getFieldNameFromKey(key);
                TypePair pair = renameMap.get(key);
                replacementMap.put(oldName, pair.name);
            }
        }

        return replacementMap;
    }


    public List<TypePair> getFinalFields(DStructType structType) {
        List<TypePair> finalList = new ArrayList<>();
        for (TypePair pair : structType.getAllFields()) {
            MigrationField mf = new MigrationField(structType, pair.name);
            if (hasFinal(mf)) {
                if (! hasAdd(mf)) {
                    finalList.add(pair);
                }
            } else {
                if (! hasDelete(mf)) {
                    finalList.add(pair); //a field in structType that has no migrations
                }
            }
        }
        for (String key : addMap.keySet()) {
            DTypeName dTypeName = MigrationField.getTypeNameFromKey(key);
            if (dTypeName.equals(structType.getTypeName())) {
                TypePair pair = addMap.get(key);
                if (pair != null) {
                    finalList.add(pair);
                }
            }
        }
        return finalList;
    }

    public boolean isAddedField(DStructType structType, String fieldName) {
        MigrationField mf = new MigrationField(structType, fieldName);
        return hasAdd(mf);
    }
}
