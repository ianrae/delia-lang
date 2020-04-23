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

	public FieldGen(FactoryService factorySvc, DTypeRegistry registry, TypePair pair, DStructType dtype, boolean isAlter) {
		super(factorySvc, registry, pair, dtype, isAlter);
	}
	
	public void setIsAssocTblField(boolean isOptional) {
		this.isAssocTblField = true;
		this.isAssocTblFieldOptional = isOptional;
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
			suffix1 = " UNIQUE";
		}
		String suffix2 = dtype.fieldIsOptional(name) ? " NULL" : "";
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
			return "Int";
		case LONG:
			return "BIGINT";
		case NUMBER:
			return "DOUBLE";
		case DATE:
			return "TIMESTAMP";
		case STRING:
			return "VARCHAR(4096)"; //TODO: should be this bigger? or configurable?
		case BOOLEAN:
			return "BOOLEAN";
		case STRUCT:
		{
			TypePair innerPair = DValueHelper.findPrimaryKeyFieldPair(pair.type); //TODO: support multiple keys later
			return deliaToSql(innerPair);
		}
		default:
			return null;
		}
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