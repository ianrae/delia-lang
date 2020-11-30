package org.delia.db;

/**
 * Describes the abilities of the database.
 * @author Ian Rae
 *
 */
public class DBCapabilties {
	private boolean requiresSchemaMigration;
	private boolean supportsReferentialIntegrity; //on FKs
	private boolean supportsOrderBy;
	private boolean supportsOffsetAndLimit;
	
	public DBCapabilties(boolean requiresSchemaMigration, boolean supportsReferentialIntegrity, 
			boolean supportsOrderBy, boolean supportsOffsetAndLimit) {
		super();
		this.requiresSchemaMigration = requiresSchemaMigration;
		this.supportsReferentialIntegrity = supportsReferentialIntegrity;
		this.supportsOrderBy = supportsOrderBy;
		this.supportsOffsetAndLimit = supportsOffsetAndLimit;
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

	public boolean supportsOrderBy() {
		return supportsOrderBy;
	}

	public boolean supportsOffsetAndLimit() {
		return supportsOffsetAndLimit;
	}

}