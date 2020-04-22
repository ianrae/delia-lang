package org.delia.db.sql.prepared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.QuerySpec;
import org.delia.db.TableExistenceService;
import org.delia.db.h2.DBListingType;
import org.delia.db.h2.SqlHelperFactory;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.TableInfo;
import org.delia.runner.VarEvaluator;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class PreparedStatementGenerator extends ServiceBase {
	protected DTypeRegistry registry;
	protected SqlNameFormatter nameFormatter;
	protected WhereClauseGenerator pwheregen;
	protected SelectFuncHelper selectFnHelper;
	protected SqlHelperFactory sqlHelperFactory;
	private VarEvaluator varEvaluator;
	private TableExistenceService existSvc;

	public PreparedStatementGenerator(FactoryService factorySvc, DTypeRegistry registry, SqlHelperFactory sqlHelperFactory, 
				VarEvaluator varEvaluator, TableExistenceService existSvc) {
		super(factorySvc);
		this.registry = registry;
		this.sqlHelperFactory = sqlHelperFactory;
		this.varEvaluator = varEvaluator;
		this.existSvc = existSvc;
		
		DBAccessContext dbctx = new DBAccessContext(registry, varEvaluator);
		this.nameFormatter = sqlHelperFactory.createNameFormatter(dbctx);
		this.pwheregen = sqlHelperFactory.createPWhereGen(dbctx);
		this.selectFnHelper = sqlHelperFactory.createSelectFuncHelper(dbctx);
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
			sc.o("SELECT TOP 1 * FROM %s", typeName);
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
	 * @param sc output writer
	 * @param spec query
	 * @param typeName type being queried
	 * @return adjusted query spec
	 */
	protected QuerySpec doSelectLast(StrCreator sc, QuerySpec spec, String typeName) {
		sc.o("SELECT TOP 1 * FROM %s", typeName);
		if (selectFnHelper.isOrderByPresent(spec)) {
			return spec;
		}

		return selectFnHelper.doLastFixup(spec, typeName);
	}

	protected void generateQueryFns(StrCreator sc, QuerySpec spec, String typeName) {
		this.selectFnHelper.doOrderByIfPresent(sc, spec, typeName);
		this.selectFnHelper.doLimitIfPresent(sc, spec, typeName);
		this.selectFnHelper.doOffsetIfPresent(sc, spec, typeName);
	}

	public SqlStatement generateDelete(QuerySpec spec) {
		StrCreator sc = new StrCreator();
		sc.o("DELETE FROM %s", tblName(spec.queryExp.getTypeName()));
		SqlStatement statement = pwheregen.generateAWhere(spec);
		sc.o(statement.sql);
		sc.o(";");
		
		statement.sql = sc.str;
		return statement;
	}
	protected String tblName(String typeName) {
		return nameFormatter.convert(typeName);
	}
	
	//--------------
	public SqlStatement generateUpdate(DValue dval, List<TableInfo> tblInfoL, QuerySpec spec) {
		Map<String,DRelation> map = new HashMap<>(); //ok to not use ConcurrentHashMap here
		SqlStatement statement = new SqlStatement();
		String sql = doGenerateUpdate(dval, map, statement);
		if (sql.isEmpty()) {
			statement.sql = "";
			return statement;
		}
//		sql += doGenerateAssocInsertIfNeeded(dval, tblInfoL, map);
		
		SqlStatement st2 = generateQuery(spec);
		statement.paramL.addAll(st2.paramL);
		int pos = st2.sql.indexOf("WHERE ");
		if (pos > 0) {
			String query = st2.sql.substring(pos);
			statement.sql = String.format("%s %s", sql, query);
		} else {
			statement.sql = sql;
		}
		return statement;
	}		
	protected String doGenerateUpdate(DValue dval, Map<String, DRelation> map, SqlStatement statement) {
//		UPDATE table_name
//		SET column1 = value1, column2 = value2, ...
//		WHERE condition;		
		
		DStructType dtype = (DStructType) dval.getType();
		StrCreator sc = new StrCreator();
		sc.o("UPDATE %s SET ", tblName(dtype));
		
		InsertStatementGenerator insgen = new InsertStatementGenerator(factorySvc, registry, nameFormatter, existSvc);
//		InsertStatementGenerator insgen = sqlHelperFactory.createPrepInsertSqlGen(dbctx, existSvc);
		String s = insgen.generateUpdateBody(sc, dval, map, statement);
		return s;
	}
	protected String tblName(DType dtype) {
		return nameFormatter.convert(dtype);
	}

	public String generateTableDetect(String tableName) {
		StrCreator sc = new StrCreator();
		sc.o("SELECT EXISTS ( ");
		sc.o(" SELECT FROM information_schema.tables"); 
		boolean b = false;
		if (b) {
			sc.o(" WHERE  table_schema = '%s'", "PUBLIC");
			sc.o(" AND    table_name   = '%s' )", tblName(tableName));
		} else {
//			sc.o(" WHERE  table_schema = '%s'", "PUBLIC");
//			sc.o(" WHERE    table_name   = '%s' )", tableName.toLowerCase());
			sc.o(" WHERE    table_name   = '%s' )", tblName(tableName));
		}
		return sc.str;
	}
	
	public String generateSchemaListing(DBListingType listingType) {
		StrCreator sc = new StrCreator();
		switch(listingType) {
		case ALL_TABLES:
		{
			sc.o("SELECT * FROM information_schema.tables"); 
			boolean b = true;
			if (b) {
				sc.o(" WHERE  table_schema = '%s'", "PUBLIC");
			} else {
			}
		}
			break;
		case ALL_CONSTRAINTS:
		{
			sc.o("SELECT * FROM information_schema.constraints"); 
			boolean b = true;
			if (b) {
				sc.o(" WHERE  table_schema = '%s'", "PUBLIC");
			} else {
			}
		}
			break;
		}
		return sc.str;
	}

	public String generateFieldDetect(String tableName, String fieldName) {
		StrCreator sc = new StrCreator();
		sc.o("SELECT EXISTS ( ");
		sc.o(" SELECT FROM information_schema.columns"); 
		boolean b = false;
		if (b) {
			sc.o(" WHERE  table_schema = '%s'", "PUBLIC");
			sc.o(" AND    table_name   = '%s' ", tblName(tableName));
			sc.o(" AND    column_name   = '%s' )", tblName(fieldName));
		} else {
//			sc.o(" WHERE  table_schema = '%s'", "PUBLIC");
//			sc.o(" WHERE    table_name   = '%s' )", tableName.toLowerCase());
			sc.o(" WHERE    table_name   = '%s' ", tblName(tableName));
			sc.o(" AND    column_name   = '%s' )", tblName(fieldName));
		}
		return sc.str;
	}

}