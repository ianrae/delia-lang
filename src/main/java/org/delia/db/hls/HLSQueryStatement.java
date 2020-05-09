package org.delia.db.hls;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.compiler.ast.QueryExp;

public class HLSQueryStatement implements HLSElement {
	public List<HLSQuerySpan> hlspanL = new ArrayList<>();
	public QueryExp queryExp;
	
	public HLSQuerySpan getMainHLSSpan() {
		return hlspanL.get(0);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",");
		for(HLSQuerySpan hlspan: hlspanL) {
			joiner.add(hlspan.toString());
		}
		return joiner.toString();
	}
}