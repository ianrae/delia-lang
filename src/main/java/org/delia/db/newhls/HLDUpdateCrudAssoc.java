package org.delia.db.newhls;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.newhls.cud.AssocBundle;
import org.delia.db.newhls.cud.HLDDsonBuilder;
import org.delia.db.newhls.cud.HLDInsert;
import org.delia.db.newhls.cud.HLDUpdate;
import org.delia.db.newhls.simple.SimpleBase;
import org.delia.db.newhls.simple.SimpleInsert;
import org.delia.db.newhls.simple.SimpleSqlBuilder;
import org.delia.db.newhls.simple.SimpleUpdate;
import org.delia.db.sql.fragment.UpdateStatementFragment;
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.type.DRelation;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

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

	public HLDUpdateCrudAssoc(DTypeRegistry registry, FactoryService factorySvc, Log log, DatIdMap datIdMap, SprigService sprigSvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.log = log;
		this.sprigSvc = sprigSvc;
		this.simpleBuilder = new SimpleSqlBuilder();

	}
	

	public boolean isCrudAction(HLDUpdate hld, String fieldName) {
		if (hld.cres.assocCrudMap != null) {
			return hld.cres.assocCrudMap.containsKey(fieldName);
		}
		return false;
	}
	public void genAssocCrudZZ(HLDUpdate hld, DValue dval, DValue pkval, RelationInfo relinfo, HLDDsonBuilder hldBuilder, List<SimpleBase> moreL) {
		System.out.println("cedar");
		String fieldName = relinfo.fieldName;
		String assocAction = hld.cres.assocCrudMap.get(fieldName);
		switch(assocAction) {
		case "insert":
			assocCrudInsert(hld, dval, pkval, relinfo, hldBuilder, moreL);
			break;
//		case "update":
//			assocCrudUpdate(selectFrag, structType, mmMap, fieldName, info, selectFrag.whereL, selectFrag.tblFrag.alias, selectFrag.statement);
//			break;
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
		DValue fkval = drel.getForeignKey(); //TODO can there ever be more than one?
		HLDInsert hldins = hldBuilder.buildAssocInsert(relinfo, pkval, fkval, datIdMap);
		SimpleInsert simple = simpleBuilder.buildFrom(hldins);
		moreL.add(simple);
	}
	
}