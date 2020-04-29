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
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.error.DeliaError;
import org.delia.log.LogLevel;
import org.delia.runner.DValueIterator;
import org.delia.runner.ResultValue;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.valuebuilder.ScalarValueBuilder;
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
	
	public static class InputFunctionRunner extends ServiceBase {

		private DTypeRegistry registry;
		private ScalarValueBuilder scalarBuilder;
		private InputFunctionDefStatementExp inFnExp;

		public InputFunctionRunner(FactoryService factorySvc, DTypeRegistry registry) {
			super(factorySvc);
			this.registry = registry;
			this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
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
				case STRING:
					inner = buildString(input);
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
			if (input == null) {
				return null;
			}
			
			if (input instanceof Integer) {
				Integer value = (Integer) input; 
				return scalarBuilder.buildInt(value);
			} else {
				String s = input.toString();
				return scalarBuilder.buildInt(s);
			}
		}
		private DValue buildString(Object input) {
			if (input == null) {
				return null;
			}
			
			String s = input.toString();
			return scalarBuilder.buildString(s);
		}

		private List<ProcessedInputData> runTLang(Map<String, String> inputData) {
			List<ProcessedInputData> list = new ArrayList<>();
			ProcessedInputData data = new ProcessedInputData();
			data.structType = (DStructType) registry.getType("Customer");
			list.add(data);
			
			for(String inputField: inputData.keySet()) {
				String value = inputData.get(inputField);
				IdentPairExp outPair = findOutputMapping(inputField);
				//match with Customer!!
				//run tlang...
				data.map.put(outPair.argName(), value); //fieldname might be different
			}
			return list;
		}

		private IdentPairExp findOutputMapping(String inputField) {
			for(Exp exp: this.inFnExp.bodyExp.statementL) {
				InputFuncMappingExp mappingExp = (InputFuncMappingExp) exp;
				if (mappingExp.inputField.name().equals(inputField)) {
					return mappingExp.outputField;
				}
			}
			return null;
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

		public void setFnExp(InputFunctionDefStatementExp inFnExp) {
			this.inFnExp = inFnExp;
		}
		
	}
	
//	@Test
//	public void test() {
//		InputFunctionRunner inFuncRunner = createXConv();
//		inFuncRunner.foo();
//		
//		List<DeliaError> totalErrorL = new ArrayList<>();
//		HdrInfo hdr = createHdr();
//		LineObj lineObj = createLineObj();
//		List<DValue> dvals = inFuncRunner.process(hdr, lineObj, totalErrorL);
//		assertEquals(0, totalErrorL.size());
//		assertEquals(1, dvals.size());
//		
//		//hmm. or do we do insert Customer {....}
//		//i think we can do insert Customer {} with empty dson and somehow
//		//pass in the already build dval runner.setAlreadyBuiltDVal()
//		
//		DValueIterator iter = new DValueIterator(dvals);
//		delia.getOptions().insertPrebuiltValueIterator = iter;
//		String s = String.format("insert Customer {}");
//		ResultValue res = delia.continueExecution(s, session);
//		assertEquals(true, res.ok);
//		delia.getOptions().insertPrebuiltValueIterator = null;
//		
//		DeliaDao dao = new DeliaDao(delia, session);
//		res = dao.queryByPrimaryKey("Customer", "1");
//		assertEquals(true, res.ok);
//		DValue dval = res.getAsDValue();
//		assertEquals("bob", dval.asStruct().getField("name").asString());
//	}
//	
	@Test
	public void test2() {
		InputFunctionRunner inFuncRunner = createXConv();
		inFuncRunner.foo();
		
		delia.getLog().setLevel(LogLevel.DEBUG);
		InputFunctionDefStatementExp inFnExp = findInputFn(session, "foo");
		
		List<DeliaError> totalErrorL = new ArrayList<>();
		HdrInfo hdr = createHdrFrom(inFnExp);
		LineObj lineObj = createLineObj();
		inFuncRunner.setFnExp(inFnExp);
		List<DValue> dvals = inFuncRunner.process(hdr, lineObj, totalErrorL);
		chkNoErrors(totalErrorL);
		assertEquals(1, dvals.size());
		
		//hmm. or do we do insert Customer {....}
		//i think we can do insert Customer {} with empty dson and somehow
		//pass in the already build dval runner.setAlreadyBuiltDVal()
		
		DValueIterator iter = new DValueIterator(dvals);
		delia.getOptions().insertPrebuiltValueIterator = iter;
		String s = String.format("insert Customer {}");
		ResultValue res = delia.continueExecution(s, session);
		assertEquals(true, res.ok);
		delia.getOptions().insertPrebuiltValueIterator = null;
		
		DeliaDao dao = new DeliaDao(delia, session);
		res = dao.queryByPrimaryKey("Customer", "1");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("bob", dval.asStruct().getField("name").asString());
	}

	private void chkNoErrors(List<DeliaError> totalErrorL) {
		for(DeliaError err: totalErrorL) {
			delia.getLog().log("err: %s", err.toString());
		}
		assertEquals(0, totalErrorL.size());
	}

	private HdrInfo createHdrFrom(InputFunctionDefStatementExp inFnExp) {
		HdrInfo hdr = new HdrInfo();
		int index = 0;
		for(Exp exp: inFnExp.bodyExp.statementL) {
			InputFuncMappingExp mapping = (InputFuncMappingExp) exp;
			hdr.map.put(index, mapping.inputField.name());
			index++;
		}
		return hdr;
	}

	private InputFunctionDefStatementExp findInputFn(DeliaSession session2, String fnName) {
		InputFunctionDefStatementExp infnExp = session2.getExecutionContext().inputFnMap.get(fnName);
		return infnExp;
	}

	private LineObj createLineObj() {
		String[] ar = { "1", "33","bob" };
		LineObj lineObj = new LineObj(ar, 1);
		return lineObj;
	}


	private HdrInfo createHdr() {
		HdrInfo hdr = new HdrInfo();
		hdr.map.put(0, "id");
		hdr.map.put(1, "name");
		hdr.map.put(2, "wid");
		return hdr;
	}


	private InputFunctionRunner createXConv() {
		return new InputFunctionRunner(delia.getFactoryService(), registry);
	}


	// --
//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;
	private DTypeRegistry registry;

	@Before
	public void init() {
		DeliaDao dao = this.createDao();
		this.delia = dao.getDelia();
		String src = buildSrc();
		this.session = delia.beginSession(src);
		this.registry = session.getExecutionContext().registry;
	}
	private String buildSrc() {
		String src = " type Customer struct {id int unique, wid int, name string } end";
		src += " input function foo(Customer c) { ID -> c.id, WID -> c.wid, NAME -> c.name}";
		
		return src;
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}


	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
	
	
}
