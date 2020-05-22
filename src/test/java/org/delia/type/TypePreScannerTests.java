package org.delia.type;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.delia.api.Delia;
import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.DeliaCompiler;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.StructFieldExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.error.DeliaError;
import org.delia.runner.ResultValue;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;


public class TypePreScannerTests extends BDDBase {
	
	public static class PreTypeRegistry {
		private Map<String,DType> map = new HashMap<>(); 
		private Map<String,String> definedMap = new HashMap<>(); 

		public boolean existsType(String typeName) {
			return map.containsKey(typeName);
		}
		public DType getType(String typeName) {
			return map.get(typeName);
		}
		public void addMentionedType(DType dtype) {
			map.put(dtype.getName(), dtype);
		}
		public void addTypeDefinition(DType dtype) {
			map.put(dtype.getName(), dtype);
			definedMap.put(dtype.getName(), "");
		}
		public int size() {
			return map.size();
		}
		public List<String> getUndefinedTypes() {
			List<String> list = new ArrayList<>();
			for(String typeName: map.keySet()) {
				if (!definedMap.containsKey(typeName)) {
					list.add(typeName);
				}
			}
			return list;
		}
		public Map<String, DType> getMap() {
			return map;
		}
	}
	
	
	public static class TypePreRunner extends ServiceBase {
		private PreTypeRegistry preRegistry;
		private DTypeRegistry actualRegistry; //holds builtIn types only.

		public TypePreRunner(FactoryService factorySvc, DTypeRegistry actualRegistry) {
			super(factorySvc);
			this.preRegistry = new PreTypeRegistry();
			this.actualRegistry = actualRegistry;
		}

		public void executeStatements(List<Exp> extL, List<DeliaError> allErrors) {
			for(Exp exp: extL) {
				ResultValue res = executeStatement(exp);
				if (! res.ok) {
					allErrors.addAll(res.errors);
				}
			}
		}
		
		private ResultValue executeStatement(Exp exp) {
			ResultValue res = new ResultValue();
			if (exp instanceof TypeStatementExp) {
				DType dtype = createType((TypeStatementExp) exp, res);
				preRegistry.addTypeDefinition(dtype);
			}
			return res;
		}
		
		public DType createType(TypeStatementExp typeStatementExp, ResultValue res) {
			et.clear();
			if (typeStatementExp.structExp == null ) {
				DType dtype = new DType(Shape.INTEGER, typeStatementExp.typeName, null); //not correct values. will fix later
				return dtype;
			}
			
			//build struct type
			OrderedMap omap = new OrderedMap();
			for(StructFieldExp fieldExp: typeStatementExp.structExp.argL) {
				DType fieldType = getTypeForField(fieldExp);
				omap.add(fieldExp.getFieldName(), fieldType, fieldExp.isOptional, fieldExp.isUnique, fieldExp.isPrimaryKey, fieldExp.isSerial);
			}
			
			DType dtype = new DStructType(Shape.STRUCT, typeStatementExp.typeName, null, null, null);
			return dtype;
		}
		
		private DType getTypeForField(StructFieldExp fieldExp) {
			DType strType = actualRegistry.getType(BuiltInTypes.STRING_SHAPE);
			DType intType = actualRegistry.getType(BuiltInTypes.INTEGER_SHAPE);
			DType longType = actualRegistry.getType(BuiltInTypes.LONG_SHAPE);
			DType numberType = actualRegistry.getType(BuiltInTypes.NUMBER_SHAPE);
			DType boolType = actualRegistry.getType(BuiltInTypes.BOOLEAN_SHAPE);
			DType dateType = actualRegistry.getType(BuiltInTypes.DATE_SHAPE);
			
			String s = fieldExp.typeName;
			if (s.equals("string")) {
				return strType;
			} else if (s.equals("int")) {
				return intType;
			} else if (s.equals("boolean")) {
				return boolType;
			} else if (s.equals("long")) {
				return longType;
			} else if (s.equals("number")) {
				return numberType;
			} else if (s.equals("date")) {
				return dateType;
			} else {
				DType possibleStruct = preRegistry.getType(fieldExp.typeName);
				if (possibleStruct != null) {
					return possibleStruct;
				} else {
					DType dtype = new DStructType(Shape.STRUCT, fieldExp.typeName, null, null, null);
					preRegistry.addMentionedType(dtype);
					return dtype;
				}
			}
		}

		public PreTypeRegistry getPreRegistry() {
			return preRegistry;
		}
	}	
	
	
	@Test
	public void test() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		DTypeRegistry registry = dao.getRegistry();
		assertEquals(DTypeRegistry.NUM_BUILTIN_TYPES + 2, registry.size());

		Set<String> set = registry.getAllCustomTypes();
		assertEquals(2, set.size());
		assertEquals(true, set.contains("Customer"));
	}

	@Test
	public void test2() {
		String src = buildSrc();
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);
		
		Delia delia = dao.getDelia();
		DeliaCompiler compiler = delia.createCompiler();
		List<Exp> expL = compiler.parse(src);
		
		DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
		registryBuilder.init();
		DTypeRegistry registry = registryBuilder.getRegistry();
		
		TypePreRunner preRunner = new TypePreRunner(delia.getFactoryService(), registry);
		List<DeliaError> allErrors = new ArrayList<>();
		preRunner.executeStatements(expL, allErrors);
		
		PreTypeRegistry preReg = preRunner.getPreRegistry();
		assertEquals(2, preReg.size());
		assertEquals(0, preReg.getUndefinedTypes().size());
		
		for(String typeName: preReg.getMap().keySet()) {
			DType dtype = preReg.getMap().get(typeName);
			log.log("%s: %s", dtype.getShape().name(), typeName);
		}
		
		assertEquals(DTypeRegistry.NUM_BUILTIN_TYPES + 0, registry.size());
	}
	
	//---

	@Before
	public void init() {
	}

	private DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaGenericDao(delia);
	}

	private String buildSrc() {
		String src = "type Customer struct {id int primaryKey, x int, relation addr Address one optional} end";
		src += "\n type Address struct {sid string primaryKey, y int, relation cust Customer one} end";
		return src;
	}

	@Override
	public ZDBInterfaceFactory createForTest() {
		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
		return db;
	}

}
