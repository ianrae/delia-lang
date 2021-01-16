package org.delia.db.newhls.cud;

import java.util.*;

import org.delia.db.newhls.HLDStatement;
import org.delia.db.newhls.simple.SimpleBase;

public class HLDUpdateStatement extends HLDStatement {
	public HLDUpdate hldupdate;
	public List<HLDUpdate> updateL = new ArrayList<>();
//	public List<HLDInsert> assocInsertL;
	public List<AssocBundle> assocBundleL;
	public List<SimpleBase> moreL = new ArrayList<>();
}
