package org.delia.codegen.fluent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.delia.DeliaLoader;
import org.delia.DeliaSession;
import org.delia.api.DeliaSimpleStarter;
import org.delia.codegen.CodeGeneratorService;
import org.delia.codegen.generators.DaoBaseCodeGen;
import org.delia.codegen.generators.DaoCodeGen;
import org.delia.core.FactoryService;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

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
	public static CodeGenBuilder createFromResource(String resourcePath) throws IOException {
		DeliaLoader loader = new DeliaLoader();
		String src = loader.fromResource(resourcePath);
		DeliaSimpleStarter simpleStarter = new DeliaSimpleStarter();
		DeliaSession session = simpleStarter.execute(src);
		CodeGenBuilder builder = new CodeGenBuilder(session.getRegistry(), session.getDelia().getFactoryService());
		return builder;
	}
	public static void createDaoAndEntities(String deliaSrc, String baseDaoPackageName, File baseSrcDir) {
		DeliaSimpleStarter simpleStarter = new DeliaSimpleStarter();
		DeliaSession session = simpleStarter.execute(deliaSrc);
		createDaoAndEntities(session, baseDaoPackageName, baseSrcDir);
	}
	public static void createDaoAndEntities(DeliaSession session, String baseDaoPackageName, File baseSrcDir) {
		FactoryService factorySvc = session.getDelia().getFactoryService();

		//step 1. build entities in {basepackage}.entities
		String entityPackage = String.format("%s.entities", baseDaoPackageName);
		CodeGenBuilder builder = new CodeGenBuilder(session.getRegistry(), factorySvc);
		CodeGeneratorService gen = builder.allTypes().addStandardGenerators().toPackage(entityPackage).build();
		if (!gen.run(baseSrcDir)) {
			DeliaExceptionHelper.throwError("codegen-fail", "building entities failed");
		}
		
		//step 2. build base daos in {basepackage}.base
		String basePackage = String.format("%s.base", baseDaoPackageName);
		builder = new CodeGenBuilder(session.getRegistry(), factorySvc);
		DaoBaseCodeGen baseGen = new DaoBaseCodeGen(entityPackage);
		
		gen = builder.allTypes().addGenerator(baseGen).toPackage(basePackage).build();
		if (!gen.run(baseSrcDir)) {
			DeliaExceptionHelper.throwError("codegen-fail", "building base daos failed");
		}
		
		//step 3. build base daos in {basepackage}.base
		builder = new CodeGenBuilder(session.getRegistry(), factorySvc);
		DaoCodeGen daoGen = new DaoCodeGen(entityPackage, basePackage);
		gen = builder.allTypes().addGenerator(daoGen).toPackage(baseDaoPackageName).build();
		gen.getOptions().overrideIfExists = false;
		if (!gen.run(baseSrcDir)) {
			DeliaExceptionHelper.throwError("codegen-fail", "building daos failed");
		}
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