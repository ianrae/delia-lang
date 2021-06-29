package org.delia.seed;


import org.delia.DeliaSession;
import org.delia.db.sql.StrCreator;
import org.delia.other.StringTrail;
import org.delia.scope.scopetest.relation.DeliaClientTestBase;
import org.delia.scope.scopetest.relation.ValueBuilder;
import org.delia.seed.code.SeedDValueHelper;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.Assert.assertEquals;


public class DeliaSeedTests extends DeliaClientTestBase {

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

        List<DValue> getData();
    }

    public static class SdExistAction implements SdAction {
        private String name;
        private String table;
        private List<DValue> dataL = new ArrayList<>();

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

        public MyValidator(DBInterface dbInterface) {
            this.dbInterface = dbInterface;
        }

        @Override
        public SdValidationResults validate(SdScript script, DTypeRegistry registry) {
            this.registry = registry;
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

            DStructType structType = (DStructType) registry.getType(action.getTable());
            if (action.getKey() != null) {
                if (!dbInterface.columnExists(action.getTable(), action.getKey())) {
                    res.errors.add(new SbError("key.unknown.column", String.format("key references unknown column '%s' in table: '%s'", action.getKey(), action.getTable())));
                }
            } else if (structType.getPrimaryKey() == null) {
                res.errors.add(new SbError("key.missing", String.format("table: '%s' has no primary key. Action.key must not be empty", action.getTable())));
            } else {
                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
                int missingCount = 0;
                for(DValue dval: action.getData()) {
                    if (! dval.asStruct().hasField(pkpair.name)) {
                        missingCount++;
                    }
                }
                if (missingCount > 0) {
                    res.errors.add(new SbError("pk.missing", String.format("table: '%s'. Data rows must have a value for the primary key. %d rows are missing one", action.getTable(), missingCount)));
                }
            }

//            DStructType structType = dbInterface.getTypeSchema(action.getTable());

            for (DValue dval : action.getData()) {
                validateDValue(dval, structType, res);
            }
        }

        private void validateDValue(DValue dval, DStructType structType, SdValidationResults res) {
            //dval will always be the correct dtype typeName
            //idea here is that dval's stype is simply a structural type built from the data provided
            //We compare against the actual db schema structType
            DStructHelper helper = dval.asStruct();
            for (TypePair pair : helper.getType().getAllFields()) {
                String fieldName = pair.name;
                DValue fieldVal = helper.getField(fieldName);
                if (fieldVal == null) {
                    if (!structType.fieldIsOptional(fieldName)) {
                        res.errors.add(new SbError("data.missing.value", String.format("data column '%s': null not allowed", fieldName)));
                    }
                } else {
                    DType dataType = pair.type;
                    DType typeInDB = DValueHelper.findFieldType(structType, fieldName);
                    if (!areCompatible(dataType, typeInDB)) {
                        res.errors.add(new SbError("data.wrong.type", String.format("data column '%s': wrong type in value: '%s'", fieldName, fieldVal.asString())));
                    }

                    //for fk values we will let the db validate those
                }
            }
        }

        private boolean areCompatible(DType dataType, DType typeInDB) {
            //TODO need complete logic here
            return dataType.getShape().equals(typeInDB.getShape()); //simple for now
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
            for(DValue dval: action.getData()) {
                sc.o("upsert %s[%s] ", action.getTable(), getKey(dval, action));
                sc.o("{ %s } ", buildDataValues(dval));
                sc.nl();
            }
        }

        private String buildDataValues(DValue dval) {
            StringJoiner joiner = new StringJoiner(", ");
            DStructType structType = dval.asStruct().getType(); //want in declared order
            for(TypePair pair: structType.getAllFields()) {
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
        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        dbInterface.knownColumns = "firstName";
        validator = new MyValidator(dbInterface);
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
        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        dbInterface.knownColumns = "firstName";
        validator = new MyValidator(dbInterface);
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
        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        dbInterface.knownColumns = "firstName";
        validator = new MyValidator(dbInterface);
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
        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        dbInterface.knownColumns = "firstName";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry);
        chkValOK(res, "exist");

        MyDeliaGenerator gen = new MyDeliaGenerator();
        String src = gen.generate(script, sess);
        //TODO  insert Flight {id: 55, wid: 20 }
        assertEquals("upsert Customer[firstName=='sue'] { firstName: 'sue',id: 45 }", src);
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
        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        dbInterface.knownColumns = "firstName";
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script, dbRegistry);
        chkValOK(res, "exist");

        MyDeliaGenerator gen = new MyDeliaGenerator();
        String src = gen.generate(script, sess);
        //TODO  insert Flight {id: 55, wid: 20 }
        assertEquals("upsert Customer[firstName=='sue'] { id: 45, firstName: 'sue' }", getIthLine(src, 0));
        assertEquals("upsert Customer[firstName=='tom'] { id: 46, firstName: 'tom' }", getIthLine(src, 1));
        //upsert Customer[firstName=='tom'] { firstName: 'tom',id: 46 }
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
        assertEquals(errId, res.errors.get(0).id);
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
