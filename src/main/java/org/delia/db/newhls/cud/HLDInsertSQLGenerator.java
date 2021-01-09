package org.delia.db.newhls.cud;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.newhls.HLDAliasManager;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.zdb.ZTableExistenceService;

public class HLDInsertSQLGenerator {
	private DTypeRegistry registry;
	private FactoryService factorySvc;
	private DatIdMap datIdMap;
	private HLDWhereGen whereGen;

	public HLDInsertSQLGenerator(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap, HLDWhereGen whereGen) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.whereGen = whereGen;
	}

	
	public SqlStatementGroup generate(DValue dval) {
//		DBAccessContext dbctx = new DBAccessContext(registry, new DoNothingVarEvaluator());
//		WhereFragmentGenerator whereGen = createWhereFragmentGenerator(dbctx.varEvaluator, zexec.getDatIdMap());
//		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, 
//				new DoNothingVarEvaluator(), tableCreator.alreadyCreatedL, dbctx, sqlHelperFactory, whereGen, null);
//		ZTableExistenceService existSvc = new ZTableExistenceService();
//		fpSvc.setExistSvc(existSvc);
		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
		HLDInsertFragmentParser parser = new HLDInsertFragmentParser(factorySvc, datIdMap, registry, whereGen, aliasMgr);
		parser.existSvc = new ZTableExistenceService();
		parser.tblinfoL = buildTblList(dval); 
		
		String typeName = dval.getType().getName();
		InsertStatementFragment selectFrag = parser.parseInsert(typeName, dval);
		SqlStatementGroup stgroup = parser.renderInsertGroup(selectFrag);
		
		return stgroup;
	}

	public SqlStatementGroup generateUpdate(DValue dval) {
		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
		HLDInsertFragmentParser parser = new HLDInsertFragmentParser(factorySvc, datIdMap, registry, whereGen, aliasMgr);
		parser.existSvc = new ZTableExistenceService();
		parser.tblinfoL = buildTblList(dval); 
		
		String typeName = dval.getType().getName();
		InsertStatementFragment selectFrag = parser.parseInsert(typeName, dval);
		SqlStatementGroup stgroup = parser.renderInsertGroup(selectFrag);
		
		return stgroup;
	}
	


	private List<TableInfo> buildTblList(DValue dval) {
		List<TableInfo> tblinfoL = new ArrayList<>();
		DStructType structType = (DStructType) dval.getType();
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo != null && relinfo.isManyToMany()) {
					String assocTbl = datIdMap.getAssocTblName(relinfo.getDatId());
					TableInfo info = new TableInfo(structType.getName(), assocTbl);
					tblinfoL.add(info);
				}
			}
		}
		return tblinfoL;
	}
}
