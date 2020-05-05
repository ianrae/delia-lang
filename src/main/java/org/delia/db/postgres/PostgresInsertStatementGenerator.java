//package org.delia.db.postgres;
//
//import org.delia.core.FactoryService;
//import org.delia.db.TableExistenceService;
//import org.delia.db.sql.SqlNameFormatter;
//import org.delia.db.sql.prepared.InsertStatementGenerator;
//import org.delia.type.DTypeRegistry;
//
//public class PostgresInsertStatementGenerator extends InsertStatementGenerator {
//
//	public PostgresInsertStatementGenerator(FactoryService factorySvc, DTypeRegistry registry, SqlNameFormatter nameFormatter, TableExistenceService existSvc) {
//		super(factorySvc, registry, nameFormatter, existSvc);
//		specialHandlingForEmptyInsertFlag = true;
//	}
//
//}