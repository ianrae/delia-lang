package org.delia.compiler.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class RuleSetExp extends ExpBase {
	public List<RuleExp> ruleL = new ArrayList<>();

	public RuleSetExp(int pos, List<List<RuleExp>> list1) {
		super(pos);
		
		if (list1 != null) {
			List<RuleExp> list = new ArrayList<>();
			if (! list1.isEmpty()) {
				for(List<RuleExp> sublist : list1) {
					for(RuleExp inner: sublist) {
						list.add(inner);
					}
				}
			}
			ruleL = list;
		}
	}
	
	public boolean hasRules() {
		return !ruleL.isEmpty();
	}
	
	@Override
	public String strValue() {
		StringJoiner sj = new StringJoiner(",");
		
		if (ruleL != null && ! ruleL.isEmpty()) {
			for(RuleExp ruleExp: ruleL) {
				String tmp = ruleExp.toString();
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