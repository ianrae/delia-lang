package org.delia.seed;


import org.delia.runner.ResultValue;
import org.delia.scope.scopetest.relation.DeliaClientTestBase;
import org.delia.seed.code.MyEntity;
import org.delia.seed.code.SeedDValueBuilder;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class SeedDValueBuilderTests extends DeliaClientTestBase {

    @Test
    public void testData() {
        createCustomerType();
        if (true) {
            execStatement("insert Customer {id: 44, firstName:'bob'}");
            ResultValue res = this.execStatement("let x = Customer[true]");
            assertEquals(true, res.ok);
            DValue dval = res.getAsDValue();
            assertEquals("bob", dval.asStruct().getField("firstName").asString());
            assertEquals(44, dval.asStruct().getField("id").asInt());
        }

        MyEntity entity = new MyEntity();
        entity.fieldMap.put("id", 45);
        entity.fieldMap.put("firstName", "sue");
        String typeName = "Customer";

        SeedDValueBuilder builder = new SeedDValueBuilder(sess, typeName);
        DValue dval = builder.buildFromEntityEx(entity, typeName);
    }


    //---

    @Before
    public void init() {
        super.init();
        enableAutoCreateTables();
    }


    private String createCustomerSrc() {
        String src = String.format("type %s struct { id int primaryKey, firstName string} end", "Customer");
        src += "\n";
        return src;
    }

    private void createCustomerType() {
        String src = createCustomerSrc();
        execTypeStatement(src);
        DTypeRegistry registry = sess.getExecutionContext().registry;
        DStructType dtype = (DStructType) registry.getType("Customer");
    }


}
