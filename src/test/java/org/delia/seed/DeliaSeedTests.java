package org.delia.seed;


import org.delia.app.DaoTestBase;
import org.delia.other.StringTrail;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class DeliaSeedTests extends DaoTestBase {

    public static class SbError {
        private String id;
        private String msg;

        public SbError(String id, String msg) {
            this.id = id;
            this.msg = msg;
        }
    }

    public static class SdException extends RuntimeException {
        public SdException(String msg) {
            super(msg);
        }
    }

    public interface SdAction {
        String getName();

        String getTable();
    }

    public static class SdExistAction implements SdAction {
        private String name;
        private String table;
        //private String whereClause;

        public SdExistAction() {
            name = "exist";
        }
        public SdExistAction(String table) {
            this();
            this.table = table;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTable() {
            return table;
        }
    }

    public static class SdScript {
        private List<SdAction> actions = new ArrayList<>();

        public List<SdAction> getActions() {
            return actions;
        }

        public void addAction(SdExistAction action) {
            actions.add(action);
        }
    }

    public static class SdExecutionResults {
        public boolean ok;
        public List<SbError> errors = new ArrayList<>();
    }

    public interface SdExecutor {
        SdExecutionResults execute(SdScript script);
    }

    public static class MyExecutor implements SdExecutor {
        public StringTrail trail = new StringTrail();

        @Override
        public SdExecutionResults execute(SdScript script) {
            SdExecutionResults res = new SdExecutionResults();
            for (SdAction action : script.getActions()) {
                trail.add(action.getName());
            }

            res.ok = true;
            return res;
        }
    }

    //-- valid
    public interface DBInterface {
        boolean tableExists(String table);
    }

    public static class SdValidationResults {
        public boolean ok;
        public List<SbError> errors = new ArrayList<>();
    }

    public interface SdValidator {
        SdValidationResults validate(SdScript script);
    }

    public static class MyValidator implements SdValidator {
        private final DBInterface dbInterface;
        public StringTrail trail = new StringTrail();

        public MyValidator(DBInterface dbInterface) {
            this.dbInterface = dbInterface;
        }

        @Override
        public SdValidationResults validate(SdScript script) {
            SdValidationResults res = new SdValidationResults();

            for (SdAction action : script.getActions()) {
                trail.add(action.getName());
                switch (action.getName()) {
                    case "exist":
                        validateExist(action, res);
                        break;
                    default:
                        throw new SdException("unknown action: " + action.getName());
                }
            }

            res.ok = true;
            return res;
        }

        private void validateExist(SdAction action, SdValidationResults res) {
            if (!dbInterface.tableExists(action.getTable())) {
                res.errors.add(new SbError("unknown-table", String.format("unknown table: '%s'", action.getTable())));
            }
        }
    }

    public static class MyDBInterface implements DBInterface {
        public String knownTables;

        @Override
        public boolean tableExists(String table) {
            return knownTables.contains(table);
        }
    }

    @Test
    public void testEmpty() {
        executor = new MyExecutor();
        SdScript script = new SdScript();
        SdExecutionResults res = executor.execute(script);
        chkOk(res, "");
    }

    @Test
    public void testOne() {
        executor = new MyExecutor();
        SdScript script = new SdScript();
        script.addAction(new SdExistAction());

        SdExecutionResults res = executor.execute(script);
        chkOk(res, "exist");
    }

    @Test
    public void testValidate() {
        SdScript script = new SdScript();
        script.addAction(new SdExistAction("Customer"));

        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script);
        chkValOk(res, "exist");
    }

    @Test
    public void testValidateUnknownTable() {
        SdScript script = new SdScript();
        script.addAction(new SdExistAction("Customer"));

        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Address";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script);
        chkValOk(res, "exist");
    }

    //---
    private MyExecutor executor;
    private MyValidator validator;

    @Before
    public void init() {
    }

    private void chkOk(SdExecutionResults res, String trail) {
        assertEquals(true, res.ok);
        assertEquals(trail, executor.trail.getTrail());
    }

    private void chkValOk(SdValidationResults res, String trail) {
        assertEquals(true, res.ok);
        assertEquals(trail, validator.trail.getTrail());
    }
}
