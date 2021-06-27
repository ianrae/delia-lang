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

        @Override
        public String toString() {
            return "SbError{" +
                    "id='" + id + '\'' +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }

    public static class SdException extends RuntimeException {
        public SdException(String msg) {
            super(msg);
        }
    }

    public interface SdAction {
        String getName();
        String getKey();
        String getTable();
    }

    public static class SdExistAction implements SdAction {
        private String name;
        private String table;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        private String key;
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

            res.ok = res.errors.isEmpty();
            return res;
        }
    }

    //-- valid
    public interface DBInterface {
        boolean tableExists(String table);
        boolean columnExists(String table, String column);
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

            res.ok = res.errors.isEmpty();
            return res;
        }

        private void validateExist(SdAction action, SdValidationResults res) {
            if (!dbInterface.tableExists(action.getTable())) {
                res.errors.add(new SbError("unknown.table", String.format("unknown table: '%s'", action.getTable())));
            }

            if (action.getKey() != null) {
                if (!dbInterface.columnExists(action.getTable(), action.getKey())) {
                    res.errors.add(new SbError("key.unknown.column", String.format("key references unknown column '%s' in table: '%s'", action.getKey(), action.getTable())));
                }
            }
        }
    }

    public static class MyDBInterface implements DBInterface {
        public String knownTables = "";
        public String knownColumns = "";

        @Override
        public boolean tableExists(String table) {
            return knownTables.contains(table);
        }

        @Override
        public boolean columnExists(String table, String column) {
            if (!  knownTables.contains(table)) {
                return false;
            }
            return knownColumns.contains(column);
        }
    }

    @Test
    public void testEmpty() {
        executor = new MyExecutor();
        SdScript script = new SdScript();
        SdExecutionResults res = executor.execute(script);
        chkOK(res, "");
    }

    @Test
    public void testOne() {
        executor = new MyExecutor();
        SdScript script = new SdScript();
        script.addAction(new SdExistAction());

        SdExecutionResults res = executor.execute(script);
        chkOK(res, "exist");
    }

    @Test
    public void testValidate() {
        SdScript script = new SdScript();
        script.addAction(new SdExistAction("Customer"));

        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script);
        chkValOK(res, "exist");
    }

    @Test
    public void testValidateUnknownTable() {
        SdScript script = new SdScript();
        script.addAction(new SdExistAction("Customer"));

        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Address";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script);
        chkValFail(res, "unknown.table");
    }


    @Test
    public void testValidateColumn() {
        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);

        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        dbInterface.knownColumns = "firstName";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script);
        chkValOK(res, "exist");
    }
    @Test
    public void testValidateUnknownColumn() {
        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);

        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script);
        chkValFail(res, "key.unknown.column");
    }

    //---
    private MyExecutor executor;
    private MyValidator validator;

    @Before
    public void init() {
    }

    private void chkOK(SdExecutionResults res, String trail) {
        assertEquals(true, res.ok);
        assertEquals(trail, executor.trail.getTrail());
    }

    private void chkValOK(SdValidationResults res, String trail) {
        assertEquals(true, res.ok);
        assertEquals(trail, validator.trail.getTrail());
    }
    private void chkValFail(SdValidationResults res, String errId) {
        assertEquals(false, res.ok);
        res.errors.forEach(err -> log(err.toString()));
        assertEquals(errId, res.errors.get(0).id);
    }

    private void log(String s) {
        System.out.println(s);
    }
}
