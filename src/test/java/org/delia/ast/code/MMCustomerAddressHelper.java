package org.delia.ast.code;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.dbimpl.ExpTestHelper;

import java.util.Arrays;

public class MMCustomerAddressHelper extends ServiceBase {

    private final ExpTestHelper expHelper;

    public MMCustomerAddressHelper(FactoryService factorySvc) {
        super(factorySvc);
        this.expHelper = new ExpTestHelper(factorySvc);
    }

    public AST.DeliaScript buildScriptStart(ScalarValueBuilder scalarBuilder) {
        return buildScriptStart(scalarBuilder, true);
    }
    public AST.DeliaScript buildScriptStart(ScalarValueBuilder scalarBuilder, boolean addSchema) {
        AST.DeliaScript script = new AST.DeliaScript();
        if (addSchema) {
            script.add(new AST.SchemaAst("alpha"));
        }

        AST.TypeAst type = buildCustomer();
        script.add(type);
        type = buildAddress();
        script.add(type);

        AST.InsertStatementAst ins = new AST.InsertStatementAst();
        ins.typeName = "Customer";
//        ins.fields = Arrays.asList(expHelper.buildInsertField("id", "7"), expHelper.buildInsertField("firstName", "bob"), null); //TODO: later support that you don't need to pass null for addr
        ins.fields = Arrays.asList(expHelper.buildInsertFieldInt("id", 7),
                expHelper.buildInsertField("firstName", "bob"));

        script.add(ins);

        ins = new AST.InsertStatementAst();
        ins.typeName = "Address";
        ins.fields = Arrays.asList(expHelper.buildInsertFieldInt("id", 100), expHelper.buildInsertField("city", "toronto"), expHelper.buildInsertFieldInt("cust", 7));
        script.add(ins);

        return script;
    }

    private static AST.TypeAst buildCustomer() {
        AST.TypeAst type = new AST.TypeAst("Customer");
        type.baseName = "struct";
        AST.TypeFieldAst field = new AST.TypeFieldAst("id");
        field.isPrimaryKey = true;
        field.typeName = "int";
        type.fields.add(field);
        field = new AST.TypeFieldAst("firstName");
        field.isOptional = true;
        field.typeName = "string";
        type.fields.add(field);
        field = new AST.TypeFieldAst("addr");
        field.isOptional = true;
        field.typeName = "Address";
        field.isParent = field.isRelation = true;
        field.isOne = false;
        field.isMany = true;

        type.fields.add(field);
        return type;
    }

    private static AST.TypeAst buildAddress() {
        AST.TypeAst type = new AST.TypeAst("Address");
        type.baseName = "struct";
        AST.TypeFieldAst field = new AST.TypeFieldAst("id");
        field.isPrimaryKey = true;
        field.typeName = "int";
        type.fields.add(field);
        field = new AST.TypeFieldAst("city");
        field.isOptional = true;
        field.typeName = "string";
        type.fields.add(field);
        field = new AST.TypeFieldAst("cust");
        field.isOptional = true;
        field.typeName = "Customer";
        field.isRelation = true;
        field.isOne = false;
        field.isMany = true;
        type.fields.add(field);
        return type;
    }

}
