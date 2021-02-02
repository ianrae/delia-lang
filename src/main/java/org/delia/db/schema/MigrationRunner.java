package org.delia.db.schema;

import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.schema.modify.OperationType;
import org.delia.db.schema.modify.SchemaChangeOperation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;
import org.stringtemplate.v4.compiler.Bytecode.OperandType;

public class MigrationRunner extends ServiceBase {

	private DTypeRegistry registry;
	private DBExecutor dbexecutor;
	private MigrateInsertRunner insertRunner;

	public MigrationRunner(FactoryService factorySvc, DTypeRegistry registry, DBExecutor dbexecutor, DBInterfaceFactory dbInterface) {
		super(factorySvc);
		this.dbexecutor = dbexecutor;
		this.registry = registry;
		this.insertRunner = new MigrateInsertRunner(factorySvc, registry, dbexecutor, dbInterface);
	}

	public boolean performMigrations(String currentFingerprint, MigrationPlan plan, List<String> orderL) {
		log.log("running migration with %d steps:", orderL.size());
		for(String typeName: orderL) {
			for(SchemaType st: plan.diffL) {
				if (st.typeName.equals(typeName)) {
					if (st.isTblInsert()) {
						log.log("  create-table: %s", st.typeName);
						dbexecutor.createTable(st.typeName);
					} else if (st.isFieldInsert()) {
						log.log("  add-field: %s", st);
						dbexecutor.createField(st.typeName, st.field, st.sizeof);
					} else if (st.isFieldRename()) {
						log.log("  rename-field: %s %s", st, st.newName);
						dbexecutor.renameField(st.typeName, st.field, st.newName);
					} else if (st.isFieldAlterType()) {
						log.log("  alter-field-type: %s %s", st, st.newName);
						dbexecutor.alterFieldType(st.typeName, st.field, st.newName, 0);
					} else if (st.isFieldAlter()) {
						log.log("  alter-field: %s '%s'", st, st.newName);
						dbexecutor.alterField(st.typeName, st.field, st.newName);
					} else if (st.isFieldAlterSizeInt()) {
						log.log("  alter-field-sizeof-int: %s %s", st, st.newName);
						dbexecutor.alterFieldType(st.typeName, st.field, st.newName, st.sizeof);
					} else if (st.isFieldAlterSizeString()) {
						log.log("  alter-field-sizeof-str: %s %s", st, st.newName);
						dbexecutor.alterFieldType(st.typeName, st.field, st.newName, st.sizeof);
					}
				}
			}
		}
		
		for(SchemaChangeAction action: plan.changeActionL) {
			log.log("  change-action: %s, %s", action.typeName, action.changeType);
			dbexecutor.performSchemaChangeAction(action);
		}
		
		for(SchemaType st: plan.diffL) {
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
	
	//=new one
	public boolean sxPerformMigrations(String currentFingerprint, List<SchemaChangeOperation> opList, List<String> orderL) {
		log.log("running sxmigration with %d steps:", orderL.size());
		for(String typeName: orderL) {
			for(SchemaChangeOperation op: opList) {
				//when we rename a table, op will be the old name, so check op.newName
				boolean isMatchToNewName = false;
				if (op.opType.equals(OperationType.TABLE_RENAME)) {
					isMatchToNewName = op.newName.equals(typeName);
				}
				
				if (op.typeName.equals(typeName) || isMatchToNewName) {
					log.log("  %s: %s. fld: %s", op.opType.name(), op.typeName, op.fieldName);
					dbexecutor.executeSchemaChangeOperation(op);
				}
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
	
	

}