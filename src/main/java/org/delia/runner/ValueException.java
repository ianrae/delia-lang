package org.delia.runner;

import java.util.ArrayList;
import java.util.List;

import org.delia.error.DeliaError;

//TODO: remove this class. not needed
public class ValueException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public List<DeliaError> errL;
	
	public ValueException(DeliaError err) {
		super(err.getMsg());
		errL = new ArrayList<>();
		this.errL.add(err);
	}
	public ValueException(List<DeliaError> errL) {
		super(errL.get(0).getMsg());
		this.errL = errL;
	}
}