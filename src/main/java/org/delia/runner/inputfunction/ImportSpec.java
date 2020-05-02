package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.List;

import org.delia.type.DStructType;

public class ImportSpec {
	public DStructType structType;
	public List<InputFieldHandle> ifhList = new ArrayList<>();
	public List<OutputFieldHandle> ofhList = new ArrayList<>();
	public List<OutputFieldHandle> unMappedOfhList = new ArrayList<>();
}