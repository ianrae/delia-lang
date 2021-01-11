package org.delia.db.newhls.cud;

import java.util.List;
import java.util.Optional;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.QueryBuilderService;
import org.delia.db.newhls.HLDField;
import org.delia.db.newhls.HLDQueryBuilderAdapter;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
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
import org.delia.type.OrderedMap;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.PartialStructValueBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;

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
		
		fillArrays(hldins.cres.dval, hldins.fieldL, hldins.valueL, true);
		
		return hldins;
	}

	private boolean shouldSkipField(DStructHelper helper, TypePair pair) {
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(helper.getType(), pair);
		if (relinfo != null && relinfo.notContainsFK()) {
			return true; //parent doesn't have fk value.
		}
		return false;
	}

	private HLDField createFieldVal(DValue dval, String fieldName, DValue inner, DStructHelper helper) {
		HLDField fld = new HLDField();
		fld.fieldName = fieldName;
		fld.fieldType = inner == null ? determineField(fieldName, helper) : inner.getType();
		fld.structType = helper.getType();
		return fld;
	}
//	private HLDField createEmptyFieldVal(String fieldName, DStructType type) {
//		HLDField fld = new HLDField();
//		fld.fieldName = fieldName;
//		fld.fieldType = null;
//		fld.structType = type;
//		return fld;
//	}

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
		
		fillArraysForUpdate(hldupdate);
		
		return hldupdate;
	}

	private void fillArraysForUpdate(HLDUpdate hldupdate) {
		fillArrays(hldupdate.cres.dval, hldupdate.fieldL, hldupdate.valueL, false);
	}

	private void fillArrays(DValue dval, List<HLDField> fieldL, List<DValue> valueL, boolean includePK) {
		DStructHelper helper = dval.asStruct();
		TypePair skipPKPair = (includePK) ? null : DValueHelper.findPrimaryKeyFieldPair(helper.getType());
		
		for(TypePair pair: helper.getType().getAllFields()) {
			if (shouldSkipField(helper, pair)) {
				continue;
			}
			if (skipPKPair != null && skipPKPair.name.equals(pair.name)) {
				continue; //update doesn't include pk in fieldL
			}
			if (helper.hasField(pair.name)) {
				DValue inner = dval.asStruct().getField(pair.name);
				fieldL.add(createFieldVal(dval, pair.name, inner, helper));
				valueL.add(inner);
			}
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
		fillArraysForUpdate(hldupdate);
		
		return hldupdate;
	}

	public HLDInsert buildSimpleInsert(DStructType structType, String pkFieldName, DValue pkval, String fieldName, DValue fkval) {
		HLDInsert hldins = new HLDInsert(new TypeOrTable(structType));
		
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
		
		hldins.cres = cres;
		fillArrays(hldins.cres.dval, hldins.fieldL, hldins.valueL, true);
		
		return hldins;
	}

	public HLDInsert buildAssocInsert(RelationInfo relinfo, DValue dval1, DValue dval2, DatIdMap datIdMap) {
		String assocTbl = datIdMap.getAssocTblName(relinfo.getDatId());
		String fld1 = datIdMap.getAssocFieldFor(relinfo);
		String fld2 = datIdMap.getAssocOtherField(relinfo);
		
		HLDInsert hldins = new HLDInsert(new TypeOrTable(assocTbl));
		
		ConversionResult cres = new ConversionResult();
		cres.localET = new SimpleErrorTracker(log);

		//create a temp type for the assoc table
		DStructType structType = buildTempDatType(assocTbl, relinfo, datIdMap); 
		
		PartialStructValueBuilder builder = new PartialStructValueBuilder(structType);
		builder.addField(fld1, dval1);
		builder.addField(fld2, dval2);
		if (!builder.finish()) {
			DeliaExceptionHelper.throwError("buildSimpleUpdate-fail", structType.getName());
		}
		cres.dval = builder.getDValue();
		
		hldins.cres = cres;
		fillArrays(hldins.cres.dval, hldins.fieldL, hldins.valueL, true);
		
		return hldins;
	}
	public HLDUpdate buildAssocUpdate(HLDQueryBuilderAdapter builderAdapter, RelationInfo relinfo, QueryExp queryExp, DValue dval1, DValue dval2, DatIdMap datIdMap, boolean isMergeInto) {
		String assocTbl = datIdMap.getAssocTblName(relinfo.getDatId());
		String fld1 = datIdMap.getAssocFieldFor(relinfo);
		String fld2 = datIdMap.getAssocOtherField(relinfo);
		
		HLDUpdate hld = new HLDUpdate(new TypeOrTable(assocTbl), null);
		
		hld.cres = fillCResForUpdate(hld, fld1, fld2, assocTbl, dval1, dval2, relinfo, datIdMap);
		fillArrays(hld.cres.dval, hld.fieldL, hld.valueL, true);
		hld.hld = builderAdapter.buildQuery(queryExp);
		if (isMergeInto) {
			hld.isMergeInto = true;
			hld.mergeKey = fld1;
		}
		return hld;
	}
	private ConversionResult fillCResForUpdate(HLDUpdate hld, String fld1, String fld2, String assocTbl, DValue dval1,
			DValue dval2, RelationInfo relinfo, DatIdMap datIdMap) {
		ConversionResult cres = new ConversionResult();
		cres.localET = new SimpleErrorTracker(log);

		//create a temp type for the assoc table
		DStructType structType = buildTempDatType(assocTbl, relinfo, datIdMap); 
		
		PartialStructValueBuilder builder = new PartialStructValueBuilder(structType);
		builder.addField(fld1, dval1);
		builder.addField(fld2, dval2);
		if (!builder.finish()) {
			DeliaExceptionHelper.throwError("buildSimpleUpdate-fail", structType.getName());
		}
		cres.dval = builder.getDValue();
		return cres;
	}

	public HLDUpdate buildAssocUpdateAll(HLDQueryBuilderAdapter builderAdapter, RelationInfo relinfo, QueryExp queryExp, DValue dval1, DatIdMap datIdMap, boolean isMergeInto) {
		String assocTbl = datIdMap.getAssocTblName(relinfo.getDatId());
		String fld1 = datIdMap.getAssocFieldFor(relinfo);
		String fld2 = datIdMap.getAssocOtherField(relinfo);

		HLDUpdate hld = new HLDUpdate(new TypeOrTable(assocTbl), null);
		hld.cres = fillCResForUpdate(hld, fld1, fld2, assocTbl, dval1, null, relinfo, datIdMap);
		fillArrays(hld.cres.dval, hld.fieldL, hld.valueL, true);

		//create a temp type for the assoc table
//		DStructType structType = hld.cres.dval.asStruct().getType(); 
//		TypePair pair = DValueHelper.findField(structType, fld1);
		
//		DValue inner = null;
//		hld.fieldL.add(createEmptyFieldVal(pair.name, structType));
//		hld.valueL.add(inner);
		
		hld.hld = builderAdapter.buildQuery(queryExp);
		if (isMergeInto) {
			hld.isMergeAllInto = true;
			hld.mergeKey = fld1;
			hld.mergeKeyOther = fld2;
			DStructType entityType = datIdMap.isFlipped(relinfo) ? relinfo.farType : relinfo.nearType;
			hld.mergeType = entityType.getName();
			TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(entityType);
			hld.mergePKField = pkpair.name;
		}
		return hld;
	}
	
//    delete CustomerAddressAssoc where leftv=55 and rightv <> 100
	public HLDDelete buildAssocDelete(HLDQueryBuilderAdapter builderAdapter, QueryExp queryExp, RelationInfo relinfo, DValue dval1, DValue dval2, DatIdMap datIdMap) {
		String assocTbl = datIdMap.getAssocTblName(relinfo.getDatId());
		
		String fld1 = datIdMap.getAssocFieldFor(relinfo);
		String fld2 = datIdMap.getAssocOtherField(relinfo);
		
		//create a temp type for the assoc table
		DStructType structType = buildTempDatType(assocTbl, relinfo, datIdMap); 
		
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		QueryExp exp1 = builderSvc.createEqQuery(assocTbl, fld1, dval1);
		QueryExp exp2 = builderSvc.createNotEqQuery(assocTbl, fld2, dval2);
		QueryExp exp3 = builderSvc.createAndQuery(assocTbl, exp1, exp2);
		
		HLDDelete hld = new HLDDelete(new TypeOrTable(assocTbl));
		hld.hld = builderAdapter.buildQueryEx(exp3, structType);
		
		return hld;
	}
//  delete CustomerAddressAssoc 
	public HLDDelete buildAssocDeleteAll(HLDQueryBuilderAdapter builderAdapter, QueryExp queryExp, RelationInfo relinfo, DatIdMap datIdMap) {
		//create a temp type for the assoc table
		String assocTbl = datIdMap.getAssocTblName(relinfo.getDatId());
		DStructType structType = buildTempDatType(assocTbl, relinfo, datIdMap); 
		
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		QueryExp exp1 = builderSvc.createAllRowsQuery(assocTbl);
		
		HLDDelete hld = new HLDDelete(new TypeOrTable(assocTbl));
		hld.hld = builderAdapter.buildQueryEx(exp1, structType);
		
		return hld;
	}
//  delete CustomerAddressAssoc where rightv <> 100 and leftv in (SELECT id FROM Address as a WHERE a.z > ?)
	public HLDDelete buildAssocDeleteOther(HLDQueryBuilderAdapter builderAdapter, QueryExp queryExp, RelationInfo relinfo, DValue dval1, DValue dval2, DatIdMap datIdMap) {
		String assocTbl = datIdMap.getAssocTblName(relinfo.getDatId());
		
		String fld1 = datIdMap.getAssocFieldFor(relinfo);
		String fld2 = datIdMap.getAssocOtherField(relinfo);
		
		//create a temp type for the assoc table
		DStructType structType = buildTempDatType(assocTbl, relinfo, datIdMap); 
		
		//need a fake value just so fields created. we don't use the value in sql
		DStructType entityType = datIdMap.isFlipped(relinfo) ? relinfo.farType : relinfo.nearType;
		ScalarValueBuilder valBuilder = factorySvc.createScalarValueBuilder(registry);
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(entityType);
		switch(pkpair.type.getShape()) {
		case INTEGER:
			dval1 = valBuilder.buildInt("999");
			break;
		case LONG:
			dval1 = valBuilder.buildInt("999");
			break;
		case DATE:
		case STRING:
			dval1 = valBuilder.buildInt("999");
			break;
		default:
			DeliaExceptionHelper.throwError("unknown-pk-type", "%s: %s can't be pk", pkpair.name, pkpair.type);
		}
		
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		QueryExp exp1 = builderSvc.createEqQuery(assocTbl, fld1, dval1);
		QueryExp exp2 = builderSvc.createNotEqQuery(assocTbl, fld2, dval2);
		QueryExp exp3 = builderSvc.createAndQuery(assocTbl, exp1, exp2);
		
		HLDDelete hld = new HLDDelete(new TypeOrTable(assocTbl));
		hld.hld = builderAdapter.buildQueryEx(exp3, structType);
		hld.useDeleteIn = true;
		return hld;
	}
	

//	private StructField addStructField(FilterVal val1, FilterVal val2, DStructType structType, String fld1) {
//		if (val1.isSymbol()) {
//			TypePair pair = DValueHelper.findField(structType, fld1);
//			return new StructField(structType, fld1, pair.type);
//		} else if (val2.isSymbol()) {
//			TypePair pair = DValueHelper.findField(structType, fld1);
//			return new StructField(structType, fld1, pair.type);
//		} else {
//			return null;
//		}
//	}

	private DStructType buildTempDatType(String assocTbl, RelationInfo relinfo, DatIdMap datIdMap) {
		TypePair pkpair1 = DValueHelper.findPrimaryKeyFieldPair(relinfo.nearType);
		TypePair pkpair2 = DValueHelper.findPrimaryKeyFieldPair(relinfo.farType);
		boolean flipped = datIdMap.isFlipped(relinfo);
		if (flipped) { //swap
			TypePair tmp = pkpair1;
			pkpair1 = pkpair2;
			pkpair2 = tmp;
		}
		
		OrderedMap omap = new OrderedMap();
		//normally not optional but sometimes for MERGE INTO it is
		omap.add("leftv", pkpair1.type, true, false, false, false);
		omap.add("rightv", pkpair2.type, true, false, false, false);
		DStructType structType = new DStructType(Shape.STRUCT, assocTbl, null, omap, null);
		//we don't register this type
		return structType;
	}
}
