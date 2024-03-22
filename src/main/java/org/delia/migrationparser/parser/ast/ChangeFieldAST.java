package org.delia.migrationparser.parser.ast;

import org.delia.migration.MigrationHelper;
import org.delia.migration.action.AddFieldAction;
import org.delia.migration.action.AlterFieldAction;
import org.delia.migration.action.MigrationActionBase;
import org.delia.migrationparser.MigrationContext;
import org.delia.migrationparser.MigrationField;
import org.delia.migrationparser.OrderedMapEx;
import org.delia.type.*;
import org.delia.util.StrCreator;

import java.util.List;

import static java.util.Objects.isNull;

public class ChangeFieldAST extends AlterFieldASTBase {
    private final List<String> pieces;
    public String fieldSrc;
    protected AddFieldAction addAction;
    private AlterFieldAction alterAction;

    public ChangeFieldAST(String typeName, String fieldName, List<String> pieces) {
        super(typeName, fieldName);
        this.pieces = pieces;
        StrCreator sc = new StrCreator();
        for (int i = 2; i < pieces.size(); i++) {
            String s = pieces.get(i);
            sc.addStr(s);
            sc.addStr(" ");
        }
        fieldSrc = sc.toString();
    }

    @Override
    protected String getActionName() {
        return "ChangeField";
    }

    @Override
    protected void onMigrateField(DStructType structType, TypePair pair, MigrationContext ctx) {
        MigrationField mf = createMigrationField(structType);
        ctx.migrationFieldResult.applyAlter(mf, pair);

        OrderedMapEx omapex = createChangeFieldMapIfNeeded(structType, ctx);
        String[] ar = fieldSrc.split("\\s");
        String fieldType;
        int startIndex;
        if (ar[0].equals("relation")) {
            fieldType = ar[2];
            startIndex = 3;
        } else {
            fieldType = ar[1]; //fieldSrc.get(1);
            startIndex = 2;
        }

        BuiltInTypes bit = BuiltInTypes.fromDeliaTypeName(fieldType);
        DType dtype;
        if (bit == null) {
            dtype = registry.getType(new DTypeName(null, fieldType));
            if (isNull(dtype)) {
                throwAnExeption(String.format("unknown field type '%s'", fieldType));
            }
        } else {
            dtype = registry.getType(bit); //TODO bit can be null if non simple type. fix!
        }

        boolean isOptional = false;
        boolean isUnique = false;
        boolean isPrimaryKey = false;
        boolean isSerial = false;
        boolean isParent = false;
        boolean isOne = false;
        boolean isMany = false;
        int sizeofAmount = 0;
        for (int i = startIndex; i < ar.length; i++) {
            String s = ar[i];
            if (s.equals("optional")) {
                isOptional = true;
            } else if (s.equals("unique")) {
                isUnique = true;
            } else if (s.equals("primaryKey")) {
                isPrimaryKey = true;
            } else if (s.equals("serial")) {
                isSerial = true;
            } else if (s.equals("one")) {
                isOne = true;
            } else if (s.equals("many")) {
                isMany = true;
            } else if (s.equals("parent")) {
                isParent = true;
            } else if (s.equals("sizeof")) {
                sizeofAmount = extractSizeof(ar, i, s);
                i += 3;
            } else {
                throwAnExeption(String.format("unknown field modifier '%s'", s));
            }
        }
        String defaultVal = null; //TODO do we need to support this here?
        omapex.omap.add(fieldName, dtype, isOptional, isUnique, isPrimaryKey, isSerial, defaultVal);
        omapex.parentMap.put(fieldName, isParent);
        omapex.oneMap.put(fieldName, isOne);
        omapex.manyMap.put(fieldName, isMany);

        //we need a struct type to calc change flags
        String currentFlags = MigrationHelper.makeFieldFlags(structType, fieldName);
        DStructType tmpType = new DStructTypeImpl(structType.getShape(), structType.getSchema(), structType.getName(),
                structType.getBaseType(), omapex.omap, structType.getPrimaryKey());
        String newFlags = MigrationHelper.makeFieldFlags(tmpType, fieldName);

        if (this instanceof AddFieldAST) {
            addAction = new AddFieldAction(structType);
            addAction.changeFlags = MigrationHelper.makeFieldFlags(tmpType, fieldName);
            addAction.fieldName = fieldName;
            addAction.type = dtype;
            addAction.sizeOf = sizeofAmount;
            setRelationFlags(addAction, isParent, isOne, isMany);
        } else {
            alterAction = new AlterFieldAction(structType);
            alterAction.changeFlags = MigrationHelper.mergeFlags(currentFlags, newFlags);
            alterAction.fieldName = fieldName;
            alterAction.type = dtype;
            alterAction.sizeOf = sizeofAmount;
            setRelationFlags(alterAction, isParent, isOne, isMany);
        }
    }

    private int extractSizeof(String[] ar, int i, String s) {
        if (i + 3 >= ar.length) {
            throwAnExeption(String.format("bad sizeof '%s'", s));
        }
        if (ar[i+1].equals("(") && ar[i+3].equals(")")) {
            Integer n = Integer.parseInt(ar[i+2]);
            return n;
        }
        throwAnExeption(String.format("bad sizeof '%s'.", s));
        return 0;
    }

    public void setRelationFlags(AddFieldAction action, boolean isParent, boolean isOne, boolean isMany) {
        action.setRelationFlags(isParent, isOne, isMany);
    }

    @Override
    public MigrationActionBase generateAction() {
        return isNull(addAction) ? alterAction : addAction;
    }
}
