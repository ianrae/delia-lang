package org.delia.db.schema.modify;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.SqlStatement;

public class SchemaChangeOperation {
	public OperationType opType;
	public String typeName;
	public String fieldName;
	public String newName; //rename
	public String fieldType;
	public Integer sizeof;
	public String flags;
	public String otherName; //index or constraint
	public List<String> argsL = new ArrayList<>();
	public SxTypeInfo typeInfo; //when adding
	public SxFieldInfo fieldInfo; //when adding
	public boolean canCreateAssocTable; //only used in FIELD_ADD of MM relation field
//	public String assocTableName; //only used in FIELD_RENAME of MM relation field
	public SqlStatement assocUpdateStm; //only used for rename MM field
	public SqlStatement otherStm;
	public SxOtherInfo otherInfo;

	public SchemaChangeOperation(OperationType opType) {
		this.opType = opType;
	}

	@Override
	public String toString() {
		return String.format("%s: %s.%s", opType.name(), typeName, fieldName);
	}
}