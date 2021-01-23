//package org.delia.db.sql.fragment;
//
//import org.delia.core.FactoryService;
//import org.delia.db.QueryDetails;
//import org.delia.db.QuerySpec;
//import org.delia.type.DStructType;
//
//public class DeleteFragmentParser extends FragmentParserBase {
//
//		public DeleteFragmentParser(FactoryService factorySvc, FragmentParserService fpSvc) {
//			super(factorySvc, fpSvc);
//		}
//		
//		public DeleteStatementFragment parseDelete(QuerySpec spec, QueryDetails details) {
//			DeleteStatementFragment selectFrag = new DeleteStatementFragment();
//			
//			//init tbl
//			DStructType structType = getMainType(spec); 
//			TableFragment tblFrag = createTable(structType, selectFrag);
//			selectFrag.tblFrag = tblFrag;
//			
//			initWhere(spec, structType, selectFrag);
////			addJoins(spec, structType, selectFrag, details);
//
//			generateDeleteFns(spec, structType, selectFrag);
//			
//			fixupForParentFields(structType, selectFrag);
//			if (needJoinBase(spec, structType, selectFrag, details)) {
//				//used saved join if we have one
//				if (savedJoinedFrag == null) {
//					addJoins(spec, structType, selectFrag, details);
//				} else {
//					selectFrag.joinFrag = savedJoinedFrag;
//				}
//			}
//
//			if (selectFrag.fieldL.isEmpty()) {
//				FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
//				selectFrag.fieldL.add(fieldF);
//			}
//			
//			
//			return selectFrag;
//		}
//		
//		public void generateDeleteFns(QuerySpec spec, DStructType structType, DeleteStatementFragment selectFrag) {
//			this.doLimitIfPresent(spec, structType, selectFrag);
//		}
//		
//		public String renderDelete(DeleteStatementFragment selectFrag) {
//			selectFrag.statement.sql = selectFrag.render();
//			return selectFrag.statement.sql;
//		}
//		
//	}