package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.runner.DValueIterator;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.tlang.runner.TLangProgram;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class InputFunctionService extends ServiceBase {
	public InputFunctionService(FactoryService factorySvc) {
		super(factorySvc);
	}

	public ProgramSet buildProgram(String inputFnName, DeliaSession session) {
		ProgramSet progset = new ProgramSet();
		InputFunctionDefStatementExp infnExp = findFunction(inputFnName, session);
		for(Exp exp: infnExp.bodyExp.statementL) {
			InputFuncMappingExp mappingExp = (InputFuncMappingExp) exp;
			TLangProgram program = new TLangProgram();
			String infield = mappingExp.inputField.name();

			ProgramSpec spec = new ProgramSpec();
			spec.outputField = mappingExp.outputField;
			spec.prog = program;
			progset.map.put(infield, spec);
		}

		progset.hdr = this.createHdrFrom(infnExp);
		return progset;
	}
	private InputFunctionDefStatementExp findFunction(String inputFnName, DeliaSession session) {
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
			hdr.map.put(index, mapping.inputField.name());
			index++;
		}
		return hdr;
	}

	public InputFunctionResult process(InputFunctionRequest request, LineObjIterator lineObjIter) {
		InputFunctionResult fnResult = new InputFunctionResult();
		ErrorTracker localET = new SimpleErrorTracker(log);
		InputFunctionRunner inFuncRunner = new InputFunctionRunner(factorySvc, request.session.getExecutionContext().registry, localET);
		HdrInfo hdr = request.progset.hdr;
		inFuncRunner.setProgramSet(request.progset);
		
		int lineNum = 1;
		while(lineObjIter.hasNext()) {
			log.logDebug("line%d:", lineNum);
			fnResult.numRowsProcessed++;
			LineObj lineObj = lineObjIter.next();

			List<DeliaError> errL = new ArrayList<>();
			List<DValue> dvals = processLineObj(inFuncRunner, hdr, lineObj, errL); //one row
			if (! errL.isEmpty()) {
				log.logError("failed!");
				addErrors(errL, fnResult.totalErrorL, lineNum);
			} else {
				for(DValue dval: dvals) {
					TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
					DValue inner = pair == null ? null : DValueHelper.getFieldValue(dval, pair.name);
					
					log.logDebug("line%d: dval '%s' %s", lineNum, dval.getType().getName(), inner == null ? "null" : inner.asString());
					fnResult.numDValuesProcessed++;
					executeInsert(dval, request, fnResult, lineNum);
				}
			}
			lineNum++;
		}

		if (localET.errorCount() > 0) {
			fnResult.totalErrorL.addAll(localET.getErrors());
		}
		
		return fnResult;
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
					addError(err, fnResult.totalErrorL, lineNum);
				}
			}
		} catch (DeliaException e) {
			addError(e, fnResult.totalErrorL, lineNum);
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