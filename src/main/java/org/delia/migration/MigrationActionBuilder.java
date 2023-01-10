package org.delia.migration;

import org.apache.commons.lang3.StringUtils;
import org.delia.ConnectionDefinitionBuilder;
import org.delia.Delia;
import org.delia.DeliaFactory;
import org.delia.DeliaSession;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.error.DeliaError;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.DeliaLog;
import org.delia.migration.action.CreateTableAction;
import org.delia.migration.action.MigrationActionBase;
import org.delia.migrationparser.DeliaSourceGenerator;
import org.delia.migrationparser.MigrationContext;
import org.delia.migrationparser.parser.ast.AST;
import org.delia.migrationparser.parser.ast.RenameTypeAST;
import org.delia.runner.DeliaException;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class MigrationActionBuilder {

    private final DeliaLog log;
    private final AssocActionBuilder assocActionBuilder;
    private FactoryService factorySvc;
    private ConnectionDefinition connDef;
    private String finalSource;
    private DeliaSession finalSess;

    public MigrationActionBuilder(DeliaLog log) {
        this.log = log;
        this.factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        this.connDef = ConnectionDefinitionBuilder.createMEM();
        this.assocActionBuilder = new AssocActionBuilder(log);
    }

    public SchemaMigration updateMigration(DeliaSession sess, List<AST> asts, String additionsSrc) {
        List<MigrationActionBase> addActions = new ArrayList<>();
        this.finalSource = generateNewSource(sess, asts, additionsSrc, addActions);

        SchemaMigration schemaMigration = convertASTsToActions(sess, asts);
        schemaMigration.actions.addAll(addActions);
        assocActionBuilder.addAssocActions(schemaMigration, finalSess); //assume not done yet

        return schemaMigration;
    }

    private SchemaMigration convertASTsToActions(DeliaSession sess, List<AST> asts) {
        SchemaMigration schemaMigration = new SchemaMigration();
        schemaMigration.sess = sess;
        for (AST ast : asts) {
            MigrationActionBase action = ast.generateAction();
            if (action != null) {
                schemaMigration.addAction(action);
            } else {
                DeliaExceptionHelper.throwNotImplementedError("ast to action not impl");
            }
        }

        return schemaMigration;
    }

    public String getFinalSource() {
        return finalSource;
    }

    public DeliaSession getFinalSess() {
        return finalSess;
    }

    private String generateNewSource(DeliaSession sess, List<AST> asts, String additionalSrc, List<MigrationActionBase> addActions) {
        String deliaSrc = null;
        //apply addition to sess. ok if sess is null
        MigrationContext ctx = new MigrationContext();
        applyMigration(asts, sess, ctx);

        //render sess to deliaSrc
        deliaSrc = renderToDelia(sess, ctx);
        DeliaSession sessAfterAlterations;
        sessAfterAlterations = initDelia(deliaSrc);

        finalSess = null;
        if (StringUtils.isEmpty(additionalSrc)) {
            finalSess = sessAfterAlterations;
        } else {
            deliaSrc += "\n";
            deliaSrc += additionalSrc;
            finalSess = initDelia(deliaSrc);
            //now handle the additionalSrc
            addAdditionalSrcActions(sessAfterAlterations, finalSess, asts, addActions);
        }


        return deliaSrc;
    }

    private void addAdditionalSrcActions(DeliaSession sessAfterAlterations, DeliaSession finalSess, List<AST> asts, List<MigrationActionBase> addActions) {
        List<DStructType> structTypes1 = buildAllStructTypes(sessAfterAlterations.getRegistry());
        List<DStructType> structTypes2 = buildAllStructTypes(finalSess.getRegistry());
        for (DStructType structType : structTypes2) {
            String target = structType.getTypeName().toString();
            DStructType foundIn1 = null;
            for (DStructType inner : structTypes1) {
                if (inner.getTypeName().toString().equals(target)) {
                    foundIn1 = inner;
                }
            }

            if (isNull(foundIn1)) {
                boolean foundRenameType = false;
                for (AST ast : asts) {
                    if (ast instanceof RenameTypeAST) {
                        RenameTypeAST renameAst = (RenameTypeAST) ast;
                        if (structType.getName().equals(renameAst.newName)) {
                            foundRenameType = true;
                        }
                    }
                }

                if (!foundRenameType) {
                    //is a new type in finalSess, so must be from additionalSrc
                    CreateTableAction action = new CreateTableAction(structType);
                    addActions.add(action);
                }
            }
        }
    }


    private List<DStructType> buildAllStructTypes(DTypeRegistry registry) {
        List<DStructType> list = registry.getOrderedList().stream().filter(t -> t.isStructShape()).map(x -> (DStructType) x).collect(Collectors.toList());
        return list;
    }

    private String renderToDelia(DeliaSession sess, MigrationContext ctx) {
        DeliaSourceGenerator generator = new DeliaSourceGenerator(ctx);
        return generator.render(sess.getRegistry());
    }

    private void applyMigration(List<AST> asts, DeliaSession sess, MigrationContext ctx) {
        DTypeRegistry registry = sess == null ? null : sess.getRegistry();
        for (AST ast : asts) {
            ast.applyMigration(registry, ctx);
        }
    }

    private DeliaSession initDelia(String src) {
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();

        Delia delia = DeliaFactory.create(connDef, log, factorySvc);
        delia.getOptions().generateSqlWhenMEMDBType = true;
        DeliaSession sess = delia.beginSession(src);
        if (!sess.ok()) {
            for (DeliaError err : sess.getFinalResult().errors) {
                log.logError("ERR: %s", err.toString());
            }
            throw new DeliaException(sess.getFinalResult().errors);
        }
        return sess;
    }

}
