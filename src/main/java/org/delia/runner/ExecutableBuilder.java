package org.delia.runner;

import org.delia.DeliaOptions;
import org.delia.compiler.BuildCallback;
import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBType;
import org.delia.db.SqlStatement;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLDBuilder;
import org.delia.hld.HLDFirstPassResults;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.lld.processor.LLDBuilder;
import org.delia.runner.bulkinsert.BulkInsertBuilder;
import org.delia.sort.table.ListRearranger;
import org.delia.sort.topo.DeliaTypeSorter;
import org.delia.sql.LLDSqlGenerator;
import org.delia.type.DTypeName;
import org.delia.util.DTypeNameUtil;
import org.delia.util.DeliaExceptionHelper;
import org.delia.varevaluator.VarEvaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class ExecutableBuilder extends ServiceBase {

    private final DatService datSvc;
    private final VarEvaluator varEvaluator;
    private final DeliaOptions deliaOptions; //can be null
    private final Map<String, String> syntheticIdMap;
    private final String defaultSchema;
    public BuildCallback buildCallback;

    public ExecutableBuilder(FactoryService factorySvc, DatService datSvc, VarEvaluator varEvaluator) {
        this(factorySvc, datSvc, varEvaluator, new DeliaOptions(), new HashMap<String,String>(), null);
    }

    public ExecutableBuilder(FactoryService factorySvc, DatService datSvc, VarEvaluator varEvaluator, DeliaOptions deliaOptions,
                             Map<String,String> syntheticIdMap, String defaultSchema) {
        super(factorySvc);
        this.datSvc = datSvc;
        this.varEvaluator = varEvaluator;
        this.deliaOptions = deliaOptions;
        this.syntheticIdMap = syntheticIdMap;
        this.defaultSchema = defaultSchema;
    }

    public DeliaExecutable buildFromScript(AST.DeliaScript script, HLDFirstPassResults firstPassResults, DBType dbType) {
        DeliaExecutable exec = buildHLD(script, firstPassResults, dbType);
//        boolean isMem = DBType.MEM.equals(dbType);
//        if (!isMem) {
//            //build LLD
//            //build SQL
//        }
        LLDBuilder builder = new LLDBuilder(factorySvc, datSvc, deliaOptions);
        builder.buildLLD(exec);

        //apply any optimizations
        generateBulkInsertsIfEnabled(exec);

        //do create-table re-ordering. If Address has an fk to Customer then must
        //create Customer before Address
        reOrderStatements(exec);

        //and now gen sql
        LLDSqlGenerator gen = new LLDSqlGenerator(factorySvc, deliaOptions, exec.registry, datSvc, varEvaluator);
        gen.prepare(exec.lldStatements);
        boolean genSql = shouldGenerateSQL(dbType);
        if (genSql) {
            for (LLD.LLStatement lldStatement : exec.lldStatements) {
                SqlStatement sql = gen.generateSql(lldStatement);
                if (sql != null) {
                    lldStatement.setSql(sql);
                }
            }
        }
        exec.datSvc = datSvc;

        return exec;
    }

    private void generateBulkInsertsIfEnabled(DeliaExecutable exec) {
        if (!deliaOptions.bulkInsertEnabled) return;

        BulkInsertBuilder bulkInsertBuilder = new BulkInsertBuilder(deliaOptions);
        exec.lldStatements  = bulkInsertBuilder.process(exec.lldStatements);
    }

    private boolean shouldGenerateSQL(DBType dbType) {
        if (DBType.MEM.equals(dbType)) {
            return deliaOptions.generateSqlWhenMEMDBType;
        }
        return true;
    }

    private void reOrderStatements(DeliaExecutable exec) {
        DeliaTypeSorter typeSorter = new DeliaTypeSorter();
        try {
            Optional<LLD.LLStatement> createTableStmt = exec.lldStatements.stream().filter(lld -> lld instanceof LLD.LLCreateTable).findAny();
            if (!createTableStmt.isPresent()) {
                return; //nothing to do
            }

            List<DTypeName> orderL = typeSorter.topoSort(exec.registry, log);
            log.log("types: %s", DTypeNameUtil.flatten(orderL));

            List<LLD.LLStatement> list = exec.lldStatements;
            Predicate<LLD.LLStatement> createTableStatmentsPredicate = x -> {
                return x instanceof LLD.LLCreateTable || x instanceof LLD.LLCreateAssocTable;
            };
            ListRearranger<LLD.LLStatement> mySorter = new ListRearranger<>();
            MyCreateTableSorter createTableSorter = new MyCreateTableSorter(orderL);
            exec.lldStatements = mySorter.sortSubList(list, createTableStatmentsPredicate, createTableSorter);

        } catch (IllegalArgumentException e) {
            DeliaExceptionHelper.throwError("type-dependency-cycle", "did you forget a 'parent' modifier?");
        }

    }

    private DeliaExecutable buildHLD(AST.DeliaScript script, HLDFirstPassResults firstPassResults, DBType dbType) {
        DeliaExecutable exec = new DeliaExecutable();
        exec.script = script;
        exec.registry = firstPassResults.registry;
        exec.dbType = dbType;
        HLDBuilder builder = new HLDBuilder(factorySvc, datSvc, deliaOptions, defaultSchema);
        exec.hldStatements = builder.buildHLD(script, firstPassResults.registry, dbType, syntheticIdMap);

        if (buildCallback != null) {
            buildCallback.doCallback(exec.hldStatements, firstPassResults.registry);
        }

        builder.assignDATs(exec.hldStatements, firstPassResults.registry, dbType);
        return exec;
    }
}
