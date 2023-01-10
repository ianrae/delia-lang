package org.delia.migration;


import org.delia.*;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.DeliaLog;
import org.delia.log.SimpleLog;
import org.delia.sql.SqlTypeConverter;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.util.ListWalker;
import org.delia.util.StrCreator;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.util.Objects.isNull;

/**
     //schema actions (only be called after init2)
        * //    boolean doesTableExist(String tableName);
        * //    boolean doesFieldExist(String tableName, String fieldName);
        * //    void createTable(String tableName);
        * //    void deleteTable(String tableName);
        * //    void renameTable(String tableName, String newTableName);
        * //    void createField(String typeName, String field, int sizeof);
        * //    void deleteField(String typeName, String field, int datId);
        * //    void renameField(String typeName, String fieldName, String newName);
        * //    void alterFieldType(String typeName, String fieldName, String newFieldType, int sizeof);
        * //    void alterField(String typeName, String fieldName, String deltaFlags);
        * //    void performSchemaChangeAction(SchemaChangeAction action);
        * //    void executeSchemaChangeOperation(SchemaChangeOperation op);
 */

public class FlywayStatementTests {

    public interface DDLGenerator {
        void createTable(DStructType structType, List<String> fields);
        void renameTable(DStructType structType, String newName);
        void dropTable(DStructType structType);

        void renameField(DStructType structType, String fieldName, String newName);
        void dropField(DStructType structType, String fieldName);
        //change flags "+O-U" OUSP
        void alterField(DStructType structType, String fieldName, String changeFlags, DType newType, Integer newSizeof);
        void alterUniqueFieldsConstraint(DStructType structType, List<String> fields, List<String> newFields); //use to create/delete/alter
        //TODO indexes
        
        String end();
        String getDDLText();
    }
    
    public static class PostgresDDLGenerator implements DDLGenerator {

        private final DeliaLog log;
        private final DeliaOptions options;
        private final StrCreator sc;
        private final SqlTypeConverter sqlTypeConverter;

        public PostgresDDLGenerator(DeliaLog log, DeliaOptions options) {
            this.log = log;
            this.options = options;
            this.sc = new StrCreator();
            this.sqlTypeConverter = new SqlTypeConverter(options);
        }

        @Override
        public void createTable(DStructType structType, List<String> fields) {
            sc.o("CREATE TABLE %s", buildTableName(structType));
            sc.nl();
            ListWalker<String> walker = new ListWalker<>(fields);
            while (walker.hasNext()) {
                String fieldName = walker.next();
                renderAddField(fieldName, structType);
                walker.addIfNotLast(sc, ",");
                sc.nl();
            }

            sc.o(");");
            sc.nl();
        }

        private void renderAddField(String fieldName, DStructType structType) {
            TypePair pair = DValueHelper.findField(structType, fieldName);
            String name = renderFieldName(pair.name);
            sc.addStr(name);
            sc.addStr(" ");

            String typeStr = sqlTypeConverter.getSqlType(pair.type);
            boolean isPK = structType.fieldIsPrimaryKey(fieldName);
            if (isPK) {
                if (structType.fieldIsSerial(fieldName)) {
                    sc.addStr("SERIAL"); //TODO: bigserial
                } else {
                    sc.addStr(typeStr);
                }
                sc.addStr(" PRIMARY KEY");
            } else {
                sc.addStr(typeStr);
                if (structType.fieldIsOptional(fieldName)) {
                    sc.addStr(" NOT NULL");
                }
                if (structType.fieldIsUnique(fieldName)) {
                    sc.addStr(" UNIQUE");
                }
            }
        }

        private String renderFieldName(String name) {
            return name.toLowerCase(Locale.ROOT);
        }

        private String buildTableName(DStructType structType) {
            String schema = structType.getSchema();
            if (isNull(schema)) {
                return structType.getName().toLowerCase(Locale.ROOT);
            } else {
                String s = String.format("%s.%s", schema, structType.getName());
                return s.toLowerCase(Locale.ROOT);
            }
        }

        @Override
        public void renameTable(DStructType structType, String newName) {
            //ALTER TABLE supplier_groups RENAME TO groups;
            //TODO only support rename within its current schema
            //note we use renderFieldName even though its a tablename
            sc.o("ALTER TABLE %s RENAME TO %s;", buildTableName(structType), renderFieldName(newName));
            sc.nl();
        }

        @Override
        public void dropTable(DStructType structType) {
            sc.o("DROP TABLE IF EXISTS %s;", buildTableName(structType));
            sc.nl();
        }

        @Override
        public void renameField(DStructType structType, String fieldName, String newName) {
            sc.o("ALTER TABLE %s", buildTableName(structType));
            sc.o("RENAME COLUMN %s TO %s;", fieldName, newName);
            sc.nl();
        }

        @Override
        public void dropField(DStructType structType, String fieldName) {
            sc.o("ALTER TABLE %s", buildTableName(structType));
            sc.o("DROP COLUMN %s;", fieldName);
            sc.nl();
        }

        @Override
        public void alterField(DStructType structType, String fieldName, String changeFlags, DType newType, Integer newSizeof) {
            //TODO
        }

        @Override
        public void alterUniqueFieldsConstraint(DStructType structType, List<String> fields, List<String> newFields) {
            //TODO
        }

        @Override
        public String end() {
            return sc.toString();
        }

        @Override
        public String getDDLText() {
            return sc.toString();
        }
    }



    @Test
    public void test2() {
        DeliaLog deliaLog = new SimpleLog();
        FactoryService factorySvc = new FactoryServiceImpl(deliaLog, new SimpleErrorTracker(deliaLog));
        ConnectionDefinition connDef = ConnectionDefinitionBuilder.createMEM();

        Delia delia = DeliaFactory.create(connDef, deliaLog, factorySvc);
        String src = "type Customer struct {id int primaryKey, wid int } wid.maxlen(4) end";

        DeliaSession sess = delia.beginSession(src);
        DTypeRegistry registry = sess.getRegistry();
        DStructType structType = registry.getStructType(new DTypeName(null, "Customer"));
        PostgresDDLGenerator ddlgen = new PostgresDDLGenerator(deliaLog, sess.getDelia().getOptions());
        List<String> fields = Arrays.asList("id", "wid");
        ddlgen.createTable(structType, fields);
        log(ddlgen.end());
    }


    //---
    private void log(String s) {
        System.out.println(s);
    }

}