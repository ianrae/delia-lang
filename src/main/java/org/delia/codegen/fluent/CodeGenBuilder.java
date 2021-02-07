package org.delia.codegen.fluent;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.delia.api.DeliaSession;
import org.delia.api.DeliaSimpleStarter;
import org.delia.core.FactoryService;
import org.delia.type.DTypeRegistry;

public class CodeGenBuilder {
	boolean allTypes;
	List<String> theseTypes;
	DTypeRegistry registry;
	FactoryService factorySvc;
	
	public static CodeGenBuilder create(DeliaSession session) {
		CodeGenBuilder builder = new CodeGenBuilder(session.getRegistry(), session.getDelia().getFactoryService());
		return builder;
	}
	public static CodeGenBuilder create(String deliaSrc) {
		DeliaSimpleStarter simpleStarter = new DeliaSimpleStarter();
		DeliaSession session = simpleStarter.execute(deliaSrc);
		CodeGenBuilder builder = new CodeGenBuilder(session.getRegistry(), session.getDelia().getFactoryService());
		return builder;
	}
	public static CodeGenBuilder create(FileReader reader) throws IOException {
		DeliaSimpleStarter simpleStarter = new DeliaSimpleStarter();
		DeliaSession session = simpleStarter.execute(reader);
		CodeGenBuilder builder = new CodeGenBuilder(session.getRegistry(), session.getDelia().getFactoryService());
		return builder;
	}
	
	public CodeGenBuilder(DTypeRegistry registry, FactoryService factorySvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
	}
	
	public CGBuilder2 allTypes() {
		allTypes = true;
		return new CGBuilder2(this);
	}
	public CGBuilder2 theseTypes(List<String> typeNames) {
		theseTypes = typeNames;
		return new CGBuilder2(this);
	}
}