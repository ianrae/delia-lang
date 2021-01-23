package org.delia.db.schema;

import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

public class MigrationRunner extends ServiceBase {

	private DTypeRegistry registry;
	private ZDBExecutor dbexecutor;
	private MigrateInsertRunner insertRunner;

	public MigrationRunner(FactoryService factorySvc, DTypeRegistry registry, ZDBExecutor dbexecutor, ZDBInterfaceFactory dbInterface) {
		super(factorySvc);
		this.dbexecutor = dbexecutor;
		this.registry = registry;
		this.insertRunner = new MigrateInsertRunner(factorySvc, registry, dbexecutor, dbInterface);
	}

	public boolean performMigrations(String currentFingerprint, List<SchemaType> diffL, List<String> orderL) {
		log.log("running migration with %d steps:", orderL.size());
		for(String typeName: orderL) {
			for(SchemaType st: diffL) {
				if (st.typeName.equals(typeName)) {
					if (st.isTblInsert()) {
						log.log("  create-table: %s", st.typeName);
						dbexecutor.createTable(st.typeName);
					} else if (st.isFieldInsert()) {
						log.log("  add-field: %s", st);
						dbexecutor.createField(st.typeName, st.field);
					} else if (st.isFieldRename()) {
						log.log("  rename-field: %s %s", st, st.newName);
						dbexecutor.renameField(st.typeName, st.field, st.newName);
					} else if (st.isFieldAlterType()) {
						log.log("  alter-field-type: %s %s", st, st.newName);
						dbexecutor.alterFieldType(st.typeName, st.field, st.newName);
					} else if (st.isFieldAlter()) {
						log.log("  alter-field: %s '%s'", st, st.newName);
						dbexecutor.alterField(st.typeName, st.field, st.newName);
					}
				}
			}
		}
		
		for(SchemaType st: diffL) {
			if (st.isTblDelete()) {
				log.log("  delete-table: %s", st.typeName);
				doSoftDelete(st.typeName);
			} else if (st.isFieldDelete()) {
				log.log("  delete-field: %s", st);
				//TODO: implement soft delete
				dbexecutor.deleteField(st.typeName, st.field, st.datId);
			} else if (st.isTblRename()) {
				log.log("  rename-table: %s %s", st, st.newName);
				dbexecutor.renameTable(st.typeName, st.newName);
			} 
		}

		//write new schema to db
		DStructType dtype = registry.getSchemaVersionType();
		DValue dval = createSchemaObj(dtype, currentFingerprint);
		if (dval == null) {
			return false;
		}

//		InsertContext ictx = new InsertContext();
//		dbexecutor.executeInsert(dval, ictx);
		insertRunner.doInsert(dval);

		return true;
	}
	
	
	private void doSoftDelete(String typeName) {
//		SchemaContext ctx = createSchemaContext();
		String backupName = String.format("%s__BAK", typeName);
		if (dbexecutor.doesTableExist(backupName)) {
			//only keep one backup table per type
			dbexecutor.deleteTable(backupName);
		}
		dbexecutor.renameTable(typeName, backupName);
		log.log("rename TBL: %s -> %s", typeName, backupName);
	}
	private void doSoftDeleteField(String typeName, String fieldName) {
//		SchemaContext ctx = createSchemaContext();
		String backupName = String.format("%s__BAK", fieldName);
		if (dbexecutor.doesTableExist(backupName)) {
			//only keep one backup table per type
			dbexecutor.deleteTable(backupName);
		}
		dbexecutor.renameTable(typeName, backupName);
		log.log("rename TBL: %s -> %s", typeName, backupName);
	}
	

	private DValue createSchemaObj(DStructType type, String fingerprint) {
		StructValueBuilder structBuilder = new StructValueBuilder(type);

		ScalarValueBuilder builder = factorySvc.createScalarValueBuilder(registry);
		DValue dval = builder.buildString(fingerprint);
		structBuilder.addField("fingerprint", dval);

		boolean b = structBuilder.finish();
		if (! b) {
			return null;
		}
		dval = structBuilder.getDValue();
		return dval;
	}

}