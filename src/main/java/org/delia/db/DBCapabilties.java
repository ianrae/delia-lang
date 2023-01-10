package org.delia.db;

/**
 * Describes the abilities of the database.
 * @author Ian Rae
 *
 */
public class DBCapabilties {
	private boolean requiresSchemaMigration;
	private boolean supportsReferentialIntegrity; //on FKs
	private boolean supportsUniqueConstraint;
	private String defaultSchema;

	public boolean isRequiresSql() {
		return requiresSql;
	}

	private boolean requiresSql;
	
	public DBCapabilties(boolean requiresSchemaMigration, boolean supportsReferentialIntegrity,
                         boolean supportsUniqueConstraint, boolean requiresSql, String defaultSchema) {
		super();
		this.requiresSchemaMigration = requiresSchemaMigration;
		this.supportsReferentialIntegrity = supportsReferentialIntegrity;
		this.supportsUniqueConstraint = supportsUniqueConstraint;
		this.requiresSql = requiresSql;
		this.defaultSchema = defaultSchema;
	}

	public boolean requiresSchemaMigration() {
		return requiresSchemaMigration;
	}
	public void setRequiresSchemaMigration(boolean b) {
		requiresSchemaMigration = b;
	}

	public boolean supportsReferentialIntegrity() {
		return supportsReferentialIntegrity;
	}

	public boolean supportsUniqueConstraint() {
		return supportsUniqueConstraint;
	}

	public String getDefaultSchema() {
		return defaultSchema;
	}
}