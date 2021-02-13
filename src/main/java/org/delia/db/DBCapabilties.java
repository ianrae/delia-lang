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
	
	public DBCapabilties(boolean requiresSchemaMigration, boolean supportsReferentialIntegrity, 
			boolean supportsUniqueConstraint) {
		super();
		this.requiresSchemaMigration = requiresSchemaMigration;
		this.supportsReferentialIntegrity = supportsReferentialIntegrity;
		this.supportsUniqueConstraint = supportsUniqueConstraint;
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


}