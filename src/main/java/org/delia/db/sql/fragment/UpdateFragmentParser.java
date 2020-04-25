package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.db.DBInterface;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.sql.table.TableInfo;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

//single use!!!
	public class UpdateFragmentParser extends SelectFragmentParser {

		private boolean useAliases;

		public UpdateFragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, DBInterface dbInterface, 
				SqlHelperFactory sqlHelperFactory, WhereFragmentGenerator whereGen) {
			super(factorySvc, registry, varEvaluator, tblinfoL, dbInterface, sqlHelperFactory, whereGen);
		}

		public UpdateStatementFragment parseUpdate(QuerySpec spec, QueryDetails details, DValue partialVal) {
			UpdateStatementFragment selectFrag = new UpdateStatementFragment();

			//init tbl
			DStructType structType = getMainType(spec); 
			TableFragment tblFrag = createTable(structType, selectFrag);
			selectFrag.tblFrag = tblFrag;

			generateSetFields(spec, structType, selectFrag, partialVal);
			initFieldsAndWhere(spec, structType, selectFrag);
			
			//no min,max,etc in UPDATE

			generateUpdateFns(spec, structType, selectFrag);

			fixupForParentFields(structType, selectFrag);
			if (needJoin(spec, structType, selectFrag, details)) {
				//used saved join if we have one
				if (savedJoinedFrag == null) {
					addJoins(spec, structType, selectFrag, details);
				} else {
					selectFrag.joinFrag = savedJoinedFrag;
				}
			}
			
			if (! useAliases) {
				removeAllAliases(selectFrag);
			}

			return selectFrag;
		}

		/**
		 * Postgres doesn't like alias in UPDATE statements
		 * @param selectFrag
		 */
		private void removeAllAliases(UpdateStatementFragment selectFrag) {
			for(FieldFragment ff: selectFrag.fieldL) {
				ff.alias = null;
			}
//			public List<SqlFragment> earlyL = new ArrayList<>();
			selectFrag.tblFrag.alias = null;
//			public JoinFragment joinFrag; //TODO later a list
			for(SqlFragment ff: selectFrag.whereL) {
				if (ff instanceof OpFragment) {
					OpFragment opff = (OpFragment) ff;
					opff.left.alias = null;
					opff.right.alias = null;
				}
			}
		}

		private void generateSetFields(QuerySpec spec, DStructType structType, UpdateStatementFragment selectFrag,
				DValue partialVal) {
			//we assume partialVal same type as structType!! (or maybe a base class)
			
			int index = selectFrag.fieldL.size(); //setValuesL is parallel array to fieldL
			if (index != 0) {
				
				log.log("WHY FILLING INNNNNNNNN");
				for(int i = 0; i < index; i++) {
					selectFrag.setValuesL.add("????");
				}
				DeliaExceptionHelper.throwError("unexpeced-fields-in-update", "should not occur");
			}
			
			for(String fieldName: partialVal.asMap().keySet()) {
				DValue inner = partialVal.asMap().get(fieldName);

				TypePair pair = DValueHelper.findField(structType, fieldName);
				FieldFragment ff = FragmentHelper.buildFieldFrag(structType, selectFrag, pair);
				String valstr = inner.asString();
				selectFrag.setValuesL.add(valstr == null ? "null" : valstr);
				selectFrag.fieldL.add(ff);
				
				index++;
			}
		}

		protected boolean needJoin(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag, QueryDetails details) {
			if (needJoinBase(spec, structType, selectFrag, details)) {
				return true;
			}

//			if (selectFrag.joinFrag == null) {
//				return false;
//			}
//
//			String alias = savedJoinedFrag.joinTblFrag.alias;
//
//			boolean mentioned = false;
//			if (selectFrag.orderByFrag != null) {
//				if (alias.equals(selectFrag.orderByFrag.alias)) {
//					mentioned = true;
//				}
//				for(OrderByFragment obff: selectFrag.orderByFrag.additionalL) {
//					if (alias.equals(obff.alias)) {
//						mentioned = true;
//						break;
//					}
//				}
//			}
//
//
//			if (mentioned) {
//				log.log("need join..");
//				return true;
//			}
			return false;
		}


		public void generateUpdateFns(QuerySpec spec, DStructType structType, UpdateStatementFragment selectFrag) {
			//orderby supported only by MySQL which delia does not support
//			this.doOrderByIfPresent(spec, structType, selectFrag);
			this.doLimitIfPresent(spec, structType, selectFrag);
//			this.doOffsetIfPresent(spec, structType, selectFrag);
		}


//		protected void doOffsetIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
//			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "offset");
//			if (qfexp == null) {
//				return;
//			}
//			IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
//			Integer n = exp.val;
//
//			OffsetFragment frag = new OffsetFragment(n);
//			selectFrag.offsetFrag = frag;
//		}

		public String renderUpdate(UpdateStatementFragment selectFrag) {
			if(selectFrag.setValuesL.isEmpty()) {
				return "";
			}
			
			selectFrag.statement.sql = selectFrag.render();
			return selectFrag.statement.sql;
		}

		public void useAliases(boolean b) {
			this.useAliases = b;
		}
	}