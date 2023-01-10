package org.delia.sql;

import org.delia.error.DeliaError;
import org.delia.error.DetailedError;

import java.util.ArrayList;
import java.util.List;

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
	public ValueException(List<DetailedError> detailedErrL) {
		super(detailedErrL.get(0).getMsg());
		errL = new ArrayList<>();
		for(DeliaError err: detailedErrL) {
			errL.add(err);
		}
	}
}