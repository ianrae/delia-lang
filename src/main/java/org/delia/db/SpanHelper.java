package org.delia.db;

import java.util.List;

import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.LetSpan;

public class SpanHelper {

	private List<LetSpan> spanL;

	public SpanHelper(List<LetSpan> spanL) {
		this.spanL = spanL;
	}

	public QueryFuncExp isTargetAField() {
		if (spanL.isEmpty()) {
			return null;
		}
		LetSpan span = spanL.get(0); //always first for now
		QueryFuncExp fieldExp = null;
		int afterCount = 0;
		for(QueryFuncExp qfe: span.qfeL) {
			if (qfe instanceof QueryFieldExp) {
				fieldExp = qfe;
			} else if (fieldExp != null) {
				afterCount++;
			}
		}
		//if any functions after the field then it's not field access (eg. .max())
		return afterCount > 0 ? null : fieldExp;
	}
}
