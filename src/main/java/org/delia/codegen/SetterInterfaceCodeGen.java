package org.delia.codegen;

import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.StringUtil;

public class SetterInterfaceCodeGen extends CodeGenBase {

	public SetterInterfaceCodeGen(DTypeRegistry registry) {
		super(registry);
	}

	public String generate(DStructType structType) {
		String typeName = structType.getName();

//		public interface FlightSetter extends DeliaEntity {
//			void setField1(int val);
//			void setField2(Integer val);
//		}
		
		
		
		StrCreator sc = new StrCreator();
		sc.o("public interface %s extends DeliaEntity {", typeName + "Setter");
		sc.nl();
		for(String fieldName: structType.getDeclaredFields().keySet()) {
			DType ftype = structType.getDeclaredFields().get(fieldName);

			String javaType = convertToJava(structType, fieldName);
			sc.o("  void set%s(%s val);", StringUtil.uppify(fieldName), javaType);
			sc.nl();
		}
		sc.o("}");
		sc.nl();

		return sc.str;
	}
}