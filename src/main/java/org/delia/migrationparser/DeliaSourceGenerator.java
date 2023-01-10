package org.delia.migrationparser;

import org.delia.migrationparser.parser.RelationGenerator;
import org.delia.rule.DRule;
import org.delia.rule.RuleGeneratorContext;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.*;
import org.delia.util.ListWalker;
import org.delia.util.StrCreator;

import java.util.*;
import java.util.stream.Collectors;

public class DeliaSourceGenerator {

    private final MigrationContext ctx;

    public DeliaSourceGenerator(MigrationContext ctx) {
        this.ctx = ctx;
    }

    public String render(DTypeRegistry registry) {
        StrCreator sc = new StrCreator();
        List<DType> allStructs = registry.getOrderedList().stream().filter(type -> type.isStructShape()).collect(Collectors.toList());
        for (DType dtype : allStructs) {
            DStructType structType = (DStructType) dtype;
            if (isDoomedTypeName(structType)) {
                continue;
            }

            String baseStr = structType.getBaseType() == null ? "struct" : getBaseTypeName(structType.getBaseType());
            sc.o("type %s %s {", getTypeName(structType), baseStr);
            sc.nl();
            doFields(sc, structType);
            sc.o("}");
            sc.nl();
            doRules(sc, structType);
            sc.o("end");
            sc.nl();
        }

        return sc.toString();
    }

    private String getBaseTypeName(DType dtype) {
        DStructType structType = (DStructType) dtype;
        return getTypeName(structType);
    }

    private String getTypeName(DStructType structType) {
        String typeName = structType.getName();
        if (ctx.renamedTypeMap.containsKey(typeName)) {
            return ctx.renamedTypeMap.get(typeName);
        } else {
            return typeName;
        }
    }

    private boolean isDoomedTypeName(DStructType structType) {
        Optional<DStructType> opt = ctx.doomedL.stream().filter(x -> x == structType).findAny();
        return opt.isPresent();
    }

    private void doRules(StrCreator sc, DStructType structType) {
        ListWalker<DRule> walker = new ListWalker<>(structType.getRawRules());
        while (walker.hasNext()) {
            DRule rule = walker.next();
            String fieldName = rule.getSubject();
            boolean skip = false;

//            Map<String,String> replaceMap = new HashMap<>();
//            List<TypePair> pairsWithRenames = applyFieldRenames(structType, structType.getAllFields(), replaceMap);

            if (rule instanceof RelationOneRule || rule instanceof RelationManyRule) {
                skip = true;
            } else if (fieldName != null) {
                if (ctx.migrationFieldResult.hasDelete(new MigrationField(structType,fieldName))) {
                    skip = true;
                }
            }

            RuleGeneratorContext ruleGenCtx = new RuleGeneratorContext();
            ruleGenCtx.replaceMap = ctx.migrationFieldResult.buildNameReplacementMap(structType);

            if (!skip) {
                String s = rule.renderAsDelia(ruleGenCtx);
                sc.addStr(s);
                walker.addIfNotLast(sc, ",");
                sc.nl();
            }
        }
    }

    private void doFields(StrCreator sc, DStructType structType) {
        String typeName = structType.getName();
        List<TypePair> fieldPairs = ctx.migrationFieldResult.getFinalFields(structType);

        //TODO: if we rename a type then no other actions allowed for that type in this migration. Fix this later
        if (ctx.renamedTypeMap.containsKey(structType.getName())) {
            fieldPairs = structType.getAllFields();;
        }

        ListWalker<TypePair> walker;
        walker = new ListWalker<>(fieldPairs);
        while (walker.hasNext()) {
            TypePair pair = walker.next();
            DType fieldType = findChangedField(typeName, pair);
            MigrationField mf = new MigrationField(structType, pair.name);
            if (ctx.migrationFieldResult.hasAdd(mf)) {
                if (pair.type == null) {
                    pair.type = fieldType;
                }
                renderNewOrChangedField(sc, typeName, pair);
            } else if (ctx.migrationFieldResult.hasAlter(mf)) {
                renderNewOrChangedField(sc, typeName, pair);
            } else {
                TypePair tmp = pair;
                if (ctx.migrationFieldResult.hasRename(mf)) {
                    TypePair renamedPair = ctx.migrationFieldResult.getRename(mf);
                    tmp.name = renamedPair.name;
                }
                renderExistingField(sc, structType, tmp);
            }

            walker.addIfNotLast(sc, ",");
            sc.nl();
        }
    }


    private void renderExistingField(StrCreator sc, DStructType structType, TypePair pair) {
        RelationGenerator.renderRelationStartIfNeeded(sc, pair);
        String typeStr = BuiltInTypes.convertDTypeNameToDeliaName(pair.type.getName());
        sc.o("  %s %s", pair.name, typeStr);
        //TODO are more things to render here
        if (structType.fieldIsPrimaryKey(pair.name)) {
            sc.addStr(" primaryKey");
        }
        if (structType.fieldIsSerial(pair.name)) {
            sc.addStr(" serial");
        }
        if (structType.fieldIsOptional(pair.name)) {
            sc.addStr(" optional");
        }
        if (structType.fieldIsUnique(pair.name)) {
            sc.addStr(" unique");
        }

        if (pair.type.isStructShape()) {
            RelationGenerator.renderRelationIfNeeded(sc, structType, pair, ctx);
        }
    }

    private void renderNewOrChangedField(StrCreator sc, String typeName, TypePair originalPair) {
        OrderedMapEx omapex = ctx.changeFieldMap.get(typeName);
        OrderedMap omap = omapex.omap;
        DType fieldType = omap.map.get(originalPair.name);
        //when changed field: originalPair is the old type, we need the new type
        TypePair pair = new TypePair(originalPair.name, fieldType);

        RelationGenerator.renderRelationStartIfNeeded(sc, pair);
        String typeStr = BuiltInTypes.convertDTypeNameToDeliaName(pair.type.getName());
        sc.o("  %s %s", pair.name, typeStr);
        if (omap.isPrimaryKey(pair.name)) {
            sc.addStr(" primaryKey");
        }
        if (omap.isSerial(pair.name)) {
            sc.addStr(" serial");
        }
        if (omap.isOptional(pair.name)) {
            sc.addStr(" optional");
        }
        if (omap.isUnique(pair.name)) {
            sc.addStr(" unique");
        }
        RelationGenerator.renderNewRelationIfNeeded(sc, pair, omapex);
    }

    private DType findChangedField(String typeName, TypePair pair) {
        if (ctx.changeFieldMap.containsKey(typeName)) {
            OrderedMap omap = ctx.changeFieldMap.get(typeName).omap;
            if (omap.containsKey(pair.name)) {
                return omap.map.get(pair.name);
            }
        }
        return null;
    }
}
