package org.delia.runner;

import java.util.Collections;
import java.util.List;

import org.delia.error.DeliaError;

public class DeliaException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<DeliaError> errorL;
	
	public DeliaException(DeliaError err) {
		super(err.getId() + ": " + err.getMsg());
		this.errorL = Collections.singletonList(err);
	}
	public DeliaException(List<DeliaError> errL) {
		super(errL.get(0).getId() + ": " + errL.get(0).getMsg()); //TODO: what if errL empty??
		this.errorL = errL;
	}
	public DeliaError getLastError() {
		return errorL.get(errorL.size() - 1);
	}
}
