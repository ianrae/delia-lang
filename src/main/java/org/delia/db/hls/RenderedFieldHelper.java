package org.delia.db.hls;

import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.delia.db.sql.StrCreator;
import org.delia.log.Log;
import org.delia.type.DStructType;

public class RenderedFieldHelper {

	
	public static void logRenderedFieldList(HLSQueryStatement hls, Log log) {
		List<RenderedField> list = hls.getRenderedFields();
		StrCreator scx = new StrCreator();
		scx.o(String.format("RF[%d]", list.size()));
		
		StringJoiner joiner = new StringJoiner(",");
		for(RenderedField rf: list) {
			StrCreator sc = new StrCreator();
			if (rf.isAssocField) {
				sc.o("*");
			}
			sc.o(rf.field);
			if (rf.structType != null) {
				sc.o("(%s", rf.structType.getName());
			}
			if (rf.pair != null) {
				sc.o(".%s)", rf.pair.name);
			}
			
			joiner.add(sc.toString());
		}
		scx.o(joiner.toString());
		log.log(scx.toString());
	}
	
	public static String getAssocFieldName(RenderedField rff) {
		if (rff.structType != null) {
			return rff.field;
		} else {
			//alias
			String fieldName = StringUtils.substringAfter(rff.field, " as ");
			return fieldName;
		}
	}

}
