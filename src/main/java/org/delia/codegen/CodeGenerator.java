package org.delia.codegen;

import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public interface CodeGenerator {
	 void setOptions(CodeGeneratorOptions options);
	 void setRegistry(DTypeRegistry registry);
	 void setPackageName(String packageName);
     boolean canProcess(DType dtype);
     String buildJavaFileName(DType dtype);
     String generate(DType dtype);
}