package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueConverterService;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.tlang.runner.TLangResult;
import org.delia.tlang.runner.TLangRunner;
import org.delia.tlang.runner.TLangRunnerImpl;
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
	private boolean haltNowFlag;
	private DValueConverterService dvalConverter;

	public InputFunctionRunner(FactoryService factorySvc, DTypeRegistry registry, ErrorTracker localET, TLangVarEvaluator varEvaluator) {
		super(factorySvc);
		this.registry = registry;
		this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
		this.et = localET;
		this.varEvaluator = varEvaluator;
		this.dvalConverter = new DValueConverterService(factorySvc);
	}
	
	public List<DValue> process(HdrInfo hdr, LineObj lineObj, List<DeliaError> lineErrorsL) {
		List<DValue> dvalL = new ArrayList<>();
		haltNowFlag = false;
		
		Map<String,Object> inputData = createInputMap(hdr, lineObj);
		//it can produce multiple
		List<ProcessedInputData> processedDataL = runTLang(inputData);
		if (haltNowFlag) {
			return dvalL;
		}

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
		
		for(String outputFieldName: data.outputFieldMap.keySet()) {
			TypePair pair = DValueHelper.findField(data.structType, outputFieldName);
			if (pair == null) {
				String msg = String.format("%s.%s - field not found. bad mapping", data.structType.getName(), outputFieldName);
				errL.add(new DeliaError("bad-mapping-in-input-function", msg));
				return null;
			}
			
			Object input = data.outputFieldMap.get(pair.name);
			
			DValue inner = null;
			DType dtype = pair.type;
			Shape shape = dtype.getShape();
			inner = dvalConverter.buildFromObject(input, shape, scalarBuilder);
			if (input != null && inner == null) {
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

	private List<ProcessedInputData> runTLang(Map<String, Object> inputData) {
		List<ProcessedInputData> list = new ArrayList<>();
		
		int index = 0;
		for(ProgramSet.OutputSpec ospec: progset.outputSpecs) {
			String alias = ospec.alias;
			ProcessedInputData data = runTLangForType(alias, ospec.structType, inputData);
			data.structType = ospec.structType;
			list.add(data);
			index++;
		}
		return list;
	}
	private ProcessedInputData runTLangForType(String alias, DStructType structType, Map<String, Object> inputData) {
		ProcessedInputData data = new ProcessedInputData();
		
		for(String inputField: inputData.keySet()) {
			ProgramSpec spec = findOutputMapping(inputField);
			if (spec == null || spec.outputField == null) {
				continue;
			}
			if (!spec.outputField.val1.equals(alias)) {
				continue; //for a different output type
			}
			
			String value;
			Object obj = inputData.get(inputField);
			if (obj instanceof DValue) {
				DValue synthValue = (DValue) obj;
				value = synthValue.asString();
				//TODO we should keep this as a dval all the way through
			} else {
				value = obj.toString(); //was a string
			}
			IdentPairExp outPair = spec.outputField;
			//match with Customer!!
			//run tlang...
			if (spec.prog != null) {
				TLangRunner tlangRunner = new TLangRunnerImpl(factorySvc, registry);
				tlangRunner.setVarEvaluator(varEvaluator);
				DValue initialValue = scalarBuilder.buildString(value);
				varEvaluator.setValueVar(initialValue);
				tlangRunner.setInputMap(inputData);
				
				TLangResult res = tlangRunner.execute(spec.prog, initialValue);
				if (!res.ok) {
					log.log("ltang failed!");
				}
				log.log("trail: %s", tlangRunner.getTrail());
				
				DValue finalValue = (DValue) res.val;
				value = finalValue.asString();
				if (res.failFlag) {
					haltNowFlag = true;
					data.outputFieldMap.put(outPair.argName(), value); //fieldname might be different
					return data;
				}
			}
			
			data.outputFieldMap.put(outPair.argName(), value); //fieldname might be different
		}
		return data;
	}

	private ProgramSpec findOutputMapping(String inputField) {
		ProgramSpec spec = progset.fieldMap.get(inputField);
		if (spec == null) {
			//err
			String msg = String.format("input field '%s' - no mapping found in input function", inputField);
			et.add("bad-mapping-output-field", msg);
			return null;
		}
		
		return spec;
	}

	//produces a map of String (for CSV data) or DValues (for synthetic fields)
	private Map<String, Object> createInputMap(HdrInfo hdr, LineObj lineObj) {
		Map<String,Object> inputData = new HashMap<>();
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
		
		//and do synthetic fields
		for(String inputField: progset.fieldMap.keySet()) {
			ProgramSpec spec = progset.fieldMap.get(inputField);
			if (spec.syntheticValue != null) {
				inputData.put(inputField, spec.syntheticValue);
			}
		}

		return inputData;
	}

	public void setProgramSet(ProgramSet progset) {
		this.progset = progset;
	}

	public boolean wasHalted() {
		return haltNowFlag;
	}
	
}