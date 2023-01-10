package org.delia.hld;

import org.apache.commons.lang3.StringUtils;
import org.delia.DeliaOptions;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.core.ConfigureServiceImpl;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBType;
import org.delia.error.DeliaError;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.dat.DatService;
import org.delia.runner.DeliaException;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//TODO need multiple passes: 1st pass just builds types
public class HLDBuilder extends ServiceBase {
    private final DeliaOptions options;
    private final String defaultSchema;
    private Map<Class, HLDStatementBuilder> builderMap = new HashMap<>();
    private final DatService datSvc;

    public HLDBuilder(FactoryService factorySvc, DatService datSvc, DeliaOptions options, String defaultSchema) {
        super(factorySvc);
        this.datSvc = datSvc;
        this.options = options;
        this.defaultSchema = defaultSchema;

        //each type of AST statement
        builderMap.put(AST.SchemaAst.class, new SchemaHLDStatementBuilder());
        builderMap.put(AST.TypeAst.class, new TypeHLDStatementBuilder(options, log));
        builderMap.put(AST.LetStatementAst.class, new LetHLDStatementBuilder(factorySvc));
        builderMap.put(AST.InsertStatementAst.class, new InsertHLDStatementBuilder(factorySvc));
        builderMap.put(AST.DeleteStatementAst.class, new DeleteHLDStatementBuilder(factorySvc));
        builderMap.put(AST.UpdateStatementAst.class, new UpdateHLDStatementBuilder(factorySvc));
        builderMap.put(AST.UpsertStatementAst.class, new UpsertHLDStatementBuilder(factorySvc));
        builderMap.put(AST.ConfigureStatementAst.class, new ConfigureHLDStatementBuilder());
        builderMap.put(AST.LogStatementAst.class, new LogHLDStatementBuilder());
    }

    private Class getASTFor(HLD.HLDStatement statement) {
        if (statement instanceof HLD.SchemaHLDStatement) {
            return AST.SchemaAst.class;
        } else if (statement instanceof HLD.TypeHLDStatement) {
            return AST.TypeAst.class;
        } else if (statement instanceof HLD.LetHLDStatement) {
            return AST.LetStatementAst.class;
        } else if (statement instanceof HLD.InsertHLDStatement) {
            return AST.InsertStatementAst.class;
        } else if (statement instanceof HLD.ConfigureHLDStatement) {
            return AST.ConfigureStatementAst.class;
        } else if (statement instanceof HLD.LogHLDStatement) {
            return AST.LogStatementAst.class;
        } else if (statement instanceof HLD.DeleteHLDStatement) {
            return AST.DeleteStatementAst.class;
        } else if (statement instanceof HLD.UpdateHLDStatement) {
            return AST.UpdateStatementAst.class;
        } else if (statement instanceof HLD.UpsertHLDStatement) {
            return AST.UpsertStatementAst.class;
        } else {
            return null;
        }
    }

    public HLDFirstPassResults buildTypesOnly(AST.DeliaScript script) {
        return buildTypesOnly(script, null);
    }
    /*
     used to create registry and make type decl order not important
     */
    public HLDFirstPassResults buildTypesOnly(AST.DeliaScript script, DTypeRegistry existingRegistry) {
        HLDFirstPassResults results = new HLDFirstPassResults();
//        List<HLD.HLDStatement> hldStatements = new ArrayList<>();

        DTypeRegistry registry = null;
        if (existingRegistry == null) {
            //step 1. create basic registry (built-in types)
            DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
            registryBuilder.init();
            registry = registryBuilder.getRegistry();

            //step 2. pre-reg where we find all types and fill in as much as we can
            NewTypePreRunner preRunner = new NewTypePreRunner(factorySvc, registry, defaultSchema);
            List<DeliaError> allErrors = new ArrayList<>();
            preRunner.executeStatements(script.statements, allErrors);
            if (!allErrors.isEmpty()) {
                //something went wrong
                throw new DeliaException(allErrors);
            }

            //step 3. do actual type creation and populate registry
            NewTypeRunner typeRunner = new NewTypeRunner(factorySvc, registry);
            typeRunner.setPreRegistry(preRunner.getPreRegistry());
            typeRunner.executeStatements(script.statements, allErrors, true);
            registry = typeRunner.getRegistry();

            if (!allErrors.isEmpty()) {
                //something went wrong
                throw new DeliaException(allErrors);
            }
        } else {
            registry = existingRegistry;
        }

        //step 3. init joinInfo in FieldExp
        List<AST.StatementAst> letAsts = script.statements.stream().filter(x -> x instanceof AST.LetStatementAst).collect(Collectors.toList());
        for (AST.StatementAst stmt : letAsts) {
            AST.LetStatementAst letAst = (AST.LetStatementAst) stmt;
            if (letAst.scalarElem != null) {
                continue;
            }

            MyFieldVisitor visitor = new MyFieldVisitor();
            visitor.onlyJoinFields = true;
            letAst.whereClause.visit(visitor);
            for (Exp.FieldExp field : visitor.allFields) {
                Exp.JoinInfo jinfo = field.joinInfo;
                log.log("  joinInfo: %s and %s", jinfo.leftTypeName, jinfo.rightTypeName);
                jinfo.leftType = (DStructType) registry.getType(jinfo.leftTypeName);
                jinfo.rightType = (DStructType) registry.getType(jinfo.rightTypeName);
            }

        }

        //step 4. replace error converter with a registry aware one (better at parsing errors)
//            DBErrorConverter errorConverter = mainDBInterface.getDBErrorConverter();
//            if (errorConverter != null) {
////		RegistryAwareDBErrorConverter radbec = new RegistryAwareDBErrorConverter(errorConverter, registry);
////		dbInterface.setDBErrorConverter(radbec);
//                errorConverter.setRegistry(registry);
//            }

        results.registry = registry;
        return results;
    }

    public Map<String,String> gatherSyntheticIds(AST.DeliaScript script, DTypeRegistry existingRegistry) {
        Map<String,String> map = new HashMap<>();
        List<AST.StatementAst> configureASTs = script.statements.stream().filter(x -> x instanceof AST.ConfigureStatementAst).collect(Collectors.toList());
        for (AST.StatementAst stmt : configureASTs) {
            AST.ConfigureStatementAst configAst = (AST.ConfigureStatementAst) stmt;
            if (configAst.configName.endsWith(ConfigureServiceImpl.SYNTHETIC_IDS_TARGET)) {
                String typeName = StringUtils.substringBefore(configAst.configName, ".");
                map.put(typeName, configAst.scalarElem.strValue());
            }
        }
        return map;
    }

    private HLDBuilderContext createContext(DTypeRegistry registry, DBType dbType, Map<String, String> syntheticIdMap) {
        HLDBuilderContext ctx = new HLDBuilderContext();
        ctx.registry = registry;
        ctx.valueBuilder = new ScalarValueBuilder(factorySvc, registry);
        ctx.datSvc = datSvc;
        ctx.localET = new SimpleErrorTracker(log);
        ctx.dbType = dbType;
        ctx.syntheticIdMap = syntheticIdMap;
        return ctx;
    }

    public List<HLD.HLDStatement> buildHLD(AST.DeliaScript script, DTypeRegistry registry, DBType dbType, Map<String, String> syntheticIdMap) {
        List<HLD.HLDStatement> hldStatements = new ArrayList<>();
        HLDBuilderContext ctx = createContext(registry, dbType, syntheticIdMap);

        for (AST.StatementAst statement : script.statements) {
            HLDStatementBuilder builder = builderMap.get(statement.getClass());
            if (builder == null) {
                DeliaExceptionHelper.throwNotImplementedError("unknown-ast: %s", statement.getClass().getSimpleName());
            }

            //most builders will add loc, but add here if not yet set
            int currentSize = hldStatements.size();
            ctx.loc = statement.getLoc();

            try {
                builder.build(statement, hldStatements, ctx); //build the statement(s)
            } catch (DeliaException e) {
                for(DeliaError err: e.getErrors()) {
                    if (err.getLoc() == null) {
                        err.setLoc(statement.getLoc());
                    }
                }
                throw e;
            }

            for(int i = currentSize; i < hldStatements.size(); i++) {
                HLD.HLDStatement stmt = hldStatements.get(i);
                if (stmt.getLoc() == null) {
                    if (stmt instanceof HLD.HLDStatementBase) {
                        HLD.HLDStatementBase baseStmt = (HLD.HLDStatementBase) stmt;
                        baseStmt.loc = statement.getLoc();
                    }
                }
            }
        }

        if (ctx.localET.errorCount() > 0) {
            throw new DeliaException(ctx.localET.getFirstError());
        }

        return hldStatements;
    }

    public void assignDATs(List<HLD.HLDStatement> hldStatements, DTypeRegistry registry, DBType dbType) {
        HLDBuilderContext ctx = createContext(registry, dbType, null);

        //now that we have all HLD statements, let's add DAT info
        for (HLD.HLDStatement stmt : hldStatements) {
            Class clazz = getASTFor(stmt);
            if (clazz == null) continue;
            HLDStatementBuilder builder = builderMap.get(clazz);
            builder.assignDATs(stmt, hldStatements, ctx);
        }
    }
}
