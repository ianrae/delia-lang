package org.delia.runner.inputfunction;


import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueConverterService;
import org.delia.error.DeliaError;
import org.delia.runner.inputfunction.ViaService.ViaInfo;
import org.delia.type.DValue;


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

	public void executeInsert(ViaPendingInfo vpi, InputFunctionRequest request, InputFunctionResult fnResult,
			int lineNum, List<ViaInfo> viaL, List<DeliaError> errL) {
		// TODO Auto-generated method stub
		log.log("vv: %s: %s", vpi.outputFieldName, vpi.processedInputValue);
		for(ViaInfo info: viaL) {
			if (vpi.outputFieldName.equals(info.spec.outputField.val2)) {
				log.log("SD");
			}
		}
	}

	public ViaInfo findMatch(List<ViaInfo> viaL, String outputFieldName) {
		for(ViaInfo viaInfo: viaL) {
			if (outputFieldName.equals(viaInfo.spec.outputField.val2)) {
				return viaInfo;
			}
		}
		return null;
	}

}