package org.delia.migrationparser.parser;

import org.delia.migrationparser.MigrationContext;
import org.delia.migrationparser.MigrationField;
import org.delia.migrationparser.OrderedMapEx;
import org.delia.migrationparser.RelationDetails;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.StrCreator;

import static java.util.Objects.isNull;

public class RelationGenerator {

    public static void renderRelationStartIfNeeded(StrCreator sc, TypePair pair) {
        if (pair.type.isStructShape()) {
            sc.o(" relation");
        }
    }

    public static void renderRelationIfNeeded(StrCreator sc, DStructType structType, TypePair pair, MigrationContext ctx) {
        RelationDetails relDetails = buildRelationDetails(structType, pair, ctx);
        if (relDetails != null) {
            renderRelationModifiers(sc, relDetails.isOne, relDetails.isMany, relDetails.isParent);
        }
    }

    public static RelationDetails buildRelationDetails(DStructType structType, TypePair pair, MigrationContext ctx) {
        if (pair.type.isStructShape()) {
            RelationOneRule oneRule = DRuleHelper.findOneRule(structType, pair.name);
            RelationManyRule manyRule = DRuleHelper.findManyRule(structType, pair.name);

            if (isNull(oneRule) && isNull(manyRule)) {
                String key = String.format("%s:%s", structType.getTypeName().toString(), pair.name);
                if (ctx != null) {
                    String oldName = ctx.migrationFieldResult.findRenameByNewName(structType, pair.name);
                    if (oldName != null) {
                        oneRule = DRuleHelper.findOneRule(structType, oldName);
                        manyRule = DRuleHelper.findManyRule(structType, oldName);
                    }
                }
            }

            boolean isOne = oneRule != null;
            boolean isMany = manyRule != null;
            boolean isParent = (oneRule != null && oneRule.isParent());
            return new RelationDetails(isParent, isOne, isMany);
        } else {
            return null;
        }
    }

    private static void renderRelationModifiers(StrCreator sc, boolean isOne, boolean isMany, boolean isParent) {
        if (isOne) {
            sc.addStr(" one");
            if (isParent) {
                sc.addStr(" parent");
            }
        }
        if (isMany) {
            sc.addStr(" many");
        }
    }

    public static void renderNewRelationIfNeeded(StrCreator sc, TypePair pair, OrderedMapEx omapex) {
        if (omapex.isRelationField(pair.name)) { //pair.type.isStructShape()) {
            boolean isOne = false;
            boolean isMany = false;
            boolean isParent = false;
            if (omapex != null) {
                if (omapex.parentMap.containsKey(pair.name)) {
                    isParent = omapex.parentMap.get(pair.name);
                }
                if (omapex.oneMap.containsKey(pair.name)) {
                    isOne = omapex.oneMap.get(pair.name);
                }
                if (omapex.manyMap.containsKey(pair.name)) {
                    isMany = omapex.manyMap.get(pair.name);
                }
            }

            renderRelationModifiers(sc, isOne, isMany, isParent);
        }
    }
}
