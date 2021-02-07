package org.delia.codegen.generators;

import org.delia.codegen.CodeGenBase;
import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.util.StringUtil;

public class SetterInterfaceCodeGen extends CodeGenBase {

	public SetterInterfaceCodeGen() {
		super(true);
	}
	
	@Override
	public String buildJavaFileName(DType dtype) {
		String filename = String.format("%sSetter.java", dtype.getName());
		return filename;
	}

	@Override
	public String generate(DType typeParam) {
		DStructType structType = (DStructType) typeParam; //structTypesOnly is true
		String typeName = structType.getName();

		StrCreator sc = new StrCreator();
		addDoNotModifyComment(sc);
		helper().addImports(sc, structType);
		sc.o("import org.delia.codegen.DeliaEntity;");
		sc.nl();
		sc.nl();
		
		String baseType = (structType.getBaseType() == null) ? "DeliaEntity" : structType.getBaseType().getName();
		sc.o("public interface %s extends %s {", typeName + "Setter", baseType);
		sc.nl();
		for(String fieldName: structType.getDeclaredFields().keySet()) {
			DType ftype = structType.getDeclaredFields().get(fieldName);

			String javaType = helper().convertToJava(structType, fieldName);
			sc.o("  void set%s(%s val);", StringUtil.uppify(fieldName), javaType);
			sc.nl();
		}
		sc.o("}");
		sc.nl();

		return sc.toString();
	}
}