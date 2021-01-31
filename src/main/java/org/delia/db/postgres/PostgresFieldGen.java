package org.delia.db.postgres;

import org.delia.core.FactoryService;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.FieldGen;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class PostgresFieldGen extends FieldGen {

	public PostgresFieldGen(FactoryService factorySvc, DTypeRegistry registry, TypePair pair, DStructType dtype, 
			boolean isAlter, int sizeof) {
		super(factorySvc, registry, pair, dtype, isAlter, sizeof);
	}
	
	public void generateField(StrCreator sc) {
		if (isAssocTblField) {
			generateAssocField(sc);
			return;
		}
		
		String name = pair.name;
		String type = deliaToSql(pair);
		//	Department		Char(35)		NOT NULL,
		boolean b = dtype.fieldIsUnique(name) || dtype.fieldIsPrimaryKey(name);
		String suffix1 =  "";
		String suffix1a = "";
		if (dtype.fieldIsSerial(name)) {
			suffix1a = " PRIMARY KEY GENERATED ALWAYS AS IDENTITY"; //only works for ints. need different syntax for string sequence
		} else if (b || makeFieldUnique) {
			suffix1 = " UNIQUE";
		}
		String suffix2 = dtype.fieldIsOptional(name) ? " NULL" : "";
		sc.o("  %s %s%s%s", name, type, suffix1, suffix1a, suffix2);
	}
	
	public String deliaToSql(TypePair pair) {
		switch(pair.type.getShape()) {
		case INTEGER:
			return calcIntColumnType();
		case LONG:
			return "BIGINT";
		case NUMBER:
			return "DOUBLE PRECISION";
		case DATE:
			return "TIMESTAMP";
		case BLOB:
			return "bytea";
		case STRING:
		{
			int n = sizeof == 0 ? 65536 : sizeof; 
			return String.format("VARCHAR(%d)", n); 
		}
		case BOOLEAN:
			return "BOOLEAN";
		case STRUCT:
		{
			TypePair innerPair = DValueHelper.findPrimaryKeyFieldPair(pair.type); 
			return deliaToSql(innerPair);
		}
		default:
			return null;
		}
	}
	
	private String calcIntColumnType() {
		String type;
		switch(sizeof) {
		case 8:
		case 16:
			type = "SMALLINT";
			break;
		case 32:
			type = "INTEGER";
			break;
		case 64:
			type = "BIGINT";
			break;
		default:
			type = "INT";
			break;
		}
		return type;
	}

}
