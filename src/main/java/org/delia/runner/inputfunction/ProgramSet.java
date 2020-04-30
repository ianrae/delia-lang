package org.delia.runner.inputfunction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProgramSet {
	public Map<String,ProgramSpec> map = new ConcurrentHashMap<>();
	public HdrInfo hdr;
}