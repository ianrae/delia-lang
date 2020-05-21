package org.delia.codegen;

import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.StringUtil;

public class ImmutCodeGen extends CodeGenBase {

	public ImmutCodeGen(DTypeRegistry registry) {
		super(registry);
	}

	public String generate(DStructType structType) {
		String typeName = structType.getName();

		StrCreator sc = new StrCreator();
		sc.o("public class %sImmut implements %s {", typeName, typeName);
		sc.nl();
		line(sc, "  private DValue dval;");
		line(sc, "  private DStructHelper helper;");
		sc.nl();

		sc.o("  %sImmut(DValue dval) {", typeName);
		sc.nl();
		line(sc, "  this.dval = dval;");
		line(sc, "	this.helper = dval.asStruct();");

		line(sc, "@Override");
		line(sc, "public DValue internalDValue() {");
		line(sc, "  return dval;");
		line(sc, "}");

		for(String fieldName: structType.getDeclaredFields().keySet()) {
			DType ftype = structType.getDeclaredFields().get(fieldName);

			String javaType = convertToJava(ftype);
			String asFn = convertToAsFn(ftype);
			sc.nl();
			line(sc, "@Override");
			sc.o("public %s get%s() {", javaType, StringUtil.uppify(fieldName));
			sc.nl();
			sc.o(" return helper.getField(\"%s\").%s();", fieldName, asFn);
			sc.nl();
			line(sc, "}");

		}
		sc.o("}");
		sc.nl();

		return sc.str;
	}

	private void line(StrCreator sc, String str) {
		sc.o("%s\n", str);
	}

}