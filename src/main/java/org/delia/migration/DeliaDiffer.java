package org.delia.migration;

import org.delia.ConnectionDefinitionBuilder;
import org.delia.Delia;
import org.delia.DeliaFactory;
import org.delia.DeliaSession;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.DeliaLog;
import org.delia.migration.action.*;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.*;
import org.delia.util.DRuleHelper;

import java.util.HashMap;
import java.util.Optional;

public class DeliaDiffer {

    private final DeliaLog log;
    private final AssocActionBuilder assocActionBuilder;
    private FactoryService factorySvc;
    private ConnectionDefinition connDef;

    public DeliaDiffer(DeliaLog log) {
        this.log = log;
        this.factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        this.connDef = ConnectionDefinitionBuilder.createMEM();
        this.assocActionBuilder = new AssocActionBuilder(log);
    }

    /**
     * Compares two delia sources and produces a list of migration actions to turn
     * deliaSrc1 into deliaSrc2
     * <p>
     * Note. Does not detect table or field renames. If delia1 has type Customer and
     * delia2 has same type with name Customer2, it will be seen as a delete of Customer
     * and a create of Customer2.  You need to add code to detect this as needed.
     *
     * @param deliaSrc1
     * @param deliaSrc2
     * @return migration
     */
    public SchemaMigration compare(String deliaSrc1, String deliaSrc2) {
        Delia delia1 = DeliaFactory.create(connDef, log, factorySvc);
        DeliaSession sess1 = delia1.beginSession(deliaSrc1);

        Delia delia2 = DeliaFactory.create(connDef, log, factorySvc);
        DeliaSession sess2 = delia2.beginSession(deliaSrc2);

        return compareTypes(sess1, sess2);
    }

    private SchemaMigration compareTypes(DeliaSession sess1, DeliaSession sess2) {
        SchemaMigration schemaMigration = new SchemaMigration();
        schemaMigration.sess = sess1;
        HashMap updatedMap = new HashMap();

        DTypeRegistry registry2 = sess2.getRegistry();
        for (DTypeName dTypeName : registry2.getAll()) {
            DType dtype = registry2.getType(dTypeName);
            if (!dtype.isStructShape()) {
                continue;
            }
            DStructType structType = (DStructType) dtype;

            DStructType cur;
            String key;
            cur = findMatch(dtype, sess1);
            if (cur == null) {
                CreateTableAction action = new CreateTableAction(structType);
                schemaMigration.addAction(action);
            } else {
                key = cur.getTypeName().toString();
                updatedMap.put(key, true);

                mergeVal(cur, structType, schemaMigration);
            }
        }

        DTypeRegistry registry1 = sess1.getRegistry();
        for (DTypeName dTypeName : registry1.getAll()) {
            DType dtype = registry1.getType(dTypeName);
            if (!dtype.isStructShape()) {
                continue;
            }

            String key = dtype.getTypeName().toString();
            if (!updatedMap.containsKey(key)) {
                DeleteTableAction action = new DeleteTableAction((DStructType) dtype);
                schemaMigration.addAction(action);
            }
        }

        addAssocActions(schemaMigration, sess2);
        return schemaMigration;
    }


    public void mergeVal(DStructType cur, DStructType structType2, SchemaMigration schemaMigration) {
        for (TypePair pair : structType2.getAllFields()) {
            Optional<TypePair> match = cur.getAllFields().stream().filter(pr -> pr.name.equals(pair.name)).findAny();
            if (!match.isPresent()) {
                AddFieldAction action = new AddFieldAction(structType2);
                action.fieldName = pair.name;
                action.changeFlags = MigrationHelper.makeFieldFlags(structType2, pair.name);
                action.fieldName = pair.name;
                action.type = pair.type;
                action.sizeOf = MigrationHelper.calcFieldSize(structType2, pair.name);
                if (pair.type.isStructShape()) {
                    RelationOneRule oneRule = DRuleHelper.findOneRule(structType2, pair.name);
                    RelationManyRule manyRule = DRuleHelper.findManyRule(structType2, pair.name);
                    action.setRelationFlags((oneRule != null && oneRule.isParent()), oneRule != null, manyRule != null);
                }
                schemaMigration.addAction(action);
            } else {
                //merge
                String flags1 = MigrationHelper.makeFieldFlags(cur, pair.name);
                String flags2 = MigrationHelper.makeFieldFlags(structType2, pair.name);
                boolean relationChanged = false;
                if (pair.type.isStructShape() && !MigrationHelper.relationsAreTheSame(cur, structType2, pair.name)) {
                    relationChanged = true;
                }
                AlterFieldAction action = new AlterFieldAction(cur);
                action.fieldName = pair.name;
                action.changeFlags = MigrationHelper.mergeFlags(flags1, flags2);
                if (relationChanged) {
                    RelationOneRule oneRule = DRuleHelper.findOneRule(structType2, pair.name);
                    RelationManyRule manyRule = DRuleHelper.findManyRule(structType2, pair.name);
                    action.setRelationFlags((oneRule != null && oneRule.isParent()), oneRule != null, manyRule != null);
                }

                boolean sizeOfAreEqual = true;
                if (pair.type.isShape(Shape.INTEGER) || pair.type.isShape(Shape.STRING)) {
                    sizeOfAreEqual = MigrationHelper.areFieldSizeofEqual(cur, structType2, pair.name);
                }
                boolean typesAreEqual = MigrationHelper.areFieldTypesEqual(cur, structType2, pair.name);
                if (!action.changeFlags.isEmpty() || relationChanged || !typesAreEqual ||!sizeOfAreEqual) {
                    action.type = pair.type;
                    action.sizeOf = MigrationHelper.calcFieldSize(structType2, pair.name);
                    schemaMigration.addAction(action);
                }
            }
        }

        for (TypePair pair : cur.getAllFields()) {
            Optional<TypePair> match = structType2.getAllFields().stream().filter(pr -> pr.name.equals(pair.name)).findAny();
            if (!match.isPresent()) {
                RemoveFieldAction action = new RemoveFieldAction(cur); //note this is the previous type, not structType2
                action.fieldName = pair.name;
                schemaMigration.addAction(action);
            }
        }
    }

    private DStructType findMatch(DType dtype, DeliaSession sess2) {
        //must match by name
        String target = dtype.getTypeName().toString();
        Optional<DTypeName> match = sess2.getRegistry().getAll().stream().filter(x -> x.toString().equals(target)).findAny();
        if (match.isPresent()) {
            return (DStructType) sess2.getRegistry().getType(match.get());
        } else {
            return null;
        }
    }


    private void addAssocActions(SchemaMigration schemaMigration, DeliaSession sess2) {
        assocActionBuilder.addAssocActions(schemaMigration, sess2);
    }
}
