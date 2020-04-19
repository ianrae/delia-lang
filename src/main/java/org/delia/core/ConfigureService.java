package org.delia.core;

import org.delia.compiler.ast.ConfigureStatementExp;
import org.delia.type.DTypeRegistry;

public interface ConfigureService {

	boolean validate(String varName);
	void execute(ConfigureStatementExp exp, DTypeRegistry registry, Object sprigSvc);
	boolean isPopulateFKsFlag();
	void setPopulateFKsFlag(boolean b);
}
