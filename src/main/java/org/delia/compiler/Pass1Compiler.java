package org.delia.compiler;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.db.DBCapabilties;
import org.delia.db.DBType;
import org.delia.error.ErrorFormatter;
import org.delia.hld.HLDFirstPassResults;
import org.delia.type.DTypeName;

import java.util.ArrayList;
import java.util.List;

/**
 * assign schema
 *
 * @author ian
 */
public class Pass1Compiler extends CompilerPassBase {

    private final String defaultSchema;
    private List<String> syntheticIds = new ArrayList<>();
    private String currentSchema = null;

    public Pass1Compiler(FactoryService factorySvc, HLDFirstPassResults firstPassResults, DBType dbType, ErrorFormatter errorFormatter, String defaultSchema) {
        super(factorySvc, firstPassResults, dbType, errorFormatter);
        this.defaultSchema = defaultSchema;
    }

    public CompilerPassResults process(AST.DeliaScript script, String currentSchema) {
        CompilerPassResults results = new CompilerPassResults();
        this.currentSchema = calcSchema(currentSchema);

        for (AST.StatementAst statement : script.statements) {
            if (statement instanceof AST.SchemaAst) {
                AST.SchemaAst exp = (AST.SchemaAst) statement;
                this.currentSchema = calcSchema(exp.schemaName);
            } else if (statement instanceof AST.TypeAst) {
                AST.TypeAst typeExp = (AST.TypeAst) statement;
                //pretyperegistry already set schema
            } else if (statement instanceof AST.LetStatementAst) {
                AST.LetStatementAst letAST = (AST.LetStatementAst) statement;
                DTypeName dtypeName = extractTypeNameAndSchema(letAST.schemaName, letAST.typeName);
                letAST.typeName = dtypeName.getTypeName();
                letAST.schemaName = dtypeName.getSchema();
            } else if (statement instanceof AST.ConfigureStatementAst) {
                AST.ConfigureStatementAst configAST = (AST.ConfigureStatementAst) statement;
                checkConfigureStatement(results, configAST);
                //TODO fix configAST.schemaName = currentSchema;
            } else if (statement instanceof AST.UpdateStatementAst) {
                AST.UpdateStatementAst upExp = (AST.UpdateStatementAst) statement;
                DTypeName dtypeName = extractTypeNameAndSchema(upExp.schemaName, upExp.typeName);
                upExp.typeName = dtypeName.getTypeName();
                upExp.schemaName = dtypeName.getSchema();
            } else if (statement instanceof AST.UpsertStatementAst) {
                AST.UpsertStatementAst upExp = (AST.UpsertStatementAst) statement;
                DTypeName dtypeName = extractTypeNameAndSchema(upExp.schemaName, upExp.typeName);
                upExp.typeName = dtypeName.getTypeName();
                upExp.schemaName = dtypeName.getSchema();
            } else if (statement instanceof AST.DeleteStatementAst) {
                AST.DeleteStatementAst delAST = (AST.DeleteStatementAst) statement;
                DTypeName dtypeName = extractTypeNameAndSchema(delAST.schemaName, delAST.typeName);
                delAST.typeName = dtypeName.getTypeName();
                delAST.schemaName = dtypeName.getSchema();
            } else if (statement instanceof AST.InsertStatementAst) {
                AST.InsertStatementAst insAST = (AST.InsertStatementAst) statement;
                DTypeName dtypeName = extractTypeNameAndSchema(insAST.schemaName, insAST.typeName);
                insAST.typeName = dtypeName.getTypeName();
                insAST.schemaName = dtypeName.getSchema();
            }
        }
        return results;
    }

    private String calcSchema(String schema) {
        if (defaultSchema != null && defaultSchema.equals(schema)) {
            return null;
        }
        return schema;
    }

    private DTypeName extractTypeNameAndSchema(String schemaName, String typeName) {
        if (schemaName != null) {
            return new DTypeName(schemaName, typeName);
        }
        if (typeName != null && typeName.contains(".")) {
            String[] ar = typeName.split("\\.");
            //s2.Customer
            //TODO assume only two for now. fix later.
            String schema = calcSchema(ar[0]);
            return new DTypeName(schema, ar[1]);
        }
        return new DTypeName(currentSchema, typeName);
    }


    private void checkConfigureStatement(CompilerPassResults results, AST.ConfigureStatementAst statement) {
        String[] ar = statement.configName.split("\\.");
        if (ar.length == 2 && currentSchema != null) {
            statement.configName = String.format("%s.%s.%s", currentSchema, ar[0], ar[2]);
        }
    }
}