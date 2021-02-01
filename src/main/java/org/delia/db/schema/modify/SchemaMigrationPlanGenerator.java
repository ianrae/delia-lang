package org.delia.db.schema.modify;

import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.RegAwareServiceBase;
import org.delia.db.DBType;
import org.delia.type.DTypeRegistry;

/*
 * Converts a schema delta into a list of low-level schema change operations.
 * Manages ordering requirements (eg. must delete a constraint before you can
 * remove a field).
 */
public class SchemaMigrationPlanGenerator extends RegAwareServiceBase {
	private boolean isMemDB;

	public SchemaMigrationPlanGenerator(DTypeRegistry registry, FactoryService factorySvc, DBType dbType) {
		super(registry, factorySvc);
		this.isMemDB = DBType.MEM.equals(dbType);
	}
	
	public List<SchemaChangeOperation> generate(SchemaDelta delta) {
		List<SchemaChangeOperation> opList = new ArrayList<>();
		
		//tbl I
		for(SxTypeDelta td: delta.typesI) {
			SchemaChangeOperation op = createAndAdd(opList, OperationType.TABLE_ADD); 
			op.typeName = td.typeName;
			op.fieldName = null;
			op.newName = null;
			op.fieldType = null;
			op.sizeof = 0;
			op.flags = null;
			op.otherName = null; //index or constraint
			op.typeInfo = td.info;
			op.fieldInfo = null;
		}
		
		//tbl U
		for(SxTypeDelta td: delta.typesU) {
			SchemaChangeOperation op = createAndAdd(opList, OperationType.TABLE_RENAME); 
			op.typeName = td.typeName;
			op.typeInfo = td.info;
			op.newName = td.nmDelta; //should be non null!!
			
			doFields(opList, td);
		}
		
		
		//tbl D
		for(SxTypeDelta td: delta.typesD) {
			SchemaChangeOperation op = createAndAdd(opList, OperationType.TABLE_DELETE); 
			op.typeName = td.typeName;
			op.typeInfo = td.info;
		}
		
		//other
		//TODO
		
		
		return opList;
	}

	private void doFields(List<SchemaChangeOperation> opList, SxTypeDelta td) {
		
		for(SxFieldDelta fd: td.fldsI) {
			SchemaChangeOperation op = createAndAdd(opList, OperationType.FIELD_ADD);
			initForField(op, fd, td);
		}
		
		doFieldUpdates(opList, td);
		
		for(SxFieldDelta fd: td.fldsD) {
			SchemaChangeOperation op = createAndAdd(opList, OperationType.FIELD_DELETE); 
			initForField(op, fd, td);
		}
	}

	private void initForField(SchemaChangeOperation op, SxFieldDelta fd, SxTypeDelta td) {
		op.typeName = td.typeName;
		op.typeInfo = td.info;
		op.fieldName = fd.fieldName;
		op.newName = null;
		if (fd.info == null) {
			op.fieldType = null;
			op.sizeof = 0;
			op.flags = null;
		} else {
			op.fieldType = fd.info.t;
			op.sizeof = fd.info.sz;
			op.flags = fd.info.flgs;
		}
		op.otherName = null; //index or constraint
		op.typeInfo = td.info;
		op.fieldInfo = fd.info;
	}

	private void doFieldUpdates(List<SchemaChangeOperation> opList, SxTypeDelta td) {
		for(SxFieldDelta fd: td.fldsU) {
			if (fd.fDelta != null) {
				SchemaChangeOperation op = createAndAdd(opList, OperationType.FIELD_RENAME); 
				initForField(op, fd, td);
				op.newName = fd.fDelta;
			}
			
			if (fd.flgsDelta != null) {
				SchemaChangeOperation op = createAndAdd(opList, OperationType.FIELD_ALTER); 
				initForField(op, fd, td);
				op.flags = fd.flgsDelta;
			}
			
			if (fd.szDelta != null) {
				SchemaChangeOperation op = createAndAdd(opList, OperationType.FIELD_ALTER_TYPE); 
				initForField(op, fd, td);
				op.sizeof = fd.szDelta;
			}
			
			if (fd.tDelta != null) {
				SchemaChangeOperation op = createAndAdd(opList, OperationType.FIELD_ALTER_TYPE); 
				initForField(op, fd, td);
				op.fieldType = fd.tDelta;
			}
		}
	}

	private SchemaChangeOperation createAndAdd(List<SchemaChangeOperation> opList, OperationType opType) {
		SchemaChangeOperation op = new SchemaChangeOperation(opType);
		opList.add(op);
		return op;
	}
}