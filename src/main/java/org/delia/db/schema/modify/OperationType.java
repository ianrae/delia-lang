package org.delia.db.schema.modify;

//New migration plan
public enum OperationType {
	TABLE_ADD,
	TABLE_DELETE,
	TABLE_RENAME,
	FIELD_ADD,
	FIELD_DELETE,
	FIELD_RENAME,
	FIELD_ALTER, //flags
	FIELD_ALTER_TYPE, //includes size
	INDEX_ADD,
	INDEX_DELETE,
	INDEX_ALTER,
	CONSTRAINT_ADD,
	CONSTRAINT_DELETE,
	CONSTRAINT_ALTER,
}