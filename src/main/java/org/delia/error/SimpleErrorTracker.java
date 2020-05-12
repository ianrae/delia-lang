package org.delia.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.delia.log.Log;


public class SimpleErrorTracker implements ErrorTracker {
	private List<DeliaError> errL = Collections.synchronizedList(new ArrayList<>());
	private Log log;

	public SimpleErrorTracker(Log log) {
		this.log = log;
	}
	@Override
	public int errorCount() {
		return errL.size();
	}
	@Override
	public boolean areNoErrors() {
		return errL.isEmpty();
	}
	@Override
	public void add(DeliaError err) {
		errL.add(err);

		String s = err.toString();
		String errType = (err instanceof ValidationError) ? "VALERROR" : "ERROR";
		log.logError("%s: %s", errType, s);
	}
	@Override
	public DeliaError addNoLog(DeliaError err) {
		errL.add(err); //don't log the error
		return err;
	}
	
	@Override
	public void clear() {
		errL.clear();
	}
	@Override
	public DeliaError add(String id, String msg) {
		DeliaError err = new DeliaError(id, msg);
		this.add(err);
		return err;
	}
	@Override
	public void dump() {
		//when pipe err propogated up we can get duplicates
		List<DeliaError> already = new ArrayList<>();
		for(DeliaError err: errL) {
			if (already.contains(err)) {
//				log.log("dup");
			} else {
				String s = err.toString();
				if (err instanceof ValidationError) {
					log.log("VALERROR: %s", s);
				} else {
					log.log("ERROR: %s", s);
				}
				already.add(err);
			}
		}
	}
	@Override
	public List<DeliaError> getErrors() {
		return errL;
	}
	public List<DeliaError> getErrorsSinceMark(int mark) {
		List<DeliaError> list = new ArrayList<>();
		for(int i = mark; i < errL.size(); i++) {
			list.add(errL.get(i));
		}
		return list;
	}
	@Override
	public void copyErrorsFrom(ErrorTracker et, ErrorCopyFilter filter) {
		switch(filter) {
		case ALL:
			errL.addAll(et.getErrors());
			break;
		case VALIDATION_ERRORS_ONLY: 
			for(ValidationError err: et.getValidationErrors()) {
				errL.add(err);
			}
			break;
		case PLAT_ERRORS_ONLY: 
			for(DeliaError err: et.getErrors()) {
				if (err instanceof ValidationError) {
				} else {
					errL.add(err);
				}
			}
			break;
		}
	}
	
	@Override
	public List<ValidationError> getValidationErrors() {
		List<ValidationError> list = new ArrayList<>();
		for(DeliaError err: errL) {
			if (err instanceof ValidationError) {
				list.add((ValidationError) err);
			}
		}
		return list;
	}
	@Override
	public DeliaError getLastError() {
		if (errL.isEmpty()) {
			return null;
		}
		return errL.get(errL.size() - 1);
	}
	@Override
	public void addAll(List<DeliaError> errL) {
		for(DeliaError err: errL) {
			add(err);
		}
	}
}