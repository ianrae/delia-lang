package org.delia.db.sql.table;

import java.util.List;

import org.delia.core.FactoryService;
import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class FieldGen extends SqlElement {

	public boolean makeFieldUnique;
	protected boolean isAssocTblField;
	protected boolean isAssocTblFieldOptional;
	protected int sizeof;

	public FieldGen(FactoryService factorySvc, DTypeRegistry registry, TypePair pair, DStructType dtype, 
			boolean isAlter, int sizeof) {
		super(factorySvc, registry, pair, dtype, isAlter);
		this.sizeof = sizeof;
	}
	
	public void setIsAssocTblField() {
		this.isAssocTblField = true;
		this.isAssocTblFieldOptional = false;
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
			suffix1a = " IDENTITY";
		} else if (b || makeFieldUnique) {
			suffix1 = dtype.fieldIsPrimaryKey(name) ? " PRIMARY KEY" : " UNIQUE";
		}
		String suffix2 = "";
		if (dtype.fieldIsOptional(name)) {
			suffix2 = " NULL";
		} else if (!dtype.fieldIsPrimaryKey(name) && suffix1a.isEmpty()) {
			suffix2 = " NOT NULL";
		}
//		String suffix2 = dtype.fieldIsOptional(name) ? " NULL" : " NOT NULL";
		sc.o("  %s %s%s%s%s", name, type, suffix1, suffix1a, suffix2);
	}

	protected void generateAssocField(StrCreator sc) {
		String name = pair.name;
		String type = deliaToSql(pair);
		//	Department		Char(35)		NOT NULL,
		String suffix2 = this.isAssocTblFieldOptional ? " NULL" : "";
		sc.o("  %s %s%s", name, type, suffix2);
	}

	public String deliaToSql(TypePair pair) {
		switch(pair.type.getShape()) {
		case INTEGER:
			return calcIntColumnType(); 
		case LONG:
			return "BIGINT"; 
		case NUMBER:
			return "DOUBLE";
		case DATE:
			return "TIMESTAMP";
		case BLOB:
			return "BLOB"; //h2
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
			type = "TINYINT";
			break;
		case 16:
			type = "SMALLINT";
			break;
		case 32:
			type = "INT";
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

	public void visitConstraints(List<ConstraintGen> constraints) {
		for(ConstraintGen constraint: constraints) {
			if ( constraint.pair.name.equals(this.pair.name)) {
				if (constraint.makeFieldUnique) {
					this.makeFieldUnique = true;
				}
			}
		}
	}
	
}