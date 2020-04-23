package org.delia.db.postgres;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.QuerySpec;
import org.delia.db.TableExistenceService;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.PreparedStatementGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;

public class PostgresPreparedStatementGenerator extends PreparedStatementGenerator {

	public PostgresPreparedStatementGenerator(FactoryService factorySvc, DTypeRegistry registry, PostgresSqlHelperFactory sqlHelperFactory, 
			VarEvaluator varEvaluator, TableExistenceService existSvc) {
		super(factorySvc, registry, sqlHelperFactory, varEvaluator, existSvc);
	}

	public SqlStatement generateQuery(QuerySpec spec) {
		StrCreator sc = new StrCreator();
		QueryExp exp = spec.queryExp;
		String typeName = exp.getTypeName();
		//TODO: for now we implement exist using count(*). improve later
		if (selectFnHelper.isCountPresent(spec) || selectFnHelper.isExistsPresent(spec)) {
			sc.o("SELECT COUNT(*) FROM %s", typeName);
		} else if (selectFnHelper.isMinPresent(spec)) {
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "min");
			sc.o("SELECT MIN(%s) FROM %s", fieldName, typeName);
		} else if (selectFnHelper.isMaxPresent(spec)) {
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "max");
			sc.o("SELECT MAX(%s) FROM %s", fieldName, typeName);
		} else if (selectFnHelper.isFirstPresent(spec)) {
			spec = doSelectFirst(sc, spec, typeName);
		} else if (selectFnHelper.isLastPresent(spec)) {
			spec = doSelectLast(sc, spec, typeName);
		} else {
			sc.o("SELECT * FROM %s", typeName);
		}
		SqlStatement statement = new SqlStatement();

		statement = pwheregen.generateAWhere(spec);
		sc.o(statement.sql);
		
		generateQueryFns(sc, spec, typeName);
		
		sc.o(";");
		statement.sql = sc.str;
		return statement;
	}
	
	/**
	 * must copy queryspec since we modify it.
	 * @return adjusted query spec
	 * @param sc output writer
	 * @param spec query spec
	 * @param typeName type
	 */
	protected QuerySpec doSelectFirst(StrCreator sc, QuerySpec spec, String typeName) {
		sc.o("SELECT * FROM %s", typeName);
		return selectFnHelper.doFirstFixup(spec, typeName, null);
	}

	
	/**
	 * must copy queryspec since we modify it.
	 * @return adjusted query spec
	 * @param sc output writer
	 * @param spec query spec
	 * @param typeName type
	 * @return adjusted query spec
	 */
	protected QuerySpec doSelectLast(StrCreator sc, QuerySpec spec, String typeName) {
		sc.o("SELECT * FROM %s", typeName);
		if (selectFnHelper.isOrderByPresent(spec)) {
			return spec;
		}

		return selectFnHelper.doLastFixup(spec, typeName, null);
	}


}