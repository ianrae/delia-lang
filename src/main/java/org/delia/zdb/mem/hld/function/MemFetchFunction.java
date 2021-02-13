package org.delia.zdb.mem.hld.function;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.hls.HLSQuerySpan;
import org.delia.hld.QueryFnSpec;
import org.delia.queryfunction.QueryFuncContext;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.BuiltInTypes;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.RelationValueBuilder;
import org.delia.zdb.mem.hld.MemFunctionBase;

public class MemFetchFunction extends MemFunctionBase {
	private FetchRunner fetchRunner;
	private boolean doRFetch;

	public MemFetchFunction(DTypeRegistry registry, FetchRunner fetchRunner, boolean doRFetch) {
		super(registry);
		this.fetchRunner = fetchRunner;
		this.doRFetch = doRFetch;
	}

	@Override
	public QueryResponse process(HLSQuerySpan hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		String targetFieldName = (doRFetch) ? hlspan.rEl.rfieldPair.name : hlspan.subEl.fetchL.get(0); //getStringArg(qfe, ctx);
		return doProcess(targetFieldName, qresp, ctx);
	}
	
	private QueryResponse doProcess(String targetFieldName, QueryResponse qresp, QueryFuncContext ctx) {
		//TODO support multiple fetch('aaa','bbbb')

		//find type of targetFieldName. Address
		//query Address[addrId] for each DValue in qresp.dvalList
		//FUTURE later use IN so can do single query
		
		QueryResponse qresResult = new QueryResponse();
		qresResult.ok = true;
		qresResult.dvalList = new ArrayList<>();
		List<DValue> dvalList = ctx.getDValList();
		List<DValue> newScopeList = new ArrayList<>();
		
		boolean checkFieldExists = true;
		for(DValue dval: dvalList) {
			if (checkFieldExists) {
				checkFieldExists = false;
				DValueHelper.throwIfFieldNotExist("fetch", targetFieldName, dval);
			}
			
			DValue inner = dval.asStruct().getField(targetFieldName);
			if (inner == null) {
//				continue;
				//experimental. auto-populate parent relation
				DStructType structType = (DStructType) dval.getType();
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, targetFieldName);
				if (relinfo != null && relinfo.isParent && ! relinfo.isManyToMany()) {
					inner = this.createRelation(dval, targetFieldName);
					dval.asMap().put(targetFieldName, inner);
					RelationOneRule oneRule = DRuleHelper.findOneRule(structType, targetFieldName);
					if (oneRule != null) {
						oneRule.populateFK(dval, fetchRunner);
						inner = dval.asStruct().getField(targetFieldName); //new relation obj
					} else {
						RelationManyRule manyRule = DRuleHelper.findManyRule(structType, targetFieldName);
						manyRule.populateFK(dval, fetchRunner);
						inner = dval.asStruct().getField(targetFieldName);
					}
					
					//it may be that its an empty relation. if so, don't fetch
					if (inner.asRelation().getMultipleKeys().isEmpty()) {
						inner = null;
						dval.asMap().put(targetFieldName, inner);
						continue;
					}
				}
			}
			
			DRelation drel = inner.asRelation();
			QueryResponse qrespFetch = fetchRunner.load(drel);
			if (!qrespFetch.ok) {
				qresResult.ok = false;
				qresResult.err = qrespFetch.err;
			} else {
				qresResult.dvalList.addAll(qrespFetch.dvalList);
				newScopeList.addAll(qrespFetch.dvalList);
				
				drel.setFetchedItems(qrespFetch.dvalList);
			}
		}

		return qresp;
	}
	
	//create empty relation
	private DValue createRelation(DValue dval, String fieldName) {
		DType relType = registry.getType(BuiltInTypes.RELATION_SHAPE);
		DType farEndType = DValueHelper.findFieldType(dval.getType(), fieldName);
		String typeName = farEndType.getName();
		RelationValueBuilder builder = new RelationValueBuilder(relType, typeName, registry);
		builder.buildEmptyRelation();
		boolean b = builder.finish();
		if (!b) {
			DeliaExceptionHelper.throwError("relation-create-failed-assocCrud", "Type '%s': Failed to create empty relation", typeName);
			return null;
		} else {
			DValue dvalx = builder.getDValue();
			return dvalx;
		}
	}
	
	
	@Override
	public QueryResponse process(QueryFnSpec hlspan, QueryResponse qresp, QueryFuncContext ctx) {
		String targetFieldName = hlspan.structField.fieldName; //.filterFn.argL.get(0).asString();
		return doProcess(targetFieldName, qresp, ctx);
	}
	

}