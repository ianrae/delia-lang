package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.DBInterface;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.sql.prepared.TableInfoHelper;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.VarEvaluator;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
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

			Map<String, DRelation> mmMap = new HashMap<>();
			
			//init tbl
			DStructType structType = getMainType(spec); 
			TableFragment tblFrag = createTable(structType, selectFrag);
			selectFrag.tblFrag = tblFrag;

			generateSetFields(spec, structType, selectFrag, partialVal, mmMap);
			initFieldsAndWhere(spec, structType, selectFrag);
			generateAssocUpdateIfNeeded(spec, structType, selectFrag, mmMap);
			
			//no min,max,etc in UPDATE

			generateUpdateFns(spec, structType, selectFrag);

			fixupForParentFields(structType, selectFrag);
//			if (needJoin(spec, structType, selectFrag, details)) {
//				//used saved join if we have one
//				if (savedJoinedFrag == null) {
//					addJoins(spec, structType, selectFrag, details);
//				} else {
//					selectFrag.joinFrag = savedJoinedFrag;
//				}
//			}
			
			if (! useAliases) {
				removeAllAliases(selectFrag);
				if (selectFrag.assocUpdateFrag != null) {
					removeAllAliases(selectFrag.assocUpdateFrag);
				}
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
				DValue partialVal, Map<String, DRelation> mmMap) {
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
				TypePair pair = DValueHelper.findField(structType, fieldName);
				
				if (pair.type.isStructShape()) {
					if (! shouldGenerateFKConstraint(pair, structType)) {
						continue;
					}
					if (DRuleHelper.isManyToManyRelation(pair, structType)) {
						DValue inner = partialVal.asStruct().getField(pair.name);
						if (inner != null) {
							mmMap.put(pair.name, inner.asRelation());
						}
						continue;
					}
				}
				
				DValue inner = partialVal.asMap().get(fieldName);
				if (inner == null) {
					continue;
				}
				
				DValue dvalToUse = inner;
				if (inner.getType().isRelationShape()) {
					DRelation drel = inner.asRelation();
					dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later
				}
				
				FieldFragment ff = FragmentHelper.buildFieldFrag(structType, selectFrag, pair);
				String valstr = dvalToUse.asString();
				selectFrag.setValuesL.add(valstr == null ? "null" : valstr);
				selectFrag.fieldL.add(ff);
				
				index++;
			}
		}
		private boolean shouldGenerateFKConstraint(TypePair pair, DStructType dtype) {
			//key goes in child only
			RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
			if (info != null && !info.isParent) {
				return true;
			}
			return false;
		}
		private void generateAssocUpdateIfNeeded(QuerySpec spec, DStructType structType,
				UpdateStatementFragment selectFrag, Map<String, DRelation> mmMap) {
			if (mmMap.isEmpty()) {
				return;
			}
			
			for(String fieldName: mmMap.keySet()) {
				RelationManyRule ruleMany = DRuleHelper.findManyRule(structType, fieldName);
				if (ruleMany != null) {
					RelationInfo info = ruleMany.relInfo;
					selectFrag.assocUpdateFrag = new UpdateStatementFragment();
					genAssocField(selectFrag.assocUpdateFrag, structType, mmMap, fieldName, info, selectFrag.whereL);
				}
			}
		}

		private void genAssocField(UpdateStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, RelationInfo info, List<SqlFragment> existingWhereL) {
			//update assoctabl set leftv=x where rightv=y
			TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);
			assocUpdateFrag.tblFrag = this.createAssocTable(assocUpdateFrag, tblinfo.assocTblName);
			
			//struct is Address AddressCustomerAssoc
			if (tblinfo.tbl1.equalsIgnoreCase(structType.getName())) {
				if (existingWhereL.isEmpty()) {
					buildAll(assocUpdateFrag, structType, mmMap, fieldName, info, "rightv");
					return;
				} else if (this.isOnlyPrimaryKeyQuery(existingWhereL, info.farType)) {
					List<OpFragment> oplist = findPrimaryKeyQuery(existingWhereL, info.farType);
					log.log("aaaakkkkkkkkkkkkkkkkkk");
					buildIdOnlyQuery(assocUpdateFrag, structType, mmMap, fieldName, info, "rightv", "leftv", oplist);
				}
			} else {
				if (existingWhereL.isEmpty()) {
					buildAll(assocUpdateFrag, structType, mmMap, fieldName, info, "leftv");
					return;
				} else if (this.isOnlyPrimaryKeyQuery(existingWhereL, info.farType)) {
					List<OpFragment> oplist = findPrimaryKeyQuery(existingWhereL, info.farType);
					log.log("kkkkkkkkkkkkkkkkkk");
					buildIdOnlyQuery(assocUpdateFrag, structType, mmMap, fieldName, info, "leftv", "rightv", oplist);
				}
			}
		}

		private List<OpFragment> findPrimaryKeyQuery(List<SqlFragment> existingWhereL, DStructType farType) {
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(farType);
			List<OpFragment> oplist = new ArrayList<>();
			
			for(SqlFragment ff: existingWhereL) {
				if (ff instanceof OpFragment) {
					OpFragment opff = (OpFragment) ff;
					if (opff.left.name.equals(pair.name) || opff.right.name.equals(pair.name)) {
						oplist.add(opff);
					}
				}
			}
			return oplist;
		}
		private boolean isOnlyPrimaryKeyQuery(List<SqlFragment> existingWhereL, DStructType farType) {
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(farType);
			
			int failCount = 0;
			for(SqlFragment ff: existingWhereL) {
				if (ff instanceof OpFragment) {
					OpFragment opff = (OpFragment) ff;
					if (!opff.left.name.equals(pair.name) && !leftIsQuestionMark(opff)) {
						failCount++;
					}
					if (!opff.right.name.equals(pair.name) && !rightIsQuestionMark(opff)) {
						failCount++;
					}
				}
			}
			return failCount == 0;
		}
		private List<OpFragment> changeIdToAssocFieldName(List<OpFragment> existingWhereL, DStructType farType, String alias, String assocFieldName) {
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(farType);
			List<OpFragment> oplist = new ArrayList<>();
			
			for(OpFragment opff: existingWhereL) {
				if (opff.left.name.equals(pair.name)) {
					OpFragment clone = new OpFragment(opff);
					clone.left.alias = alias;
					clone.left.name = assocFieldName;
					oplist.add(clone);
				} else if (opff.right.name.equals(pair.name)) {
					OpFragment clone = new OpFragment(opff);
					clone.right.alias = alias;
					clone.right.name = assocFieldName;
					oplist.add(clone);
				}
			}
			return oplist;
		}
		
		private boolean leftIsQuestionMark(OpFragment opff) {
			return opff.left.name.equals("?");
		}
		private boolean rightIsQuestionMark(OpFragment opff) {
			return opff.right.name.equals("?");
		}
		private void buildIdOnlyQuery(UpdateStatementFragment assocUpdateFrag, DStructType structType,
				Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName,
				String assocField2, List<OpFragment> oplist) {
			DRelation drel = mmMap.get(fieldName); //100
			DValue dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later
			
//			TypePair pair1 = DValueHelper.findField(info.nearType, info.fieldName); //Customer.addr
//			TypePair leftPair = new TypePair("rightv", pair1.type);
//			FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocUpdateFrag.tblFrag, assocUpdateFrag, leftPair);
//			String valstr = dvalToUse == null ? null : dvalToUse.asString();
//			assocUpdateFrag.setValuesL.add(valstr == null ? "null" : valstr);
//			assocUpdateFrag.fieldL.add(ff);
			
//			TypePair otherPrimaryKeyPair = DValueHelper.findPrimaryKeyFieldPair(info.farType);
			RelationInfo farInfo = DRuleHelper.findOtherSideMany(info.farType, structType);
			TypePair pair2 = DValueHelper.findField(farInfo.nearType, farInfo.fieldName);
			TypePair rightPair = new TypePair(assocFieldName, pair2.type);
			FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocUpdateFrag.tblFrag, assocUpdateFrag, rightPair);
			String valstr = dvalToUse == null ? null : dvalToUse.asString();
			assocUpdateFrag.setValuesL.add(valstr == null ? "null" : valstr);
			assocUpdateFrag.fieldL.add(ff);
			
			List<OpFragment> clonedL = changeIdToAssocFieldName(oplist, info.farType, assocUpdateFrag.tblFrag.alias, assocField2);
			assocUpdateFrag.whereL.addAll(clonedL);
		}


		protected void buildAll(UpdateStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName) {
			DRelation drel = mmMap.get(fieldName); //100
			DValue dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later
			
//			TypePair pair1 = DValueHelper.findField(info.nearType, info.fieldName); //Customer.addr
//			TypePair leftPair = new TypePair("rightv", pair1.type);
//			FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocUpdateFrag.tblFrag, assocUpdateFrag, leftPair);
//			String valstr = dvalToUse == null ? null : dvalToUse.asString();
//			assocUpdateFrag.setValuesL.add(valstr == null ? "null" : valstr);
//			assocUpdateFrag.fieldL.add(ff);
			
//			TypePair otherPrimaryKeyPair = DValueHelper.findPrimaryKeyFieldPair(info.farType);
			RelationInfo farInfo = DRuleHelper.findOtherSideMany(info.farType, structType);
			TypePair pair2 = DValueHelper.findField(farInfo.nearType, farInfo.fieldName);
			TypePair rightPair = new TypePair(assocFieldName, pair2.type);
			FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocUpdateFrag.tblFrag, assocUpdateFrag, rightPair);
			String valstr = dvalToUse == null ? null : dvalToUse.asString();
			assocUpdateFrag.setValuesL.add(valstr == null ? "null" : valstr);
			assocUpdateFrag.fieldL.add(ff);
			
//			OpFragment opFrag = new OpFragment("=");
//			opFrag.left = FragmentHelper.buildAliasedFrag(null, "rightv");
//			opFrag.right = FragmentHelper.buildAliasedFrag(null, "999999999");
//			assocUpdateFrag.whereL.add(opFrag);
		}

		private String findFromWhere(List<SqlFragment> exitingWhereL) {
			// TODO Auto-generated method stub
			return null;
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
				selectFrag.statement.sql = ""; //nothing to do
				return selectFrag.statement.sql;
			}
			
			selectFrag.statement.sql = selectFrag.render();
			return selectFrag.statement.sql;
		}

		public void useAliases(boolean b) {
			this.useAliases = b;
		}
	}