package org.delia.dbimpl;

import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.type.DValue;
import org.delia.runner.SimpleDValueBuilder;
import org.delia.runner.StatementBuilderPlugin;

import java.util.Arrays;

public class PersonStatementBuilderPlugin implements StatementBuilderPlugin {
    private SimpleDValueBuilder dvalBuilder;

    public PersonStatementBuilderPlugin(FactoryService factorySvc) {
        dvalBuilder = new SimpleDValueBuilder(factorySvc);
    }

    @Override
    public AST.DeliaScript process(AST.DeliaScript script) {
        script.add(new AST.SchemaAst("alpha"));

        AST.TypeAst type = new AST.TypeAst("Person");
        type.baseName = "struct";
        AST.TypeFieldAst field = new AST.TypeFieldAst("id");
        field.isPrimaryKey = true;
        field.typeName = "int";
        type.fields.add(field);
        field = new AST.TypeFieldAst("firstName");
        field.isOptional = true;
        field.typeName = "string";
        type.fields.add(field);
        script.add(type);

        AST.InsertStatementAst ins = new AST.InsertStatementAst();
        ins.typeName = "Person";
        ins.fields = Arrays.asList(buildInsertFieldInt("id", "7"), buildInsertField("firstName", "bob"));
        script.add(ins);
        return script;
    }

    private AST.InsertFieldStatementAst buildInsertFieldInt(String fieldName, String s) {
        AST.InsertFieldStatementAst field = new AST.InsertFieldStatementAst();
        field.fieldName = fieldName;
        Exp.ValueExp valueExp = new Exp.ValueExp();
        valueExp.value = buildDValueInt(s);
        field.valueExp = valueExp;
        return field;
    }

    private AST.InsertFieldStatementAst buildInsertField(String fieldName, String s) {
        AST.InsertFieldStatementAst field = new AST.InsertFieldStatementAst();
        field.fieldName = fieldName;
        Exp.ValueExp valueExp = new Exp.ValueExp();
        valueExp.value = buildDValueString(s);
        field.valueExp = valueExp;
        return field;
    }

    private DValue buildDValueString(String s) {
        return dvalBuilder.buildDValueString(s);
    }

    private DValue buildDValueInt(String s) {
        return dvalBuilder.buildDValueInt(s);
    }


}
