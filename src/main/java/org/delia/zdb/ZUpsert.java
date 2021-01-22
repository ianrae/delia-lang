package org.delia.zdb;

import java.util.Map;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.hld.HLDSimpleQueryService;
import org.delia.db.sql.fragment.AssocTableReplacer;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.UpsertFragmentParser;
import org.delia.db.sql.fragment.UpsertStatementFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class ZUpsert extends ServiceBase {
	
	protected DTypeRegistry registry;
	protected H2SqlHelperFactory sqlHelperFactory;
	private ZDBInterfaceFactory dbInterface;

	public ZUpsert(FactoryService factorySvc, DTypeRegistry registry, ZDBInterfaceFactory dbInterface) {
		super(factorySvc);
		this.registry = registry;
		this.sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
		this.dbInterface = dbInterface;
	}
	
	
	public SqlStatementGroup generate(QuerySpec spec, DValue dval, Map<String, String> assocCrudMap, boolean noUpdateFlag,
			VarEvaluator varEvaluator, ZTableCreator tableCreator, ZDBExecutor zexec) {
		SqlStatementGroup stgroup;
		
		WhereFragmentGenerator whereGen = createWhereFragmentGenerator(varEvaluator, zexec.getDatIdMap());
		DBAccessContext dbctx = new DBAccessContext(registry, varEvaluator);
		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, 
				new DoNothingVarEvaluator(), tableCreator.alreadyCreatedL, dbctx, sqlHelperFactory, whereGen, null);
		ZTableExistenceService existSvc = new ZTableExistenceService();
		fpSvc.setExistSvc(existSvc);
		
		AssocTableReplacer assocTblReplacer = createAssocTableReplacer(fpSvc);
//		UpdateFragmentParser parser = new UpdateFragmentParser(factorySvc, fpSvc, assocTblReplacer);
//		whereGen.tableFragmentMaker = parser;
//		UpdateStatementFragment selectFrag = parser.parseUpdate(spec, details, dvalPartial, assocCrudMap);
//		stgroup = parser.renderUpdateGroup(selectFrag);
		
		/////
		
		UpsertFragmentParser parser = createUpsertFragmentParser(fpSvc, assocTblReplacer);

		//hack hack hack TODO:improve this
		//this works but is slow, and has race conditions if other thread does insert
		//between the time we call executeQuery and do the update.

		if (noUpdateFlag) {
			QueryBuilderService queryBuilder = factorySvc.getQueryBuilderService();
			HLDSimpleQueryService querySvc = factorySvc.createHLDSimpleQueryService(dbInterface, registry);
			DValue keyVal = parser.getPrimaryKeyValue(spec, dval);
			QueryExp queryExp = queryBuilder.createPrimaryKeyQuery(spec.queryExp.typeName, keyVal);
//			QuerySpec query = queryBuilder.buildSpec(queryExp, new DoNothingVarEvaluator());
//			QueryContext qtx = new QueryContext();
//			QueryResponse qresp = zexec.rawQuery(query, qtx);
			QueryResponse qresp = querySvc.execQuery(queryExp, zexec);
			if (!qresp.emptyResults()) {
				return null;
			}
		}

		whereGen.tableFragmentMaker = parser;
		QueryDetails details = new QueryDetails();
		UpsertStatementFragment selectFrag = parser.parseUpsert(spec, details, dval, assocCrudMap, noUpdateFlag);
		stgroup = parser.renderUpsertGroup(selectFrag);
		
		return stgroup;
	}	
	
	protected AssocTableReplacer createAssocTableReplacer(FragmentParserService fpSvc) {
		return new AssocTableReplacer(factorySvc, fpSvc);
	}

	protected WhereFragmentGenerator createWhereFragmentGenerator(VarEvaluator varEvaluator, DatIdMap datIdMap) {
		return new WhereFragmentGenerator(factorySvc, registry, varEvaluator, datIdMap);
	}
	
	protected UpsertFragmentParser createUpsertFragmentParser(FragmentParserService fpSvc, AssocTableReplacer assocTblReplacer) {
		return new UpsertFragmentParser(factorySvc, fpSvc, assocTblReplacer);
	}

}
