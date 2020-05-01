package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.delia.api.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueConverterService;
import org.delia.error.DeliaError;
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
				spec.syntheticValue = buildSyntheticValue(mappingExp, scalarBuilder);
			} else {
				infield = mappingExp.getInputField();
			}

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
			progset.outputTypes.add((DStructType) dtype);
			progset.outputAliases.add(pair.argName());
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
		
		DeliaSessionImpl sessionimpl = (DeliaSessionImpl) session;
		for(Exp exp: sessionimpl.expL) {
			if (exp instanceof InputFunctionDefStatementExp) {
				InputFunctionDefStatementExp infnExp = (InputFunctionDefStatementExp) exp;
				if (infnExp.funcName.equals(inputFnName)) {
					return infnExp;
				}
			}
		}
		return null;
	}
	private HdrInfo createHdrFrom(InputFunctionDefStatementExp inFnExp) {
		HdrInfo hdr = new HdrInfo();
		int index = 0;
		for(Exp exp: inFnExp.bodyExp.statementL) {
			InputFuncMappingExp mapping = (InputFuncMappingExp) exp;
			hdr.map.put(index, mapping.getInputField());
			index++;
		}
		return hdr;
	}

	public InputFunctionResult process(InputFunctionRequest request, LineObjIterator lineObjIter) {
		InputFunctionResult fnResult = new InputFunctionResult();
		ErrorTracker localET = new SimpleErrorTracker(log);
		TLangVarEvaluator varEvaluator = new TLangVarEvaluator(request.session.getExecutionContext());
		
		InputFunctionRunner inFuncRunner = new InputFunctionRunner(factorySvc, request.session.getExecutionContext().registry, localET, varEvaluator);
		inFuncRunner.setProgramSet(request.progset);
		
		//read header
		HdrInfo hdr = readHeader(request, lineObjIter);
		request.progset.hdr = hdr;
		fnResult.numColumnsProcessedPerRow = request.progset.fieldMap.size();
		fnResult.progset = request.progset;
		
		TypePair keyPair = null;
		int lineNum = 1;
		while(lineObjIter.hasNext()) {
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
					executeInsert(dval, request, fnResult, lineNum);
				}
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
			return this.createHdrFrom(request.progset.inFnExp);
		}
		
		//build hdr from header row
		HdrInfo hdr = new HdrInfo();
		Map<String,ProgramSpec> saveFieldMap = new HashMap<>(request.progset.fieldMap);
		request.progset.fieldMap.clear();
		int index = 0;
		for(String columnName: hdrLineObj.elements) {
			if (saveFieldMap.containsKey(columnName)) {
				ProgramSpec spec = saveFieldMap.get(columnName);
				request.progset.fieldMap.put(columnName, spec);
				hdr.map.put(index, columnName);
			}
			index++;
		}
		
		int numMissed = saveFieldMap.size() - request.progset.fieldMap.size();
		if (numMissed != 0) {
			log.log("%d columns missed. Perhaps input function has incorrect input column name?", numMissed);
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

	private void executeInsert(DValue dval, InputFunctionRequest request, InputFunctionResult fnResult, int lineNum) {
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
					addError(err, fnResult.errors, lineNum);
				}
			}
		} catch (DeliaException e) {
			addError(e, fnResult.errors, lineNum);
		}
		request.session.setInsertPrebuiltValueIterator(null);
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
}