package org.delia.db.newhls;

import org.delia.compiler.ast.QueryExp;

public interface HLDQueryBuilderAdapter {
	HLDQuery buildQuery(QueryExp queryExp);
}
