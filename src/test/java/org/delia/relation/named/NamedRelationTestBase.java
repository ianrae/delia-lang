package org.delia.relation.named;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.DeliaException;
import org.delia.sort.topo.TopoTestBase;
import org.delia.type.DStructType;
import org.delia.util.DRuleHelper;

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
	
	public String expectNoException(Consumer<String> fn) {
		String errId = null;
		try {
			fn.accept(null);
		} catch (DeliaException e) {
			errId = e.getLastError().getId();
		}
		log.log("e: %s", errId);
		assertEquals(null, errId);
		return errId;
	}
	
	
	protected RelationOneRule getOneRule(String typeName, String relField) {
		DStructType dtype = (DStructType) sess.getExecutionContext().registry.getType(typeName);
		RelationOneRule rr = DRuleHelper.findOneRule(dtype, relField);
		return rr;
	}
	protected void chkRule(RelationOneRule rr, boolean b, String expected, String expectedOtherSide) {
		assertEquals(b, rr.nameIsExplicit);
		assertEquals(expected, rr.getRelationName());
		RelationInfo relinfo = rr.relInfo;
		assertEquals(expected, relinfo.relationName);
		assertEquals(expectedOtherSide, relinfo.otherSide.relationName);
	}
	protected void chkRuleOneSided(RelationOneRule rr, boolean b, String expected) {
		assertEquals(b, rr.nameIsExplicit);
		assertEquals(expected, rr.getRelationName());
		RelationInfo relinfo = rr.relInfo;
		assertEquals(expected, relinfo.relationName);
		assertEquals(null, relinfo.otherSide);
	}

}
