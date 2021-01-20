package org.delia.db.sqlgen;

import java.util.List;

import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;
import org.delia.type.DRelation;
import org.delia.type.DValue;

public class SqlValueListClause implements SqlClauseGenerator {

	private List<DValue> valueL;
	
	public void init(List<DValue> valueL) {
		this.valueL = valueL;
	}
	
	@Override
	public String render(SqlStatement stm) {
		StrCreator sc = new StrCreator();
		sc.o(" VALUES(");
		int index = 0;
		ListWalker<DValue> walker = new ListWalker<>(valueL);
		while(walker.hasNext()) {
			DValue inner = walker.next();
			stm.paramL.add(renderValue(inner));
			
			sc.o("?");
			walker.addIfNotLast(sc, ", ");
			index++;
		}
		sc.o(")");
		return sc.toString();
	}

	private DValue renderValue(DValue inner) {
		if (inner != null && inner.getType().isRelationShape()) {
			DRelation drel = inner.asRelation();
			return drel.getForeignKey(); //better only be one!
		}
		return inner;
	}

}
