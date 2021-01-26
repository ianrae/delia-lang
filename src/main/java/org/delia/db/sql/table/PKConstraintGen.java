package org.delia.db.sql.table;

import org.delia.core.FactoryService;
import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class PKConstraintGen extends ConstraintGen {

//	public boolean makeFieldUnique = false;

	public PKConstraintGen(FactoryService factorySvc, DTypeRegistry registry, TypePair pair, DStructType dtype, boolean isAlter) {
		super(factorySvc, registry, pair, dtype, isAlter);
		
//		RelationOneRule oneRule = DRuleHelper.findOneRule(dtype.getName(), pair.name, registry);
//		if (oneRule != null && oneRule.relInfo.cardinality.equals(RelationCardinality.ONE_TO_ONE)) {
//			makeFieldUnique = true;
//		}
	}
	
	public void generateField(StrCreator sc) {
		//FOREIGN KEY (PersonID) REFERENCES Persons(PersonID)
		String fieldName = pair.name;
		
		DStructType targetType = (DStructType) pair.type;
		TypePair keyField =  DValueHelper.findPrimaryKeyFieldPair(targetType);
		String s = String.format("xxxxxFOREIGN KEY (%s) REFERENCES %s(%s)", fieldName, targetType.getName(), keyField.name);
		sc.o(s);
		sc.nl();
	}
}