package org.delia.dbimpl.mem.impl;

import org.delia.type.DValue;

import java.util.ArrayList;
import java.util.List;

public interface MemDBTable {
	String getName();
	List<DValue> getList();
	boolean needsSynchronizationOnTraverse(); //if true, then use synchronized(list) on list traversal
}