package org.delia.db;

import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;

/**
 * database reports a validation error such as JdbcSQLIntegrityConstraintViolationException.
 * These occur when we let the db do part of validation.
 * @author Ian Rae
 *
 */
public class DBValidationException extends DeliaException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DBValidationException(DeliaError err) {
		super(err);
	}
}