package org.delia.db.newhls;

import java.util.List;
import java.util.stream.Collectors;

import org.delia.db.hls.AliasInfo;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.newhls.cond.SymbolChain;
import org.delia.type.DType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

/**
 * Final step of creating HLDQuery is assigning aliases
 * @author ian
 *
 */
public class HLDAliasBuilder {
	private HLDAliasManager aliasMgr;

	public HLDAliasBuilder(HLDAliasManager aliasMgr) {
		this.aliasMgr = aliasMgr;
	}

	public void assignAliases(HLDQuery hld) {
		AliasInfo info = aliasMgr.createMainTableAlias(hld.fromType);
		hld.fromAlias = info.alias;
		for(HLDField rf: hld.fieldL) {
			if (rf.structType == hld.fromType) {
				rf.alias = info.alias;
			} else {
				if (rf.source instanceof JoinElement) {
					JoinElement el = (JoinElement) rf.source;
					if (el.aliasName == null) {
						AliasInfo info2 = aliasMgr.createFieldAlias(el.relationField.dtype, el.relationField.fieldName);
						el.aliasName = info2.alias;
						info2 = aliasMgr.createMainTableAlias(el.relationField.dtype);
						el.srcAlias = info2.alias;
						//TODO:this needs to be smarter. self-joins,multiple addr fields, etc
						//need to determine which instance of Customer this is!!
					}
					
					//need 2nd alias if M:M and a fetch
					if (el.relinfo.isManyToMany() && el.fetchSpec != null && !el.fetchSpec.isFK) {
						AliasInfo infoAdd = aliasMgr.createOrGetFieldAliasAdditional(el.relationField.dtype, el.relationField.fieldName);
						el.aliasNameAdditional = infoAdd.alias;
						rf.alias = el.aliasNameAdditional;
					} else {
						rf.alias = el.aliasName;
					}

				}
				//TODO!!
			}
		}

		//now populate SYMBOL FilterdVal
		if (hld.filter instanceof SingleFilterCond) {
			SingleFilterCond sfc = (SingleFilterCond) hld.filter;
			doFilterPKVal(sfc.val1, hld);
		} else if (hld.filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) hld.filter;
			doFilterVal(ofc.val1, hld);
			doFilterVal(ofc.val2, hld);
		}
		
		//now do any other implicit joins
		for(JoinElement el: hld.joinL) {
			if (el.aliasName == null) {
				AliasInfo info2 = aliasMgr.createFieldAlias(el.relationField.dtype, el.relationField.fieldName);
				el.aliasName = info2.alias;
				info2 = aliasMgr.createMainTableAlias(el.relationField.dtype); //TODO fix later
				el.srcAlias = info2.alias;
				
				List<HLDField> fields = hld.fieldL.stream().filter(x -> x.source == el).collect(Collectors.toList());
				for(HLDField fld: fields) {
					fld.alias = info2.alias;
				}
			}
		}
		
		//and propogate alias to query fns
		for(QueryFnSpec fnspec: hld.funcL) {
			JoinElement el = findMatch(fnspec, hld);
			if (el != null) {
				if (el.relinfo.isParent) {
					fnspec.structField.alias = el.aliasName;
				} else if (el.relationField.dtype == hld.fromType) {
					fnspec.structField.alias = hld.fromAlias;
				} else {
					fnspec.structField.alias = el.aliasName;
				}
			} else {
				fnspec.structField.alias = hld.fromAlias;
			}
		}
	}

	private JoinElement findMatch(QueryFnSpec fnspec, HLDQuery hld) {
		if (fnspec.structField.fieldName == null) {
			return null;
		}
		for(JoinElement el: hld.joinL) {
			if (fnspec.structField.fieldName.equals(el.relationField.fieldName) && 
					fnspec.structField.dtype == el.relationField.dtype) {
				return el;
			}
		}
		return null;
	}

	private void doFilterVal(FilterVal val1, HLDQuery hld) {
		if (val1.isSymbol()) {
			String fieldName = val1.exp.strValue();
			DType fieldType = DValueHelper.findFieldType(hld.fromType, fieldName);
			val1.structField = new StructField(hld.fromType, fieldName, fieldType);
			val1.alias = hld.fromAlias;
		} else if (val1.isSymbolChain()) {
			String fieldName = val1.exp.strValue();
			SymbolChain chain = val1.asSymbolChain();
			DType fieldType = DValueHelper.findFieldType(chain.fromType, fieldName);
			val1.structField = new StructField(chain.fromType, fieldName, fieldType);
			AliasInfo info = aliasMgr.createFieldAlias(chain.fromType, fieldName);
			val1.alias = info.alias;
			JoinElement el = hld.findMatch(chain.fromType, fieldName, hld);
			if (el != null && el.aliasName == null) {
				el.aliasName = info.alias;
				info = aliasMgr.createMainTableAlias(el.relationField.dtype); //TODO fix later
				el.srcAlias = info.alias;
			}
		}
	}
	private void doFilterPKVal(FilterVal val1, HLDQuery hld) {
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(hld.fromType);
		String fieldName = pkpair.name;
		val1.structField = new StructField(hld.fromType, fieldName, pkpair.type);
		val1.alias = hld.fromAlias;
	}

}