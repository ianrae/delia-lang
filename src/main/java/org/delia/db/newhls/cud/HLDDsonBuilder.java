package org.delia.db.newhls.cud;

import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.UpdateStatementExp;
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
	private SprigService sprigSvc;

	public HLDDsonBuilder(DTypeRegistry registry, FactoryService factorySvc, Log log, SprigService sprigSvc) {
		this.registry = registry;
		this.log = log;
		this.factorySvc = factorySvc;
		this.sprigSvc = sprigSvc;
	}

	public HLDInsert buildInsert(InsertStatementExp insertExp) {
		HLDInsert hldins = new HLDInsert();
		
		DStructType dtype = (DStructType) registry.getType(insertExp.typeName);
		DValueIterator insertPrebuiltValueIterator = null; //TODO
		hldins.cres = buildValue(true, dtype, insertExp.dsonExp, insertPrebuiltValueIterator, sprigSvc);
		return hldins;
	}

	private ConversionResult buildValue(boolean doFull, DStructType dtype, DsonExp dsonExp, DValueIterator insertPrebuiltValueIterator, SprigService sprigSvc) {
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
		if (doFull) {
			cres.dval = converter.convertOne(dtype.getName(), dsonExp, cres);
		} else {
			cres.dval = converter.convertOnePartial(dtype.getName(), dsonExp);
		}
		return cres;
	}

	public HLDUpdate buildUpdate(UpdateStatementExp updateExp) {
		HLDUpdate hldupdate = new HLDUpdate(null);//fill in later
		
		DStructType dtype = (DStructType) registry.getType(updateExp.typeName);
		DValueIterator insertPrebuiltValueIterator = null; //TODO
		hldupdate.cres = buildValue(false, dtype, updateExp.dsonExp, insertPrebuiltValueIterator, sprigSvc);
		return hldupdate;
	}
	
	
}
