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
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
import org.delia.sprig.SprigService;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

/**
 * Generates the lower-level HLD objects such as HLDQuery,HLDInsert,etc
 * The HLD statement classes contain one or more of these lower-level objects
 * 
 * single use!!!
 * 
 * @author ian
 *
 */
public class HLDEngineAssoc {
	protected DTypeRegistry registry;
	protected FactoryService factorySvc;
	protected DatIdMap datIdMap;
	protected Log log;
	protected SprigService sprigSvc;

	public HLDEngineAssoc(DTypeRegistry registry, FactoryService factorySvc, Log log, DatIdMap datIdMap, SprigService sprigSvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.log = log;
		this.sprigSvc = sprigSvc;
	}
	
	public List<AssocBundle> xgenAssocField(HLDQuery hldQuery, QueryExp queryExp, DStructType structType, DValue dval, DValue pkval, HLDQueryBuilderAdapter builderAdapter) {
		List<AssocBundle> bundleL = new ArrayList<>();
		
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				DValue inner = dval.asStruct().getField(pair.name);
				if (inner == null) {
					continue;
				}
				
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo.isManyToMany()) {
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
	public List<AssocBundle> xdoAddAssocField(RelationInfo relinfo, HLDQuery hldQuery, QueryExp queryExp, DStructType structType, DValue dval, DValue pkval, DValue fkval, HLDQueryBuilderAdapter builderAdapter) {
		
		//3 scenarios here:
		// 1. updating all records in assoc table
		// 2. updating where filter by primaykey only
		// 3. updating where filter includes other fields (eg Customer.firstName) which may include primaryKey fields.
		if (isAllQuery(hldQuery)) {
			log.logDebug("m-to-n:scenario1");
			return buildUpdateAll(relinfo, queryExp, structType, dval, fkval, builderAdapter);
		} else if (isPKQuery(hldQuery)) {
//			List<OpFragment> oplist = WhereListHelper.findPrimaryKeyQuery(existingWhereL, info.farType);
			log.logDebug("m-to-n:scenario2");
			return buildUpdateByIdOnly(relinfo, queryExp, structType, pkval, fkval, builderAdapter);
		} else {
			log.logDebug("m-to-n:scenario3");
//			buildUpdateOther(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, field1, field2, existingWhereL, mainUpdateAlias, statement);
			return null;
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
		
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);
		AssocBundle bundle = new AssocBundle();
		bundle.hlddelete = hldBuilder.buildAssocDeleteAll(builderAdapter, queryExp, relinfo, datIdMap);
		bundle.hlddelete.assocRelInfo = relinfo; 
		
//		bundle.hldupdate = hldBuilder.buildAssocUpdate(builderAdapter, relinfo, queryExp, pkval, fkval, datIdMap, true);
//		bundle.hldupdate.assocRelInfo = relinfo; 
		
		return Collections.singletonList(bundle);
	}
	protected List<AssocBundle> buildUpdateByIdOnly(RelationInfo relinfo, QueryExp queryExp, DStructType structType, DValue pkval, DValue fkval, HLDQueryBuilderAdapter builderAdapter) {
//		  scenario 2 id:
//		  update Customer[55] {wid: 333, addr: [100]}
//		  has sql:
//		   DONE ALREADY update Customer set wid=333 where id=55
//		    delete CustomerAddressAssoc where leftv=55 and rightv <> 100
//		    merge into CustomerAddressAssoc key(leftv) values(55,100) //only works if 1 record updated/inserted

		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);
		AssocBundle bundle = new AssocBundle();
		bundle.hlddelete = hldBuilder.buildAssocDelete(builderAdapter, queryExp, relinfo, pkval, fkval, datIdMap);
		bundle.hlddelete.assocRelInfo = relinfo; 
		
		bundle.hldupdate = hldBuilder.buildAssocUpdate(builderAdapter, relinfo, queryExp, pkval, fkval, datIdMap, true);
		bundle.hldupdate.assocRelInfo = relinfo; 
		
		return Collections.singletonList(bundle);
	}
//	protected void buildUpdateOther(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType,
//			Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, String assocField2,
//			List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {
//
//		updateFrag.doUpdateLast = true; //in case we're updating any of the fields in the query
//		if (assocTblReplacer != null) {
//			log.logDebug("use assocTblReplacer");
//			assocTblReplacer.buildUpdateOther(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, assocField2, existingWhereL, mainUpdateAlias, statement);
//		} else {
//			int startingNumParams = statement.paramL.size();
//			buildAssocTblUpdate(assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, statement);
//	
//			//update CAAssoc set rightv=100 where (select id from customer where lastname='smith')
//			//Create a sub-select whose where list is a copy of the main update statement's where list.
//			TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
//			StrCreator sc = new StrCreator();
//			sc.o(" %s.%s IN", assocUpdateFrag.tblFrag.alias, assocField2);
//			sc.o(" (SELECT %s FROM %s as %s WHERE", keyPair.name, info.nearType.getName(), mainUpdateAlias);
//	
//			List<OpFragment> clonedL = WhereListHelper.cloneWhereList(existingWhereL);
//			for(OpFragment opff: clonedL) {
//				sc.o(opff.render());
//			}
//			sc.o(")");
//			RawFragment rawFrag = new RawFragment(sc.toString());
//	
//			assocUpdateFrag.whereL.add(rawFrag);
//			int extra = statement.paramL.size() - startingNumParams;
//			cloneParams(statement, clonedL, extra);
//		}
//	}
	
	
	
	
	
}