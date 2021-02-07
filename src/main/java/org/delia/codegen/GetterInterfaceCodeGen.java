package org.delia.codegen;

import java.util.List;

import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.util.StringUtil;

public class GetterInterfaceCodeGen extends CodeGenBase {

	public GetterInterfaceCodeGen() {
		super(true);
	}
	
	@Override
	public String buildJavaFileName(DType dtype) {
		String filename = String.format("%s.java", dtype.getName());
		return filename;
	}

	@Override
	public String generate(DType typeParam) {
		DStructType structType = (DStructType) typeParam; //structTypesOnly is true
		String typeName = structType.getName();

		StrCreator sc = new StrCreator();
		addDoNotModifyComment(sc);
		helper().addImports(sc, getImportList(structType));
		sc.o("import org.delia.codegen.DeliaImmutable;");
		sc.nl();
		sc.nl();

		String baseType = (structType.getBaseType() == null) ? "DeliaImmutable" : structType.getBaseType().getName();
		sc.o("public interface %s extends %s {", typeName, baseType);
		sc.nl();
		for(String fieldName: structType.getDeclaredFields().keySet()) {
			DType ftype = structType.getDeclaredFields().get(fieldName);

			String javaType = helper().convertToJava(structType, fieldName);
			boolean hasPK = helper().hasPK(ftype);
			if (options.addJsonIgnoreToRelations && hasPK) {
				sc.o("  @JsonIgnore");
				sc.nl();
			}
			sc.o("  %s get%s();", javaType, StringUtil.uppify(fieldName));
			sc.nl();
			
			if (hasPK) {
				String pkType = helper().getPKType(ftype);
				sc.o("  %s get%sPK();", pkType, StringUtil.uppify(fieldName));
				sc.nl();
			}

		}
		sc.o("}");
		sc.nl();

		return sc.toString();
	}

	protected List<String> getImportList(DStructType structType) {
		List<String> list = helper().getImportList(structType);
		if (options.addJsonIgnoreToRelations) {
			list.add("import com.fasterxml.jackson.annotation.JsonIgnore;");
		}
		return list;
	}
}