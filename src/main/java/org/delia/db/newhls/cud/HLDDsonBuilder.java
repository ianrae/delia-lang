package org.delia.db.newhls.cud;

import java.util.List;
import java.util.Optional;

import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.newhls.HLDField;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.runner.ConversionResult;
import org.delia.runner.DValueIterator;
import org.delia.runner.DsonToDValueConverter;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigVarEvaluator;
import org.delia.type.DStructHelper;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.PartialStructValueBuilder;

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
		
		DStructType dtype = (DStructType) registry.getType(insertExp.typeName);
		HLDInsert hldins = new HLDInsert(new TypeOrTable(dtype));
		DValueIterator insertPrebuiltValueIterator = null; //TODO
		hldins.cres = buildValue(true, dtype, insertExp.dsonExp, insertPrebuiltValueIterator, sprigSvc);
		
		DValue dval = hldins.cres.dval;
		DStructHelper helper = dval.asStruct();
		for(TypePair pair: helper.getType().getAllFields()) {
			DValue inner = dval.asStruct().getField(pair.name);
			hldins.fieldL.add(createFieldVal(dval, pair.name, inner, helper));
			hldins.valueL.add(inner);
		}
		
		return hldins;
	}

	private HLDField createFieldVal(DValue dval, String fieldName, DValue inner, DStructHelper helper) {
		HLDField fld = new HLDField();
		fld.fieldName = fieldName;
		fld.fieldType = inner == null ? determineField(fieldName, helper) : inner.getType();
		fld.structType = helper.getType();
		return fld;
	}

	private DType determineField(String fieldName, DStructHelper helper) {
		List<TypePair> list = helper.getType().getAllFields();
		Optional<TypePair> opt = list.stream().filter(x -> x.name.equals(fieldName)).findAny();
		return opt.get().type;
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
		DStructType dtype = (DStructType) registry.getType(updateExp.typeName);
		HLDUpdate hldupdate = new HLDUpdate(new TypeOrTable(dtype), null);//fill in later
		
		DValueIterator insertPrebuiltValueIterator = null; //TODO
		hldupdate.cres = buildValue(false, dtype, updateExp.dsonExp, insertPrebuiltValueIterator, sprigSvc);
		
		fillArrays(hldupdate);
		
		return hldupdate;
	}

	private void fillArrays(HLDUpdate hldupdate) {
		DValue dval = hldupdate.cres.dval;
		DStructHelper helper = dval.asStruct();
		for(TypePair pair: helper.getType().getAllFields()) {
			DValue inner = dval.asStruct().getField(pair.name);
			hldupdate.fieldL.add(createFieldVal(dval, pair.name, inner, helper));
			hldupdate.valueL.add(inner);
		}
	}

	public HLDUpdate buildSimpleUpdate(DStructType structType, String pkFieldName, DValue pkval, String fieldName, DValue fkval) {
		HLDUpdate hldupdate = new HLDUpdate(new TypeOrTable(structType), null);//fill in later
		
		ConversionResult cres = new ConversionResult();
		cres.localET = new SimpleErrorTracker(log);

		//build partial type with pk and one val
		PartialStructValueBuilder builder = new PartialStructValueBuilder(structType);
		builder.addField(pkFieldName, pkval);
		builder.addField(fieldName, fkval);
		if (!builder.finish()) {
			DeliaExceptionHelper.throwError("buildSimpleUpdate-fail", structType.getName());
		}
		cres.dval = builder.getDValue();
		
		hldupdate.cres = cres;
		fillArrays(hldupdate);
		
		return hldupdate;
	}
	
	
}
