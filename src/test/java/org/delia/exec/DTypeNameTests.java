package org.delia.exec;

import org.delia.type.DTypeName;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DTypeNameTests extends DeliaRunnerTestBase {

    @Test
    public void test() {
        DTypeName typeName1 = new DTypeName(null, "abc");
        DTypeName typeName2 = new DTypeName(null, "abc");
        assertEquals(true, typeName1.equals(typeName2));

        DTypeName typeName3 = new DTypeName(null, "abcd");
        assertEquals(false, typeName1.equals(typeName3));

        DTypeName typeName1a = new DTypeName("s2", "abc");
        DTypeName typeName2a = new DTypeName("s2", "abc");
        assertEquals(true, typeName1a.equals(typeName2a));

        assertEquals(false, typeName1.equals(typeName1a));
    }

    @Test
    public void test2() {
        DTypeName typeName1 = new DTypeName(null, "abc");
        DTypeName typeName2 = new DTypeName(null, "abc");
        assertEquals(true, typeName1.equals(typeName2));

        Map<DTypeName, String> map = new HashMap<>();
        map.put(typeName1, "s");
        map.put(typeName2, "t");
        assertEquals(1, map.size());
        assertEquals("t", map.get(typeName1));

        DTypeName typeName1a = new DTypeName("s2", "abc");
        map.put(typeName1a, "u");
        assertEquals(2, map.size());
        assertEquals("u", map.get(typeName1a));
    }


    //---

    @Before
    public void init() {
    }

}
