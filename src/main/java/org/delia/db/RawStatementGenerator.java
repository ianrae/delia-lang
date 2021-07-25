package org.delia.db;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.StrCreator;
import org.delia.zdb.DBListingType;

public class RawStatementGenerator extends ServiceBase {
	private SqlNameFormatter nameFormatter;
	private String contraintsTbl;

	public RawStatementGenerator(FactoryService factorySvc, DBType dbType) {
		super(factorySvc);
		
		switch(dbType) {
		case H2:
			nameFormatter = new SimpleSqlNameFormatter(null);
			contraintsTbl = "information_schema.constraints";
			break;
		case POSTGRES:
			nameFormatter = new SimpleSqlNameFormatter(null, true);
			contraintsTbl = "information_schema.table_constraints";
			break;
		default:
			break;
		}
		
	}

	private String tblName(String typeName) {
		return nameFormatter.convert(typeName);
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
		return sc.toString();
	}
	
	public String generateSchemaListing(DBListingType listingType) {
		StrCreator sc = new StrCreator();
		switch(listingType) {
		case ALL_TABLES:
		{
			sc.o("SELECT * FROM information_schema.tables"); 
			boolean b = true;
			if (b) {
				sc.o(" WHERE  table_schema = '%s'", tblName("PUBLIC"));
			} else {
			}
		}
			break;
		case ALL_CONSTRAINTS:
		{
			sc.o("SELECT * FROM %s", contraintsTbl); 
			boolean b = true;
			if (b) {
				sc.o(" WHERE  table_schema = '%s'", tblName("PUBLIC"));
			} else {
			}
		}
			break;
		}
		return sc.toString();
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
		return sc.toString();
	}

}