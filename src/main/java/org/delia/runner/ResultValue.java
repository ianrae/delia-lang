package org.delia.runner;

import java.util.ArrayList;
import java.util.List;

import org.delia.error.DeliaError;
import org.delia.type.DValue;
import org.delia.type.Shape;

public class ResultValue {
	public boolean ok;
	public List<DeliaError> errors = new ArrayList<>();
	public Shape shape;
	public Object val;
	public String varName;
	
	public void copyFrom(ResultValue other) {
		this.errors.clear();
		this.errors.addAll(other.errors);
		this.ok = other.ok;
		this.shape = other.shape;
		this.val = other.val;
	}
	public void addIfNotNull(DeliaError err) {
		if (err != null) {
			errors.add(err);
		}
	}
	public DValue getAsDValue() {
		if (val instanceof QueryResponse) {
			QueryResponse qresp = (QueryResponse) val;
			if (qresp.dvalList == null) {
				return null; //err
			} else if (qresp.dvalList.size() != 1) {
				return null; //err
			} else {
				return qresp.dvalList.get(0);
			}
		}
		
		DValue dval = (DValue) val;
		return dval;
	}
	public int getDValueCount() {
		if (val instanceof QueryResponse) {
			QueryResponse qresp = (QueryResponse) val;
			if (qresp.dvalList == null) {
				return 0; //err
			} else {
				return qresp.dvalList.size();
			}
		}
		return 0;
	}
	public List<DValue> getAsDValueList() {
		if (val instanceof QueryResponse) {
			QueryResponse qresp = (QueryResponse) val;
			return qresp.dvalList;
		}
		return null;
	}
	public DeliaError getLastError() {
		return errors.isEmpty() ? null : errors.get(errors.size() - 1);
	}
	public boolean isSuccess() {
		return errors.isEmpty();
	}
}