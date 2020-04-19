package org.delia.db.memdb;

import java.util.ArrayList;
import java.util.List;

import org.delia.type.DValue;

public class MemDBTable {
	public String name;
	public List<DValue> rowL = new ArrayList<>();
}