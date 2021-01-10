package org.delia.db.newhls.cud;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.delia.assoc.DatIdMap;
import org.delia.assoc.DatIdMap.DatInfo;
import org.delia.core.FactoryService;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.newhls.HLDAliasManager;
import org.delia.db.sql.fragment.AssocTableReplacer;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.db.sql.fragment.UpdateStatementFragment;
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

	public SqlStatementGroup generateUpdate(HLDUpdate hldupdate) {
//		WhereFragmentGenerator whereGen = createWhereFragmentGenerator(varEvaluator, zexec.getDatIdMap());
//		DBAccessContext dbctx = new DBAccessContext(registry, varEvaluator);
//		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, 
//				new DoNothingVarEvaluator(), tableCreator.alreadyCreatedL, dbctx, sqlHelperFactory, whereGen, null);
//		ZTableExistenceService existSvc = new ZTableExistenceService();
//		fpSvc.setExistSvc(existSvc);
//		
//		AssocTableReplacer assocTblReplacer = createAssocTableReplacer(fpSvc);
//		UpdateFragmentParser parser = new UpdateFragmentParser(factorySvc, fpSvc, assocTblReplacer);
//		whereGen.tableFragmentMaker = parser;
//		adjustParser(parser);
//		QueryDetails details = new QueryDetails();
//		UpdateStatementFragment updateFrag = parser.parseUpdate(spec, details, dvalPartial, assocCrudMap);
//		stgroup = parser.renderUpdateGroup(updateFrag);
//		return stgroup;
		
		
		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
//		HLDInsertFragmentParser parser = new HLDInsertFragmentParser(factorySvc, datIdMap, registry, whereGen, aliasMgr);
		AssocTableReplacer assocTblReplacer = new AssocTableReplacer(factorySvc, null);
		HLDUpdateFragmentParser parser = new HLDUpdateFragmentParser(factorySvc, datIdMap, registry, whereGen, aliasMgr, assocTblReplacer);
		
		parser.existSvc = new ZTableExistenceService();
		parser.tblinfoL = buildTblList(hldupdate.cres.dval); 
		QueryDetails details = new QueryDetails();
		QuerySpec spec = hldupdate.querySpec;
		UpdateStatementFragment selectFrag = parser.parseUpdate(spec, details, hldupdate.cres.dval, hldupdate.cres.assocCrudMap);
		SqlStatementGroup stgroup = parser.renderUpdateGroup(selectFrag);
		
		return stgroup;
	}
	


	private List<TableInfo> buildTblList(DValue dval) {
		List<TableInfo> tblinfoL = new ArrayList<>();
		DStructType structType = (DStructType) dval.getType();
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo != null && relinfo.isManyToMany()) {
					DatInfo datinfo = datIdMap.getAssocTblInfo(relinfo.getDatId());
					String assocTbl = datinfo.tableName; //datIdMap.getAssocTblName(relinfo.getDatId());
					TableInfo info = new TableInfo(structType.getName(), assocTbl);
					if (datinfo.matchesLeft(relinfo)) {
						info.tbl1 = StringUtils.substringBefore(datinfo.left, ".");
						info.tbl2 = StringUtils.substringBefore(datinfo.right, ".");
					} else {
						info.tbl1 = StringUtils.substringBefore(datinfo.right, ".");
						info.tbl2 = StringUtils.substringBefore(datinfo.left, ".");
					}
					tblinfoL.add(info);
				}
			}
		}
		return tblinfoL;
	}
}
