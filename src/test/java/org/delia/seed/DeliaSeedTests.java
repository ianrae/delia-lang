package org.delia.seed;


import org.delia.DeliaSession;
import org.delia.db.sql.StrCreator;
import org.delia.other.StringTrail;
import org.delia.scope.scopetest.relation.DeliaClientTestBase;
import org.delia.seed.code.*;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;


public class DeliaSeedTests extends DeliaClientTestBase {

    public interface SdAction {
        String getName();

        String getKey();

        String getTable();

        List<DValue> getData();
    }

    public static class SdExistAction implements SdAction {
        private String table;
        private List<DValue> dataL = new ArrayList<>();

        public boolean isNotExist() {
            return isNotExist;
        }

        public void setNotExist(boolean notExist) {
            isNotExist = notExist;
        }

        private boolean isNotExist;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        private String key;
        //private String whereClause;

        public SdExistAction() {
        }

        public SdExistAction(String table) {
            this();
            this.table = table;
        }

        @Override
        public String getName() {
            return isNotExist ? "not exist" : "exist";
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
        SdValidationResults validate(SdScript script, DTypeRegistry registry);
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
        }

        @Override
        public SdValidationResults validate(SdScript script, DTypeRegistry registry) {
            this.registry = registry;
            for (String key : validatorMap.keySet()) {
                ActionValidator av = validatorMap.get(key);
                av.init(dbInterface, registry);
            }
            SdValidationResults res = new SdValidationResults();

            for (SdAction action : script.getActions()) {
                trail.add(action.getName());
                if (!validatorMap.containsKey(action.getName())) {
                    throw new SdException("unknown action: " + action.getName());
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

    public interface DeliaGenerator {
        String generate(SdScript script, DeliaSession sess);
    }

    public static class MyDeliaGenerator implements DeliaGenerator {
        private DeliaSession sess;

        @Override
        public String generate(SdScript script, DeliaSession sess) {
            this.sess = sess;
            StrCreator sc = new StrCreator();
            for (SdAction action : script.getActions()) {
                switch (action.getName()) {
                    case "exist":
                        genExist(action, sc);
                        break;
                    default:
                        throw new SdException("unknown action: " + action.getName());
                }
            }

            return sc.toString().trim();
        }

        private void genExist(SdAction action, StrCreator sc) {
            for (DValue dval : action.getData()) {
                sc.o("upsert %s[%s] ", action.getTable(), getKey(dval, action));
                sc.o("{ %s } ", buildDataValues(dval));
                sc.nl();
            }
        }

        private String buildDataValues(DValue dval) {
            StringJoiner joiner = new StringJoiner(", ");
            DStructType structType = dval.asStruct().getType(); //want in declared order
            for (TypePair pair : structType.getAllFields()) {
                String fieldName = pair.name;
                DValue inner = dval.asStruct().getField(fieldName);
                String s = String.format("%s: %s", fieldName, SeedDValueHelper.renderAsDelia(inner));
                joiner.add(s);
            }
            return joiner.toString();
        }

        private String getKey(DValue dval, SdAction action) {
            if (action.getKey() != null) {
                String fieldName = action.getKey(); //TODO handle multiple keys later
                String deliaStrExpr = getFieldAsDelia(dval, fieldName);
                return String.format("%s==%s", fieldName, deliaStrExpr);
            }
            //TODO: support schema.table. parcels.address
            DStructType structType = (DStructType) sess.getExecutionContext().registry.getType(action.getTable());
            String fieldName = structType.getPrimaryKey().getFieldName(); //already validated that its not null
            return getFieldAsDelia(dval, fieldName);
        }

        private String getFieldAsDelia(DValue dvalParent, String fieldName) {
            return SeedDValueHelper.getFieldAsDelia(dvalParent, fieldName);
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

        DTypeRegistry dbRegistry = createDbRegistry("Customer", createCustomerSrc());
        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry);
        chkValOK(res, "exist");
    }

    @Test
    public void testValidateUnknownTable() {
        SdScript script = new SdScript();
        script.addAction(new SdExistAction("Customer"));

        DTypeRegistry dbRegistry = createDbRegistry("Customer", createCustomerSrc());
        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Address";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry);
        chkValFail(res, "unknown.table");
    }


    @Test
    public void testValidateColumn() {
        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);

        DTypeRegistry dbRegistry = createDbRegistry("Customer", createCustomerSrc());
        validator = new MyValidator(createDBInterface());
        SdValidationResults res = validator.validate(script, dbRegistry);
        chkValOK(res, "exist");
    }

    @Test
    public void testValidateUnknownColumn() {
        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);

        DTypeRegistry dbRegistry = createDbRegistry("Customer", createCustomerSrc());
        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry);
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

        DTypeRegistry dbRegistry = createDbRegistry("Customer", createCustomerSrc());
        validator = new MyValidator(createDBInterface());
        SdValidationResults res = validator.validate(script, dbRegistry);
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

        DTypeRegistry dbRegistry = createDbRegistry("Customer", createCustomerWrongSrc());
        validator = new MyValidator(createDBInterface());
        SdValidationResults res = validator.validate(script, dbRegistry);
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

        DTypeRegistry dbRegistry = createDbRegistry("Customer", createCustomerSrc());
        validator = new MyValidator(createDBInterface());
        SdValidationResults res = validator.validate(script, dbRegistry);
        chkValOK(res, "exist");

        MyDeliaGenerator gen = new MyDeliaGenerator();
        String src = gen.generate(script, sess);
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

        DTypeRegistry dbRegistry = createDbRegistry("Customer", createCustomerSrc());
        validator = new MyValidator(createDBInterface());
        SdValidationResults res = validator.validate(script, dbRegistry);
        chkValOK(res, "exist");

        MyDeliaGenerator gen = new MyDeliaGenerator();
        String src = gen.generate(script, sess);
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

        DTypeRegistry dbRegistry = createDbRegistry("Customer", createCustomerSrc());
        validator = new MyValidator(createDBInterface());
        SdValidationResults res = validator.validate(script, dbRegistry);
        chkValOK(res, "exist");

        MyDeliaGenerator gen = new MyDeliaGenerator();
        String src = gen.generate(script, sess);
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

        DTypeRegistry dbRegistry = createDbRegistry("Customer", createCustomerSrc());
        validator = new MyValidator(createDBInterface());
        SdValidationResults res = validator.validate(script, dbRegistry);
        chkValOK(res, "exist");

        MyDeliaGenerator gen = new MyDeliaGenerator();
        String src = gen.generate(script, sess);
        assertEquals("delete Customer[45]", src);
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

    private DTypeRegistry createDbRegistry() {
        MySdTypeGenerator generator = new MySdTypeGenerator();
        generator.registry = sess.getExecutionContext().registry;
        return sess.getExecutionContext().registry;
    }

    private ValueBuilder createValueBuilder(String src) {
        ValueBuilder vb = new ValueBuilder();
        vb.init();
        vb.createCustomerType(src);
        return vb;
    }


    //---
    private MyExecutor executor;
    private MyValidator validator;

    @Before
    public void init() {
        super.init();
        enableAutoCreateTables();
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
