package org.delia.db.newhls.cud;

import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.core.FactoryService;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.runner.ConversionResult;
import org.delia.runner.DValueIterator;
import org.delia.runner.DsonToDValueConverter;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;

public class HLDDsonBuilder {

	private DTypeRegistry registry;
	private Log log;
	private FactoryService factorySvc;

	public HLDDsonBuilder(DTypeRegistry registry, FactoryService factorySvc, Log log) {
		this.registry = registry;
		this.log = log;
		this.factorySvc = factorySvc;
	}

	public HLDInsert buildInsert(InsertStatementExp insertExp) {
		// TODO Auto-generated method stub
		return null;
	}

	private ConversionResult buildValue(DStructType dtype, DsonExp dsonExp, DValueIterator insertPrebuiltValueIterator, SprigService sprigSvc) {
		ConversionResult cres = new ConversionResult();
		cres.localET = new SimpleErrorTracker(log);
		if (insertPrebuiltValueIterator != null) {
			cres.dval = insertPrebuiltValueIterator.next();
			return cres;
		}

		VarEvaluator varEvaluator = null;//runner;
		//			if (sprigSvc.haveEnabledFor(dtype.getName())) {
		varEvaluator = new SprigVarEvaluator(factorySvc, null); //TODO fixrunner);
		//			}

		DsonToDValueConverter converter = new DsonToDValueConverter(factorySvc, cres.localET, registry, varEvaluator, sprigSvc);
		cres.dval = converter.convertOne(dtype.getName(), dsonExp, cres);
		return cres;
	}
	
	
}
