package org.delia.db.newhls;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.newhls.cud.AssocBundle;
import org.delia.db.newhls.cud.HLDUpdate;
import org.delia.db.newhls.simple.SimpleBase;
import org.delia.db.sql.fragment.UpdateStatementFragment;
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

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

	public HLDUpdateCrudAssoc(DTypeRegistry registry, FactoryService factorySvc, Log log, DatIdMap datIdMap, SprigService sprigSvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.log = log;
		this.sprigSvc = sprigSvc;
	}
	

	public boolean isCrudAction(HLDUpdate hld, String fieldName) {
		if (hld.cres.assocCrudMap != null) {
			return hld.cres.assocCrudMap.containsKey(fieldName);
		}
		return false;
	}
	public void genAssocCrudZZ(HLDUpdate hld, RelationInfo relinfo, List<SimpleBase> moreL) {
		System.out.println("cedar");
		String fieldName = relinfo.fieldName;
		String assocAction = hld.cres.assocCrudMap.get(fieldName);
		switch(assocAction) {
		case "insert":
			assocCrudInsert(hld, relinfo, moreL);
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


	private Object assocCrudInsert(HLDUpdate hld, RelationInfo relinfo, List<SimpleBase> moreL) {
		// TODO Auto-generated method stub
		return null;
	}
	
}