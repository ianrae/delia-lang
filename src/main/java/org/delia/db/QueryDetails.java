package org.delia.db;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.hls.JoinFrag;

public class QueryDetails {
	public boolean mergeRows;
	public List<String> mergeOnFieldL = new ArrayList<>();
	public boolean isManyToMany;
	public List<JoinFrag> joinFragL = new ArrayList<>();
}
