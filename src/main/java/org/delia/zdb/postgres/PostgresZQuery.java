//package org.delia.zdb.postgres;
//
//import org.delia.assoc.DatIdMap;
//import org.delia.core.FactoryService;
//import org.delia.db.postgres.PostgresWhereFragmentGenerator;
//import org.delia.db.sql.fragment.WhereFragmentGenerator;
//import org.delia.runner.VarEvaluator;
//import org.delia.type.DTypeRegistry;
//import org.delia.zdb.ZQuery;
//
//public class PostgresZQuery extends ZQuery {
//
//	public PostgresZQuery(FactoryService factorySvc, DTypeRegistry registry) {
//		super(factorySvc, registry);
//	}
//
//	@Override
//	protected WhereFragmentGenerator createWhereFragmentGenerator(VarEvaluator varEvaluator, DatIdMap datIdMap) {
//		return new PostgresWhereFragmentGenerator(factorySvc, registry, varEvaluator, datIdMap);
//	}
//
//}
