package org.delia.db.hls;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.StringJoiner;

import org.delia.db.sql.StrCreator;
import org.delia.log.Log;

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
				sc.o("|%s", rf.structType.getName());
			}
			if (rf.pair != null) {
				sc.o(".%s", rf.pair.name);
			}
			
			joiner.add(sc.str);
		}
		scx.o(joiner.toString());
		log.log(scx.str);
	}

}
