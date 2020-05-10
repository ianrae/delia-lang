package org.delia.db.hls;

import java.util.ArrayList;
import java.util.List;

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
		
		public SqlJoinHelper(AliasAllocator aliasAlloc, AssocTblManager assocTblMgr) {
			this.aliasAlloc = aliasAlloc;
			this.assocTblMgr = assocTblMgr;
		}

		public void genJoin(SQLCreator sc, HLSQuerySpan hlspan) {
			List<TypePair> joinL = genJoinList(hlspan);

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
					break;
				case MANY_TO_MANY:
					doManyToMany(sc, hlspan, pair, relinfoA);
					return;
				}
				
				String s;
				if (bHasFK) {
					DStructType pairType = (DStructType) pair.type; //Address
					RelationInfo relinfoB = findOtherSide(pairType, hlspan.fromType);
					PrimaryKey pk = pairType.getPrimaryKey();
//					PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
					
					String tbl1 = aliasAlloc.buildTblAlias((DStructType) pair.type);
					String on1 = aliasAlloc.buildAlias(hlspan.fromType, pk.getKey()); //a.id
					String on2 = aliasAlloc.buildAlias(pairType, relinfoB.fieldName); //b.cust
					s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
				} else {
					DStructType pairType = (DStructType) pair.type; //Address
					RelationInfo relinfoB = findOtherSide(pairType, hlspan.fromType);
					PrimaryKey pk = pairType.getPrimaryKey();
//					PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
					
					String tbl1 = aliasAlloc.buildTblAlias((DStructType) pair.type);
					String on1 = aliasAlloc.buildAlias(hlspan.fromType, relinfoA.fieldName); //a.addr
					String on2 = aliasAlloc.buildAlias(pairType, pk.getKey()); //b.id
					s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
				}
				
				sc.out(s);
			}
		}

		private void doManyToMany(SQLCreator sc, HLSQuerySpan hlspan, TypePair pair, RelationInfo relinfoA) {
			String s;
			PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
			String assocTable = assocTblMgr.getTableFor(hlspan.fromType, (DStructType) pair.type); //"CustomerAddressAssoc"; //TODO fix
			boolean flipLeftRight = assocTblMgr.isFlipped();
			if (hlspan.doubleFlip) {
				flipLeftRight = !flipLeftRight;
			}
			
			if (flipLeftRight) {
				String tbl1 = aliasAlloc.buildTblAliasAssoc(assocTable);
				String on1 = aliasAlloc.buildAliasAssoc(hlspan.fromType.getName(), mainPk.getFieldName()); //b.cust
				String on2 = aliasAlloc.buildAliasAssoc(assocTable, "rightv"); //a.id
				s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
			} else {
				// SELECT a.x,c.leftv FROM Customer as a zzLEFT JOIN AddressCustomerAssoc as c ON a.id=c.rightv
				String tbl1 = aliasAlloc.buildTblAliasAssoc(assocTable);
				String on1 = aliasAlloc.buildAliasAssoc(hlspan.fromType.getName(), mainPk.getFieldName()); //b.cust
				String on2 = aliasAlloc.buildAliasAssoc(assocTable, "leftv"); //a.id
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
			
			if (flipLeftRight) {
				DStructType pairType = (DStructType) pair.type; //Address
//				RelationInfo relinfoB = findOtherSide(pairType, hlspan.fromType);
				PrimaryKey pk = pairType.getPrimaryKey();
//				PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
				
				String tbl1 = aliasAlloc.buildTblAlias((DStructType) pair.type);
				String on1 = aliasAlloc.buildAlias(pairType, pk.getFieldName()); //b.id
				String on2 = aliasAlloc.buildAliasAssoc(assocTable, "rigthv"); //c.rightv
				s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
			} else {
				DStructType pairType = (DStructType) pair.type; //Address
//				RelationInfo relinfoB = findOtherSide(pairType, hlspan.fromType);
				PrimaryKey pk = pairType.getPrimaryKey();
//				PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
				
				String tbl1 = aliasAlloc.buildTblAlias((DStructType) pair.type);
				String on1 = aliasAlloc.buildAlias(pairType, pk.getFieldName()); //b.id
				String on2 = aliasAlloc.buildAliasAssoc(assocTable, "rigthv"); //c.leftv
				s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
			}
			sc.out(s);
		}

		private RelationInfo findOtherSide(DStructType pairType, DStructType fromType) {
			RelationInfo relinfo = DRuleHelper.findOtherSideOne(pairType, fromType);
			if (relinfo != null) {
				return relinfo;
			}

			relinfo = DRuleHelper.findOtherSideMany(pairType, fromType);
			if (relinfo != null) {
				return relinfo;
			}
			//err!!
			return null;
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
						joinL.add(pair);
					}
				}
			}

			//TODO: later to fk(field)
			return joinL;
		}
		

		public boolean addFKofJoins(HLSQuerySpan hlspan, List<String> fieldL) {
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
				fieldL.add(aliasAlloc.buildAlias(pairType, pk.getFieldName()));
				return true;
			}
			return false;
		}
		private void doManyToManyAddFKofJoins(HLSQuerySpan hlspan, List<String> fieldL, TypePair pair,
				RelationInfo relinfoA) {
			String assocTbl = assocTblMgr.getTableFor(hlspan.fromType, (DStructType) pair.type);
			String fieldName = assocTblMgr.isFlipped() ? "leftv" : "rightv";
			fieldL.add(aliasAlloc.buildAliasAssoc(assocTbl, fieldName));
		}

		public void addFullofJoins(HLSQuerySpan hlspan, List<String> fieldL) {
			List<TypePair> joinL = genFullJoinList(hlspan);

			for(TypePair pair: joinL) {
				addStructFields((DStructType) pair.type, fieldL);
			}
		}
		public void addStructFields(DStructType fromType, List<String> fieldL) {
			for(TypePair pair: fromType.getAllFields()) {
				if (pair.type.isStructShape()) {
					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fromType, pair);
					if (!relinfo.isParent && !RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
						fieldL.add(aliasAlloc.buildAlias(fromType, pair.name));
					}
				} else {
					fieldL.add(aliasAlloc.buildAlias(fromType, pair.name));
				}
			}
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