package org.delia.db.sql.table;

import org.delia.core.FactoryService;
import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;

public class PKConstraintGen extends ConstraintGen {

	public PKConstraintGen(FactoryService factorySvc, DTypeRegistry registry, TypePair pair, DStructType dtype, boolean isAlter) {
		super(factorySvc, registry, pair, dtype, isAlter);
	}
	
	public void generateField(StrCreator sc) {
		//CONSTRAINT bill_product_pkey PRIMARY KEY (bill_id, product_id)
		//FOREIGN KEY (PersonID) REFERENCES Persons(PersonID)
		String assocTblName = pair.name;
		
		String s = String.format("CONSTRAINT %s_leftv_rightv PRIMARY KEY (leftv,rightv)", assocTblName);
		sc.o(s);
		sc.nl();
	}
}