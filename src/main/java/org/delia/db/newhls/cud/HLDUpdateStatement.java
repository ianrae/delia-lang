package org.delia.db.newhls.cud;

import java.util.*;

import org.delia.db.hld.HLDStatement;
import org.delia.db.newhls.simple.SimpleBase;

public class HLDUpdateStatement extends HLDStatement {
	public HLDUpdate hldupdate;
	public List<AssocBundle> assocBundleL;
	public List<SimpleBase> moreL = new ArrayList<>();

	/**
	 * Return true if statement will do nothing (no need to execute on DB)
	 * @return
	 */
	public boolean isEmpty() {
		if (hldupdate.fieldL.isEmpty()) {
			return assocBundleL.isEmpty() && moreL.isEmpty();
		}
		return false;
	}
}
