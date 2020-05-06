package org.delia.runner.inputfunction;


import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueConverterService;
import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.ViaService.ViaInfo;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.ScalarValueBuilder;


public class ViaService extends ServiceBase {
	public static class ViaInfo {
		String inputField;
		ProgramSpec spec;
	}
	
	public static class ViaRow {
		DValue val1;
		DValue val2;
	}
	
	
	private DValueConverterService dvalConverter;
	private ImportMetricObserver metricsObserver;
	private InputFunctionServiceOptions options = new InputFunctionServiceOptions();

	public ViaService(FactoryService factorySvc) {
		super(factorySvc);
		this.dvalConverter = new DValueConverterService(factorySvc);
	}

	public List<ViaInfo> detectVias(InputFunctionRequest request) {
		List<ViaInfo> viaL = new ArrayList<>();
		
		for(String inputField: request.progset.fieldMap.keySet()) {
			ProgramSpec spec = request.progset.fieldMap.get(inputField);
			if (spec.viaPK != null) {
				ViaInfo viaInfo = new ViaInfo();
				viaInfo.inputField = inputField;
				viaInfo.spec = spec;
				viaL.add(viaInfo);
			}
		}
		
		return viaL;
	}

	public void executeInsert(ViaLineInfo viaLineInfo, InputFunctionRequest request, InputFunctionResult fnResult,
			int lineNum, List<DeliaError> errL) {
		
		for(ViaPendingInfo vpi: viaLineInfo.viaPendingL) {
			String pkFieldName = vpi.viaInfo.spec.viaPK;
			Object x = viaLineInfo.inputData.get(pkFieldName);
			log.log("vv: %s:%s, %s:%s", vpi.outputFieldName, vpi.processedInputValue, pkFieldName,x);
			
			//update Film[1] { add actors:'a10'}
			String typeName = vpi.structType.getName();
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(vpi.structType);
			
			ScalarValueBuilder builder = new ScalarValueBuilder(factorySvc, request.session.getExecutionContext().registry);
			DValue keyVal = dvalConverter.buildFromObject(x, pair.type.getShape(), builder);
			
			executeSql(request, typeName, pair, keyVal, vpi);
		}
		
	}

	private void executeSql(InputFunctionRequest request, String typeName, TypePair pair, DValue keyVal, ViaPendingInfo vpi) {
		TypePair relPair = DValueHelper.findField(vpi.structType, vpi.outputFieldName);
		TypePair relKeyPair = DValueHelper.findPrimaryKeyFieldPair(relPair.type);
		
		String addstr = renderx(vpi.processedInputValue, relKeyPair.type); 
		String keystr = renderx(keyVal.asString(), pair.type);
		String src = String.format("update %s[%s] { insert %s:[%s]}", typeName, keystr, vpi.outputFieldName, addstr);
		log.log(src);

		ResultValue res;
		try {
			res = request.delia.continueExecution(src, request.session);
			if (! res.ok) {
//				//err
//				for(DeliaError err: res.errors) {
//					addError(err, errL, lineNum);
//				}
			}
		} catch (DeliaException e) {
//			fnResult.numFailedRowInserts++;
//			DeliaError err = e.getLastError();
//			boolean addErrorFlag = true;
		}		
	}

	private String renderx(Object processedInputValue, DType type) {
		if (type.isShape(Shape.STRING)) {
			return String.format("'%s'", processedInputValue);
		} else { 
			return processedInputValue.toString();
		}
	}

	public ViaInfo findMatch(ViaLineInfo viaLineInfo, String outputFieldName) {
		for(ViaInfo viaInfo: viaLineInfo.viaL) {
			if (outputFieldName.equals(viaInfo.spec.outputField.val2)) {
				return viaInfo;
			}
		}
		return null;
	}

}