//package org.delia.zdb;
//
//import java.util.List;
//
//import org.delia.assoc.DatIdMap;
//import org.delia.core.FactoryService;
//import org.delia.core.ServiceBase;
//import org.delia.db.DBAccessContext;
//import org.delia.db.QueryContext;
//import org.delia.db.QueryDetails;
//import org.delia.db.QuerySpec;
//import org.delia.db.h2.H2SqlHelperFactory;
//import org.delia.db.sql.fragment.FragmentParserService;
//import org.delia.db.sql.fragment.SelectFragmentParser;
//import org.delia.db.sql.fragment.SelectStatementFragment;
//import org.delia.db.sql.fragment.WhereFragmentGenerator;
//import org.delia.db.sql.prepared.SqlStatement;
//import org.delia.queryresponse.LetSpan;
//import org.delia.runner.DoNothingVarEvaluator;
//import org.delia.runner.VarEvaluator;
//import org.delia.type.DTypeRegistry;
//import org.delia.util.DeliaExceptionHelper;
//
//public class ZQuery extends ServiceBase {
//	
//	protected DTypeRegistry registry;
//	protected H2SqlHelperFactory sqlHelperFactory;
//
//	public ZQuery(FactoryService factorySvc, DTypeRegistry registry) {
//		super(factorySvc);
//		this.registry = registry;
//		this.sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
//	}
//
//	public SqlStatement generate(QuerySpec spec, QueryContext qtx, ZTableCreator tableCreator, List<LetSpan> spanL, 
//			QueryDetails details, VarEvaluator varEvaluator, ZDBExecutor zexec) {
//		buildSpans(spec, qtx, spanL);
//		failIfMultiSpan(spec, qtx, spanL);
//		SqlStatement statement;
//		
//		DBAccessContext dbctx = new DBAccessContext(registry, new DoNothingVarEvaluator());
//		WhereFragmentGenerator whereGen = createWhereFragmentGenerator(varEvaluator, zexec.getDatIdMap());
//		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, new DoNothingVarEvaluator(), tableCreator.alreadyCreatedL, dbctx, sqlHelperFactory, whereGen, spanL);
//		SelectFragmentParser parser = new SelectFragmentParser(factorySvc, fpSvc);
//		whereGen.tableFragmentMaker = parser;
//		SelectStatementFragment selectFrag = parser.parseSelect(spec, details);
//		parser.renderSelect(selectFrag);
//		statement = selectFrag.statement;
//		
//		return statement;
//	}
//	
//	protected void failIfMultiSpan(QuerySpec spec, QueryContext qtx, List<LetSpan> spanL) {
//		if (qtx.letSpanEngine == null) return;
//		
//		if (spanL.size() > 1) {
//			String msg = "Query of '%s' contains %d spans. Only one span supported in current version";
//			DeliaExceptionHelper.throwError("db-multiple-spans-not-supported", msg, spec.queryExp.typeName, spanL.size());
//		}
//	}
//	protected void buildSpans(QuerySpec spec, QueryContext qtx, List<LetSpan> spanLParam) {
//		if (qtx.letSpanEngine == null) return;
//		
//		List<LetSpan> spanL = qtx.letSpanEngine.buildAllSpans(spec.queryExp);
//		spanLParam.addAll(spanL);
//	}
//	
//	protected WhereFragmentGenerator createWhereFragmentGenerator(VarEvaluator varEvaluator, DatIdMap datIdMap) {
//		return new WhereFragmentGenerator(factorySvc, registry, varEvaluator, datIdMap);
//	}
//	
//}
