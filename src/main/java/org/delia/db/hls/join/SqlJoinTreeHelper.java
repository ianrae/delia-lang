package org.delia.db.hls.join;

import java.util.*;
import java.util.stream.*;

import org.delia.assoc.DatIdMap;
import org.delia.db.QueryDetails;
import org.delia.db.hls.*;
import org.delia.db.sql.fragment.MiniSelectFragmentParser;
import org.delia.relation.*;
import org.delia.type.*;
import org.delia.util.*;

public class SqlJoinTreeHelper implements SqlJoinHelper {
	private AliasManager aliasManager;
	private DatIdMap datIdMap;

	public SqlJoinTreeHelper(AliasManager aliasManager, DatIdMap datIdMap, Map<String, String> asNameMap, MiniSelectFragmentParser miniSelectParser) {
		this.aliasManager = aliasManager;
		this.datIdMap = datIdMap;
	}

	@Override
	public QueryDetails genJoin(SQLCreator sc, HLSQuerySpan hlspan) {
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

	private void doManyToMany(SQLCreator sc, HLSQuerySpan hlspan, TypePair pair, RelationInfo relinfoA, TypePair actualPair) {
		String s;
		PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
		String assocTable = datIdMap.getAssocTblName(relinfoA.getDatId()); 

		if (true) {
			AliasInfo aliasInfo = aliasManager.getAssocAlias(relinfoA.nearType, relinfoA.fieldName, assocTable);
			String tbl1 = aliasManager.buildTblAlias(aliasInfo);
			String on1 = buildMainAlias(hlspan, mainPk.getFieldName()); //b.cust
			String fff = datIdMap.getAssocFieldFor(relinfoA);
			String on2 = aliasManager.buildFieldAlias(aliasInfo, fff); //a.id

			s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
		}

		sc.out(s);
	}

	@Override
	public boolean needJoin(HLSQuerySpan hlspan) {
		return !hlspan.joinTreeL.isEmpty();
	}

	@Override
	public int addFKofJoins(HLSQuerySpan hlspan, List<RenderedField> fieldL) {
		List<JTElement> elL = hlspan.joinTreeL.stream().filter(x -> x.usedForFK).collect(Collectors.toList());

		int numAdded = 0;
		for(JTElement el: elL) {
			TypePair pair = el.createPair();
			DStructType pairType = (DStructType) pair.type;
			PrimaryKey pk = pairType.getPrimaryKey();

			RelationInfo relinfoA = el.relinfo; 
			switch(relinfoA.cardinality) {
			case ONE_TO_ONE:
			case ONE_TO_MANY:
				break;
			case MANY_TO_MANY:
				doManyToManyAddFKofJoins(fieldL, pair, relinfoA);
				numAdded++;
				continue;
			}

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
		String fieldName = datIdMap.getAssocFieldFor(relinfoA);

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

		AliasInfo aliasInfo = aliasManager.getFieldAlias(hlspan.fromType, joinPair.name);
		for(TypePair pair: joinType.getAllFields()) {
			if (pair.name.equals(pk)) {
				String s = aliasManager.buildFieldAlias(aliasInfo, pair.name);
				s = String.format("%s as %s", s, joinPair.name);
				addField(fieldL, joinType, pair, s);
			} else if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(joinType, pair);
				if (RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
				} else if (!relinfo.isParent) {
					String s = aliasManager.buildFieldAlias(aliasInfo, pair.name);
					addField(fieldL, joinType, pair, s);
				}
			} else {
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