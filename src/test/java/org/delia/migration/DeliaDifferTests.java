package org.delia.migration;

import org.delia.exec.DeliaRunnerTestBase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/*
 TODO
 -delia differ. compare two delia srcs. produce SchemaMigration
 -generate from migration (ALTERATIONS,ADDITIONS)
   -compile current src (delia1)
   -apply alterations and add additions -> delia2
   -compile delia2. compare LLCreateTable and LLCreateAssocTable with delia1 to find the additional tables
     -that is. LLCreateTable and LLCreateAssocTable in delia1 and apply alterations so any renames are don
       into a local list of typenames
       -then compare with delia2. if exist in delia2 but not in 1 then is an addition.
 -SQL generator. generate sql statements for actions

 */
public class DeliaDifferTests extends DeliaRunnerTestBase {


    @Test
    public void test() {
        String src = "type Customer struct {id int primaryKey, wid int, name string } wid.maxlen(4) end";
        DeliaDiffer differ = new DeliaDiffer(log);
        SchemaMigration schemaMigration = differ.compare(src, src);
        assertEquals(0, schemaMigration.actions.size());
    }

    @Test
    public void test1() {
        String src = "type Customer struct {id int primaryKey, wid int, name string } wid.maxlen(4) end";
        String src2 = src + "\ntype Address struct {id int primaryKey, wid int}  end";
        DeliaDiffer differ = new DeliaDiffer(log);
        SchemaMigration schemaMigration = differ.compare(src, src2);
        assertEquals(1, schemaMigration.actions.size());
    }

    @Test
    public void test2() {
        String src = "type Customer struct {id int primaryKey, wid int, name string } wid.maxlen(4) end";
        String src2 = " type Customer struct {id int primaryKey, wid int, name string, relation addr Address optional many }wid.maxlen(4) end\n" +
                " type Address struct {id int primaryKey, relation cust Customer optional many } end\n";
        DeliaDiffer differ = new DeliaDiffer(log);
        SchemaMigration schemaMigration = differ.compare(src, src2);
        assertEquals(3, schemaMigration.actions.size());
    }

    @Test
    public void test3() {
        String src = "type Customer struct {id int primaryKey, wid int, name string } wid.maxlen(4) end";
        String src2 = "type Customer struct {id int primaryKey, wid2 int, name string } wid2.maxlen(4) end";;
        DeliaDiffer differ = new DeliaDiffer(log);
        SchemaMigration schemaMigration = differ.compare(src, src2);
        assertEquals(2, schemaMigration.actions.size());
    }
    //---

    @Before
    public void init() {
    }
}