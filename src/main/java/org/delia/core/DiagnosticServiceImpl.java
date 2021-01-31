package org.delia.core;

import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.compiler.generate.SimpleFormatOutputGenerator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.StringUtil;

public class DiagnosticServiceImpl extends ServiceBase implements DiagnosticService {
	private String filterStr = "";
	
	public DiagnosticServiceImpl(FactoryService factorySvc) {
		super(factorySvc);
	}

	@Override
	public void configure(String filter) {
		filterStr = filter;
	}

	@Override
	public String getConfigue() {
		return filterStr;
	}

	@Override
	public boolean isActive(String filterId) {
		return filterStr.contains(filterId);
	}

	@Override
	public void log(String filterId, DValue dval, DTypeRegistry registry) {
		SimpleFormatOutputGenerator gen = new SimpleFormatOutputGenerator();
		gen.includeVPrefix = false;
		gen.truncateLargeBlob = true; //avoid large log lines
		DeliaGeneratePhase phase = new DeliaGeneratePhase(factorySvc, registry);
		boolean b = phase.generateValue(gen, dval, "a");
		String s = StringUtil.flattenNoComma(gen.outputL);
		int pos = s.indexOf('{');
		String ss = s.substring(pos);
		log.log("dval(%s): %s", filterId, ss);
	}
	
}
