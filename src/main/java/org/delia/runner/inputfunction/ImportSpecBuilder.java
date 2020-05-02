package org.delia.runner.inputfunction;

import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.type.DStructType;
import org.delia.type.TypePair;

public class ImportSpecBuilder {

	public ImportSpec buildSpecFor(ProgramSet progset, DStructType structType) {
		ImportSpec ispec = new ImportSpec();
		ispec.structType = structType;

		String alias = findAlias(progset, structType);

		int index = 0;
		for(TypePair pair: structType.getAllFields()) {
			ProgramSpec pspec = findField(progset, alias, pair.name);
			if (pspec == null) {
				//not being imported
			} else {
				OutputFieldHandle ofh = new OutputFieldHandle();
				ofh.structType = structType;
				ofh.fieldIndex = index;
				ofh.fieldName = pair.name;
				ofh.syntheticValue = pspec.syntheticValue; 
				ofh.arMetrics = new int[OutputFieldHandle.NUM_METRICS];
				
				ispec.ofhList.add(ofh);
			}
			index++;
		}

		return ispec;
	}

	private String findAlias(ProgramSet progset, DStructType structType) {
		for(ProgramSet.OutputSpec ospec: progset.outputSpecs) {
			if (ospec.structType == structType) {
				return ospec.alias;
			}
		}
		return null;
	}
	public ImportSpec findImportSpec(ProgramSet progset, InputFuncMappingExp mapping) {
		for(ProgramSet.OutputSpec ospec: progset.outputSpecs) {
			if (ospec.alias.equals(mapping.outputField.val1)) {
				return ospec.ispec;
			}
		}
		return null;
	}

	private ProgramSpec findField(ProgramSet progset, String alias, String outputFieldName) {
		for(String inputField: progset.fieldMap.keySet()) {
			ProgramSpec pspec = progset.fieldMap.get(inputField);
			if (pspec.outputField.val1.equals(alias) && pspec.outputField.val2.equals(outputFieldName)) {
				return pspec;
			}
		}
		return null;
	}

	public void addInputColumn(ImportSpec ispec, String columnName, int colIndex, String outputFieldName) {
		InputFieldHandle ifh = new InputFieldHandle();
		ifh.columnIndex = colIndex;
		ifh.columnName = columnName;
		ispec.ifhList.add(ifh);

		for(OutputFieldHandle ofh: ispec.ofhList) {
			if (ofh.fieldName.equals(outputFieldName)) {
				ofh.ifhIndex = ispec.ifhList.size() - 1;
				break;
			}
		}

	}
}