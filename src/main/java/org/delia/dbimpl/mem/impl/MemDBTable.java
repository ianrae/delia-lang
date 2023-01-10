package org.delia.dbimpl.mem.impl;

import org.delia.type.DValue;

import java.util.ArrayList;
import java.util.List;

public class MemDBTable {
	public String name;
	public List<DValue> rowL = new ArrayList<>();

	public MemDBTable(String name) {
		this.name = name;
	}
}