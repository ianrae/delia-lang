package org.delia.codegen;


import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.delia.app.DaoTestBase;
import org.delia.dao.DeliaDao;
import org.delia.db.sql.StrCreator;
import org.delia.runner.ResultValue;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.StringUtil;
import org.junit.Before;
import org.junit.Test;


public class CodegenTests extends DaoTestBase {
	
	public interface Flight {
		Integer getField1();
		Integer getField2();
	}
	public interface FlightSetter {
		void setField1(Integer val);
		void setField2(Integer val);
	}
	public static class FlightImmut implements Flight {
		private DValue dval;

		public FlightImmut(DValue dval) {
			this.dval = dval;
		}
		@Override
		public Integer getField1() {
			return dval.asStruct().getField("field1").asInt();
		}

		@Override
		public Integer getField2() {
			return dval.asStruct().getField("field1").asInt();
		}
	}
	
	public static class FlightEntity implements Flight,FlightSetter {
		private DValue dval;
		private Map<String,Object> setMap = new HashMap<>();

		public FlightEntity(DValue dval) {
			this.dval = dval;
		}
		public FlightEntity(FlightImmut immut) {
			this.dval = immut.dval;
		}
		
		@Override
		public Integer getField1() {
			String fieldName = "field1";
			if (setMap.containsKey(fieldName)) {
				return (Integer)setMap.get(fieldName); //can return null
			}
			return dval.asStruct().getField(fieldName).asInt();
		}

		@Override
		public Integer getField2() {
			String fieldName = "field2";
			if (setMap.containsKey(fieldName)) {
				return (Integer)setMap.get(fieldName); //can return null
			}
			return dval.asStruct().getField(fieldName).asInt();
		}
		@Override
		public void setField1(Integer val) {
			setMap.put("field1", val);
		}
		@Override
		public void setField2(Integer val) {
			setMap.put("field2", val);
		}
	}
	
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
				sc.o("  %s get%s();", javaType, StringUtil.uppify(fieldName));
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
		String java = gen.generate(structType);

		log.log(java);
		
		ResultValue res = dao.queryByPrimaryKey("Flight", "1");
		assertEquals(true, res.ok);
		
		Flight flight = new FlightImmut(res.getAsDValue());
		assertEquals(1, flight.getField1().intValue());
		assertEquals(20, flight.getField2().intValue());
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
