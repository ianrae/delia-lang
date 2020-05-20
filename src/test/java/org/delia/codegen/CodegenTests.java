package org.delia.codegen;


import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.app.DaoTestBase;
import org.delia.dao.DeliaDao;
import org.delia.db.sql.StrCreator;
import org.delia.runner.ResultValue;
import org.delia.type.DStructHelper;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.StringUtil;
import org.junit.Before;
import org.junit.Test;


public class CodegenTests extends DaoTestBase {
	public interface DeliaImmutable {
		DValue internalDValue();
	}
	public interface DeliaEntity {
		Map<String,Object> internalSetValueMap();
		//resetUnchangedFields();
	}
	public interface Flight extends DeliaImmutable {
		int getField1();
		Integer getField2();
	}
	public interface FlightSetter extends DeliaEntity {
		void setField1(int val);
		void setField2(Integer val);
	}
	public static class FlightImmut implements Flight {
		private DValue dval;
		private DStructHelper helper;

		public FlightImmut(DValue dval) {
			this.dval = dval;
			this.helper = dval.asStruct();
		}
		@Override
		public int getField1() {
			return helper.getField("field1").asInt();
		}

		@Override
		public Integer getField2() {
			return helper.getField("field2").asInt();
		}
//		@Override
//		public Address getAddr() {
//			return new AddressImmut(helper.getField("addr"));
//		}
		@Override
		public DValue internalDValue() {
			return dval;
		}
	}
	
	public static class FlightEntity implements Flight,FlightSetter {
		private DValue dval;
		private DStructHelper helper;
		private Map<String,Object> setMap = new HashMap<>();

		public FlightEntity(DValue dval) {
			this.dval = dval;
			this.helper = dval.asStruct();
		}
		public FlightEntity(Flight immut) {
			FlightImmut x = (FlightImmut) immut;
			this.dval = x.dval;
			this.helper = dval.asStruct();
		}
		
		@Override
		public int getField1() {
			String fieldName = "field1";
			if (setMap.containsKey(fieldName)) {
				return (Integer)setMap.get(fieldName); //can return null
			}
			return helper.getField(fieldName).asInt();
		}

		@Override
		public Integer getField2() {
			String fieldName = "field2";
			if (setMap.containsKey(fieldName)) {
				return (Integer)setMap.get(fieldName); //can return null
			}
			return helper.getField(fieldName).asInt();
		}
		@Override
		public void setField1(int val) {
			setMap.put("field1", val);
		}
		@Override
		public void setField2(Integer val) {
			setMap.put("field2", val);
		}
		@Override
		public DValue internalDValue() {
			return dval; //can be null, if disconnected entity
		}
		@Override
		public Map<String, Object> internalSetValueMap() {
			return setMap;
		}
	}
	
	
	public class FlightDao {
		private DeliaDao innerDao;
		protected String typeName;
		
		public FlightDao(Delia delia, DeliaSession session) {
		}

		public Flight queryByPrimaryKey(int primaryKey) {
			ResultValue res = null; //innerDao.queryByPrimaryKey(typeName, primaryKey);
			return new FlightImmut(res.getAsDValue());
		}

		public long count() {
			return innerDao.count(typeName);
		}

		public void insert(Flight flight) {
			//used setInsertPrebuiltValueIterator
			//return innerDao.insertOne(typeName, fields);
			//if flight is entity then set serial pk
		}
		public int mostRecentSerialPK() {
			return 0; //from last insert()
		}

		public int update(Flight flight) {
//			return innerDao.updateOne(typeName, primaryKey, fields);
			return 0; //# updated rows
		}
		public int save(Flight flight) {
			//if flight is entity then set serial pk
			return 0; //insert or update
		}
		
		public void delete(Flight flight) {
		}
	}
	
	//====
	public static class CodeGenBase {
		protected DTypeRegistry registry;

		public CodeGenBase(DTypeRegistry registry) {
			this.registry = registry;
		}
		protected String convertToJava(DType ftype) {
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

		protected String convertToJava(DStructType structType, String fieldName) {
			boolean flag = !structType.fieldIsOptional(fieldName);
			DType ftype = structType.getDeclaredFields().get(fieldName);
			switch(ftype.getShape()) {
			case INTEGER:
				return flag ? "int" : "Integer";
			case LONG:
				return flag ? "long": "Long";
			case NUMBER:
				return flag ? "double" : "Double";
			case BOOLEAN:
				return flag ? "boolean" : "Boolean";
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
		
		protected String convertToAsFn(DType ftype) {
			switch(ftype.getShape()) {
			case INTEGER:
				return "asInt";
			case LONG:
				return "asLong";
			case NUMBER:
				return "asNumber";
			case BOOLEAN:
				return "asBoolean";
			case STRING:
				return "asString";
			case DATE:
				return "asDate";
			case STRUCT:
			{
				return "TODOfix" + ftype.getName();
			}
			default:
				return null;
			}
		}

		
	}
	
	public static class GetterInterfaceCodeGen extends CodeGenBase {

		public GetterInterfaceCodeGen(DTypeRegistry registry) {
			super(registry);
		}
		
		public String generate(DStructType structType) {
			String typeName = structType.getName();

			StrCreator sc = new StrCreator();
			sc.o("public interface %s {", typeName);
			sc.nl();
			for(String fieldName: structType.getDeclaredFields().keySet()) {
				DType ftype = structType.getDeclaredFields().get(fieldName);

				String javaType = convertToJava(structType, fieldName);
				sc.o("  %s get%s();", javaType, StringUtil.uppify(fieldName));
				sc.nl();

			}
			sc.o("}");
			sc.nl();

			return sc.str;
		}
	}
	public static class ImmutCodeGen extends CodeGenBase {

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
				line(sc, "  return dval;");
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
	

	@Test
	public void test1() {
		String src = buildSrc();
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		ResultValue res = dao.queryByPrimaryKey("Flight", "1");
		assertEquals(true, res.ok);
		
		Flight flight = new FlightImmut(res.getAsDValue());
		assertEquals(1, flight.getField1());
		assertEquals(10, flight.getField2().intValue());
		
		FlightEntity entity = new FlightEntity(flight);
		entity.setField1(12);
		assertEquals(12, entity.getField1());
	}

	@Test
	public void test2() {
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
		log.log("////");
		ImmutCodeGen gen2 = new ImmutCodeGen(registry);
		java = gen2.generate(structType);
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
