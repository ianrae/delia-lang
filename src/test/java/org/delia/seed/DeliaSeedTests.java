package org.delia.seed;


import org.delia.DeliaSession;
import org.delia.db.sql.StrCreator;
import org.delia.other.StringTrail;
import org.delia.scope.scopetest.relation.DeliaClientTestBase;
import org.delia.seed.code.*;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class DeliaSeedTests extends DeliaClientTestBase {

    public interface SdAction {
        String getName();

        String getKey();

        String getTable();

        List<DValue> getData();
    }

    public static abstract class SdActionBase implements SdAction {
        private String name;
        private String table;
        private List<DValue> dataL = new ArrayList<>();
        private String key;

        public SdActionBase(String name) {
            this.name = name;
        }

        public SdActionBase(String name, String table) {
            this(name);
            this.table = table;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTable() {
            return table;
        }

        @Override
        public List<DValue> getData() {
            return dataL;
        }
    }

    public static class SdExistAction extends SdActionBase {
        private boolean isNotExist;

        public SdExistAction() {
            super("exist");
        }
        public SdExistAction(String table) {
            super("exist", table);
        }

        public boolean isNotExist() {
            return isNotExist;
        }

        public void setNotExist(boolean notExist) {
            isNotExist = notExist;
        }

        @Override
        public String getName() {
            return isNotExist ? "not exist" : "exist";
        }
    }
    public static class SdInsertAction extends SdActionBase {
        public SdInsertAction() {
            super("insert");
        }
        public SdInsertAction(String table) {
            super("insert", table);
        }
    }
    public static class SdUpdateAction extends SdActionBase {
        private String whereClause;

        public SdUpdateAction() {
            super("update");
        }
        public SdUpdateAction(String table) {
            super("update", table);
        }
        public String getWhereClause() {
            return whereClause;
        }

        public void setWhereClause(String whereClause) {
            this.whereClause = whereClause;
        }

    }
    public static class SdDeleteAction extends SdActionBase {
        private boolean isDeleteAll;

        public SdDeleteAction() {
            super("delete");
        }
        public SdDeleteAction(String table) {
            super("delete", table);
        }

        @Override
        public String getName() {
            return isDeleteAll ? "delete all" : "delete";
        }

        public boolean isDeleteAll() {
            return isDeleteAll;
        }

        public void setDeleteAll(boolean deleteAll) {
            isDeleteAll = deleteAll;
        }

    }

    public static class SdScript {
        private List<SdAction> actions = new ArrayList<>();

        public List<SdAction> getActions() {
            return actions;
        }

        public void addAction(SdAction action) {
            actions.add(action);
        }
    }

    public static class SdExecutionResults {
        public boolean ok;
        public String deliaSrc;
        public List<SbError> errors = new ArrayList<>();
    }

    public interface SdExecutor {
        SdExecutionResults execute(SdScript script);
    }

    public static class MyExecutor implements SdExecutor {
        private final DBInterface dbInterface;
        private final DTypeRegistry registry;
        private final DeliaSession sess;
        public StringTrail trail = new StringTrail();
        private Map<String, ActionExecutor> executorMap = new HashMap<>();

        public MyExecutor(DBInterface dbInterface, DTypeRegistry registry, DeliaSession sess) {
            this.dbInterface = dbInterface;
            this.registry = registry;
            this.sess = sess;
            executorMap.put("exist", new ExistActionExecutor());
            executorMap.put("not exist", new NotExistActionExecutor());
            executorMap.put("insert", new InsertActionExecutor());
            executorMap.put("delete", new DeleteActionExecutor());
            executorMap.put("delete all", new DeleteActionExecutor());
            executorMap.put("update", new UpdateActionExecutor());
        }

        @Override
        public SdExecutionResults execute(SdScript script) {
            SdExecutionResults res = new SdExecutionResults();

            for (String key : executorMap.keySet()) {
                ActionExecutor av = executorMap.get(key);
                av.init(dbInterface, registry, sess);
            }

            StrCreator sc = new StrCreator();
            for (SdAction action : script.getActions()) {
                trail.add(action.getName());
                if (!executorMap.containsKey(action.getName())) {
                    throw new SdException("unknown action: " + action.getName());
                }
                ActionExecutor exec = executorMap.get(action.getName());
                exec.executeAction(action, sc, res);
            }

            res.deliaSrc = sc.toString();
            res.ok = res.errors.isEmpty();
            return res;
        }
    }

    //-- valid
    public interface DBInterface {
        boolean tableExists(String table);

        boolean columnExists(String table, String column);

        //        DStructType getTypeSchema(String table);
        boolean isCascadingFk(String table, String column);
    }

    public interface SdTypeGenerator {
        DTypeRegistry findEntityTypes();
    }

    public static class SdValidationResults {
        public boolean ok;
        public List<SbError> errors = new ArrayList<>();
    }

    public interface SdValidator {
        SdValidationResults validate(SdScript script, DTypeRegistry registry, DeliaSession sess);
    }

    public static class MyValidator implements SdValidator {
        private final DBInterface dbInterface;
        public StringTrail trail = new StringTrail();
        private DTypeRegistry registry;
        private Map<String, ActionValidator> validatorMap = new HashMap<>();

        public MyValidator(DBInterface dbInterface) {
            this.dbInterface = dbInterface;
            validatorMap.put("exist", new ExistActionValidator());
            validatorMap.put("not exist", new NotExistActionValidator());
            validatorMap.put("insert", new InsertActionValidator());
            validatorMap.put("delete", new DeleteActionValidator());
            validatorMap.put("delete all", new DeleteActionValidator());
            validatorMap.put("update", new UpdateActionValidator());
        }

        @Override
        public SdValidationResults validate(SdScript script, DTypeRegistry registry, DeliaSession sess) {
            this.registry = registry;
            for (String key : validatorMap.keySet()) {
                ActionValidator av = validatorMap.get(key);
                av.init(dbInterface, registry, sess);
            }
            SdValidationResults res = new SdValidationResults();

            for (SdAction action : script.getActions()) {
                trail.add(action.getName());
                if (!validatorMap.containsKey(action.getName())) {
                    throw new SdException("no validator for action: " + action.getName());
                }
                validateAction(action, res);
            }

            res.ok = res.errors.isEmpty();
            return res;
        }

        private void validateAction(SdAction action, SdValidationResults res) {
            ActionValidator av = validatorMap.get(action.getName());
            av.validateAction(action, res);
        }
    }

    public static class MyDBInterface implements DBInterface {
        public String knownTables = "";
        public String knownColumns = "";
//        public DStructType structType;

        @Override
        public boolean tableExists(String table) {
            return knownTables.contains(table);
        }

        @Override
        public boolean columnExists(String table, String column) {
            if (!knownTables.contains(table)) {
                return false;
            }
            return knownColumns.contains(column);
        }

        @Override
        public boolean isCascadingFk(String table, String column) {
            return false;
        }

//        @Override
//        public DStructType getTypeSchema(String table) {
//            return structType;
//        }
    }

    public static class MySdTypeGenerator implements SdTypeGenerator {
        public DTypeRegistry registry;

        @Override
        public DTypeRegistry findEntityTypes() {
            return registry;
        }
    }


    @Test
    public void testEmpty() {
        initDBAndReg();
        executor = new MyExecutor(dbInterface, dbRegistry, sess);
        SdScript script = new SdScript();
        SdExecutionResults res = executor.execute(script);
        log("src: " + res.deliaSrc);
        chkOK(res, "");
    }

    @Test
    public void testOne() {
        initDBAndReg();
        executor = new MyExecutor(dbInterface, dbRegistry, sess);
        SdScript script = new SdScript();
        script.addAction(new SdExistAction());

        SdExecutionResults res = executor.execute(script);
        log("src: " + res.deliaSrc);
        chkOK(res, "exist");
    }

    @Test
    public void testValidate() {
        SdScript script = new SdScript();
        script.addAction(new SdExistAction("Customer"));

        initDBAndReg();
        dbInterface.knownTables = "Customer";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "exist");
    }

    @Test
    public void testValidateUnknownTable() {
        SdScript script = new SdScript();
        script.addAction(new SdExistAction("Customer"));

        initDBAndReg();
        dbInterface.knownTables = "Address";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValFail(res, "unknown.table");
    }


    @Test
    public void testValidateColumn() {
        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "exist");
    }

    @Test
    public void testValidateUnknownColumn() {
        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);

        initDBAndReg();
        dbInterface.knownColumns = "";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValFail(res, "key.unknown.column");
    }

    @Test
    public void testData() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());
        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);
        DValue dval = vb.buildDVal(45, "sue");
        action.getData().add(dval);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "exist");
    }

    @Test
    public void testDataWrongType() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());

        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);
        DValue dval = vb.buildDVal(45, "sue");
        action.getData().add(dval);

        initDBAndReg();
        dbRegistry = createDbRegistry("Customer", createCustomerWrongSrc());
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValFail(res, "data.wrong.type");
    }

    @Test
    public void testDeliaGen() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());
        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);
        DValue dval = vb.buildDVal(45, "sue");
        action.getData().add(dval);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "exist");

        String src = runExec(script);
        assertEquals("upsert Customer[firstName=='sue'] { id: 45, firstName: 'sue' }", src);
    }

    @Test
    public void testDeliaGenTwo() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());
        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);
        DValue dval = vb.buildDVal(45, "sue");
        action.getData().add(dval);
        DValue dval2 = vb.buildDVal(46, "tom");
        action.getData().add(dval2);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "exist");

        String src = runExec(script);
        assertEquals("upsert Customer[firstName=='sue'] { id: 45, firstName: 'sue' }", getIthLine(src, 0));
        assertEquals("upsert Customer[firstName=='tom'] { id: 46, firstName: 'tom' }", getIthLine(src, 1));
    }

    @Test
    public void testDeliaGenPK() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());
        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        script.addAction(action);
        DValue dval = vb.buildDVal(45, "sue");
        action.getData().add(dval);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "exist");

        String src = runExec(script);
        assertEquals("upsert Customer[45] { id: 45, firstName: 'sue' }", src);
    }

    @Test
    public void testNotExist() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());
        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setNotExist(true);
        script.addAction(action);
        DValue dval = vb.buildDVal(45, "sue");
        action.getData().add(dval);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "not exist");

        String src = runExec(script);
        assertEquals("delete Customer[45]", src);
    }

    @Test
    public void testInsertDeliaGenPK() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());
        SdScript script = new SdScript();
        SdInsertAction action = new SdInsertAction("Customer");
        script.addAction(action);
        DValue dval = vb.buildDVal(45, "sue");
        action.getData().add(dval);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "insert");

        String src = runExec(script);
        assertEquals("insert Customer { id: 45, firstName: 'sue' }", src);
    }

    @Test
    public void testDeleteDeliaGenPK() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());
        SdScript script = new SdScript();
        SdDeleteAction action = new SdDeleteAction("Customer");
        script.addAction(action);
        DValue dval = vb.buildDVal(45, "sue");
        action.getData().add(dval);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "delete");

        String src = runExec(script);
        assertEquals("delete Customer[45]", src);
    }
    @Test
    public void testDeleteDeliaGenKey() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());
        SdScript script = new SdScript();
        SdDeleteAction action = new SdDeleteAction("Customer");
        action.setKey("firstName");
        script.addAction(action);
        DValue dval = vb.buildDVal(45, "sue");
        action.getData().add(dval);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "delete");

        String src = runExec(script);
        assertEquals("delete Customer[firstName=='sue']", src);
    }

    @Test
    public void testDeleteAllDeliaGenKey() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());
        SdScript script = new SdScript();
        SdDeleteAction action = new SdDeleteAction("Customer");
        action.setDeleteAll(true);
        action.setKey("firstName");
        script.addAction(action);
        DValue dval = vb.buildDVal(45, "sue");
        action.getData().add(dval);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "delete all");

        String src = runExec(script);
        assertEquals("delete Customer[true]", src);
    }

    @Test
    public void testUpdateDeliaGenPK() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());
        SdScript script = new SdScript();
        SdUpdateAction action = new SdUpdateAction("Customer");
        script.addAction(action);
        DValue dval = vb.buildDVal(45, "sue");
        action.getData().add(dval);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "update");

        String src = runExec(script);
        assertEquals("update Customer[45] { firstName: 'sue' }", src); //without pk
    }

    @Test
    public void testUpdateDeliaGenWhere() {
        ValueBuilder vb = createValueBuilder(createCustomerSrc());
        SdScript script = new SdScript();
        SdUpdateAction action = new SdUpdateAction("Customer");
        action.setWhereClause("id < 10");
        script.addAction(action);
        DValue dval = vb.buildDValNoId("sue");
        action.getData().add(dval);

        initDBAndReg();
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry, sess);
        chkValOK(res, "update");

        String src = runExec(script);
        assertEquals("update xCustomer[id < 10] { firstName: 'sue' }", src); //without pk
    }

    //---
    private MyExecutor executor;
    private MyValidator validator;
    private MyDBInterface dbInterface;
    private DTypeRegistry dbRegistry;

    @Before
    public void init() {
        super.init();
        enableAutoCreateTables();
    }

    private void initDBAndReg() {
        dbRegistry = createDbRegistry("Customer", createCustomerSrc());
        dbInterface = createDBInterface();
    }

    private String runExec(SdScript script) {
        executor = new MyExecutor(dbInterface, dbRegistry, sess);
        SdExecutionResults res = executor.execute(script);
        log("src: " + res.deliaSrc);
        return res.deliaSrc.trim();
    }

    private MyDBInterface createDBInterface() {
        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        dbInterface.knownColumns = "firstName";
        return dbInterface;
    }

    private String getIthLine(String src, int i) {
        String[] ar = src.split("\n");
        return ar[i].trim();
    }

    private DTypeRegistry createDbRegistry(String customer, String src) {
        MySdTypeGenerator generator = new MySdTypeGenerator();
        createCustomerType(src, "Customer");
        return sess.getExecutionContext().registry;
    }

    private ValueBuilder createValueBuilder(String src) {
        ValueBuilder vb = new ValueBuilder();
        vb.init();
        vb.createCustomerType(src);
        return vb;
    }

    private void chkOK(SdExecutionResults res, String trail) {
        res.errors.forEach(err -> log(err.toString()));
        assertEquals(true, res.ok);
        assertEquals(trail, executor.trail.getTrail());
    }

    private void chkValOK(SdValidationResults res, String trail) {
        res.errors.forEach(err -> log(err.toString()));
        assertEquals(true, res.ok);
        assertEquals(trail, validator.trail.getTrail());
    }

    private void chkValFail(SdValidationResults res, String errId) {
        assertEquals(false, res.ok);
        res.errors.forEach(err -> log(err.toString()));
        assertEquals(errId, res.errors.get(0).getId());
    }

    private String createCustomerSrc() {
        String src = String.format("type %s struct { id int primaryKey, firstName string} end", "Customer");
        src += "\n";
        return src;
    }

    private String createCustomerWrongSrc() {
        String src = String.format("type %s struct { id int primaryKey, firstName int} end", "Customer");
        src += "\n";
        return src;
    }

    private DStructType createCustomerType(String src) {
        execTypeStatement(src);
        DTypeRegistry registry = sess.getExecutionContext().registry;
        DStructType dtype = (DStructType) registry.getType("Customer");
        return dtype;
    }

    private DStructType createCustomerType(String src, String typeName) {
        execTypeStatement(src);
        DTypeRegistry registry = sess.getExecutionContext().registry;
        DStructType dtype = (DStructType) registry.getType(typeName);
        return dtype;
    }


}
