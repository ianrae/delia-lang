package org.delia.db.newhls.cud;

import java.util.*;

import org.delia.db.newhls.HLDStatement;

public class HLDUpdateStatement extends HLDStatement {
	public HLDUpdate hldupdate;
	public List<HLDUpdate> updateL = new ArrayList<>();
//	public List<HLDInsert> assocInsertL;
	public List<AssocBundle> assocBundleL;
}
