package org.delia.db.newhls;

import org.delia.compiler.ast.QueryExp;
import org.delia.type.DStructType;

public interface HLDQueryBuilderAdapter {
	HLDQuery buildQuery(QueryExp queryExp);
	HLDQuery buildQueryEx(QueryExp queryExp, DStructType structType);
}
