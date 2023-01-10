package org.delia.compiler;

import org.delia.hld.HLD;
import org.delia.type.DTypeRegistry;

import java.util.List;

public interface BuildCallback {
    void doCallback(List<HLD.HLDStatement> hldStatements, DTypeRegistry registry);
}
