package org.delia.core;

import org.delia.lld.LLD;
import org.delia.type.DTypeRegistry;

public interface ConfigureService {

    boolean validate(String varName);

    void execute(LLD.LLConfigure exp, DTypeRegistry registry, Object sprigSvc);

    boolean isPopulateFKsFlag();

    void setPopulateFKsFlag(boolean b);
}
