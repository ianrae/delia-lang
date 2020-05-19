package org.delia.db.hls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.delia.db.QueryDetails;
import org.delia.db.sql.fragment.MiniSelectFragmentParser;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

public class SqlJoinHelper {
		private AliasAllocator aliasAlloc;
		private AssocTblManager assocTblMgr;
//		private MiniSelectFragmentParser miniSelectParser;
		
		public SqlJoinHelper(AliasAllocator aliasAlloc, AssocTblManager assocTblMgr, Map<String, String> asNameMap, MiniSelectFragmentParser miniSelectParser) {
			this.aliasAlloc = aliasAlloc;
			this.assocTblMgr = assocTblMgr;
//			this.miniSelectParser = miniSelectParser;
		}
		
		public QueryDetails genJoin(SQLCreator sc, HLSQuerySpan hlspan) {
			List<TypePair> joinL = genJoinList(hlspan);
			QueryDetails details = new QueryDetails();
			
			//do the joins
			for(TypePair pair: joinL) {
				boolean bHasFK = false;
				RelationInfo relinfoA = DRuleHelper.findMatchingRuleInfo(hlspan.fromType, pair);
				switch(relinfoA.cardinality) {
				case ONE_TO_ONE:
					bHasFK = relinfoA.isParent;
					break;
				case ONE_TO_MANY:
					bHasFK = relinfoA.isParent;
					details.mergeRows = true;
					details.mergeOnField = relinfoA.fieldName;
					break;
				case MANY_TO_MANY:
					details.mergeRows = true;
					details.isManyToMany = true;
					details.mergeOnField = relinfoA.fieldName;
					doManyToMany(sc, hlspan, pair, relinfoA);
					return details;
				}
				
				String s;
				DStructType pairType = (DStructType) pair.type; //Address
				AliasInstance aliasInst = aliasAlloc.findOrCreateAliasInstance(pairType, pair.name);
				String tbl1 = aliasAlloc.buildTblAlias(aliasInst);
				if (bHasFK) {
					RelationInfo relinfoB = findOtherSide(pair, hlspan.fromType);
					PrimaryKey pk = hlspan.fromType.getPrimaryKey();
					
					String on1 = aliasAlloc.buildAlias(hlspan.fromType, pk.getKey()); //a.id
					String on2 = aliasAlloc.buildAlias(aliasInst, relinfoB.fieldName); //b.cust
					s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
				} else {
					PrimaryKey pk = pairType.getPrimaryKey();
					
					String on1 = aliasAlloc.buildAlias(hlspan.fromType, relinfoA.fieldName); //a.addr
					String on2 = aliasAlloc.buildAlias(aliasInst, pk.getKey().name); //b.id
					s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
				}
				
				sc.out(s);
			}
			return details;
		}

		private void doManyToMany(SQLCreator sc, HLSQuerySpan hlspan, TypePair pair, RelationInfo relinfoA) {
			String s;
			PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
			String assocTable = assocTblMgr.getTableFor(hlspan.fromType, (DStructType) pair.type); //"CustomerAddressAssoc"; //TODO fix
			
			if (true) {
				String tbl1 = aliasAlloc.buildTblAliasAssoc(assocTable);
				String on1 = aliasAlloc.buildAliasAssoc(hlspan.fromType.getName(), mainPk.getFieldName()); //b.cust
				String fff = assocTblMgr.getAssocLeftField(hlspan.fromType, (DStructType) pair.type);
				String on2 = aliasAlloc.buildAliasAssoc(assocTable, fff); //a.id
				s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
			}
			
			sc.out(s);
			
			//and now 2nd join of Address table
			List<TypePair> fullJoinL = genFullJoinList(hlspan);
			boolean found = false;
			for(TypePair tt: fullJoinL) {
				if (tt.name.equals(pair.name) && tt.type.equals(pair.type)) {
					found = true;
				}
			}
			if (! found) {
				return;
			}
			
			s = null;
			
			DStructType pairType = (DStructType) pair.type; //Address
			PrimaryKey pk = pairType.getPrimaryKey();
			String tbl1 = aliasAlloc.buildTblAlias((DStructType) pair.type);
			String on1 = aliasAlloc.buildAlias(pairType, pk.getFieldName()); //b.id
			String fff = assocTblMgr.getAssocRightField(hlspan.fromType, (DStructType) pair.type);
			String on2 = aliasAlloc.buildAliasAssoc(assocTable, fff); //c.rightv
			s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
			sc.out(s);
		}

		private RelationInfo findOtherSide(TypePair pair, DStructType fromType) {
			RelationInfo info = DRuleHelper.findRelinfoOneOrManyForField(fromType, pair.name);
			if (info != null) {
				return info.otherSide; //can be null for one-sided relation
			}
			
//			RelationInfo relinfo = DRuleHelper.findOtherSideOne(pairType, fromType);
//			if (relinfo != null) {
//				return relinfo;
//			}
//
//			relinfo = DRuleHelper.findOtherSideMany(pairType, fromType);
//			if (relinfo != null) {
//				return relinfo;
//			}
			//err!!
			return null;
		}
		
		public boolean needJoin(HLSQuerySpan hlspan) {
			return !genJoinList(hlspan).isEmpty();
		}

		private List<TypePair> genJoinList(HLSQuerySpan hlspan) {
			List<TypePair> joinL = genFullJoinList(hlspan);
			List<TypePair> join2L = genFKJoinList(hlspan);
			joinL.addAll(join2L);
			return joinL;
		}
		private List<TypePair> genFullJoinList(HLSQuerySpan hlspan) {
			List<TypePair> joinL = new ArrayList<>();

			boolean needJoin = hlspan.subEl != null;
			if (! needJoin) {
				return joinL;
			}

			for (String fieldName: hlspan.subEl.fetchL) {
				if (joinL.contains(fieldName)) {
					continue;
				}
				TypePair pair = DValueHelper.findField(hlspan.fromType, fieldName);
				joinL.add(pair);
			}

			//TODO: later to fk(field)
			return joinL;
		}
		private List<TypePair> genFKJoinList(HLSQuerySpan hlspan) {
			List<TypePair> joinL = new ArrayList<>();

			boolean needJoin = hlspan.subEl != null;
			if (! needJoin) {
				return joinL;
			}

			if (hlspan.subEl.allFKs) {
				for(TypePair pair: hlspan.fromType.getAllFields()) {
					if (pair.type.isStructShape()) {
						RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(hlspan.fromType, pair);
						if (relinfo.isParent || RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
							joinL.add(pair);
						}
					}
				}
			}

			//TODO: later to fk(field)
			return joinL;
		}
		

		public boolean addFKofJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL) {
			List<TypePair> joinL = genFKJoinList(hlspan);

			for(TypePair pair: joinL) {
				DStructType pairType = (DStructType) pair.type;
				PrimaryKey pk = pairType.getPrimaryKey();
				
				RelationInfo relinfoA = DRuleHelper.findMatchingRuleInfo(hlspan.fromType, pair);
				switch(relinfoA.cardinality) {
				case ONE_TO_ONE:
				case ONE_TO_MANY:
					break;
				case MANY_TO_MANY:
					doManyToManyAddFKofJoins(hlspan, fieldL, pair, relinfoA);
					return true;
				}
				
				//b.id as cust
				AliasInstance aliasInst = aliasAlloc.findOrCreateAliasInstance(pairType, pair.name);
				String s = aliasAlloc.buildAlias(aliasInst, pk.getFieldName());
				s = String.format("%s as %s", s, relinfoA.fieldName);
//				fieldL.add(s);
				addField(fieldL, pairType, pk.getKey(), s); 
			
				return true;
			}
			return false;
		}
		private void doManyToManyAddFKofJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL, TypePair pair,
				RelationInfo relinfoA) {
			String assocTbl = assocTblMgr.getTableFor(hlspan.fromType, (DStructType) pair.type);
//			String fieldName = assocTblMgr.isFlipped(hlspan.fromType, (DStructType) pair.type) ? "leftv" : "rightv";
			String fieldName = assocTblMgr.getAssocRightField(hlspan.fromType, (DStructType) pair.type);
			
			//b.id as cust
			String s = aliasAlloc.buildAliasAssoc(assocTbl, fieldName);
			s = String.format("%s as %s", s, relinfoA.fieldName);
			addField(fieldL, null, fieldName, s).isAssocField = true;
//			fieldL.add(s);
		}

		public void addFullofJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL) {
			List<TypePair> joinL = genFullJoinList(hlspan);

			for(TypePair pair: joinL) {
				addStructFieldsMM(pair, fieldL);
			}
		}
		public void addStructFieldsMM(TypePair joinType, List<RenderedField> fieldL) {
			DStructType fromType = (DStructType) joinType.type;
			String pk = fromType.getPrimaryKey().getFieldName();
			
			
			for(TypePair pair: fromType.getAllFields()) {
				if (pair.name.equals(pk)) {
					String s = aliasAlloc.buildAlias(fromType, pair.name);
					s = String.format("%s as %s", s, joinType.name);
					addField(fieldL, fromType, pair, s);
//					fieldL.add(s);
				} else if (pair.type.isStructShape()) {
					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fromType, pair);
					if (RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
					} else if (!relinfo.isParent) {
						String s = aliasAlloc.buildAlias(fromType, pair.name);
						addField(fieldL, fromType, pair, s);
					}
				} else {
					String s = aliasAlloc.buildAlias(fromType, pair.name);
					addField(fieldL, fromType, pair, s);
				}
			}
		}
		public void addStructFields(DStructType fromType, List<RenderedField> fieldL) {
			for(TypePair pair: fromType.getAllFields()) {
				if (pair.type.isStructShape()) {
					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fromType, pair);
					if (RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
					} else if (!relinfo.isParent) {
						addField(fieldL, fromType, pair, aliasAlloc.buildAlias(fromType, pair.name));
//						fieldL.add(aliasAlloc.buildAlias(fromType, pair.name));
					}
				} else {
					addField(fieldL, fromType, pair, aliasAlloc.buildAlias(fromType, pair.name));
//					fieldL.add(aliasAlloc.buildAlias(fromType, pair.name));
				}
			}
		}
		
		private void addField(List<RenderedField> fieldL, DStructType fromType, TypePair pair, String s) {
			RenderedField rf = new RenderedField();
			rf.pair = pair;
			rf.field = s;
			rf.structType = fromType;
			fieldL.add(rf);
		}
		private RenderedField addField(List<RenderedField> fieldL, DStructType fromType, String fieldName, String s) {
			RenderedField rf = new RenderedField();
			rf.pair = new TypePair(fieldName, null);
			rf.field = s;
			rf.structType = fromType;
			fieldL.add(rf);
			return rf;
		}

		public List<TypePair> genTwoStatementJoinList(HLSQuerySpan hlspan1, HLSQuerySpan hlspan2, SQLCreator sc) {
			List<TypePair> joinL = new ArrayList<>();

			boolean needJoin = true; //hlspan.subEl != null;
			if (! needJoin) {
				return joinL;
			}

			
			//1 is address
			//2 is customer
			
			String targetTypeName = hlspan2.fromType.getName();
			for(TypePair pair: hlspan1.fromType.getAllFields()) {
				if (pair.type.isStructShape()) {
					if (pair.type.getName().equals(targetTypeName)) {
						
						RelationInfo relinfoA = DRuleHelper.findMatchingRuleInfo(hlspan1.fromType, pair);
						if (RelationCardinality.MANY_TO_MANY.equals(relinfoA.cardinality)) {
							TypePair tmp = new TypePair("xx", relinfoA.farType);
							doManyToMany(sc, hlspan1, tmp, relinfoA);
							
							joinL.add(pair);
						}
					}
				}
			}
			return joinL;
		}
	}