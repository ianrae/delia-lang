package org.delia.error;
/**
 * A validation error is an error in input data, usually
 * related to domain business rules.
 * 
 * Validation errors are usually displayed to the user.
 * 
 * @author ian
 *
 */
public class ValidationError extends DeliaError {

	public ValidationError(String id, String msg) {
		super(id, msg);
	}

}
