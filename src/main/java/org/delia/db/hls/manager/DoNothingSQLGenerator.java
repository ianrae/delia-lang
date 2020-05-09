package org.delia.db.hls.manager;

import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.HLSSQLGenerator;
import org.delia.type.DTypeRegistry;

/**
 * MEM doesn't need sql. Only for some unit tests.
 * @author Ian Rae
 *
 */
public class DoNothingSQLGenerator implements HLSSQLGenerator {
	private HLSSQLGenerator inner; //may be null

	public DoNothingSQLGenerator(HLSSQLGenerator inner) {
		this.inner = inner;
	}
	@Override
	public String buildSQL(HLSQueryStatement hls) {
		if (inner != null) {
			return inner.buildSQL(hls);
		}
		return null;
	}

	@Override
	public String processOneStatement(HLSQuerySpan hlspan, boolean forceAllFields) {
		if (inner != null) {
			return inner.processOneStatement(hlspan, forceAllFields);
		}
		return null;
	}

	@Override
	public void setRegistry(DTypeRegistry registry) {
		if (inner != null) {
			inner.setRegistry(registry);
		}
	}
	
}