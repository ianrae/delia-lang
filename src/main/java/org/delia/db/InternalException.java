package org.delia.db;

import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;

/**
 * something bad happened in a dbinterface, that we don't want
 * to throw to client.
 * Should be caught and handled inside dbinterface.
 * 
 * @author Ian Rae
 *
 */
public class InternalException extends DeliaException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InternalException(DeliaError err) {
		super(err);
	}
}