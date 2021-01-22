package org.delia.db.newhls.cud;

import java.util.*;

import org.delia.db.hld.HLDStatement;
import org.delia.db.newhls.simple.SimpleBase;

public class HLDInsertStatement extends HLDStatement {
	public HLDInsert hldinsert;
	public List<SimpleBase> moreL = new ArrayList<>();

	@Override
	public String toString() {
		return hldinsert.toString();
	}
	
}
