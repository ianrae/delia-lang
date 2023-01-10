package org.delia.sql;

import org.delia.lld.LLD;
import org.delia.log.DeliaLog;
import org.delia.util.StrCreator;

import java.util.List;
import java.util.StringJoiner;

public class HLDFieldHelper {

	public static void logRenderedFieldList(LLD.LLSelect hld, List<LLD.LLField> list, DeliaLog log) {
		StrCreator scx = new StrCreator();
		scx.o(String.format("RF[%d]", list.size()));

		StringJoiner joiner = new StringJoiner(",");
		for(LLD.LLField rf: list) {
			StrCreator sc = new StrCreator();
//			if (rf.isAssocField) {
//				sc.o("*");
//			}
			sc.o(rf.getFieldName());
//            if (rf.structType != null) {
			if (rf.physicalTable.physicalType != null) {
				sc.o("(%s", rf.physicalTable.physicalType.getName());
			}
//			if (rf.pair != null) {
//				sc.o(".%s)", rf.pair.name);
//			}
			sc.o(")");
			joiner.add(sc.toString());
		}
		scx.o(joiner.toString());
		log.log(scx.toString());
	}

	public static String getAssocFieldName(LLD.LLField rff) {
		if (rff.physicalTable.physicalType != null) {
			return rff.getFieldName();
		} else {
			//alias
//			String fieldName = StringUtils.substringAfter(rff.fieldName, " as ");
//			return fieldName;
			return null;
		}
	}

	public static void logRenderedColumnRuns(LLD.LLSelect stmt, List<HLDResultSetConverter.ColumnRun> columnRunL, DeliaLog log) {
		StrCreator scx = new StrCreator();
		for(int i = 0; i < columnRunL.size(); i++) {
			HLDResultSetConverter.ColumnRun columnRun = columnRunL.get(i);
			scx.o(String.format(" Run%d:", i));
			StringJoiner joiner = new StringJoiner(",");
			int k = columnRun.iStart;
			for(LLD.LLField rf: columnRun.runList) {
				StrCreator sc = new StrCreator();
				sc.o(String.format(" %d:", k++));
//			if (rf.isAssocField) {
//				sc.o("*");
//			}
				sc.o(rf.getFieldName());
//            if (rf.structType != null) {
				if (rf.physicalTable.physicalType != null) {
					sc.o("(%s", rf.physicalTable.physicalType.getName());
				}
//			if (rf.pair != null) {
//				sc.o(".%s)", rf.pair.name);
//			}
				sc.o(")");
				joiner.add(sc.toString());
			}
			scx.o(joiner.toString());
		}

		log.log(scx.toString());
	}
}
