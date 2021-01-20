package org.delia.db.sqlgen;

import java.util.List;

import org.delia.db.newhls.HLDField;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;
import org.delia.type.DValue;

public class SqlFieldListClause implements SqlClauseGenerator {

	private List<HLDField> fieldL;
	
	public void init(List<HLDField> fieldL) {
		this.fieldL = fieldL;
	}
	@Override
	public String render(SqlStatement stm) {
		return null;
//		StrCreator sc = new StrCreator();
//		sc.o(" VALUES(");
//		int index = 0;
//		ListWalker<HLDField> walker = new ListWalker<>(fieldL);
//		while(walker.hasNext()) {
//			HLDField ff = walker.next();
//			DValue inner = valueL.get(index);
////			stm.paramL.add(renderValue(inner));
//			
//			sc.o("?");
//			walker.addIfNotLast(sc, ", ");
//			index++;
//		}
//		sc.o(")");
	}

}
