package org.delia.compiler.ast;

import org.delia.error.ErrorFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Holder for AST classes
 */
public class AST {
    public static class Loc {
        public int lineNum;
        public int charOffset;
        public int length;
    }
    public interface StatementAst {
        Loc getLoc();
    }


    public static class StatementBaseAst implements StatementAst {
        public Loc loc;

        public Loc getLoc() { return loc; }
    }

    public static class SchemaAst extends StatementBaseAst {
        public String schemaName;

        public SchemaAst(String name) {
            this.schemaName = name;
        }
    }

    public static class TypeFieldAst {
        public Loc loc;
        public String fieldName;
        public String schemaName;
        public String typeName;
        public boolean isOptional;
        public boolean isPrimaryKey;
        public boolean isUnique;
        public boolean isSerial;

        public boolean isRelation;
        public boolean isOne;
        public boolean isMany;
        public boolean isParent;
        public String relationName;

        public TypeFieldAst(String name) {
            this.fieldName = name;
        }
    }

    public static class TypeAst extends StatementBaseAst {
        public String baseSchemaName;
        public String baseName;
        public String schemaName;
        public String typeName;
        public boolean isScalarType;
        public List<TypeFieldAst> fields = new ArrayList<>();
        public List<Exp.RuleClause> rules = new ArrayList<>();
        public String dbTblName;

        public TypeAst(String name) {
            this.typeName = name;
        }
    }

    public static class LetStatementAst extends StatementBaseAst {
        public String varName;
        public String schemaName;
        public String typeName;
        public Exp.WhereClause whereClause;
        public Exp.DottedExp fieldAndFuncs;
        public Exp.ElementExp scalarElem;
    }

    public static class DeleteStatementAst extends StatementBaseAst {
        public String schemaName;
        public String typeName;
        public Exp.WhereClause whereClause;
    }

    public static class UpdateStatementAst extends StatementBaseAst {
        public String schemaName;
        public String typeName;
        public Exp.WhereClause whereClause;
        public List<InsertFieldStatementAst> fields = new ArrayList<>();
    }

    public static class UpsertStatementAst extends StatementBaseAst {
        public String schemaName;
        public String typeName;
        public Exp.WhereClause whereClause;
        public List<InsertFieldStatementAst> fields = new ArrayList<>();
        public boolean noUpdateFlag;
    }

    public static class InsertStatementAst extends StatementBaseAst {
        public String schemaName;
        public String typeName;
        public List<InsertFieldStatementAst> fields = new ArrayList<>();
    }

    //used for insert, update, upsert statements
    public static class InsertFieldStatementAst extends StatementBaseAst {
        public String fieldName;
        public Exp.ValueExp valueExp; //if set then ignore listExp
        public Exp.ListExp listExp;  //if set then ignore valueExp
        public Exp.FieldExp varExp;
        public String crudAction; //only allowed on update
    }

    public static class ConfigureStatementAst extends StatementBaseAst {
        public String configName;
        public Exp.ElementExp scalarElem;

        public ConfigureStatementAst(String name) {
            this.configName = name;
        }
    }

    public static class LogStatementAst extends StatementBaseAst {
        public String varName;
        public Exp.ElementExp scalarElem;

        public LogStatementAst(String name) {
            this.varName = name;
        }
    }

    public static class DeliaScript {
        public List<StatementAst> statements = new ArrayList<>();
        public ErrorFormatter errorFormatter; //source-aware error formmater

        public void add(StatementAst stmt) {
            statements.add(stmt);
        }
    }

}
