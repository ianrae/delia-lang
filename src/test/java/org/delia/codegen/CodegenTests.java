package org.delia.codegen;


import static org.junit.Assert.assertEquals;

import org.delia.app.DaoTestBase;
import org.delia.dao.DeliaDao;
import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.StringUtil;
import org.junit.Before;
import org.junit.Test;


public class CodegenTests extends DaoTestBase {
	
	public static class GetterInterfaceCodeGen {
		private DTypeRegistry registry;

		public GetterInterfaceCodeGen(DTypeRegistry registry) {
			this.registry = registry;
		}
		
		public String generate(DStructType structType) {
			String typeName = structType.getName();

			StrCreator sc = new StrCreator();
			sc.o("public interface %s {", typeName);
			sc.nl();
			for(String fieldName: structType.getDeclaredFields().keySet()) {
				DType ftype = structType.getDeclaredFields().get(fieldName);

				String javaType = convertToJava(ftype);
				sc.o("public %s get%s();", javaType, StringUtil.uppify(fieldName));
				sc.nl();

			}
			sc.o("}");
			sc.nl();

			return sc.str;
		}
		
		private String convertToJava(DType ftype) {
			switch(ftype.getShape()) {
			case INTEGER:
				return "Integer";
			case LONG:
				return "Long";
			case NUMBER:
				return "Double";
			case BOOLEAN:
				return "Boolean";
			case STRING:
				return "String";
			case DATE:
				return "Date";
			case STRUCT:
			{
				return ftype.getName();
			}
			default:
				return null;
			}
		}
	}

	@Test
	public void test1() {
		String src = buildSrc();
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		String typeName = "Flight";
		DTypeRegistry registry = dao.getMostRecentSession().getExecutionContext().registry;
		DStructType structType = (DStructType) registry.getType(typeName);
		GetterInterfaceCodeGen gen = new GetterInterfaceCodeGen(registry);
		String java = gen.convertToJava(structType);

		log.log(java);
	}


	//---

	@Before
	public void init() {
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
}
