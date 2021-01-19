package org.delia.db.newhls;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.QueryBuilderService;
import org.delia.db.newhls.cud.AssocBundle;
import org.delia.db.newhls.cud.HLDDsonBuilder;
import org.delia.db.newhls.cud.HLDInsert;
import org.delia.db.newhls.cud.HLDUpdate;
import org.delia.db.newhls.simple.SimpleBase;
import org.delia.db.newhls.simple.SimpleInsert;
import org.delia.db.newhls.simple.SimpleSqlBuilder;
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

/**
 * Generates the lower-level HLD objects such as HLDQuery,HLDInsert,etc
 * The HLD statement classes contain one or more of these lower-level objects
 * 
 * single use!!!
 * 
 * @author ian
 *
 */
public class HLDUpdateCrudAssoc {
	protected DTypeRegistry registry;
	protected FactoryService factorySvc;
	protected DatIdMap datIdMap;
	protected Log log;
	protected SprigService sprigSvc;
	protected VarEvaluator varEvaluator; //set after ctor
	private SimpleSqlBuilder simpleBuilder;
	private QueryBuilderHelper queryBuilderHelper;

	public HLDUpdateCrudAssoc(DTypeRegistry registry, FactoryService factorySvc, Log log, DatIdMap datIdMap, SprigService sprigSvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.log = log;
		this.sprigSvc = sprigSvc;
		this.simpleBuilder = new SimpleSqlBuilder();
		this.queryBuilderHelper = new QueryBuilderHelper(registry, factorySvc);
	}
	

	public boolean isCrudAction(HLDUpdate hld, String fieldName) {
		if (hld.cres.assocCrudMap != null) {
			return hld.cres.assocCrudMap.containsKey(fieldName);
		}
		return false;
	}
	public void genAssocCrudZZ(HLDUpdate hld, DValue dval, DValue pkval, RelationInfo relinfo, HLDDsonBuilder hldBuilder, HLDQueryBuilderAdapter builderAdapter, List<AssocBundle> bundleL, List<SimpleBase> moreL) {
		System.out.println("cedar");
		String fieldName = relinfo.fieldName;
		String assocAction = hld.cres.assocCrudMap.get(fieldName);
		switch(assocAction) {
		case "insert":
			assocCrudInsert(hld, dval, pkval, relinfo, hldBuilder, moreL);
			break;
		case "update":
			assocCrudUpdate(hld, dval, pkval, relinfo, hldBuilder, builderAdapter, bundleL, moreL);
			break;
//		case "delete":
//			assocCrudDelete(selectFrag, structType, mmMap, fieldName, info, selectFrag.whereL, selectFrag.tblFrag.alias, selectFrag.statement);
//			break;
		default:
			break;
		}
	}


	private void assocCrudInsert(HLDUpdate hld, DValue dval, DValue pkval, RelationInfo relinfo, HLDDsonBuilder hldBuilder, List<SimpleBase> moreL) {
		DValue dd = dval.asStruct().getField(relinfo.fieldName);
		DRelation drel = dd.asRelation();
		
		for(DValue fkval: drel.getMultipleKeys()) {
			HLDInsert hldins = hldBuilder.buildAssocInsert(relinfo, pkval, fkval, datIdMap);
			SimpleInsert simple = simpleBuilder.buildFrom(hldins);
			moreL.add(simple);
		}
	}
//	  update Address[100] { update cust:[55,57]}
	// update CAD set leftv=57 where leftv=55 and rightv=100
	private void assocCrudUpdate(HLDUpdate hld, DValue dval, DValue pkval, RelationInfo relinfo, HLDDsonBuilder hldBuilder, HLDQueryBuilderAdapter builderAdapter, List<AssocBundle> bundleL, List<SimpleBase> moreL) {
		DValue dd = dval.asStruct().getField(relinfo.fieldName);
		DRelation drel = dd.asRelation();
		
		List<DValue> list = drel.getMultipleKeys();
		if (list.size() % 2 != 0) {
			DeliaExceptionHelper.throwError("bad-assoc-crud-update", "Type '%s'. update CRUD action requires pairs of ids (old,new)", hld.typeOrTbl.getTblName());
		}
		
		//
		String assocTbl = datIdMap.getAssocTblName(relinfo.getDatId());
		String fld1 = datIdMap.getAssocFieldFor(relinfo);
		String fld2 = datIdMap.getAssocOtherField(relinfo);
		
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		
		for(int i = 0; i < list.size(); i+=2) {
			DValue oldval = list.get(i);
			DValue newval = list.get(i+1);
			
			QueryExp exp1 = builderSvc.createEqQuery(assocTbl, fld1, oldval); //leftv=55
			QueryExp exp2 = builderSvc.createEqQuery(assocTbl, fld2, pkval); //right=100
			QueryExp exp3 = builderSvc.createAndQuery(assocTbl, exp1, exp2);
			
			HLDUpdate hldup = hldBuilder.buildAssocUpdateOther(builderAdapter, relinfo, exp3, pkval, newval, datIdMap, false);
			AssocBundle bundle = new AssocBundle();
			bundle.hldupdate = hldup;
			bundleL.add(bundle);
		}
	}



}