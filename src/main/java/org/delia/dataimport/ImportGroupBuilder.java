package org.delia.dataimport;

import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.runner.inputfunction.GroupPair;
import org.delia.runner.inputfunction.LineObjIterator;

public class ImportGroupBuilder extends ServiceBase {
	private List<GroupPair> groupL = new ArrayList<>();

	public ImportGroupBuilder(FactoryService factorySvc) {
		super(factorySvc);
	}
	
	public void addImport(String inputFunctionName, LineObjIterator iter) {
		GroupPair pair = new GroupPair();
		pair.inputFnName = inputFunctionName;
		pair.iter = iter;
		this.groupL.add(pair);
	}

	public List<GroupPair> getGroupL() {
		return groupL;
	}
}