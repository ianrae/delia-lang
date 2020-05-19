package org.delia.relation.named;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.compiler.generate.SimpleFormatOutputGenerator;
import org.delia.log.LogLevel;
import org.delia.runner.ResultValue;
import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.util.render.ObjectRendererImpl;

public class MultipleRelationTestBase extends NamedRelationTestBase {
	
	protected void dumpDVal(DValue dvalC) {
		List<String> list = this.generateFromDVal(dvalC);
		for(String s: list) {
			log(s);
		}
	}

	//------------------------
	public void init() {
		super.init();
		enableAutoCreateTables();
		this.log.setLevel(LogLevel.INFO);
	}

	protected DValue chkRelation(DValue dvalA, String fieldName, Integer id) {
		DValue inner = dvalA.asStruct().getField(fieldName);
		if (id == null) {
			assertEquals(null, inner);
			return null;
		} else {
			DRelation drel = inner.asRelation();
			DValue dval2 = drel.getForeignKey();
			assertEquals(id.intValue(), dval2.asInt());
			return dval2;
		}
	}
	
	protected DValue doQuery(String src) {
		log("src: " + src);
		ResultValue res = delia.continueExecution(src, this.sess);
		DValue dval = res.getAsDValue();
		return dval;
	}
	protected void dumpObj(String title, Object obj) {
		log(title);
		ObjectRendererImpl ori = new ObjectRendererImpl();
		String json = ori.render(obj);
		log(json);
	}
	protected List<String> generateFromDVal(DValue dval) {
		SimpleFormatOutputGenerator gen = new SimpleFormatOutputGenerator();
		// ErrorTracker et = new SimpleErrorTracker(log);
		DeliaGeneratePhase phase = this.sess.getExecutionContext().generator;
		boolean b = phase.generateValue(gen, dval, "a");
		assertEquals(true, b);
		return gen.outputL;
	}

	protected void doInsert(String src) {
		ResultValue res = delia.continueExecution(src, this.sess);
		DValue dval = res.getAsDValue();
		assertEquals(null, dval);
	}
	protected void doUpdate(String src) {
		ResultValue res = delia.continueExecution(src, this.sess);
		Integer numAdded = (Integer) res.val;
		assertEquals(1, numAdded.intValue());
	}
	protected void doDelete(String src) {
		ResultValue res = delia.continueExecution(src, this.sess);
		DValue dval = res.getAsDValue();
		assertEquals(null, dval);
	}



}
