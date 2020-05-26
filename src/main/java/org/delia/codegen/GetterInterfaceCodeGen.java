package org.delia.codegen;

import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.StringUtil;

public class GetterInterfaceCodeGen extends CodeGenBase {

	public GetterInterfaceCodeGen(DTypeRegistry registry, String packageName) {
		super(registry, packageName);
	}
	
	
	public String generate(DStructType structType) {
		String typeName = structType.getName();

		StrCreator sc = new StrCreator();
		addImports(sc, structType);
		sc.o("import org.delia.codegen.DeliaImmutable;");
		sc.nl();
		sc.nl();

		sc.o("public interface %s extends DeliaImmutable {", typeName);
		sc.nl();
		for(String fieldName: structType.getDeclaredFields().keySet()) {
			DType ftype = structType.getDeclaredFields().get(fieldName);

			String javaType = convertToJava(structType, fieldName);
			sc.o("  %s get%s();", javaType, StringUtil.uppify(fieldName));
			sc.nl();
			
			if (hasPK(ftype)) {
				String pkType = getPKType(ftype);
				sc.o("  %s get%sPK();", pkType, StringUtil.uppify(fieldName));
				sc.nl();
			}

		}
		sc.o("}");
		sc.nl();

		return sc.toString();
	}

}