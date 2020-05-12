package org.delia.db.hls;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class SUBElement implements HLSElement {
	public List<String> fetchL = new ArrayList<>();
	public List<String> fksL = new ArrayList<>();
	public boolean allFKs = false;
	
	public boolean containsFetch() {
		return !fetchL.isEmpty();
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",");
		for(String s: fetchL) {
			joiner.add(s);
		}
		for(String s: fksL) {
			joiner.add(s);
		}
		String s2 = String.format(",%s", joiner.toString());
		if (s2.equals(",")) {
			s2 = "";
		}
		String s = String.format("SUB:%b%s", allFKs, s2);
		return s;
	}

	public boolean isEmpty() {
		return fetchL.isEmpty() && fksL.isEmpty() && !allFKs;
	}
}