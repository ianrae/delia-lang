package org.delia.lld;

import org.delia.DeliaOptions;
import org.delia.ast.*;
import org.delia.hld.DeliaExecutable;
import org.delia.ast.code.HLDTestHelper;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.lld.processor.LLDBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * LLD - Low-level description
 * -represents the SQL elements that delia uses
 * -LLD knows nothing about HLD
 * -the idea is that LL can render every type of SQL that we need
 */
public class LLDTests extends TestBase {


    @Test
    public void test() {
        assertEquals(1, 1);
        DeliaExecutable executable = parseIntoHLD();

        SyntheticDatService datSvc = new SyntheticDatService();
        LLDBuilder builder = new LLDBuilder(factorySvc, datSvc, new DeliaOptions());
        builder.buildLLD(executable);
        dumpLL(executable.lldStatements);

    }

    //---

    @Before
    public void init() {
        super.init();
    }

    private DeliaExecutable parseIntoHLD() {
        return HLDTestHelper.parseIntoHLD(factorySvc, new DeliaOptions());
    }

    private void dumpLL(List<LLD.LLStatement> statements) {
        log.log("--LL--");
        for (LLD.LLStatement hld : statements) {
            log.log(hld.toString());
        }
        log.log("--LL end.--");
    }

}
