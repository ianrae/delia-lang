package org.delia.runner;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.db.hld.HLDManager;
import org.delia.log.Log;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class RunnerForMigrateInsert implements Runner {

	@Override
	public List<DValue> lookupVar(String varName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String evalVarAsString(String varName, String typeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Log getLog() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeliaGeneratePhase createGenerator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InternalCompileState getCompileState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExecutionState getExecutionState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean init(ExecutionState execState) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ResultValue executeOneStatement(Exp exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultValue executeProgram(List<Exp> expL) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists(String varName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ResultValue getVar(String varName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DTypeRegistry getRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeRunner createTypeRunner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SprigService getSprigSvc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInsertPrebuiltValueIterator(DValueIterator insertPrebuiltValueIterator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FetchRunner getPrebuiltFetchRunnerToUse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPrebuiltFetchRunnerToUse(FetchRunner prebuiltFetchRunnerToUse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDatIdMap(DatIdMap datIdMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHLDManager(HLDManager mgr) {
		// TODO Auto-generated method stub
		
	}

}
