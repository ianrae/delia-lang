package org.delia.seed;


import org.delia.other.StringTrail;
import org.delia.runner.ResultValue;
import org.delia.scope.scopetest.relation.DeliaClientTestBase;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
        DStructType getTypeSchema(String table);
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

            DStructType structType = dbInterface.getTypeSchema(action.getTable());
            for(DValue dval: action.getData()) {
                validateDValue(dval, structType, res);
            }
        }

        private void validateDValue(DValue dval, DStructType structType, SdValidationResults res) {
            //dval will always be the correct dtype typeName
            //idea here is that dval's stype is simply a structural type built from the data provided
            //We compare against the actual db schema structType
            DStructHelper helper = dval.asStruct();
            for(TypePair pair: helper.getType().getAllFields()) {
                String fieldName = pair.name;
                DValue fieldVal = helper.getField(fieldName);
                if (fieldVal == null) {
                    if (! structType.fieldIsOptional(fieldName)) {
                        res.errors.add(new SbError("data.missing.value", String.format("data column '%s': null not allowed", fieldName)));
                    }
                } else {
                    DType dataType = pair.type;
                    DType typeInDB = DValueHelper.findFieldType(structType, fieldName);
                    if (! areCompatible(dataType, typeInDB)) {
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
        public DStructType structType;

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

        @Override
        public DStructType getTypeSchema(String table) {
            return structType;
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

    @Test
    public void testData() {
        createCustomerType(createCustomerSrc());
        if (true) {
            execStatement("insert Customer {id: 44, firstName:'bob'}");
            ResultValue res = this.execStatement("let x = Customer[true]");
            assertEquals(true, res.ok);
            DValue dval = res.getAsDValue();
            assertEquals("bob", dval.asStruct().getField("firstName").asString());
            assertEquals(44, dval.asStruct().getField("id").asInt());
        }

        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);
        DValue dval = buildDVal(45, "sue");
        action.getData().add(dval);

        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        dbInterface.knownColumns = "firstName";
        dbInterface.structType = dval.asStruct().getType(); //create separate one later
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script);
        chkValOK(res, "exist");
    }

    @Test
    public void testDataWrongType() {
        createCustomerType(createCustomerSrc());
        if (true) {
            execStatement("insert Customer {id: 44, firstName:'bob'}");
            ResultValue res = this.execStatement("let x = Customer[true]");
            assertEquals(true, res.ok);
            DValue dval = res.getAsDValue();
            assertEquals("bob", dval.asStruct().getField("firstName").asString());
            assertEquals(44, dval.asStruct().getField("id").asInt());
        }

        SdScript script = new SdScript();
        SdExistAction action = new SdExistAction("Customer");
        action.setKey("firstName");
        script.addAction(action);
        DValue dval = buildDVal(45, "sue");
        action.getData().add(dval);

        MyDBInterface dbInterface = new MyDBInterface();
        dbInterface.knownTables = "Customer";
        dbInterface.knownColumns = "firstName";
        dbInterface.structType = createCustomerType(createCustomerWrongSrc(), "Customer2");
        validator = new MyValidator(dbInterface);
        SdValidationResults res = validator.validate(script);
        chkValFail(res, "data.wrong.type");
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
        String src = String.format("type %s struct { id int primaryKey, firstName int} end", "Customer2");
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
    private DValue buildDVal(int id, String firstName) {
        SeedDValueBuilderTests.MyEntity entity = new SeedDValueBuilderTests.MyEntity();
        entity.fieldMap.put("id", id);
        entity.fieldMap.put("firstName", firstName);
        String typeName = "Customer";

        SeedDValueBuilder builder = new SeedDValueBuilder(sess, typeName);
        return builder.buildFromEntityEx(entity, typeName);
    }


}
