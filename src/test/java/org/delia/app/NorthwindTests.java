package org.delia.app;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.api.Delia;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.builder.ConnectionBuilder;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.runner.ResultValue;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.StringUtil;
import org.delia.util.TextFileReader;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;


public class NorthwindTests extends NewBDDBase {
	
	public static class LineObj {
		public String[] elements;
		public int lineNum;
		
		public LineObj(String[] ar, int lineNum) {
			this.elements = ar;
			this.lineNum = lineNum;
		}
		
		public List<String> toList() {
			return Arrays.asList(elements);
		}
	}
	
	public static class CSVRowConverter {
		private List<String> hdrL;
		private DTypeRegistry registry;
		private String typeName;
		private DStructType structType;
		private Map<String,String> replaceMap = new HashMap<>();
		
		public CSVRowConverter(DTypeRegistry registry, String typeName, Map<String,String> replaceMap) {
			this.registry = registry;
			this.typeName = typeName;
			this.structType = (DStructType) registry.getType(typeName);
			this.replaceMap = replaceMap;
		}
		void readHeader(String headerRow) {
			//categoryID,categoryName,description,picture
			String[] ar = headerRow.split(",");
			LineObj obj = new LineObj(ar, 0);
			hdrL = obj.toList();
		}
		String convertToDelia(String csvRow) {
			String[] ar = csvRow.split(",");
			
			StringBuilder sb = new StringBuilder();
			int index = 0;
			for(String fieldName: hdrL) {
				if (index > 0) {
					sb.append(", ");
				}
				
				DType type = DValueHelper.findFieldType(structType, fieldName);
				if (type == null && replaceMap != null) {
					String s = replaceMap.get(fieldName);
					type = DValueHelper.findFieldType(structType, s);
					if (type.isStructShape()) {
						TypePair pair = DValueHelper.findPrimaryKeyFieldPair(type);
						type = pair.type;
					}
					fieldName = s;
				}
				sb.append(fieldName);
				sb.append(": ");
				
				if (type.isShape(Shape.STRING)) {
					String value = ar[index];
					if (value.contains("'")) {
						sb.append("\"");
						sb.append(ar[index]);
						sb.append("\"");
					} else {
						sb.append("'");
						sb.append(ar[index]);
						sb.append("'");
					}
				} else {
					sb.append(ar[index]);
				}
				index++;
			}
			
			return sb.toString();
		}
	}
	
	@Test
	public void test1() {
		String src = buildSrc();
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		
		String row1 = "categoryID,categoryName,description,picture";
		String row2 = "1,Beverages,Soft drinks coffees teas beers and ales,0x151C2F00020000000D000E0014002100FFFFFFFF4269746D617020496D616765005061696E742E5069637475726500010500000200000007000000504272757368000000000000000000A0290000424D98290000000000005600000028000000AC00000078000000010004000000000000000000880B0000880B0000080000";
		
		CSVRowConverter converter = new CSVRowConverter(dao.getRegistry(), "Category", null);
		converter.readHeader(row1);
		String fields = converter.convertToDelia(row2);
		
		String type = "Category";
//		String id = "3";
		//insert
		ResultValue res = dao.insertOne(type, fields);
		assertEquals(true, res.ok);
		
		//query
		String id = "1";
		res = dao.queryByPrimaryKey(type, id);
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("Beverages", dval.asStruct().getField("categoryName").asString());
	}

	@Test
	public void test2() {
		String src = buildSrc();
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		String path = BASE_DIR + "categories.csv";
		List<String> lines = loadCSVFile(path);
		String type = "Category";
		CSVRowConverter converter = new CSVRowConverter(dao.getRegistry(), type, null);
		
		int index = 0;
		for(String row: lines) {
			if (index == 0) {
				converter.readHeader(row);
			} else {
				String fields = converter.convertToDelia(row);
				ResultValue res = dao.insertOne(type, fields);
				assertEquals(true, res.ok);
			}
			index++;
		}
		
		log.log("done..");
		
		//query
		String id = "1";
		ResultValue res = dao.queryByPrimaryKey(type, id);
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("Beverages", dval.asStruct().getField("categoryName").asString());
	}
	
	@Test
	public void test3() {
		String src = buildSrc2();
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		String type = "Category";
		loadCSV(type, "categories.csv", dao);
		String id = "1";
		ResultValue res = dao.queryByPrimaryKey(type, id);
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("Beverages", dval.asStruct().getField("categoryName").asString());
		
		type = "Customer";
		loadCSV(type, "customers.csv", dao);
		id = "ALFKI";
		res = dao.queryByPrimaryKey(type, String.format("'%s'", id));
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals("Alfreds Futterkiste", dval.asStruct().getField("companyName").asString());
		
		type = "Employee";
		loadCSV(type, "employees.csv", dao);
		id = "1";
		res = dao.queryByPrimaryKey(type, id);
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals("Davolio", dval.asStruct().getField("lastName").asString());
		
		type = "Order";
		Map<String,String> replaceMap = new HashMap<>();
		replaceMap.put("customerID", "customer");
		replaceMap.put("employeeID", "employee");
		loadCSV(type, "orders.csv", dao, replaceMap);
		id = "10248";
//		res = dao.queryByPrimaryKey(type, id);
		res = dao.queryByStatement(type, "[10248].fks()");
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals("Vins et alcools Chevalier", dval.asStruct().getField("shipName").asString());
		DRelation drel = dval.asStruct().getField("customer").asRelation();
		assertEquals("VINET", drel.getForeignKey().asString());
		drel = dval.asStruct().getField("employee").asRelation();
		assertEquals(5, drel.getForeignKey().asInt());
	}
	
	



	// ---
	private final String BASE_DIR = NorthwindHelper.BASE_DIR;

	@Before
	public void init() {
	}
	
	private String buildSrc() {
//		String path = "src/main/resources/test/northwind/northwind.txt";
		String path = BASE_DIR + "northwind-small.txt";
		String src = loadFromFile(path);
		return src;
	}
	private String buildSrc2() {
		String path = BASE_DIR + "northwind2.txt";
		String src = loadFromFile(path);
		return src;
	}
	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
//		ConnectionInfo info = ConnectionBuilder.dbType(DBType.H2).jdbcUrl("jdbc:h2:~/test").build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	public String loadFromFile(String path) {
		log.log("FILE: %s", path);
		TextFileReader reader = new TextFileReader();
		List<String> lines = reader.readFile(path);
		String src = StringUtil.convertToSingleString(lines);
		return src;
	}
	public List<String> loadCSVFile(String path) {
		log.log("CSVFILE: %s", path);
		TextFileReader reader = new TextFileReader();
		List<String> lines = reader.readFile(path);
		return lines;
	}
	private void loadCSV(String type, String filename, DeliaDao dao) {
		loadCSV(type, filename, dao, null);
	}
	
	private void loadCSV(String type, String filename, DeliaDao dao, Map<String,String> replaceMap) {
		String path = BASE_DIR + filename;
		List<String> lines = loadCSVFile(path);
		CSVRowConverter converter = new CSVRowConverter(dao.getRegistry(), type, replaceMap);
		
		int index = 0;
		for(String row: lines) {
			if (index == 0) {
				converter.readHeader(row);
			} else {
				String fields = converter.convertToDelia(row);
				ResultValue res = dao.insertOne(type, fields);
				assertEquals(true, res.ok);
			}
			index++;
		}
		
		log.log("done..");
	}

	@Override
	public ZDBInterfaceFactory createForTest() {
		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
		return db;
	}

}
