package org.delia.lld.processor;

import org.delia.DeliaOptions;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBType;
import org.delia.error.DeliaError;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLD;
import org.delia.hld.dat.AssocSpec;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.runner.DeliaException;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LLDBuilder extends ServiceBase {
    private final DeliaOptions options;
    private Map<Class, LLDProcessor> builderMap = new HashMap<>();
    private final DatService datSvc;
    private final AliasProcessor aliasProc;

    public LLDBuilder(FactoryService factorySvc, DatService datSvc, DeliaOptions deliaOptions) {
        super(factorySvc);
        this.datSvc = datSvc;
        this.aliasProc = new AliasProcessor(datSvc);
        this.options = deliaOptions;

        //each type of HLD statement
        builderMap.put(HLD.SchemaHLDStatement.class, new SchemaLLDProcessor());
        builderMap.put(HLD.TypeHLDStatement.class, new TypeLLDProcessor(datSvc));
        builderMap.put(HLD.InsertHLDStatement.class, new InsertLLDProcessor(factorySvc, datSvc));
        builderMap.put(HLD.LetHLDStatement.class, new LetLLDProcessor(factorySvc, aliasProc));
        builderMap.put(HLD.LetAssignHLDStatement.class, new LetAssignLLDProcessor());
        builderMap.put(HLD.ConfigureHLDStatement.class, new ConfigureLLDProcessor());
        builderMap.put(HLD.LogHLDStatement.class, new LogLLDProcessor());
        builderMap.put(HLD.DeleteHLDStatement.class, new DeleteLLDProcessor(factorySvc, datSvc));
        builderMap.put(HLD.UpdateHLDStatement.class, new UpdateLLDProcessor(factorySvc, datSvc));
        builderMap.put(HLD.UpsertHLDStatement.class, new UpsertLLDProcessor(factorySvc, datSvc));
    }

    public List<LLD.LLStatement> buildLLD(DeliaExecutable executable) {
        List<LLD.LLStatement> lldStatements = new ArrayList<>();
        DTypeRegistry registry = executable.registry;
        LLDBuilderContext ctx = createContext(registry, executable);

        //first create aliases
        List<HLD.HLDStatement> selects = executable.hldStatements.stream().filter(x -> x instanceof HLD.LetHLDStatement).collect(Collectors.toList());
        for (HLD.HLDStatement statement : selects) {
            aliasProc.initialAliasAssign((HLD.LetHLDStatement) statement);
        }

        for (HLD.HLDStatement hldStatement : executable.hldStatements) {
            LLDProcessor processor = builderMap.get(hldStatement.getClass());
            if (processor == null) {
                DeliaExceptionHelper.throwNotImplementedError("uknown HLD: %s", hldStatement.getClass());
            }
            ctx.loc = hldStatement.getLoc();
            try {
                processor.build(hldStatement, lldStatements, ctx);
            } catch (DeliaException e) {
                for(DeliaError err: e.getErrors()) {
                    if (err.getLoc() == null) {
                        err.setLoc(hldStatement.getLoc());
                    }
                }
                throw e;
            }
        }

        //and assoc tables
        createAssocTablesIfNeeded(ctx.assocMap, lldStatements, executable.dbType);

        //now assign alias for transitive joins
        for (HLD.HLDStatement statement : selects) {
            aliasProc.secondaryAliasAssign((HLD.LetHLDStatement) statement);
        }
        //now assign alias across fields, whereClause, etc
        for (LLD.LLStatement stmt : lldStatements) {
            aliasProc.assignAliases(stmt, aliasProc);
        }
        aliasProc.dumpAliases();

        executable.lldStatements = lldStatements;
        return lldStatements;
    }

    private void createAssocTablesIfNeeded(Map<String, RelationInfo> assocMap, List<LLD.LLStatement> lldStatements, DBType dbType) {
        if (DBType.MEM.equals(dbType) && !options.generateSqlWhenMEMDBType) {
            return; //no assoc tables for MEM
        }
        for(String key: assocMap.keySet()) {
            RelationInfo relinfo = assocMap.get(key);
            AssocSpec assocSpec = datSvc.findAssocInfo(relinfo);
            LLD.LLCreateAssocTable assocTable = new LLD.LLCreateAssocTable(null);
            assocTable.assocSpec = assocSpec;
            lldStatements.add(assocTable);
        }
    }

    private LLDBuilderContext createContext(DTypeRegistry registry, DeliaExecutable executable) {
        LLDBuilderContext ctx = new LLDBuilderContext();
        ctx.registry = registry;
        ctx.valueBuilder = new ScalarValueBuilder(factorySvc, registry);
        ctx.datSvc = datSvc;
        ctx.dbType = executable.dbType;
        return ctx;
    }
}
