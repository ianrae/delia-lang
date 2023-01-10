package org.delia.migrationparser.parser.ast;

import org.delia.migrationparser.MigrationContext;
import org.delia.migrationparser.MigrationField;
import org.delia.migrationparser.OrderedMapEx;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class AlterFieldASTBase extends BaseAST {
    public String fieldName;
    protected DTypeRegistry registry;
    protected boolean failIfFieldNotExist = true;

    public AlterFieldASTBase(String typeName, String fieldName) {
        this.target = typeName;
        this.fieldName = fieldName;
    }

    protected MigrationField createMigrationField(DStructType structType) {
        return new MigrationField(structType, fieldName);
    }

//    protected List<TypePair> createAlterFieldMapIfNeeded(DStructType structType, MigrationContext ctx) {
//        if (!ctx.alterFieldMap.containsKey(structType.getName())) {
//            List<TypePair> copy = new ArrayList<>(structType.getAllFields());
//            ctx.alterFieldMap.put(structType.getName(), copy);
//        }
//        return ctx.alterFieldMap.get(structType.getName());
//    }
//
//    protected void updateAlterFieldMap(DStructType structType, List<TypePair> pairs, MigrationContext ctx) {
//        ctx.alterFieldMap.put(structType.getName(), pairs);
//    }

    protected OrderedMapEx createChangeFieldMapIfNeeded(DStructType structType, MigrationContext ctx) {
        if (!ctx.changeFieldMap.containsKey(structType.getName())) {
            ctx.changeFieldMap.put(structType.getName(), new OrderedMapEx());
        }
        return ctx.changeFieldMap.get(structType.getName());
    }

//    protected void updateChangeFieldMap(DStructType structType, OrderedMap omap, MigrationContext ctx) {
//        ctx.alterFieldMap.put(structType.getName(), omap);
//    }

    @Override
    public void applyMigration(DTypeRegistry registry, MigrationContext ctx) {
        this.registry = registry;
        DStructType structType = ASTHelper.findType(registry, target);
        if (structType != null) {
            boolean found = false;
            List<TypePair> pairs = structType.getAllFields(); // createAlterFieldMapIfNeeded(structType, ctx);
            for (TypePair pair : pairs) {
                if (pair.name.equals(fieldName)) {
                    found = true;
                    onMigrateField(structType, pair, ctx);
                    break;
                }
            }

            if (!found) {
                if (failIfFieldNotExist) {
                    DeliaExceptionHelper.throwError("migration.error", "%s Migration. Type '%s' - can't find field '%s'", getActionName(), target, fieldName);
                } else {
                    onAddField(structType, ctx);
                }
            }
           // updateAlterFieldMap(structType, pairs, ctx);
        }
    }

    protected void onAddField(DStructType structType, MigrationContext ctx) {
    }

    protected abstract String getActionName();

    protected abstract void onMigrateField(DStructType structType, TypePair pair, MigrationContext ctx);

    protected void throwAnExeption(String msg) {
        DeliaExceptionHelper.throwError("migration.fail22", "%s Migration. Type '%s' - %s: %s", getActionName(), target, fieldName, msg);

    }
}
