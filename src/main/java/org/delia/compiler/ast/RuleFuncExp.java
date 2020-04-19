package org.delia.compiler.ast;
//package org.dang.compiler.ast;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.StringJoiner;
//
//import org.apache.commons.collections.CollectionUtils;
//
////RuleFuncExp
//	public class RuleFuncExp extends ExpBase {
//		public List<QueryFuncExp> qfeL = new ArrayList<>();
//		public boolean polarity;
//		
//		public RuleFuncExp(int pos, boolean polarity, List<List<QueryFuncExp>> list1) {
//			super(pos);
//			this.polarity = polarity;
//			if (list1 != null) {
//				List<QueryFuncExp> list = new ArrayList<>();
//				if (! list1.isEmpty()) {
//					for(List<QueryFuncExp> sublist : list1) {
//						for(QueryFuncExp inner: sublist) {
//							list.add(inner);
//						}
//					}
//				}
//				qfeL = list;
//			}
//		}
//		
//		@Override
//		public String strValue() {
//			StringJoiner sj = new StringJoiner(".");
//			
//			if (CollectionUtils.isNotEmpty(qfeL)) {
//				for(QueryFuncExp qfe: qfeL) {
////					String tmp = qfe.toString();
//					String tmp = String.format("%s", qfe.funcName);
//					if (qfe.isRuleFn) {
//						tmp += "()"; //TODO: add args later
//					}
//					sj.add(tmp);
//				}
//			}
//			
//			return sj.toString();
//		}
//		
//		@Override
//		public String toString() {
//			return strValue();
//		}
//	}