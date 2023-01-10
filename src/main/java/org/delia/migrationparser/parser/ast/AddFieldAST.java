package org.delia.migrationparser.parser.ast;

import org.delia.migrationparser.MigrationContext;
import org.delia.migrationparser.MigrationField;
import org.delia.type.DStructType;
import org.delia.type.TypePair;

import java.util.List;

public class AddFieldAST extends ChangeFieldAST {

    public AddFieldAST(String typeName, String fieldName, List<String> pieces) {
        super(typeName, fieldName, pieces);
        failIfFieldNotExist = false;
    }

    @Override
    protected String getActionName() {
        return "AddField";
    }

    @Override
    protected void onAddField(DStructType structType, MigrationContext ctx) {
        onMigrateField(structType, null, ctx);

        MigrationField mf = createMigrationField(structType);
        TypePair pair = new TypePair(fieldName, null); //NOTE. we don't know the type at this point
//        ctx.migrationFieldResult.addMap.put(mf.makeKey(), pair);
        ctx.migrationFieldResult.applyAdd(mf, pair);
    }

}
