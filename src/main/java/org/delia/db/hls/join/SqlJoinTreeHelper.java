package org.delia.db.hls.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.delia.assoc.DatIdMap;
import org.delia.db.QueryDetails;
import org.delia.db.hls.AliasInfo;
import org.delia.db.hls.AliasManager;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.RenderedField;
import org.delia.db.hls.SQLCreator;
import org.delia.db.sql.fragment.MiniSelectFragmentParser;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DeliaExceptionHelper;

public class SqlJoinTreeHelper implements SqlJoinHelper {
	private AliasManager aliasManager;
	private DatIdMap datIdMap;

	public SqlJoinTreeHelper(AliasManager aliasManager, DatIdMap datIdMap, Map<String, String> asNameMap, MiniSelectFragmentParser miniSelectParser) {
		this.aliasManager = aliasManager;
		this.datIdMap = datIdMap;
		//			this.miniSelectParser = miniSelectParser;
	}

	@Override
	public QueryDetails genJoin(SQLCreator sc, HLSQuerySpan hlspan) {
//		List<TypePair> joinL = genJoinList(hlspan);
		QueryDetails details = new QueryDetails();

		//do the joins
		for(JTElement el: hlspan.joinTreeL) {
			TypePair pair = el.createPair();
			boolean isParentL = false;
			boolean isParentR = false;
			RelationInfo relinfoA = el.relinfo; 
			
			switch(relinfoA.cardinality) {
			case ONE_TO_ONE:
				isParentL = relinfoA.isParent;
				isParentR = !isParentL;
				break;
			case ONE_TO_MANY:
				isParentL = relinfoA.isParent;
				isParentR = !isParentL;
				details.mergeRows = true;
				details.mergeOnFieldL.add(relinfoA.fieldName);
				break;
			case MANY_TO_MANY:
				details.mergeRows = true;
				details.isManyToMany = true;
				details.mergeOnFieldL.add(relinfoA.fieldName);
				TypePair actualPair = new TypePair(relinfoA.fieldName, relinfoA.nearType);
				doManyToMany(sc, hlspan, pair, relinfoA, actualPair);
				break;
			}
			
			DStructType leftJ;
			DStructType rightJ;
			String s;
			if (hlspan.fromType == el.dtype) {
				leftJ = el.dtype;
				rightJ = el.fieldType;
				AliasInfo aliasInfoR = findAlias(leftJ, relinfoA.fieldName);
				s = genJoinSQL(leftJ, rightJ, isParentL, isParentR, relinfoA, relinfoA.otherSide, aliasInfoR, false);
			} else {
				leftJ = el.fieldType;
				rightJ = el.dtype;
				AliasInfo aliasInfoR = findAlias(rightJ, relinfoA.fieldName);
				s = genJoinSQL(leftJ, rightJ, isParentR, isParentL, relinfoA.otherSide, relinfoA, aliasInfoR, true);
			}
			
//			DStructType pairType = (DStructType) pair.type; //Address
//			AliasInfo aliasInfo = aliasManager.getFieldAlias(relinfoA.nearType, relinfoA.fieldName);
//			String tbl1 = aliasManager.buildTblAlias(aliasInfo);
//			if (isParent) {
//				RelationInfo relinfoB = relinfoA.otherSide; //findOtherSide(pair, el.dtype);
//				PrimaryKey pk = relinfoA.nearType.getPrimaryKey();
//
//				String on1 = aliasManager.buildFieldAlias(aliasInfo, pk.getKey().name); //a.id
//				String on2 = buildFieldAliasEx(el.dtype, pair.name, relinfoB.fieldName); //b.cust
//				s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
//			} else {
//				PrimaryKey pk = pairType.getPrimaryKey();
//				String on1 = buildFieldAlias(relinfoA.nearType, relinfoA.fieldName); //a.addr
////				String on2 = buildFieldAlias((DStructType) pair.type, pair.name, pk.getKey().name); //b.id
//				String on2 = aliasManager.buildFieldAlias(aliasInfo, pk.getKey().name); //b.id
//				s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
//			}

			sc.out(s);
		}
		return details;
	}

	private String genJoinSQL(DStructType leftJ, DStructType rightJ, boolean isParentL, boolean isParentR, 
			RelationInfo relinfoA, RelationInfo relinfoB, AliasInfo aliasInfoR, boolean isBackwards) {
		String tbl1 = aliasManager.buildTblAlias(aliasInfoR, isBackwards);
		
		String on1 = genOn(leftJ, isParentL, relinfoA.fieldName, isBackwards); 
		String on2;
		if (isParentR) {
			on2 = genOn(rightJ, isParentR, relinfoB.fieldName, isBackwards);
		} else {
			on2 = aliasManager.buildFieldAlias(aliasInfoR, relinfoB.fieldName);
		}
		
		String s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
		return s;
	}
	
	private String genOn(DStructType dtype, boolean isParent, String fieldName, boolean isBackwards) {
		String s;
		if (isParent) {
			PrimaryKey pk = dtype.getPrimaryKey();
			AliasInfo aliasInfo = aliasManager.getMainTableAlias(dtype);
			if (aliasInfo == null) {
				aliasInfo = findAlias(dtype, fieldName);
			}
			s = aliasManager.buildFieldAlias(aliasInfo, pk.getKey().name); 
		} else {
			AliasInfo aliasInfo = findAlias(dtype, fieldName);
			s = aliasManager.buildFieldAlias(aliasInfo, fieldName); //a.addr
		}
		return s;
	}
	private AliasInfo findAlias(DStructType dtype, String fieldName) {
		AliasInfo aliasInfo = aliasManager.getFieldAlias(dtype, fieldName);
		if (aliasInfo == null) {
			aliasInfo = aliasManager.getMainTableAlias(dtype);
		}
		return aliasInfo;
	}

	private String buildMainAlias(HLSQuerySpan hlspan, String fieldName) {
		AliasInfo info = aliasManager.getMainTableAlias(hlspan.mtEl.structType);
		return aliasManager.buildFieldAlias(info, fieldName);
	}
	private String buildFieldAlias(DStructType structType, String fieldName) {
		AliasInfo info = aliasManager.getMainTableAlias(structType);
		return aliasManager.buildFieldAlias(info, fieldName);
	}
	private String buildFieldAliasEx(DStructType structType, String fieldName, String fieldNameToBeAliased) {
		AliasInfo info = aliasManager.getFieldAlias(structType, fieldName);
		return aliasManager.buildFieldAlias(info, fieldNameToBeAliased);
	}

	private void doManyToMany(SQLCreator sc, HLSQuerySpan hlspan, TypePair pair, RelationInfo relinfoA, TypePair actualPair) {
		String s;
		PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
		String assocTable = datIdMap.getAssocTblName(relinfoA.getDatId()); 

		if (true) {
			//				AliasInstance aliasInst = aliasAlloc.findOrCreateAliasInstanceAssoc(assocTable);
			//				String tbl1 = aliasAlloc.buildTblAlias(aliasInst);
			//				String on1 = aliasAlloc.buildAliasAssoc(hlspan.fromType.getName(), mainPk.getFieldName()); //b.cust
			//				String fff = assocTblMgr.xgetAssocLeftField(hlspan.fromType, aliasInst.assocTbl);
			//				String on2 = aliasAlloc.buildAlias(aliasInst, fff); //a.id

			AliasInfo aliasInfo = aliasManager.getAssocAlias(relinfoA.nearType, relinfoA.fieldName, assocTable);
			String tbl1 = aliasManager.buildTblAlias(aliasInfo);
			String on1 = buildMainAlias(hlspan, mainPk.getFieldName()); //b.cust
			//				String fffx = DatIdMapHelper.getAssocLeftField(hlspan.fromType, assocTable);
			String fff = datIdMap.getAssocFieldFor(relinfoA);
			String on2 = aliasManager.buildFieldAlias(aliasInfo, fff); //a.id

			s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
		}

		sc.out(s);

//		//and now 2nd join of Address table
//		List<TypePair> fullJoinL = genFullJoinList(hlspan);
//		boolean found = false;
//		for(TypePair tt: fullJoinL) {
//			if (tt.name.equals(pair.name) && tt.type.equals(pair.type)) {
//				found = true;
//			}
//		}
//		if (! found) {
//			return;
//		}
//
//		s = null;
//
//		DStructType pairType = (DStructType) pair.type; //Address
//		PrimaryKey pk = pairType.getPrimaryKey();
//		//			AliasInstance aliasInst = aliasAlloc.findAliasFor(pairType);
//		//			String tbl1 = aliasAlloc.buildTblAlias(aliasInst);
//		//			String on1 = aliasAlloc.buildAlias(aliasInst, pk.getFieldName()); //b.id
//		//			String fff = assocTblMgr.xgetAssocRightField(hlspan.fromType, assocTable);
//		////			String on2 = aliasAlloc.buildAliasAssoc(assocTable, fff); //c.rightv
//		//			AliasInstance ai2 = aliasAlloc.findAliasForTable(assocTable);
//		//			String on2 = aliasAlloc.buildAlias(ai2, fff); //c.rightv
//
//		AliasInfo aliasInfo = aliasManager.getFieldAlias((DStructType) actualPair.type, actualPair.name);
//		String tbl1 = aliasManager.buildTblAlias(aliasInfo);
//		String on1 = aliasManager.buildFieldAlias(aliasInfo, pk.getFieldName()); //b.id
//		//			String fff = DatIdMapHelper.getAssocRightField(hlspan.fromType, assocTable);
//		String fff = datIdMap.getAssocOtherField(relinfoA);
//		AliasInfo ai2 = aliasManager.getAssocAlias(relinfoA.nearType, relinfoA.fieldName, assocTable);
//		String on2 = aliasManager.buildFieldAlias(ai2, fff); //c.rightv
//
//		s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
//		sc.out(s);
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

	@Override
	public boolean needJoin(HLSQuerySpan hlspan) {
		return !hlspan.joinTreeL.isEmpty();
	}

//	private List<TypePair> genJoinList(HLSQuerySpan hlspan) {
//		List<TypePair> joinL = new ArrayList<>();
//		for(JTElement el: hlspan.joinTreeL) {
//			TypePair pair = el.createPair();
//			joinL.add(pair);
//		}
//		return joinL;
////		List<TypePair> joinL = genFullJoinList(hlspan);
////		List<TypePair> join2L = genFKJoinList(hlspan);
////		joinL.addAll(join2L);
////		List<TypePair> join3L = genINJoinList(hlspan);
////		List<TypePair> finalL = Stream.concat(joinL.stream(), join3L.stream()).distinct().collect(Collectors.toList());
////		TypePair relFieldPair = genRelField(hlspan);
////		if (relFieldPair != null) {
////			finalL.add(relFieldPair);
////		}
////		return finalL;
//	}
//	private TypePair genRelField(HLSQuerySpan hlspan) {
//		if (hlspan.rEl != null) {
//			return hlspan.rEl.rfieldPair;
//		}
//		return null;
//	}
//
//	private List<TypePair> genFullJoinList(HLSQuerySpan hlspan) {
//		List<TypePair> joinL = new ArrayList<>();
//
//		boolean needJoin = hlspan.subEl != null;
//		if (! needJoin) {
//			return joinL;
//		}
//
//		for (String fieldName: hlspan.subEl.fetchL) {
//			if (joinL.contains(fieldName)) {
//				continue;
//			}
//			TypePair pair = DValueHelper.findField(hlspan.fromType, fieldName);
//			joinL.add(pair);
//			//aliasAlloc.findOrCreateAliasInstance((DStructType) pair.type, pair.name);
//		}
//
//		//TODO: later support fk(field)
//		return joinL;
//	}
//	private List<TypePair> genFKJoinList(HLSQuerySpan hlspan) {
//		List<TypePair> joinL = new ArrayList<>();
//
//		boolean needJoin = hlspan.subEl != null;
//		if (! needJoin) {
//			return joinL;
//		}
//
//		if (hlspan.subEl.allFKs) {
//			for(TypePair pair: hlspan.fromType.getAllFields()) {
//				if (pair.type.isStructShape()) {
//					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(hlspan.fromType, pair);
//					if (relinfo.isParent || RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
//						joinL.add(pair);
//					}
//				}
//			}
//		}
//
//		//TODO: later to fk(field)
//		return joinL;
//	}
//	private List<TypePair> genINJoinList(HLSQuerySpan hlspan) {
//		List<TypePair> joinL = new ArrayList<>();
//		if (hlspan.filEl ==  null) {
//			return joinL;
//		}
//		//look for in [followers]
//		QueryInExp inQueryExp = hlspan.filEl.getAsInQuery();
//		if (inQueryExp != null) {
//			for(Exp arg: inQueryExp.listExp.valueL) {
//				if (arg instanceof IdentExp) {
//					String fieldName = arg.strValue();
//					TypePair pair = DValueHelper.findField(hlspan.fromType, fieldName);
//					if (pair != null) {
//						joinL.add(pair);
//					}
//				}
//			}
//		}
//		return joinL;
//	}


	@Override
	public int addFKofJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL) {
		List<JTElement> elL = hlspan.joinTreeL.stream().filter(x -> x.usedForFK).collect(Collectors.toList());

		int numAdded = 0;
		for(JTElement el: elL) {
			TypePair pair = el.createPair();
			DStructType pairType = (DStructType) pair.type;
			PrimaryKey pk = pairType.getPrimaryKey();

			RelationInfo relinfoA = el.relinfo; // DRuleHelper.findMatchingRuleInfo(hlspan.fromType, pair);
			switch(relinfoA.cardinality) {
			case ONE_TO_ONE:
			case ONE_TO_MANY:
				break;
			case MANY_TO_MANY:
				doManyToManyAddFKofJoins(fieldL, pair, relinfoA);
				numAdded++;
				continue;
			}

			//b.id as cust
			//				AliasInstance aliasInst = aliasAlloc.findOrCreateAliasInstance(pairType, pair.name);
			//				String s = aliasAlloc.buildAlias(aliasInst, pk.getFieldName());
			AliasInfo aliasInfo = aliasManager.getFieldAlias(relinfoA.nearType, relinfoA.fieldName);
			String s = aliasManager.buildFieldAlias(aliasInfo, pk.getFieldName());
			s = String.format("%s as %s", s, relinfoA.fieldName);
			addField(fieldL, pairType, pk.getKey(), s); 
			numAdded++;
		}
		return numAdded;
	}
	private void doManyToManyAddFKofJoins(List<RenderedField> fieldL, TypePair pair,
			RelationInfo relinfoA) {
		String assocTbl = datIdMap.getAssocTblName(relinfoA.getDatId()); 
		String fieldName = datIdMap.getAssocOtherField(relinfoA);

		//b.id as cust
		//			AliasInstance aliasInst = aliasAlloc.findOrCreateAliasInstanceAssoc(assocTbl);
		//			String s = aliasAlloc.buildAlias(aliasInst, fieldName);
		AliasInfo aliasInfo = aliasManager.getAssocAlias(relinfoA.nearType, relinfoA.fieldName, assocTbl);
		String s = aliasManager.buildFieldAlias(aliasInfo, fieldName);
		s = String.format("%s as %s", s, relinfoA.fieldName);
		addField(fieldL, null, fieldName, s).isAssocField = true;
	}

	private void throwNotAllowed() {
		DeliaExceptionHelper.throwError("unsupported-in-interface", "This method not allowed to be called in this subclass");
	}
	@Override
	public void addFullofJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL) {
		List<String> joinL = genFullJoinList(hlspan);

		for(JTElement el: hlspan.joinTreeL) {
			if (joinL.contains(el.fieldName)) {
				TypePair pair = el.createPair();
				addStructFieldsMM(hlspan, pair, fieldL);
			}
		}
	}
	private List<String> genFullJoinList(HLSQuerySpan hlspan) {
		List<String> joinL = new ArrayList<>();

		boolean needJoin = hlspan.subEl != null;
		if (! needJoin) {
			return joinL;
		}

		for (String fieldName: hlspan.subEl.fetchL) {
			if (joinL.contains(fieldName)) {
				continue;
			}
			joinL.add(fieldName);
		}

		//TODO: later support fk(field)
		return joinL;
	}
	
	
	@Override
	public void addStructFieldsMM(HLSQuerySpan hlspan, TypePair joinPair, List<RenderedField> fieldL) {
		DStructType joinType = (DStructType) joinPair.type;
		String pk = joinType.getPrimaryKey().getFieldName();

		//			AliasInstance aliasInst = aliasAlloc.findAliasFor(fromType);
		AliasInfo aliasInfo = aliasManager.getFieldAlias(hlspan.fromType, joinPair.name);
		for(TypePair pair: joinType.getAllFields()) {
			if (pair.name.equals(pk)) {
				//					String s = aliasAlloc.buildAlias(aliasInst, pair.name);
				String s = aliasManager.buildFieldAlias(aliasInfo, pair.name);
				s = String.format("%s as %s", s, joinPair.name);
				addField(fieldL, joinType, pair, s);
				//					fieldL.add(s);
			} else if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(joinType, pair);
				if (RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
				} else if (!relinfo.isParent) {
					//						String s = aliasAlloc.buildAlias(aliasInst, pair.name);
					String s = aliasManager.buildFieldAlias(aliasInfo, pair.name);
					addField(fieldL, joinType, pair, s);
				}
			} else {
				//					String s = aliasAlloc.buildAlias(aliasInst, pair.name);
				String s = aliasManager.buildFieldAlias(aliasInfo, pair.name);
				addField(fieldL, joinType, pair, s);
			}
		}
	}
	@Override
	public void addStructFields(DStructType fromType, List<RenderedField> fieldL) {
		AliasInfo info = aliasManager.getMainTableAlias(fromType);

		for(TypePair pair: fromType.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fromType, pair);
				if (RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
					doManyToManyAddFKofJoins(fieldL, pair, relinfo.otherSide);
				} else if (!relinfo.isParent) {
					//						addField(fieldL, fromType, pair, aliasAlloc.buildAlias(fromType, pair.name));
					addField(fieldL, fromType, pair, aliasManager.buildFieldAlias(info, pair.name));
				}
			} else {
				addField(fieldL, fromType, pair, aliasManager.buildFieldAlias(info, pair.name));
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

	@Override
	public List<TypePair> genTwoStatementJoinList(HLSQuerySpan hlspan1, HLSQuerySpan hlspan2, SQLCreator sc) {
		throwNotAllowed();
		return null;
//		List<TypePair> joinL = new ArrayList<>();
//
//		boolean needJoin = true; //hlspan.subEl != null;
//		if (! needJoin) {
//			return joinL;
//		}
//
//
//		//1 is address
//		//2 is customer
//
//		String targetTypeName = hlspan2.fromType.getName();
//		for(TypePair pair: hlspan1.fromType.getAllFields()) {
//			if (pair.type.isStructShape()) {
//				if (pair.type.getName().equals(targetTypeName)) {
//
//					RelationInfo relinfoA = DRuleHelper.findMatchingRuleInfo(hlspan1.fromType, pair);
//					if (RelationCardinality.MANY_TO_MANY.equals(relinfoA.cardinality)) {
//						TypePair tmp = new TypePair("xx", relinfoA.farType);
//						doManyToMany(sc, hlspan1, tmp, relinfoA, pair);
//
//						joinL.add(pair);
//					}
//				}
//			}
//		}
//		return joinL;
	}

	@Override
	public boolean supportsAddAllJoins() {
		return false; //TODO: supportsAddAllJoins not needed anymore. always false
	}

	@Override
	public void addAllJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL) {
		throwNotAllowed();
//		List<JTElement> elL = hlspan.joinTreeL;
//
//		for(JTElement el: elL) {
//			TypePair pair = el.createPair();
//			addStructFieldsMM(hlspan, pair, fieldL);
//		}
	}
}