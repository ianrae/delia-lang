package org.delia.exec;

import org.delia.compiler.ast.AST;
import org.delia.hld.DeliaExecutable;
import org.delia.lld.processor.TokFieldHintVisitor;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldHintVisitorTests extends DeliaRunnerTestBase {


    @Test
    public void testWhereOp() {
        String src = "type Flight struct {id int primaryKey, name string } end";
        String src2 = "\nlet x = Flight[id > 10]";
        Tok.WhereTok whereTok = buildWhere(src + src2);
        assertEquals("id > 10", whereTok.strValue());

        AST.DeliaScript script = compileSrc(src + src2);
        DeliaExecutable executable = deliaRunner1.buildExecutable(script);

        DTypeName dtypeName = new DTypeName(null, "Flight");
        DStructType structType = executable.registry.getStructType(dtypeName);
        TokFieldHintVisitor visitor = new TokFieldHintVisitor(structType, true);
        whereTok.visit(visitor, null);
    }

}
