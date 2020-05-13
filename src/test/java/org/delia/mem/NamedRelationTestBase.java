package org.delia.mem;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import org.delia.runner.DeliaException;
import org.delia.sort.topo.TopoTestBase;

public class NamedRelationTestBase extends TopoTestBase {
	
	public String expectException(String expectedErrId, Consumer<String> fn) {
		String errId = null;
		try {
			fn.accept(expectedErrId);
		} catch (DeliaException e) {
			errId = e.getLastError().getId();
		}
		log.log("e: %s", errId);
		assertEquals(expectedErrId, errId);
		return errId;
	}
	
}
