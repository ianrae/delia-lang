package org.delia.exec;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.compiler.ast.AST;
import org.delia.db.DBType;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.DeliaExecutable;
import org.delia.log.LogLevel;
import org.delia.runner.DeliaRunnerImpl;
import org.delia.sort.topo.DeliaTypeSorter;
import org.delia.type.DType;
import org.delia.type.DTypeName;
import org.delia.type.DTypeRegistry;
import org.delia.util.TextFileReader;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TopoTests extends DeliaRunnerTestBase {


    @Test
    public void test() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;

        //MM relation when both are optional has no preferred sorting, but we can't preserve decl order
        String src = "type Customer struct {\n" +
                "  id int primaryKey,\n" +
                "  firstName string optional,\n" +
                "  relation addr Address many optional\n" +
                "} end\n" +
                "type Address struct {\n" +
                "  id int primaryKey,\n" +
                "  city string optional,\n" +
                "  relation cust Customer many optional\n" +
                "} end";

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        DeliaSession sess = deliaRunner.execute(executable);
        DTypeName dTypeName = new DTypeName(null, "Customer");
        DType dtype = sess.getRegistry().getType(dTypeName);
        assertEquals("Customer", dtype.getName());

        DeliaTypeSorter sorter = new DeliaTypeSorter();
        List<DTypeName> types = sorter.topoSort(sess.getRegistry(), log);
        assertEquals("Address", types.get(0).getTypeName());
        assertEquals("Customer", types.get(1).getTypeName());
    }

    @Test
    public void test2() {
        DeliaRunnerImpl deliaRunner = createRunner(DBType.MEM);
        Delia delia = deliaRunner.getDelia();
        this.delia = delia;
        log.setLevel(LogLevel.DEBUG);

        String src = readSrc("topo-sort1.delia");

        ErrorTracker localET = new SimpleErrorTracker(delia.getLog());
        AST.DeliaScript script = deliaRunner.compile(src, localET);
        assertEquals(0, localET.errorCount());

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        DeliaSession sess = deliaRunner.execute(executable);

        DTypeName type1 = new DTypeName(null, "Project");
        DTypeName type2 = new DTypeName(null, "User");
        DeliaTypeSorter sorter = new DeliaTypeSorter();
        sorter.addCustomDependency(type1, type2);
        List<DTypeName> types = sorter.topoSort(sess.getRegistry(), log);
        types.forEach(x -> log(" " + x.toString()));

        List<String> list = types.stream().map(x -> x.toString()).collect(Collectors.toList());
        int i1 = list.indexOf("Project");
        int i2 = list.indexOf("User");
        assertEquals(true, i2 < i1);
    }

    private String readSrc(String filename) {
        String BASE_DIR = "./src/test/resources/test/";
        TextFileReader r = new TextFileReader();
        String s = r.readFileAsSingleString(BASE_DIR + filename);
        return s;
    }

    //---

    @Before
    public void init() {
    }

}
