package org.delia.db.postgres;

import org.delia.core.FactoryService;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.TableCreator;
import org.delia.type.DTypeRegistry;

public class PostgresTableCreator extends TableCreator {
	
	public PostgresTableCreator(FactoryService factorySvc, DTypeRegistry registry, FieldGenFactory fieldgenFactory, SqlNameFormatter nameFormatter) {
		super(factorySvc, registry, fieldgenFactory, nameFormatter);
	}

	public String generateRenameField(String tableName, String fieldName, String newName) {
		StrCreator sc = new StrCreator();
		sc.o("ALTER TABLE %s RENAME COLUMN %s", tblName(tableName), fieldName);
		sc.o(" TO %s", newName); 
		return sc.str;
	}

	protected void doAlterColumnUnique(StrCreator sc, String tableName, String fieldName, boolean b) {
		doAlterTablePrefix(sc, tableName);
		String constraintName = String.format("%s_%s_key", tblName(tableName), tblName(fieldName));
		if (b) {
			//public.customer_height_key
			//ALTER TABLE TEST ADD CONSTRAINT NAME_UNIQUE UNIQUE(NAME)
			sc.o(" ADD CONSTRAINT %S UNIQUE(%s)", constraintName, fieldName);  
		} else {
			sc.o(" DROP CONSTRAINT %S", constraintName);  
		}
		sc.o(";\n");
	}
	
}