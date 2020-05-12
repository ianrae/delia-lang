package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViaLineInfo {
	List<ViaService.ViaInfo> viaL; //all via rows in input fn
	List<ViaPendingInfo> viaPendingL = new ArrayList<>();
	Map<String,Object> inputData; //so can hook things up
	
	public boolean hasRows() {
		return !viaL.isEmpty();
	}

}
