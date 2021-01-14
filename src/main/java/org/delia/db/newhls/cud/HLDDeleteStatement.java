package org.delia.db.newhls.cud;

import java.util.*;

import org.delia.db.newhls.simple.SimpleBase;

public class HLDDeleteStatement {
	public HLDDelete hlddelete;
	public List<HLDUpdate> updateL = new ArrayList<>();
	public List<HLDDelete> deleteL = new ArrayList<>();
	public List<SimpleBase> moreL = new ArrayList<>();
}
