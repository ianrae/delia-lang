package org.delia.seed;


import org.delia.app.DaoTestBase;
import org.delia.other.StringTrail;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class DeliaSeedTests extends DaoTestBase {

    public interface SdAction {

        String getName();
    }

    public static class SdExistAction implements SdAction {
        private String name;
        private String table;
        //private String whereClause;

        @Override
        public String getName() {
            return "exist";
        }

    }

    public static class SdScript {
        private List<SdAction> actions = new ArrayList<>();

        public List<SdAction> getActions() {
            return actions;
        }
    }

    public static class SdExecutionResults {
        public boolean ok;
    }

    public interface SdExecutor {
        SdExecutionResults execute(SdScript script);
    }

    public static class MyExecutor implements  SdExecutor {
        public StringTrail trail = new StringTrail();

        @Override
        public SdExecutionResults execute(SdScript script) {
            SdExecutionResults res = new SdExecutionResults();
            for(SdAction action: script.getActions()) {
                trail.add(action.getName());
            }

            res.ok = true;
            return res;
        }
    }


    @Test
    public void test1() {
        MyExecutor executor = new MyExecutor();
        SdScript script = new SdScript();

        SdExecutionResults res = executor.execute(script);
        assertEquals(true, res.ok);

    }

    //---

    @Before
    public void init() {
    }

}
