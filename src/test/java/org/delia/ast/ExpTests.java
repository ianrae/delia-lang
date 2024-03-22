package org.delia.ast;

import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.DeliaLog;
import org.delia.log.SimpleLog;
import org.delia.type.*;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Exp - filter expresssions
 * we'll use the same data structure in ast,hld,lld
 */
public class ExpTests {


    @Test
    public void test() {
        assertEquals(1, 1);

        DStructType assoc1 = buildTempDatType("CustomerAddressDat1");
        //and datId int
        //maybe DStructType.isRegistered. true if in registry, false otherwise (ie assoc type)

    }


    //---
    private DTypeRegistry registry;
    private ScalarValueBuilder scalarBuilder;
    private DeliaLog log;
    private FactoryService factorySvc;

    @Before
    public void init() {
        log = new SimpleLog();
        ErrorTracker et = new SimpleErrorTracker(log);
        factorySvc = new FactoryServiceImpl(log, et);
        DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
        registryBuilder.init();
        registry = registryBuilder.getRegistry();
        scalarBuilder = new ScalarValueBuilder(factorySvc, registry);
    }

//    private Exp.OperatorExp buildExp1() {
//        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
//        return expHelper.buildExp1(scalarBuilder);
//    }

    public DStructType buildTempDatType(String assocTbl) {
//        TypePair pkpair1 = DValueHelper.findPrimaryKeyFieldPair(relinfo.nearType);
//        TypePair pkpair2 = DValueHelper.findPrimaryKeyFieldPair(relinfo.farType);

        DType intType = registry.getType(BuiltInTypes.INTEGER_SHAPE);
        TypePair pkpair1 = new TypePair("leftv", intType);
        TypePair pkpair2 = new TypePair("rightv", intType);

        boolean flipped = false;//TODO fix datIdMap.isFlipped(relinfo);
        if (flipped) { //swap
            TypePair tmp = pkpair1;
            pkpair1 = pkpair2;
            pkpair2 = tmp;
        }

        OrderedMap omap = new OrderedMap();
        //normally not optional but sometimes for MERGE INTO it is
        omap.add("leftv", pkpair1.type, true, false, false, false, null);
        omap.add("rightv", pkpair2.type, true, false, false, false, null);
        DStructType structType = new DStructTypeImpl(Shape.STRUCT, null, assocTbl, null, omap, null);
        //we don't register this type
        return structType;
    }

}
