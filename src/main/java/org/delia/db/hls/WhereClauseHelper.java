package org.delia.db.hls;

import java.util.Map;
import java.util.Optional;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.hls.join.JTElement;
import org.delia.db.sql.fragment.AliasCreator;
import org.delia.db.sql.fragment.AliasedFragment;
import org.delia.db.sql.fragment.FieldFragment;
import org.delia.db.sql.fragment.MiniSelectFragmentParser;
import org.delia.db.sql.fragment.OpFragment;
import org.delia.db.sql.fragment.SelectStatementFragment;
import org.delia.db.sql.fragment.SqlFragment;
import org.delia.db.sql.fragment.TableFragment;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.relation.RelationInfo;
import org.delia.runner.FilterEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;

public class WhereClauseHelper extends ServiceBase implements AliasCreator {

	private MiniSelectFragmentParser miniSelectParser;
	private VarEvaluator varEvaluator;
	public Map<String,String> asNameMap;
	private AliasManager aliasManager;
//	private String realAlias;
	private DatIdMap datIdMap;
	private HLSQuerySpan hlspanForAliasCreator;
	
	public WhereClauseHelper(FactoryService factorySvc, MiniSelectFragmentParser miniSelectParser, VarEvaluator varEvaluator, 
				Map<String, String> asNameMap, AliasManager aliasManager, DatIdMap datIdMap) {
		super(factorySvc);
		this.miniSelectParser = miniSelectParser;
		this.miniSelectParser.setAliasCreator(this);
		this.varEvaluator = varEvaluator;
		this.asNameMap = asNameMap;
		this.aliasManager = aliasManager;
		this.datIdMap = datIdMap;
	}

	public void genWhere(HLSQuerySpan hlspan) {
		if (hlspan.filEl ==  null) {
			return;
		}
		QuerySpec spec = new QuerySpec();
		spec.queryExp = hlspan.filEl.queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		QueryDetails details = new QueryDetails();
		
		this.hlspanForAliasCreator = hlspan; //TODO: is this thread-safe?? (i think so: only one thread will use this object at a time)
		SelectStatementFragment selectFrag = miniSelectParser.parseSelect(spec, details);
		
		//now do adjustment
		for(SqlFragment z: selectFrag.whereL) {
			OpFragment op = (OpFragment) z;
			if (op.left != null) {
				if (handleMMBackwardsRef(op.left, selectFrag, hlspan)) {
					
				} else if (!remapParentFieldIfNeeded(op.left, selectFrag)) {
//					if (op.left.alias != null) {
//						op.left.alias = aliasInfo.alias;
//					}
				}
			}
			if (op.right != null) {
				if (!remapParentFieldIfNeeded(op.right, selectFrag)) {
//					if (op.right.alias != null) {
//						op.right.alias = aliasInfo.alias;
//					}
				}
			}
		}
		
		String whereSql = miniSelectParser.renderSelectWherePartOnly(selectFrag);
		
		hlspan.finalWhereSql = "";
		if (!selectFrag.whereL.isEmpty()) {
			SqlStatement statement = selectFrag.statement;
			hlspan.paramL = statement.paramL;
			hlspan.finalWhereSql = whereSql; 
		}
	}
	
	private boolean handleMMBackwardsRef(AliasedFragment af, SelectStatementFragment selectFrag, HLSQuerySpan hlspan) {
		if (af instanceof FieldFragment) {
			FieldFragment ff = (FieldFragment) af;
			String pk = ff.structType.getPrimaryKey().getFieldName();
			if (ff.name.equals(pk)) {
				if (hlspan.fromType != hlspan.mainStructType) {
					Optional<JTElement> el = hlspan.joinTreeL.stream().filter(x -> x.fieldType == hlspan.fromType).findAny();
					if (el.isPresent()) {
						ff.fieldType = ff.structType.getPrimaryKey().getKeyType();
						
						RelationInfo relinfo = el.get().relinfo;
						if (relinfo.isManyToMany()) {
							String assocTable = datIdMap.getAssocTblName(relinfo.getDatId()); 
							ff.name = datIdMap.getAssocFieldFor(relinfo);
							AliasInfo aliasInfo = aliasManager.getAssocAlias(relinfo.nearType, relinfo.fieldName, assocTable);
							ff.alias = aliasInfo.alias;
							return true;
						}
					}
				}
			}
			
		} 
		return false;
	}

	private boolean remapParentFieldIfNeeded(AliasedFragment af, SelectStatementFragment selectFrag) {
		if (af instanceof FieldFragment) {
			FieldFragment ff = (FieldFragment) af;
			return remapParentFieldIfNeeded(ff, af);
		} else {
			for (FieldFragment ff: selectFrag.hlsRemapList) {
				if (ff.alias.equals(af.alias) && ff.name.equals(af.name)) {
					return remapParentFieldIfNeeded(ff, af);
				}
			}
		}
		return false;
	}
	private boolean remapParentFieldIfNeeded(FieldFragment ff, AliasedFragment af) {
		TypePair pair = new TypePair(ff.name, ff.fieldType);
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(ff.structType, pair);
		if (relinfo == null) {
			return false;
		} else if (relinfo.isManyToMany()) {
			String assocTbl = datIdMap.getAssocTblName(relinfo.getDatId());
			
			af.name = datIdMap.getAssocOtherField(relinfo); //this is the original version
//			boolean flipped = datIdMap.isFlipped(relinfo);
//			af.name = flipped ?  datIdMap.getAssocFieldFor(relinfo) : datIdMap.getAssocOtherField(relinfo);
			AliasInfo aliasInfo = aliasManager.getAssocAlias(ff.structType, pair.name, assocTbl);
			af.alias = aliasInfo.alias; 
			
			return true;

		} else if (relinfo.isParent) {
			RelationInfo otherSide = relinfo.otherSide; 
			if (otherSide != null) {
				af.name = relinfo.farType.getPrimaryKey().getFieldName();
				AliasInfo aliasInfo = aliasManager.getFieldAlias(relinfo.nearType, pair.name);
				af.alias = aliasInfo.alias; 

				String key = String.format("%s.%s", af.alias, af.name);
				asNameMap.put(key, relinfo.fieldName);
				return true;
			}
		}
		return false;
	}

	@Override
	public void fillInAlias(TableFragment tblFrag) {
		HLSQuerySpan hlspan = hlspanForAliasCreator;
		
		if (tblFrag.structType == null) {
			throw new RuntimeException("fillInAlias does not support assoc alias. fix!!!");
			//TODO: somehow we need the fieldName here
//			AliasInfo info = aliasManager.getAssocAlias(hlspan.mainStructType, fieldName, tblFrag.name);
//			tblFrag.alias = info.alias;
		} else {
			AliasInfo info = aliasManager.getMainTableAlias(hlspan.mainStructType);
			if (info == null) {
				info = aliasManager.findAlias(hlspan.mainStructType);
			}
			tblFrag.alias = info.alias;
		}
	}
}