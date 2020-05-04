package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.delia.api.DeliaSession;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueConverterService;
import org.delia.error.DeliaError;
import org.delia.error.DetailedError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.LogLevel;
import org.delia.runner.DValueIterator;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.tlang.TLangProgramBuilder;
import org.delia.tlang.runner.TLangProgram;
import org.delia.tlang.runner.TLangVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

public class InputFunctionService extends ServiceBase {
	private Random rand = new Random();
	private DValueConverterService dvalConverter;
	private ImportMetricObserver metricsObserver;
	private InputFunctionServiceOptions options = new InputFunctionServiceOptions();

	public InputFunctionService(FactoryService factorySvc) {
		super(factorySvc);
		this.dvalConverter = new DValueConverterService(factorySvc);
	}

	public ProgramSet buildProgram(String inputFnName, DeliaSession session) {
		ProgramSet progset = new ProgramSet();
		InputFunctionDefStatementExp infnExp = findFunction(inputFnName, session);
		if (infnExp == null) {
			return null;
		}
		progset.inFnExp = infnExp;
		
		addTargetTypes(progset, infnExp, session);
		
		ScalarValueBuilder scalarBuilder = factorySvc.createScalarValueBuilder(session.getExecutionContext().registry);
		
		for(Exp exp: infnExp.bodyExp.statementL) {
			InputFuncMappingExp mappingExp = (InputFuncMappingExp) exp;
//			TLangProgram program = new TLangProgram();
			
			TLangProgramBuilder programBuilder = new TLangProgramBuilder(factorySvc, session.getExecutionContext().registry);
			TLangProgram program = programBuilder.build(mappingExp);
			
			ProgramSpec spec = new ProgramSpec();
			String infield;
			if (mappingExp.isSyntheticInputField()) {
				infield = generateSyntheticFieldName(progset.fieldMap);
				//null is a value synthetic value
				spec.syntheticValue = buildSyntheticValue(mappingExp, scalarBuilder);
				spec.syntheticFieldName = infield;
			} else {
				infield = mappingExp.getInputField();
			}

			spec.inputField = mappingExp.getInputField();
			spec.outputField = mappingExp.outputField;
			spec.prog = program;
			progset.fieldMap.put(infield, spec);
		}

//		progset.hdr = this.createHdrFrom(infnExp);
		return progset;
	}
	private void addTargetTypes(ProgramSet progset, InputFunctionDefStatementExp infnExp, DeliaSession session) {
		DTypeRegistry registry = session.getExecutionContext().registry;
		
		for(IdentPairExp pair: infnExp.argsL) {
			DType dtype = registry.getType(pair.typeName());
			if (dtype == null) {
				DeliaExceptionHelper.throwError("type-not-found-for-import", "Can't find type '%s' in input function '%s'", pair.typeName(), infnExp.funcName);
			}
			if (! dtype.isStructShape()) {
				DeliaExceptionHelper.throwError("type-not-struct-for-import", "Type '%s' is not a struct in input function '%s'", pair.typeName(), infnExp.funcName);
			}
			
			ProgramSet.OutputSpec ospec = new ProgramSet.OutputSpec();
			ospec.structType = (DStructType) dtype;
			ospec.alias = pair.argName();
			progset.outputSpecs.add(ospec);
		}
	}

	private DValue buildSyntheticValue(InputFuncMappingExp mappingExp, ScalarValueBuilder scalarBuilder) {
		return SyntheticFieldHelper.buildSyntheticValue(mappingExp, this.dvalConverter, scalarBuilder);
	}
	private String generateSyntheticFieldName(Map<String, ProgramSpec> map) {
		return SyntheticFieldHelper.generateSyntheticFieldName(map, rand);
	}

	private InputFunctionDefStatementExp findFunction(String inputFnName, DeliaSession session) {
		if (session == null) {
			DeliaExceptionHelper.throwError("no-session", "session is null. You need to call beginSession");
		}
		
		InputFunctionDefStatementExp infnExp = session.getExecutionContext().inputFnMap.get(inputFnName);
		return infnExp;
//		for(Exp exp: sessionimpl.expL) {
//			if (exp instanceof InputFunctionDefStatementExp) {
//				InputFunctionDefStatementExp infnExp = (InputFunctionDefStatementExp) exp;
//				if (infnExp.funcName.equals(inputFnName)) {
//					return infnExp;
//				}
//			}
//		}
//		return null;
	}

	public InputFunctionResult process(InputFunctionRequest request, LineObjIterator lineObjIter) {
		InputFunctionResult fnResult = new InputFunctionResult();
		ErrorTracker localET = new SimpleErrorTracker(log);
		TLangVarEvaluator varEvaluator = new TLangVarEvaluator(request.session.getExecutionContext());
		
		InputFunctionRunner inFuncRunner = new InputFunctionRunner(factorySvc, request.session.getExecutionContext().registry, localET, varEvaluator);
		inFuncRunner.setProgramSet(request.progset);
		inFuncRunner.setMetricsObserver(metricsObserver);
		
		//read header
		HdrInfo hdr = readHeader(request, lineObjIter);
		request.progset.hdr = hdr;
		fnResult.numColumnsProcessedPerRow = request.progset.fieldMap.size();
		fnResult.progset = request.progset;
		
		// - the main loop -- reads csv file line by line
		TypePair keyPair = null;
		int lineNum = 1;
		while(lineObjIter.hasNext()) {
			if (fnResult.errors.size() > request.stopAfterErrorThreshold) {
				log.log("halting -- more than %d errors", request.stopAfterErrorThreshold);
				break;
			}
			if (metricsObserver != null) {
				metricsObserver.onRowStart(request.progset, lineNum);
			}
			
			//log.logDebug("line %d:", lineNum);
			fnResult.numRowsProcessed++;
			LineObj lineObj = lineObjIter.next();

			List<DeliaError> errL = new ArrayList<>();
			List<DValue> dvals = processLineObj(inFuncRunner, hdr, lineObj, errL); //one row
			if (! errL.isEmpty()) {
				log.logError("failed!");
				addErrors(errL, fnResult.errors, lineNum);
			} else {
				if (inFuncRunner.wasHalted()) {
					fnResult.wasHalted = true;
					return fnResult;
				}
				
				for(DValue dval: dvals) {
					if (isDebugLogEnabled()) {
						if (keyPair == null) { //only do once. perf optimization
							keyPair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
						}
						DValue inner = keyPair == null ? null : DValueHelper.getFieldValue(dval, keyPair.name);
						log.logDebug("line %d: dval '%s' %s", lineNum, dval.getType().getName(), inner == null ? "null" : inner.asString());
					}
					fnResult.numRowsInserted++;
					//TODO: queue up a bunch of dvals and then do a batch insert
					executeInsert(dval, request, fnResult, lineNum, errL);
					addErrors(errL, fnResult.errors, lineNum);
				}
			}
			
			if (metricsObserver != null) {
				metricsObserver.onRowEnd(request.progset, lineNum, errL.isEmpty());
			}
			
			lineNum++;
		}

		if (localET.errorCount() > 0) {
			fnResult.errors.addAll(localET.getErrors());
		}
		
		return fnResult;
	}

	private boolean isDebugLogEnabled() {
		return log.getLevel().equals(LogLevel.DEBUG);
	}

	private HdrInfo readHeader(InputFunctionRequest request, LineObjIterator lineObjIter) {
		LineObj hdrLineObj = null; //TODO support more than one later
		int numToIgnore = lineObjIter.getNumHdrRows();
		while (numToIgnore-- > 0) {
			if (!lineObjIter.hasNext()) {
				return null; //empty file
			}
			hdrLineObj = lineObjIter.next();
		}
		
		if (hdrLineObj == null) {
			return createHdrFrom(request.progset);
		}
		
		//build hdr from header row
		ImportSpecBuilder ispecBuilder = new ImportSpecBuilder();
		HdrInfo hdr = new HdrInfo();
		Map<String,ProgramSpec> saveFieldMap = new HashMap<>(request.progset.fieldMap);
		request.progset.fieldMap.clear();
		int index = 0;
		for(String columnName: hdrLineObj.elements) {
			if (saveFieldMap.containsKey(columnName)) {
				ProgramSpec spec = saveFieldMap.get(columnName);
				request.progset.fieldMap.put(columnName, spec);
				hdr.map.put(index, columnName);
				log.logDebug("column: %s", columnName);
				
				ImportSpec ispec = ispecBuilder.findImportSpec(request.progset, spec.outputField);
				ispecBuilder.addInputColumn(ispec, spec.inputField, index, spec.outputField.val2);
			} else {
				log.log("column: %s - can't find match", columnName);
			}
			index++;
		}
		
		log.log("found %d columns", request.progset.fieldMap.size());
		int numMissed = saveFieldMap.size() - request.progset.fieldMap.size();
		if (numMissed != 0) {
			log.log("%d columns missed. Perhaps input function has incorrect input column name?", numMissed);
		}
		
		return hdr;
	}
	private HdrInfo createHdrFrom(ProgramSet progset) {
		InputFunctionDefStatementExp inFnExp = progset.inFnExp;
		ImportSpecBuilder ispecBuilder = new ImportSpecBuilder();
		HdrInfo hdr = new HdrInfo();
		int index = 0;
		for(Exp exp: inFnExp.bodyExp.statementL) {
			InputFuncMappingExp mapping = (InputFuncMappingExp) exp;
			hdr.map.put(index, mapping.getInputField());
			
			ImportSpec ispec = ispecBuilder.findImportSpec(progset, mapping);
			ispecBuilder.addInputColumn(ispec, mapping.getInputField(), index, mapping.outputField.val2);
			index++;
		}
		return hdr;
	}


	private List<DValue> processLineObj(InputFunctionRunner inFuncRunner, HdrInfo hdr, LineObj lineObj, List<DeliaError> errL) {
		List<DValue> dvals = null;
		try {
			dvals = inFuncRunner.process(hdr, lineObj, errL);
		} catch (DeliaException e) {
			addError(e, errL, lineObj.lineNum);
		}
		return dvals;
	}

	private void executeInsert(DValue dval, InputFunctionRequest request, InputFunctionResult fnResult, int lineNum, List<DeliaError> errL) {
		DValueIterator iter = new DValueIterator(dval);
		request.session.setInsertPrebuiltValueIterator(iter);
		String typeName = dval.getType().getName();
		String s = String.format("insert %s {}", typeName);
		
		ResultValue res;
		try {
			res = request.delia.continueExecution(s, request.session);
			if (! res.ok) {
				//err
				for(DeliaError err: res.errors) {
					addError(err, errL, lineNum);
				}
			}
		} catch (DeliaException e) {
			DeliaError err = e.getLastError();
			if (errIdIStartsWith(err, "rule-")) {
				if (metricsObserver != null && err instanceof DetailedError) {
					ImportSpec ispec = findImportSpec(request, (DStructType) dval.getType());
					if (err.getId().equals("rule-relationOne") || err.getId().equals("rule-relationOne")) {
						DetailedError derr = (DetailedError) err;
						metricsObserver.onRelationError(ispec, derr.getFieldName());
					} else {
						DetailedError derr = (DetailedError) err;
						metricsObserver.onInvalid2Error(ispec, derr.getFieldName());
					}
				}
			} else if (errIdIs(err, "duplicate-unique-value")) {
				if (metricsObserver != null && err instanceof DetailedError) {
					DetailedError derr = (DetailedError) err;
					//TODO this will fails for h2 and postgres. fix them!
					ImportSpec ispec = findImportSpec(request, (DStructType) dval.getType());
					metricsObserver.onDuplicateError(ispec, derr.getFieldName());
				}
			}
			
			addError(e, errL, lineNum);
		}
		request.session.setInsertPrebuiltValueIterator(null);
	}		
	private boolean errIdIs(DeliaError err, String target) {
		if (err.getId() != null && err.getId().equals(target)) {
			return true;
		}
		return false;
	}

	private boolean errIdIStartsWith(DeliaError err, String target) {
		if (err.getId() != null && err.getId().startsWith(target)) {
			return true;
		}
		return false;
	}

	private ImportSpec findImportSpec(InputFunctionRequest request, DStructType structType) {
		for(ProgramSet.OutputSpec ospec: request.progset.outputSpecs) {
			if (ospec.structType == structType) {
				return ospec.ispec;
			}
		}
		return null;
	}
	
	private void addError(DeliaError err, List<DeliaError> errL, int lineNum) {
		err.setLineAndPos(lineNum, 0);
		errL.add(err);
		log.logError("error: %s", err.toString());
	}
	private void addError(DeliaException e, List<DeliaError> errL, int lineNum) {
		DeliaError err = e.getLastError();
		addError(err, errL, lineNum);
	}
	private void addErrors(List<DeliaError> srcErrorL, List<DeliaError> errL, int lineNum) {
		for(DeliaError err: srcErrorL) {
			addError(err, errL, lineNum);
		}
	}

	public ImportMetricObserver getMetricsObserver() {
		return metricsObserver;
	}

	public void setMetricsObserver(ImportMetricObserver metricsObserver) {
		this.metricsObserver = metricsObserver;
	}

	public InputFunctionServiceOptions getOptions() {
		return options;
	}

	public void setOptions(InputFunctionServiceOptions options) {
		this.options = options;
	}
}