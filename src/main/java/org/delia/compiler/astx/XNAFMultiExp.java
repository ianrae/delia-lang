package org.delia.compiler.astx;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.ExpBase;

public class XNAFMultiExp extends ExpBase {
	public List<XNAFSingleExp> qfeL = new ArrayList<>();
	public boolean polarity;

	public XNAFMultiExp(int pos, boolean polarity, List<List<XNAFSingleExp>> list1) {
		super(pos);
		this.polarity = polarity;
		if (list1 != null) {
			List<XNAFSingleExp> list = new ArrayList<>();
			if (! list1.isEmpty()) {
				for(List<XNAFSingleExp> sublist : list1) {
					for(XNAFSingleExp inner: sublist) {
						list.add(inner);
					}
				}
			}
			qfeL = list;
		}
	}

	@Override
	public String strValue() {
		StringJoiner sj = new StringJoiner(".");

		if (CollectionUtils.isNotEmpty(qfeL)) {
			for(XNAFSingleExp qfe: qfeL) {
				String tmp = qfe.strValue();
				sj.add(tmp);
			}
		}

		return sj.toString();
	}

	@Override
	public String toString() {
		return strValue();
	}
}