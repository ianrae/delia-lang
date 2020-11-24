package org.delia.codegen;

import java.io.File;

import org.delia.dao.DeliaGenericDao;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;
import org.delia.util.ResourceTextFileReader;
import org.delia.util.TextFileReader;

public class DeliaCodeGen {

	public void createEntityClasses(String deliaSrc, String packageName, String outputPath) {
		DeliaGenericDao dao = new DeliaGenericDao();  //uses MEM db 
		boolean b = dao.initialize(deliaSrc);
		if (! b) {
			DeliaExceptionHelper.throwError("codegen-failed", "dao.initialize() failed");
		}
		
		DTypeRegistry registry = dao.getMostRecentSession().getExecutionContext().registry;
		EntitySourceCodeGenerator codegen = new EntitySourceCodeGenerator(dao.getFactorySvc());
		b = codegen.createSourceFiles(registry, packageName, outputPath);
		if (! b) {
			DeliaExceptionHelper.throwError("codegen-failed", "codegen.createSourceFiles() failed");
		}
	}
	public void createEntityClasses(File srcFile, String packageName, String outputPath) {
		TextFileReader r = new TextFileReader();
		String path = srcFile.getAbsolutePath();
		String src = r.readFileAsSingleString(path);
		createEntityClasses(src, packageName, outputPath);
	}
	public void createEntityClassesFromResource(String resourcePath, String packageName, String outputPath) {
		ResourceTextFileReader r = new ResourceTextFileReader();
		String src = r.readAsSingleString(resourcePath);
		createEntityClasses(src, packageName, outputPath);
	}
	
}
