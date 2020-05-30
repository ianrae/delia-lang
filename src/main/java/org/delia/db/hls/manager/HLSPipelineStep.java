package org.delia.db.hls.manager;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.db.hls.HLSQueryStatement;

public interface HLSPipelineStep {

	HLSQueryStatement execute(HLSQueryStatement hls, QueryExp queryExp, DatIdMap datIdMap);
}
