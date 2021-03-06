package org.delia.zdb.postgres;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.TableExistenceService;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.table.AssocTableCreator;
import org.delia.db.sql.table.ConstraintGen;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.PKConstraintGen;
import org.delia.db.sql.table.TableInfo;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;

public class PostgresAssocTableCreator extends AssocTableCreator {
	
	public PostgresAssocTableCreator(FactoryService factorySvc, DTypeRegistry registry, FieldGenFactory fieldgenFactory, 
				SqlNameFormatter nameFormatter, TableExistenceService existSvc, List<TableInfo> alreadyCreatedL, DatIdMap datIdMap) {
		super(factorySvc, registry, fieldgenFactory, nameFormatter, existSvc, alreadyCreatedL, datIdMap);
	}
	

	@Override
	protected ConstraintGen addAdditionalPKConstraint(String string, String string2, DStructType leftType, DStructType rightType, String assocTableName) {
		TypePair pair = new TypePair(assocTableName, leftType); //hacky
		return new PKConstraintGen(factorySvc, registry, pair, null, false);
	}


}