package org.delia.db.sql.table;

import org.delia.core.FactoryService;
import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class ConstraintGen extends SqlElement {

	public ConstraintGen(FactoryService factorySvc, DTypeRegistry registry, TypePair pair, DStructType dtype) {
		super(factorySvc, registry, pair, dtype);
	}
	
	public void generateField(StrCreator sc) {
		//FOREIGN KEY (PersonID) REFERENCES Persons(PersonID)
		String fieldName = pair.name;

		DStructType targetType = (DStructType) pair.type;
		TypePair keyField =  DValueHelper.findPrimaryKeyFieldPair(targetType);
		String s = String.format("FOREIGN KEY (%s) REFERENCES %s(%s)", fieldName, targetType.getName(), keyField.name);
		sc.o(s);
		sc.nl();
	}
}