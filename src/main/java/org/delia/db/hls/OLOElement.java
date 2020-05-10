package org.delia.db.hls;

public class OLOElement implements HLSElement {
	public String orderBy; //may be null
	public Integer limit; //may be null
	public Integer offset; //may be null
	public boolean isAsc;

	@Override
	public String toString() {
		String s = String.format("OLO:%s,%d,%d", orderBy, limit, offset);
		return s;
	}
}