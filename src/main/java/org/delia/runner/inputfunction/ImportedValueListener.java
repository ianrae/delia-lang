package org.delia.runner.inputfunction;

import java.util.List;

import org.delia.error.DeliaError;
import org.delia.type.DValue;

public interface ImportedValueListener {
	void valueImported(DValue dval, List<DeliaError> errL);
}
