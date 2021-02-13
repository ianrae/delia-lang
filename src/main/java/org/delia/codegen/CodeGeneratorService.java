package org.delia.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.RegAwareServiceBase;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.StringUtil;
import org.delia.util.TextFileWriter;

public class CodeGeneratorService extends RegAwareServiceBase {

	private List<String> typeNameL;
	private List<CodeGenerator> generatorL;
	private String packageName;
	private File outputDir;
	private StringBuilder sb;
	private CodeGeneratorOptions options = new CodeGeneratorOptions();
	
	public CodeGeneratorService(DTypeRegistry registry, FactoryService factorySvc, List<String> typeNames, List<CodeGenerator> generatorsL, String packageName) {
		super(registry, factorySvc);
		this.registry = registry;
		this.typeNameL = typeNames;
		this.generatorL = generatorsL;
		this.packageName = packageName;
	}

	public boolean run(File dir) {
		this.outputDir = dir;
		return doRun(true);
	}
	public boolean run(StringBuilder sb) {
		this.sb = sb;
		return doRun(false);
	}
	
	protected boolean doRun(boolean outputToFile) {
		
		if (outputToFile && options.createPackageDirs) {
			String dirs = buildFullPathIncludingPackageDirs(); 
			try {
				Path pp = Paths.get(dirs);
				Files.createDirectories(pp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(String typeName: typeNameL) {
			DType dtype = registry.getType(typeName);
			
			for(CodeGenerator gen: generatorL) {
				gen.setRegistry(registry);
				gen.setOptions(options);
				gen.setPackageName(packageName);
				if (!gen.canProcess(dtype)) {
					continue;
				}
				
				
				String fileName = gen.buildJavaFileName(dtype);
				String text = gen.generate(dtype);
				
				if (outputToFile) {
					writeToFile(fileName, text);
				} else {
					sb.append(text);
					sb.append(StringUtil.eol());
				}
			}
			
		}
		return true;
	}

	private String buildFullPathIncludingPackageDirs() {
		char sep = getDirSepChar();
		String dirs = packageName.replace('.', sep);
		
		String targetDir = outputDir.getAbsolutePath();
		String path = String.format("%s%s%s", targetDir, sep, dirs); 
		return path;
	}
	private char getDirSepChar() {
		boolean isWindows = StringUtil.eol().length() == 2;
		char sep = isWindows ? '\\' : '/';
		return sep;
	}

	private void writeToFile(String fileName, String text) {
		String targetDir = outputDir.getAbsolutePath();
		if (options.createPackageDirs) {
			targetDir = buildFullPathIncludingPackageDirs();
		}
		char sep = getDirSepChar();
		String path = String.format("%s%s%s", targetDir, sep, fileName); 
		if (! options.overrideIfExists) {
			File f = new File(path);
			if (f.exists()) {
				log.log("already exists. not writing %s", path);
				return;
			}
		}
		
		log.log("writing %s", path);
		TextFileWriter w = new TextFileWriter();
		w.writeFile(path, Collections.singletonList(text));
	}

	public CodeGeneratorOptions getOptions() {
		return options;
	}

	public void setOptions(CodeGeneratorOptions options) {
		this.options = options;
	}
}