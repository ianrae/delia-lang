package org.delia.zdb.postgres;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.TableExistenceService;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.AssocTableCreator;
import org.delia.db.sql.table.ConstraintGen;
import org.delia.db.sql.table.FieldGen;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.SqlElement;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.TableCreator;

public class PostgresTableCreator extends TableCreator {
	
	public PostgresTableCreator(FactoryService factorySvc, DTypeRegistry registry, FieldGenFactory fieldgenFactory, 
				SqlNameFormatter nameFormatter, DatIdMap datIdMap, DBExecutor zexec) {
		super(factorySvc, registry, fieldgenFactory, nameFormatter, datIdMap, zexec);
	}
	
	@Override
	protected AssocTableCreator createAssocTblCreator(TableExistenceService existSvc) {
		return new PostgresAssocTableCreator(factorySvc, registry, fieldgenFactory, nameFormatter, existSvc, alreadyCreatedL, datIdMap);
	}
	

	@Override
	public String generateRenameField(String tableName, String fieldName, String newName) {
		StrCreator sc = new StrCreator();
		sc.o("ALTER TABLE %s RENAME COLUMN %s", tblName(tableName), fieldName);
		sc.o(" TO %s", newName); 
		return sc.toString();
	}
	
	@Override
	protected void doAlterColumnUnique(StrCreator sc, String tableName, String fieldName, boolean b, String constraintName) {
		doAlterTablePrefix(sc, tableName);
//		String constraintName = String.format("%s_%s_key", tblName(tableName), tblName(fieldName));
		if (b) {
			//public.customer_height_key
			//ALTER TABLE TEST ADD CONSTRAINT NAME_UNIQUE UNIQUE(NAME)
			sc.o(" ADD CONSTRAINT %S UNIQUE(%s)", constraintName, fieldName);  
		} else {
			sc.o(" DROP CONSTRAINT %S", constraintName);  
		}
		sc.o(";\n");
	}

	@Override
	public String generateCreateField(String typeName, DStructType dtype, String fieldName, int sizeof, boolean canCreateAssocTable) {
		if (dtype == null) {
			dtype = (DStructType) registry.getType(typeName);
		}

		StrCreator sc = new StrCreator();
		sc.o("ALTER TABLE %s ADD  ", typeName);
		sc.nl();
		List<SqlElement> fieldL = new ArrayList<>();
		int manyToManyFieldCount = 0;

		TypePair pair = DValueHelper.findField(dtype, fieldName);
		if (isManyToManyRelation(pair, dtype)) {
			manyToManyFieldCount++;
		} else {
			FieldGen field = fieldgenFactory.createFieldGen(registry, pair, dtype, true, sizeof);
			fieldL.add(field);
		}

		//add constraints
		ConstraintGen constraint = null;
		List<ConstraintGen> constraints = new ArrayList<>();
		if (pair.type.isStructShape() && !isManyToManyRelation(pair, dtype)) {
			constraint = generateFKConstraint(sc, pair, dtype, true);
			if (constraint != null) {
				constraints.add(constraint);
			}
		}

		haveFieldsVisitTheirConstrainsts(fieldL, constraints);

		int index = 0;
		for(SqlElement ff: fieldL) {
			ff.generateField(sc);
			if (index + 1 < fieldL.size()) {
				sc.o(",");
				sc.nl();
			}
			index++;
		}

		if (constraint != null) {
			sc.o(";");
			sc.nl();
			//ALTER TABLE distributors ADD CONSTRAINT distfk FOREIGN KEY (address) REFERENCES addresses (address) MATCH FULL;
			sc.o("ALTER TABLE %s ADD CONSTRAINT %s ", typeName, fieldName);
			constraint.generateField(sc);
			sc.o(";");
		}

		sc.nl();
		if (manyToManyFieldCount > 0) {
			if (fieldL.isEmpty()) {
				sc = new StrCreator(); //reset
			} else {
				sc.nl();
			}
			sc.nl();

			if (isManyToManyRelation(pair, dtype)) {
				alterGenerateAssocTable(sc, pair, dtype, canCreateAssocTable);
			}
		}
		return sc.toString();
	}

	
	
}