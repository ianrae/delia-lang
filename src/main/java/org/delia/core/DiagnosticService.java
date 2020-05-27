package org.delia.core;

import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public interface DiagnosticService {
	void configure(String filter);
	String getConfigue();
	
	boolean isActive(String filterIdStr);
	void log(DTypeRegistry registry, DValue dval);
}
