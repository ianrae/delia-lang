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
			//Note. td may be here just as a carrier of field changes.
			if (td.nmDelta != null) {
				SchemaChangeOperation op = createAndAdd(opList, OperationType.TABLE_RENAME); 
				op.typeName = td.typeName;
				op.typeInfo = td.info;
				op.newName = td.nmDelta; 
			}
			
			doFields(opList, td);
		}
		
		
		//tbl D
		for(SxTypeDelta td: delta.typesD) {
			SchemaChangeOperation op = createAndAdd(opList, OperationType.TABLE_DELETE); 
			op.typeName = td.typeName;
			op.typeInfo = td.info;
		}
		
		//----- others ------------
		for(SxOtherDelta oth: delta.othersI) {
			SchemaChangeOperation op = createAndAdd(opList, OperationType.CONSTRAINT_ADD);
			op.typeName = oth.typeName;
			op.otherName = oth.name;
			op.argsL = oth.newArgs;
		}
		
		for(SxOtherDelta oth: delta.othersU) {
			SchemaChangeOperation op = createAndAdd(opList, OperationType.CONSTRAINT_ALTER); 
			op.typeName = oth.typeName;
			op.otherName = oth.name;
			op.argsL = oth.newArgs;
		}
		
		for(SxOtherDelta oth: delta.othersD) {
			SchemaChangeOperation op = createAndAdd(opList, OperationType.CONSTRAINT_DELETE); 
			op.typeName = oth.typeName;
			op.otherName = oth.name;
			op.argsL = oth.newArgs;
		}
		
		return opList;
	}

	private void doFields(List<SchemaChangeOperation> opList, SxTypeDelta td) {
		
		for(SxFieldDelta fd: td.fldsI) {
			SchemaChangeOperation op = createAndAdd(opList, OperationType.FIELD_ADD);
			initForField(op, fd, td);
			op.canCreateAssocTable = fd.canCreateAssocTable; //true for 2nd one

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
				if (fd.assocUpdateStm != null) {
					SchemaChangeOperation op = createAndAdd(opList, OperationType.FIELD_RENAME_MANY_TO_MANY); 
					initForField(op, fd, td);
					op.newName = fd.fDelta;
					op.assocUpdateStm = fd.assocUpdateStm;
				} else {
					SchemaChangeOperation op = createAndAdd(opList, OperationType.FIELD_RENAME); 
					initForField(op, fd, td);
					op.newName = fd.fDelta;
				}
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