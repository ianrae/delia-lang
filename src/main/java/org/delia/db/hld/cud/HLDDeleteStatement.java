package org.delia.db.hld.cud;

import java.util.*;

import org.delia.db.hld.HLDStatement;
import org.delia.db.hld.simple.SimpleBase;

public class HLDDeleteStatement extends HLDStatement {
	public HLDDelete hlddelete;
	public List<SimpleBase> moreL = new ArrayList<>();
}