package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.error.DeliaError;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.valuebuilder.StructValueBuilder;
import org.junit.Before;
import org.junit.Test;

public class InputFunctionTests  extends NewBDDBase {
	
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
	
	public static class HdrInfo {
//		public Map<String,Integer> map = new HashMap<>();
		public Map<Integer,String> map = new HashMap<>();
	}
	
	public static class ProcessedInputData {
		DStructType structType;
		Map<String,Object> map = new HashMap<>();
	}
	
	public static class XConv extends ServiceBase {

		private DTypeRegistry registry;

		public XConv(FactoryService factorySvc, DTypeRegistry registry) {
			super(factorySvc);
			this.registry = registry;
		}
		
		public void foo() {
			log.log("fffff");
		}
		
		public List<DValue> process(HdrInfo hdr, LineObj lineObj, List<DeliaError> totalErrorL) {
			List<DValue> dvalL = new ArrayList<>();
			
			Map<String,String> inputData = createInputMap(hdr, lineObj);
			//it can produce multiple
			List<ProcessedInputData> processedDataL = runTLang(inputData);

			for(ProcessedInputData data: processedDataL) {
				List<DeliaError> errL = new ArrayList<>();
				DValue dval = buildFromData(data, errL);
				
				if (errL.isEmpty()) {
					dvalL.add(dval);
				} else {
					totalErrorL.addAll(errL);
				}
			}
			return dvalL;
		}

		private DValue buildFromData(ProcessedInputData data, List<DeliaError> errL) {
			StructValueBuilder structBuilder = new StructValueBuilder(data.structType);
			for(TypePair pair: data.structType.getAllFields()) {
				Object input = data.map.get(pair.name);
				
				DValue inner = null;
				DType dtype = pair.type;
				switch(dtype.getShape()) {
				case INTEGER:
					inner = buildInt(input);
					break;
				default:
					//err not supported
					break;
				}
				structBuilder.addField(pair.name, inner);
			}			
			
			boolean b = structBuilder.finish();
			if (!b) {
				//err
				errL.addAll(structBuilder.getValidationErrors());
				return null;
			} else {
				return structBuilder.getDValue();
			}
		}

		private DValue buildInt(Object input) {
			// TODO Auto-generated method stub
			return null;
		}

		private List<ProcessedInputData> runTLang(Map<String, String> inputData) {
			List<ProcessedInputData> list = new ArrayList<>();
			ProcessedInputData data = new ProcessedInputData();
			data.structType = (DStructType) registry.getType("Customer");
			
			for(String fieldName: inputData.keySet()) {
				String value = inputData.get(fieldName); 
				//run tlang...
				data.map.put(fieldName, value); //fieldname might be different
			}
			return list;
		}

		private Map<String, String> createInputMap(HdrInfo hdr, LineObj lineObj) {
			Map<String,String> inputData = new HashMap<>();
			int index = 0;
			for(String s: lineObj.elements) {
				String fieldName = hdr.map.get(index);
				if (fieldName == null) {
					//err
				} else {
					inputData.put(fieldName, s);
				}
				index++;
			}
			return inputData;
		}
	}
	
	@Test
	public void test() {
		XConv xconv = createXConv();
		xconv.foo();
		assertEquals(1,2);
		
		List<DeliaError> totalErrorL = new ArrayList<>();
		HdrInfo hdr = createHdr();
		LineObj lineObj = createLineObj();
		List<DValue> dvals = xconv.process(hdr, lineObj, totalErrorL);
		assertEquals(22, dvals.size());
	}
	

	private LineObj createLineObj() {
		String[] ar = { "bob", "33" };
		LineObj lineObj = new LineObj(ar, 1);
		return lineObj;
	}


	private HdrInfo createHdr() {
		HdrInfo hdr = new HdrInfo();
		hdr.map.put(1, "name");
		hdr.map.put(2, "wid");
		return hdr;
	}


	private XConv createXConv() {
		return new XConv(delia.getFactoryService(), registry);
	}


	// --
	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;
	private DTypeRegistry registry;

	@Before
	public void init() {
		this.dao = this.createDao();
		this.delia = dao.getDelia();
		String src = buildSrcOneToOne();
		this.session = delia.beginSession(src);
		this.registry = session.getExecutionContext().registry;
	}
	private String buildSrcOneToOne() {
		String src = " type Customer struct {id int unique, wid int, name string } end";
		return src;
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
	
	
}
