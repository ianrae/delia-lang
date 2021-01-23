package org.delia.hld.cud;

import java.util.*;

import org.delia.hld.HLDStatement;
import org.delia.hld.simple.SimpleBase;

public class HLDInsertStatement extends HLDStatement {
	public HLDInsert hldinsert;
	public List<SimpleBase> moreL = new ArrayList<>();

	@Override
	public String toString() {
		return hldinsert.toString();
	}
	
}
