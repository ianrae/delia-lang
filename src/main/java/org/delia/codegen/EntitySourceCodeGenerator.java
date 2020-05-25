package org.delia.codegen;

import java.util.Collections;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.TextFileWriter;

public class EntitySourceCodeGenerator extends ServiceBase {

	public EntitySourceCodeGenerator(FactoryService factorySvc) {
		super(factorySvc);
	}
	
	public boolean createSourceFiles(DTypeRegistry registry, String packageName, String targetDir) {
		for(DType dtype: registry.getOrderedList()) {
			if (! dtype.isStructShape()) {
				continue;
			}
			
			DStructType structType = (DStructType) dtype;
			doGetterInteface(structType, registry, packageName, targetDir);
			doImmutClass(structType, registry, packageName, targetDir);
			doSetterInterface(structType, registry, packageName, targetDir);
			doEntityClass(structType, registry, packageName, targetDir);
		}
		
		return true;
	}

	private void doGetterInteface(DStructType structType, DTypeRegistry registry, String packageName, String targetDir) {
		GetterInterfaceCodeGen gen = new GetterInterfaceCodeGen(registry, packageName);
		String java = gen.generate(structType);
		
		String filename = String.format("%s.java", structType.getName());
		writeFile(targetDir, filename, java);
	}
	private void doImmutClass(DStructType structType, DTypeRegistry registry, String packageName,
			String targetDir) {
		ImmutCodeGen gen = new ImmutCodeGen(registry, packageName);
		String java = gen.generate(structType);
		
		String filename = String.format("%sImmut.java", structType.getName());
		writeFile(targetDir, filename, java);
	}
	private void doSetterInterface(DStructType structType, DTypeRegistry registry, String packageName,
			String targetDir) {
		SetterInterfaceCodeGen gen = new SetterInterfaceCodeGen(registry, packageName);
		String java = gen.generate(structType);
		
		String filename = String.format("%sSetter.java", structType.getName());
		writeFile(targetDir, filename, java);
	}
	private void doEntityClass(DStructType structType, DTypeRegistry registry, String packageName,
			String targetDir) {
		EntityCodeGen gen = new EntityCodeGen(registry, packageName);
		String java = gen.generate(structType);
		
		String filename = String.format("%sEntity.java", structType.getName());
		writeFile(targetDir, filename, java);
	}

	private void writeFile(String targetDir, String filename, String java) {
		String path = String.format("%s/%s", targetDir, filename); //TODO fix / and \
		this.log.log("writing %s", path);
		TextFileWriter w = new TextFileWriter();
		w.writeFile(path, Collections.singletonList(java));
	}
}