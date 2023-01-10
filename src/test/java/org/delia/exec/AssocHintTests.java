package org.delia.exec;

import org.delia.compiler.ast.AST;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.dat.AssocSpec;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AssocHintTests extends DeliaRunnerTestBase {


    @Test
    public void testAssocName() {
        String src = " type Address struct {\n" +
                "  id int primaryKey,\n" +
                "  city string optional,\n" +
                "  relation cust Customer many optional\n" +
                "} end\n" +
                "type Customer struct {\n" +
                "  id int primaryKey,\n" +
                "  firstName string optional,\n" +
                "  relation addr Address many optional\n" +
                "} end\n";
        String src2 = "\nlet x = Customer[10]";


        Tok.WhereTok whereTok = buildWhere(src + src2, 2);
        assertEquals("10", whereTok.strValue());

        delia.getOptions().assocHints.add("Customer:addr:Address");
        AST.DeliaScript script = compileSrc(src + src2);
        DeliaExecutable executable = deliaRunner1.buildExecutable(script);

        assertEquals(1, executable.datSvc.findAll().size());
        AssocSpec assocSpec = executable.datSvc.findAll().get(0);
        assertEquals("CustomerAddressDat1", assocSpec.assocTblName);

        DTypeName dtypeName = new DTypeName(null, "Flight");
        DStructType structType = executable.registry.getStructType(dtypeName);
    }

}
