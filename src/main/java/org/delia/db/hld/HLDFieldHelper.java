package org.delia.db.hld;

import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.delia.db.newhls.HLDField;
import org.delia.db.newhls.HLDQueryStatement;
import org.delia.db.sql.StrCreator;
import org.delia.log.Log;

public class HLDFieldHelper {

	
	public static void logRenderedFieldList(HLDQueryStatement hld, Log log) {
		List<HLDField> list = hld.hldquery.fieldL;
		StrCreator scx = new StrCreator();
		scx.o(String.format("RF[%d]", list.size()));
		
		StringJoiner joiner = new StringJoiner(",");
		for(HLDField rf: list) {
			StrCreator sc = new StrCreator();
//			if (rf.isAssocField) {
//				sc.o("*");
//			}
			sc.o(rf.fieldName);
			if (rf.structType != null) {
				sc.o("(%s", rf.structType.getName());
			}
//			if (rf.pair != null) {
//				sc.o(".%s)", rf.pair.name);
//			}
			
			joiner.add(sc.toString());
		}
		scx.o(joiner.toString());
		log.log(scx.toString());
	}
	
	public static String getAssocFieldName(HLDField rff) {
//		if (rff.structType != null) {
//			return rff.pair.name;
//		} else {
//			//alias
			String fieldName = StringUtils.substringAfter(rff.fieldName, " as ");
			return fieldName;
//		}
	}

}
