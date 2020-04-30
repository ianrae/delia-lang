package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.runner.VarEvaluator;
import org.delia.tlang.runner.TLangResult;
import org.delia.tlang.runner.TLangRunner;
import org.delia.tlang.runner.TLangVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;

public class InputFunctionRunner extends ServiceBase {

	private DTypeRegistry registry;
	private ScalarValueBuilder scalarBuilder;
	private ProgramSet progset;
	private TLangVarEvaluator varEvaluator;

	public InputFunctionRunner(FactoryService factorySvc, DTypeRegistry registry, ErrorTracker localET, TLangVarEvaluator varEvaluator) {
		super(factorySvc);
		this.registry = registry;
		this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
		this.et = localET;
		this.varEvaluator = varEvaluator;
	}
	
	public List<DValue> process(HdrInfo hdr, LineObj lineObj, List<DeliaError> lineErrorsL) {
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
				lineErrorsL.addAll(errL);
			}
		}
		return dvalL;
	}

	private DValue buildFromData(ProcessedInputData data, List<DeliaError> errL) {
		StructValueBuilder structBuilder = new StructValueBuilder(data.structType);
		
		for(String outputFieldName: data.map.keySet()) {
			TypePair pair = DValueHelper.findField(data.structType, outputFieldName);
			if (pair == null) {
				String msg = String.format("%s.%s - field not found. bad mapping", data.structType.getName(), outputFieldName);
				errL.add(new DeliaError("bad-mapping-in-input-function", msg));
				return null;
			}
			
			Object input = data.map.get(pair.name);
			
			DValue inner = null;
			DType dtype = pair.type;
			Shape shape = dtype.getShape();
			switch(shape) {
			case INTEGER:
				inner = buildInt(input);
				break;
			case STRING:
				inner = buildString(input);
				break;
			default:
				//err not supported
				String msg = String.format("%s.%s unsupported shape %s", pair.type.getName(), pair.name,shape.name());
				errL.add(new DeliaError("unsupported-input-field-type", msg));
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
			ProgramSpec spec = findOutputMapping(inputField);
			if (spec == null || spec.outputField == null) {
				continue;
			}
			IdentPairExp outPair = spec.outputField;
			//match with Customer!!
			//run tlang...
			if (spec.prog != null) {
				TLangRunner tlangRunner = new TLangRunner(factorySvc, registry);
				tlangRunner.setVarEvaluator(varEvaluator);
				DValue initialValue = scalarBuilder.buildString(value);
				varEvaluator.setValueVar(initialValue);
				TLangResult res = tlangRunner.execute(spec.prog, initialValue);
				if (!res.ok) {
					log.log("ltang failed!");
				}
				DValue finalValue = (DValue) res.val;
				value = finalValue.asString();
			}
			
			data.map.put(outPair.argName(), value); //fieldname might be different
		}
		return list;
	}

	private ProgramSpec findOutputMapping(String inputField) {
		ProgramSpec spec = progset.map.get(inputField);
		if (spec == null) {
			//err
			String msg = String.format("input field '%s' - no mapping found in input function", inputField);
			et.add("bad-mapping-output-field", msg);
			return null;
		}
		
		return spec;
	}

	private Map<String, String> createInputMap(HdrInfo hdr, LineObj lineObj) {
		Map<String,String> inputData = new HashMap<>();
		int index = 0;
		for(String s: lineObj.elements) {
			String fieldName = hdr.map.get(index);
			if (fieldName == null) {
				//err
				String msg = String.format("line%d: column %d - no column header found", lineObj.lineNum, index);
				et.add("unknown-input-field", msg);
			} else {
				inputData.put(fieldName, s);
			}
			index++;
		}
		return inputData;
	}

	public void setProgramSet(ProgramSet progset) {
		this.progset = progset;
	}
	
}