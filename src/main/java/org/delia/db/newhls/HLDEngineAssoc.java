package org.delia.db.newhls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.newhls.cud.AssocBundle;
import org.delia.db.newhls.cud.HLDDsonBuilder;
import org.delia.db.newhls.cud.HLDUpdate;
import org.delia.db.newhls.simple.SimpleBase;
import org.delia.relation.RelationInfo;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;

/**
 * Generates the lower-level HLD objects such as HLDQuery,HLDInsert,etc
 * The HLD statement classes contain one or more of these lower-level objects
 * 
 * single use!!!
 * 
 * @author ian
 *
 */
public class HLDEngineAssoc extends HLDServiceBase {
	protected VarEvaluator varEvaluator; //set after ctor
	private HLDUpdateCrudAssoc updateCrudAssoc;

	public HLDEngineAssoc(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap, SprigService sprigSvc) {
		super(registry, factorySvc, datIdMap, sprigSvc);
		this.updateCrudAssoc = new HLDUpdateCrudAssoc(registry, factorySvc, datIdMap, sprigSvc);
	}
	
	public List<AssocBundle> xgenAssocField(HLDUpdate hld, QueryExp queryExp, DStructType structType, DValue dval, DValue pkval, HLDQueryBuilderAdapter builderAdapter, HLDDsonBuilder hldBuilder, List<SimpleBase> moreL) {
		List<AssocBundle> bundleL = new ArrayList<>();
		HLDQuery hldQuery = hld.hld;
		
//		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				DValue inner = dval.asStruct().getField(pair.name);
				if (inner == null) {
					continue;
				}
				
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				
				if (updateCrudAssoc.isCrudAction(hld, relinfo.fieldName)) {
					if (relinfo.isManyToMany() || relinfo.isOneToMany()) {
						updateCrudAssoc.genAssocCrudZZ(hld, dval, pkval, relinfo, hldBuilder, builderAdapter, bundleL, moreL);
					}		
				} else if (relinfo.isManyToMany()) {
					for(DValue fkval: inner.asRelation().getMultipleKeys()) {
						List<AssocBundle> innerL = xdoAddAssocField(relinfo, hldQuery, queryExp, structType, dval, pkval, fkval, builderAdapter);
//						insert.assocRelInfo = relinfo;
						bundleL.addAll(innerL);
					}
				}
			}
		}
		return bundleL;
	}
	private List<AssocBundle> xdoAddAssocField(RelationInfo relinfo, HLDQuery hldQuery, QueryExp queryExp, DStructType structType, DValue dval, DValue pkval, DValue fkval, HLDQueryBuilderAdapter builderAdapter) {
		
		//3 scenarios here:
		// 1. updating all records in assoc table
		// 2. updating where filter by primaykey only
		// 3. updating where filter includes other fields (eg Customer.firstName) which may include primaryKey fields.
		if (isAllQuery(hldQuery)) {
			log.logDebug("m-to-n:scenario1");
			return buildUpdateAll(relinfo, queryExp, structType, dval, fkval, builderAdapter);
		} else if (isPKQuery(hldQuery)) {
			log.logDebug("m-to-n:scenario2");
			return buildUpdateByIdOnly(relinfo, queryExp, structType, pkval, fkval, builderAdapter);
		} else {
			log.logDebug("m-to-n:scenario3");
			return buildUpdateOther(relinfo, queryExp, structType, pkval, fkval, builderAdapter);
		}
	}

	private boolean isPKQuery(HLDQuery hldQuery) {
		if (hldQuery.filter instanceof SingleFilterCond) {
			SingleFilterCond sfc = (SingleFilterCond) hldQuery.filter;
			return sfc.isPKQuery();
		}
		return false;
	}
	private boolean isAllQuery(HLDQuery hldQuery) {
		if (hldQuery.filter instanceof SingleFilterCond) {
			SingleFilterCond sfc = (SingleFilterCond) hldQuery.filter;
			return sfc.isAllQuery();
		}
		return false;
	}

	private List<AssocBundle> buildUpdateAll(RelationInfo relinfo, QueryExp queryExp, DStructType structType,
			DValue dval, DValue fkval, HLDQueryBuilderAdapter builderAdapter) {
//		  scenario 1 all:
//		  update Customer[true] {wid: 333, addr: [100]}
//		  has sql:
//		    DONE ALREADY update Customer set wid=333
//		    delete CustomerAddressAssoc
		//part 2. 
//	    MERGE INTO CustomerAddressAssoc as T USING (SELECT id FROM CUSTOMER) AS S
//	    ON T.leftv = s.id WHEN MATCHED THEN UPDATE SET T.rightv = ?
//	    WHEN NOT MATCHED THEN INSERT (leftv, rightv) VALUES(s.id, ?)
		
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc, varEvaluator);
		AssocBundle bundle = new AssocBundle();
		bundle.hlddelete = hldBuilder.buildAssocDeleteAll(builderAdapter, queryExp, relinfo, datIdMap);
		bundle.hlddelete.assocRelInfo = relinfo; 
		
		bundle.hldupdate = hldBuilder.buildAssocUpdateAll(builderAdapter, relinfo, queryExp, fkval, datIdMap, true);
		bundle.hldupdate.assocRelInfo = relinfo; 
		
		return Collections.singletonList(bundle);
	}
	protected List<AssocBundle> buildUpdateByIdOnly(RelationInfo relinfo, QueryExp queryExp, DStructType structType, DValue pkval, DValue fkval, HLDQueryBuilderAdapter builderAdapter) {
//		  scenario 2 id:
//		  update Customer[55] {wid: 333, addr: [100]}
//		  has sql:
//		   DONE ALREADY update Customer set wid=333 where id=55
//		    delete CustomerAddressAssoc where leftv=55 and rightv <> 100
//		    merge into CustomerAddressAssoc key(leftv) values(55,100) //only works if 1 record updated/inserted

		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc, varEvaluator);
		AssocBundle bundle = new AssocBundle();
		bundle.hlddelete = hldBuilder.buildAssocDelete(builderAdapter, queryExp, relinfo, pkval, fkval, datIdMap);
		bundle.hlddelete.assocRelInfo = relinfo; 
		
		bundle.hldupdate = hldBuilder.buildAssocUpdate(builderAdapter, relinfo, queryExp, pkval, fkval, datIdMap, true);
		bundle.hldupdate.assocRelInfo = relinfo; 
		
		return Collections.singletonList(bundle);
	}
	protected List<AssocBundle> buildUpdateOther(RelationInfo relinfo, QueryExp queryExp, DStructType structType, DValue pkval, DValue fkval, HLDQueryBuilderAdapter builderAdapter) {
//		  scenario 3 id:
//		  update Customer[55] {wid: 333, addr: [100]}
//		  has sql:
//		    DONE ALREADY update Customer set wid=333 where wid>20
//  	    delete CustomerAddressAssoc where rightv <> 100 and leftv in (SELECT id FROM Address as a WHERE a.z > ?)
//  	    WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerAssoc as t SELECT * from cte1

		//TODO write this!!!
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc, varEvaluator);
		AssocBundle bundle = new AssocBundle();
		bundle.hlddelete = hldBuilder.buildAssocDeleteOther(builderAdapter, queryExp, relinfo, pkval, fkval, datIdMap);
		bundle.hlddelete.assocRelInfo = relinfo; 
		
//	    WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerAssoc as t SELECT * from cte1
		bundle.hldupdate = hldBuilder.buildAssocUpdateOther(builderAdapter, relinfo, queryExp, pkval, fkval, datIdMap, true);
		bundle.hldupdate.assocRelInfo = relinfo; 
		
		return Collections.singletonList(bundle);
	}

	public VarEvaluator getVarEvaluator() {
		return varEvaluator;
	}

	public void setVarEvaluator(VarEvaluator varEvaluator) {
		this.varEvaluator = varEvaluator;
	}
	
	
	
	
	
}