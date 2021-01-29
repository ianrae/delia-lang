package org.delia.db.postgres;

import java.util.List;

import org.delia.db.SqlStatement;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;
import org.delia.db.sqlgen.SqlMergeIntoStatement;
import org.delia.db.sqlgen.SqlTableNameClause;
import org.delia.db.sqlgen.SqlValueListClause;
import org.delia.hld.HLDField;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

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
		HLDField field = hld.fieldL.get(0);
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(field.structType);
		String fld1 = calcIfFieldIsPKOrRelationField(field, pkpair);

		field = hld.fieldL.get(1);
		String fld2 = calcIfFieldIsPKOrRelationField(field, pkpair);

		if (fld1 == null && fld2 != null) {
			sc.o("(%s)", fld2);
		} else if (fld1 != null && fld2 == null) {
			sc.o("(%s)", fld1);
		} else {
			sc.o("(%s,%s)", fld1, fld2);
		}
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

	private String calcIfFieldIsPKOrRelationField(HLDField field, TypePair pkpair) {
		if (pkpair.name.equals(field.fieldName)) {
			return field.fieldName;
		} else if (field.fieldType.isStructShape()) {
			return field.fieldName;
		}
		return null;
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
