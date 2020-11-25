package org.delia.db.hls.join;

import java.util.List;

import org.delia.db.QueryDetails;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.RenderedField;
import org.delia.db.hls.SQLCreator;
import org.delia.type.DStructType;
import org.delia.type.TypePair;

public interface SqlJoinHelper {
	QueryDetails genJoin(SQLCreator sc, HLSQuerySpan hlspan);
	boolean needJoin(HLSQuerySpan hlspan);
	
	boolean supportsAddAllJoins();
	void addAllJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL);

	int addFKofJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL);
	void addFullofJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL);
	void addStructFieldsMM(HLSQuerySpan hlspan, TypePair joinPair, List<RenderedField> fieldL);
	void addStructFields(DStructType fromType, List<RenderedField> fieldL);

	List<TypePair> genTwoStatementJoinList(HLSQuerySpan hlspan1, HLSQuerySpan hlspan2, SQLCreator sc);
}