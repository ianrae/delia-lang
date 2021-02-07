//package org.delia.codegen;
//
//import java.util.List;
//
//import org.delia.db.sql.StrCreator;
//import org.delia.type.DStructType;
//import org.delia.type.DType;
//import org.delia.type.DTypeRegistry;
//import org.delia.util.StringUtil;
//
//
//public class GetterInterfaceCodeGen extends CodeGenBase {
//
//	public boolean addJsonIgnoreToRelations;
//
//
//	public GetterInterfaceCodeGen(DTypeRegistry registry, String packageName) {
//		super(registry, packageName);
//	}
//	
//	public String generate(DStructType structType) {
//		String typeName = structType.getName();
//
//		StrCreator sc = new StrCreator();
//		addImports(sc, structType);
//		sc.o("import org.delia.codegen.DeliaImmutable;");
//		sc.nl();
//		sc.nl();
//
//		String baseType = (structType.getBaseType() == null) ? "DeliaImmutable" : structType.getBaseType().getName();
//		sc.o("public interface %s extends %s {", typeName, baseType);
//		sc.nl();
//		for(String fieldName: structType.getDeclaredFields().keySet()) {
//			DType ftype = structType.getDeclaredFields().get(fieldName);
//
//			String javaType = convertToJava(structType, fieldName);
//			boolean hasPK = hasPK(ftype);
//			if (addJsonIgnoreToRelations && hasPK) {
//				sc.o("  @JsonIgnore");
//				sc.nl();
//			}
//			sc.o("  %s get%s();", javaType, StringUtil.uppify(fieldName));
//			sc.nl();
//			
//			if (hasPK) {
//				String pkType = getPKType(ftype);
//				sc.o("  %s get%sPK();", pkType, StringUtil.uppify(fieldName));
//				sc.nl();
//			}
//
//		}
//		sc.o("}");
//		sc.nl();
//
//		return sc.toString();
//	}
//
//
//	@Override
//	protected List<String> getImportList(DStructType structType) {
//		List<String> list = super.getImportList(structType);
//		if (this.addJsonIgnoreToRelations) {
//			list.add("import com.fasterxml.jackson.annotation.JsonIgnore;");
//		}
//		return list;
//	}
//
//}