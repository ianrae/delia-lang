package org.delia.db.postgres;

import java.util.List;
import java.util.stream.Collectors;

import org.delia.db.SqlStatement;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;
import org.delia.db.sqlgen.SqlMergeIntoStatement;
import org.delia.db.sqlgen.SqlTableNameClause;
import org.delia.db.sqlgen.SqlValueListClause;
import org.delia.hld.HLDField;
import org.delia.type.DValue;

public class PostgresSqlMergeIntoStatement extends SqlMergeIntoStatement {

	public PostgresSqlMergeIntoStatement(SqlTableNameClause tblClause, SqlValueListClause valueClause) {
		super(tblClause, valueClause);
	}
	
	@Override
	public SqlStatement render() {
		// insert into dat (rightv) on conflict (leftv,rightv) do update set rightv=?

		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("INSERT INTO");
		sc.o(tblClause.render(stm));
		
//		DValue first = valueClause.getValueL().remove(0);
//		valueClause.getValueL().clear();
//		valueClause.getValueL().add(first);
		sc.o(valueClause.render(stm));

		sc.o(" ON CONFLICT");
		String fld1 = hld.fieldL.get(0).fieldName;
		String fld2 = hld.fieldL.get(1).fieldName;
		sc.o("(%s,%s)", fld1, fld2);
		sc.o(" DO UPDATE");
		sc.o(" SET ");
		
		int targetIndex = findTarget(hld.fieldL, hld.mergeKey);
		ListWalker<HLDField> walker = new ListWalker<>(hld.fieldL);
		String conditionStr = null;
		int index = 0;
		while(walker.hasNext()) {
			HLDField ff = walker.next();
			if (index == targetIndex){
				DValue inner = hld.valueL.get(index);
				stm.paramL.add(valueClause.renderValue(inner));
				
				conditionStr = String.format("%s = %s", ff.render(), "?");
				sc.o(conditionStr);
				//walker.addIfNotLast(sc, ", ");
			}
			index++;
		}

		
		stm.sql = sc.toString();
		return stm;
	}

	private int findTarget(List<HLDField> fieldL, String mergeKey) {
		for(int i = 0; i < fieldL.size(); i++) {
			HLDField fld = fieldL.get(i);
			if (!fld.fieldName.equals(mergeKey)) {
				return i;
			}
		}
		return -1;
	}

}
