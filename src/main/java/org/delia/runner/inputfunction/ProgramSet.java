package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.type.DStructType;

public class ProgramSet {
	public Map<String,ProgramSpec> fieldMap = new ConcurrentHashMap<>();
	public HdrInfo hdr;
	public List<DStructType> outputTypes = new ArrayList<>();
}