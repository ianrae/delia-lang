//package org.delia.sprig;
//
//import org.delia.core.FactoryService;
//import org.delia.core.ServiceBase;
//import org.delia.runner.Runner;
//import org.delia.runner.VarEvaluator;
//import org.delia.type.DValue;
//
//import java.util.List;
//
//public class SprigVarEvaluator extends ServiceBase implements VarEvaluator {
//
//	private Runner runner;
//
//	public SprigVarEvaluator(FactoryService factorySvc, Runner runner) {
//		super(factorySvc);
//		this.runner = runner;
//	}
//
//	@Override
//	public List<DValue> lookupVar(String varName) {
//		throw new IllegalArgumentException("SprigVarEvaluator.lookupVar not IMPLEMENTED!");
//	}
//
//	@Override
//	public String evalVarAsString(String varName, String typeName) {
//		SprigService sprigSvc = runner.getSprigSvc();
//		DValue dval = sprigSvc.resolveSyntheticId(typeName, varName);
//		if (dval == null) {
//			return runner.evalVarAsString(varName, typeName);
//		}
//		return dval.asString();
//	}
//
//}
