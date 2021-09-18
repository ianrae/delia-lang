//package org.delia.bddnew.core;
//
//import org.delia.DeliaSession;
//import org.delia.seede.db.DBSchemaBuilder;
//import org.delia.seede.db.DBSchemaColumn;
//import org.delia.seede.db.DBSchemaInfo;
//import org.delia.seede.db.util.MockSchemaBuilder;
//import org.delia.seede.runner.SdDBInterface;
//import org.delia.seede.runner.executor.SeedeOptions;
//import org.delia.seede.runner.executor.SqlFileLoaderService;
//import org.delia.type.DStructType;
//import org.delia.type.DType;
//import org.delia.type.DTypeRegistry;
//import org.delia.type.TypePair;
//
//public class MEMSchemaBuilder implements DBSchemaBuilder {
//    public String schema = "public";
//    public String tbl;
//    public DeliaSession sess;
//
//    public MEMSchemaBuilder(DeliaSession sess) {
//        this.sess = sess;
//    }
//
//    @Override
//    public void setSqlLoader(SqlFileLoaderService sqlLoaderSvc) {
//    }
//
//    @Override
//    public DBSchemaInfo detectSchema(SdDBInterface dbInterface, SeedeOptions options) {
//        DTypeRegistry registry = sess.getRegistry();
//        DBSchemaInfo schemaInfo = new DBSchemaInfo();
//
//        for (String typeName : registry.getAll()) {
//            DType type = registry.getType(typeName);
//            if (!type.isStructShape()) {
//                continue;
//            }
//
//            DStructType structType = (DStructType) type;
//            MockSchemaBuilder builder = new MockSchemaBuilder();
//            builder.tbl = typeName;
//            for (TypePair pair : structType.getAllFields()) {
//                DBSchemaColumn col = builder.add(pair.name, toDBType(pair.type));
//                col.isPrimaryKey = structType.fieldIsPrimaryKey(pair.name);
//                col.isNullable = structType.fieldIsOptional(pair.name);
//            }
//            schemaInfo.allColumns.addAll(builder.schemaInfo.allColumns);
//        }
//        return schemaInfo;
//    }
//
//    @Override
//    public void setSchema(String schema) {
//    }
//
//    //TOD refactor
//    private String toDBType(DType dtype) {
////            if (col.isFK) {
////                Optional<DBSchemaColumn> opt = schemaInfo.findColumn(col.fkSchema, col.fkTable, col.fkColumn);
////                return getDeliaShape(opt.get(), schemaInfo);
////            }
//        switch (dtype.getShape()) {
//            case INTEGER:
//                return "integer"; //or smallint
//            case LONG:
//                return "bigint";
//            case NUMBER:
//                return "numeric";
//            case BOOLEAN:
//                return "boolean";
//            case STRING:
//                return "character varying"; //or text
//            case DATE:
//                return "timestamp without time zone";
//            case BLOB:
//                return "bytea";
//            case STRUCT:
//            case RELATION:
//            default:
//                return null; //should never happen
//        }
//    }
//}
