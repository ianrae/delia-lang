package org.delia.db.newhls;

import java.util.List;
import java.util.stream.Collectors;

import org.delia.assoc.DatIdMap;
import org.delia.db.hls.AliasInfo;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.InFilterCond;
import org.delia.db.newhls.cond.OpAndOrFilter;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.newhls.cond.SymbolChain;
import org.delia.db.newhls.cud.HLDBase;
import org.delia.db.newhls.cud.HLDDelete;
import org.delia.db.newhls.cud.HLDInsert;
import org.delia.db.newhls.cud.HLDUpdate;
import org.delia.db.newhls.simple.SimpleBase;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

/**
 * Final step of creating HLDQuery is assigning aliases.
 * This classes also fills in struct info on HLDFields.
 * Some SQL (like INSERT) don't like aliases so we have an outputAliases flag
 * @author ian
 *
 */
public class HLDAliasBuilder implements HLDAliasBuilderAdapter {
	private HLDAliasManager aliasMgr;
	private boolean outputAliases = true; //used to disable aliases
	private ConversionHelper conversionHelper;
	private HLDAliasHelper aliasHelper;
	private DatIdMap datIdMap;
	private DTypeRegistry registry;

	public HLDAliasBuilder(HLDAliasManager aliasMgr, ConversionHelper helper, DatIdMap datIdMap, DTypeRegistry registry) {
		this.aliasMgr = aliasMgr;
		this.conversionHelper = helper;
		this.aliasHelper = new HLDAliasHelper(aliasMgr);
		this.datIdMap = datIdMap;
		this.registry = registry;
	}
	
	@Override
	public void assignAliases(SimpleBase simple) {
		simple.assignAliases(this);
	}

	@Override
	public void assignAliases(HLDQuery hld) {
		AliasInfo info = aliasMgr.createMainTableAlias(hld.fromType);
		hld.fromAlias = assign(info.alias);
		doFieldList(hld.fieldL, hld.fromType, info);

		//now populate SYMBOL FilterdVal
		doFilter(hld);
		
		//now do any other implicit joins
		for(JoinElement el: hld.joinL) {
			if (el.aliasName == null) {
				AliasInfo info2 = aliasMgr.createFieldAlias(el.relationField.dtype, el.relationField.fieldName);
				el.aliasName = assign(info2.alias);
				info2 = aliasMgr.createMainTableAlias(el.relationField.dtype); //TODO fix later
				el.srcAlias = assign(info2.alias);
				
				List<HLDField> fields = hld.fieldL.stream().filter(x -> x.source == el).collect(Collectors.toList());
				for(HLDField fld: fields) {
					fld.alias = assign(info2.alias);
				}
			}
		}
		
		//and propogate alias to query fns
		for(QueryFnSpec fnspec: hld.funcL) {
			TypePair pair = DValueHelper.findField(fnspec.structField.dtype, fnspec.structField.fieldName);
			if (pair == null) {
				//fns that only work with fields: min, max
				//fns that work on types: count, exists, distinct, orderBy, limit, offset, first,last,ith
				if (fnspec.isFn("min") || fnspec.isFn("max")) {
					DeliaExceptionHelper.throwUnknownFieldError(fnspec.structField.dtype.getName(), fnspec.structField.fieldName);
				}
			}
			
			JoinElement el = findMatch(fnspec, hld);
			if (el != null) {
				if (el.relinfo.notContainsFKOrIsManyToMany()) {
					fnspec.structField.alias = assign(el.aliasName);
				} else if (el.relationField.dtype == hld.fromType) {
					fnspec.structField.alias = assign(hld.fromAlias);
				} else {
					fnspec.structField.alias = assign(el.aliasName);
				}
			} else {
				fnspec.structField.alias = assign(hld.fromAlias);
			}
		}
	}

	private String assign(String alias) {
		return outputAliases ? alias : null;
	}

	private void doFilter(HLDQuery hld) {
		doInnerFilter(hld.filter, hld, registry);
	}
	private void doInnerFilter(FilterCond filter, HLDQuery hld, DTypeRegistry registry) {
		if (filter instanceof SingleFilterCond) {
			SingleFilterCond sfc = (SingleFilterCond) filter;
			doFilterPKVal(sfc.val1, hld);
		} else if (filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) filter;
			if (ofc.customRenderer != null) {
				ofc.customRenderer.assignAliases(ofc, hld, (HLDAliasBuilderAdapter) this);
			}
			doFilterVal(ofc.val1, hld, registry);
			doFilterVal(ofc.val2, hld, registry);
			convertValueIfNeeded(ofc.val1, ofc.val2);
		} else if (filter instanceof OpAndOrFilter) {
			OpAndOrFilter ofc = (OpAndOrFilter) filter;
			doInnerFilter(ofc.cond1, hld, registry); //** recursion **
			doInnerFilter(ofc.cond2, hld, registry); //** recursion **
		} else if (filter instanceof InFilterCond) {
			InFilterCond ifc = (InFilterCond)filter;
			doFilterVal(ifc.val1, hld);
			for(FilterVal fval: ifc.list) {
				doFilterVal(fval, hld);
				convertValueIfNeeded(ifc.val1, fval);
			}
		}
	}
	
	private void adjustAssocFilter(HLDUpdate hld) {
		RelationInfo relinfo = hld.assocRelInfo;
		FilterCond filter = hld.hld.filter;
		doUpdateFilter(filter, hld, relinfo);
	}
	private void doUpdateFilter(FilterCond filter, HLDUpdate hld, RelationInfo relinfo) {
		if (filter instanceof SingleFilterCond) {
			SingleFilterCond sfc = (SingleFilterCond) filter;
			if (sfc.val1.structField != null) {
				TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(sfc.val1.structField.dtype);
				if (pkpair != null && pkpair.name.equals(sfc.val1.structField.fieldName)) {
					String fld1 = datIdMap.getAssocFieldFor(relinfo);
					//replace t0.cid with t1.leftv
					sfc.val1.structField = new StructField(relinfo.nearType, fld1, pkpair.type); //TODO: nearType is wrong WRONG!
					sfc.val1.alias = hld.typeOrTbl.getAlias();
				}
			}
		} else if (filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) filter;
			if (ofc.val1.isSymbol()) {
				doSpecialMMFieldVal(ofc.val1, hld, relinfo);
			}
			if (ofc.val2.isSymbol()) {
				doSpecialMMFieldVal(ofc.val2, hld, relinfo);
			}
		} else if (filter instanceof OpAndOrFilter) {
			OpAndOrFilter opfilter = (OpAndOrFilter) filter;
			doOrThrow(opfilter.cond1, hld, relinfo);
			doOrThrow(opfilter.cond2, hld, relinfo);
		} else if (filter instanceof InFilterCond) {
			DeliaExceptionHelper.throwNotImplementedError("arg");
		}
	}

	private void doSpecialMMFieldVal(FilterVal val1, HLDUpdate hld, RelationInfo relinfo) {
		String fieldName = val1.asSymbol();
		String fld1 = this.datIdMap.getAssocFieldFor(relinfo);
		String fld2 = this.datIdMap.getAssocOtherField(relinfo);
		String assocTbl = this.datIdMap.getAssocTblName(relinfo.getDatId());
		if (fieldName.equals(fld1)) {
			AliasInfo info = aliasMgr.findAssocAlias(relinfo, assocTbl);
			if (info != null) {
				val1.alias = info.alias;
			}
		} else if (fieldName.equals(fld2)) {
			AliasInfo info = aliasMgr.findAssocAlias(relinfo, assocTbl);
			if (info != null) {
				val1.alias = info.alias;
			}
		}
	}

	private void doOrThrow(FilterCond cond1, HLDUpdate hld, RelationInfo relinfo) {
		if (cond1 instanceof OpFilterCond) {
			doUpdateFilter(cond1, hld, relinfo); //** recursion **
		} else {
			DeliaExceptionHelper.throwNotImplementedError("only ofc supported here.");
		}
	}

	private void doFieldList(List<HLDField> fieldL, DStructType fromType, AliasInfo info) {
		for(HLDField rf: fieldL) {
			if (rf.source instanceof JoinElement) {
				JoinElement el = (JoinElement) rf.source;
				String childAlias = null;
				if (el.aliasName == null) {
					//if rf is scalar or is struct and is parent
					if (rf.fieldType.isScalarShape() || ! el.relinfo.containsFK()) {
						AliasInfo info2 = aliasMgr.createFieldAlias(el.relationField.dtype, el.relationField.fieldName);
						el.aliasName = assign(info2.alias);
						info2 = aliasMgr.createMainTableAlias(el.relationField.dtype);
						el.srcAlias = assign(info2.alias);
						//TODO:this needs to be smarter. self-joins,multiple addr fields, etc
						//need to determine which instance of Customer this is!!
					} else if (el.relinfo.containsFK()) {
						AliasInfo info2 = aliasMgr.createMainTableAlias(el.relationField.dtype);
						childAlias = assign(info2.alias);
					}
				}

				//need 2nd alias if M:M and a fetch
				if (el.relinfo.isManyToMany() && el.usedForFetch()) {
					AliasInfo infoAdd = aliasMgr.createOrGetFieldAliasAdditional(el.relationField.dtype, el.relationField.fieldName);
					el.aliasNameAdditional = assign(infoAdd.alias);
					rf.alias = assign(el.aliasNameAdditional);
				} else {
					rf.alias = childAlias != null ? childAlias : assign(el.aliasName);
			 	}
			} else {
				if (rf.structType == fromType) {
					rf.alias = assign(info.alias);
				}
				//TODO: error if else?
			}
		}
	}
	private void doFieldListAssoc(List<HLDField> fieldL, AliasInfo info) {
		for(HLDField rf: fieldL) {
			rf.alias = assign(info.alias);
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
		doFilterVal(val1, hld, null);
	}
	private void doFilterVal(FilterVal val1, HLDQuery hld, DTypeRegistry registry) {
		//registry only needed for update DAT when filter on main type
		if (val1.isSymbol()) {
			//when DAT actions we've already filled in structType
			String alias = aliasHelper.populateStructField(val1, hld, hld.fromType, registry);
			val1.alias = assign(alias);
		} else if (val1.isSymbolChain()) {
			String fieldName = val1.exp.strValue();
			SymbolChain chain = val1.asSymbolChain();
			
			String alias = aliasHelper.populateStructField(val1, hld, chain.fromType, registry);
			//val1.structField = buildStructType(chain.fromType, fieldName, hack);
			AliasInfo info = aliasMgr.createFieldAlias(chain.fromType, fieldName);
			val1.alias = assign(info.alias);
			JoinElement el = hld.findMatch(chain.fromType, fieldName, hld);
			if (el != null && el.aliasName == null) {
				el.aliasName = assign(info.alias);
				info = aliasMgr.createMainTableAlias(el.relationField.dtype); //TODO fix later
				el.srcAlias = assign(info.alias);
				
				if (el.relinfo.isManyToMany()) {
					AliasInfo infoAdd = aliasMgr.createOrGetFieldAliasAdditional(el.relationField.dtype, el.relationField.fieldName);
					el.aliasNameAdditional = assign(infoAdd.alias);
				}						
			}
//			convertIfNeeded(val1);
		}
	}

	private void doFilterPKVal(FilterVal val1, HLDQuery hld) {
		if (val1.isBoolean()) {
			return;
		}
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(hld.fromType);
		String fieldName = pkpair.name;
		val1.structField = new StructField(hld.fromType, fieldName, pkpair.type);
		val1.alias = assign(hld.fromAlias);
//		convertIfNeeded(val1);
		doConvertValueIfNeeded(val1, pkpair.type);
	}
	
//	private void convertIfNeeded(FilterVal val1) {
//		if (val1.structField.fieldType != null) {
//			if (val1.structField.fieldType.isShape(Shape.DATE)) {
//				System.out.println("sunny..");
//			}
//		}
//	}
	private void convertValueIfNeeded(FilterVal val1, FilterVal val2) {
		//TODO. if we ever support defining date values as long, then add LONG support here
		if (val1.isSymbol()) {
			doConvertValueIfNeeded(val2, val1.structField.fieldType);
		} else if (val2.isSymbol()) {
			doConvertValueIfNeeded(val1, val2.structField.fieldType);
		}
	}
	private void doConvertValueIfNeeded(FilterVal val2, DType fieldType) {
		//TODO. if we ever support defining date values as long, then add LONG support here
		if (val2.valType.equals(ValType.STRING)) {
			System.out.println("sunny2..");
			val2.actualDateVal = conversionHelper.convertDValToActual(fieldType, val2.asString());
		}
	}


	@Override
	public void assignAliases(HLDInsert hld) {
		if (hld.typeOrTbl.isAssocTbl) {
			assignAliasesAssoc(hld);
			return;
		}
		AliasInfo info = aliasMgr.createMainTableAlias(hld.getStructType());
		hld.typeOrTbl.alias = assign(info.alias);
		doFieldList(hld.fieldL, hld.getStructType(), info);
	}
	public void assignAliasesAssoc(HLDInsert hld) {
		AliasInfo info = doAssignAliasesAssoc(hld);
		doFieldListAssoc(hld.fieldL, info);
	}
	public void assignAliasesAssoc(HLDUpdate hld) {
		AliasInfo info = doAssignAliasesAssoc(hld);
		doFieldListAssoc(hld.fieldL, info);
		hld.hld.fromAlias = assign(info.alias);
		doFilter(hld.hld);
		adjustAssocFilter(hld);
	}
	public AliasInfo assignAliasesAssoc(HLDDelete hld) {
		AliasInfo info = doAssignAliasesAssoc(hld);
		hld.hld.fromAlias = assign(info.alias);
		doFilter(hld.hld);
		return info;
	}
	private AliasInfo doAssignAliasesAssoc(HLDBase hld) {
		RelationInfo relinfo = hld.assocRelInfo;
		String assocTbl = aliasMgr.getDatIdMap().getAssocTblName(relinfo.getDatId());
		AliasInfo info = aliasMgr.createAssocAlias(relinfo.nearType, relinfo.fieldName, assocTbl);
		hld.typeOrTbl.alias = assign(info.alias);
		return info;
	}
	
	@Override
	public void assignAliases(HLDUpdate hld) {
		AliasInfo info = aliasMgr.createMainTableAlias(hld.getStructType());
		hld.typeOrTbl.alias = assign(info.alias);
		hld.hld.fromAlias = assign(info.alias);
		doFieldList(hld.fieldL, hld.getStructType(), info);
		
		//now populate SYMBOL FilterdVal
		doFilter(hld.hld);
	}
	@Override
	public void assignAliases(HLDDelete hld) {
		AliasInfo info;
		if (hld.typeOrTbl.isAssocTbl) {
			info = assignAliasesAssoc(hld);
		} else {
			info = aliasMgr.createMainTableAlias(hld.getStructType());
			hld.typeOrTbl.alias = assign(info.alias);
			hld.hld.fromAlias = assign(info.alias);
			
			//now populate SYMBOL FilterdVal
			doFilter(hld.hld);
		}
	}

	@Override
	public void pushAliasScope(String scope) {
		aliasMgr.pushScope(scope);
	}

	@Override
	public void popAliasScope() {
		aliasMgr.popScope();
	}

	@Override
	public String createAlias() {
		return aliasMgr.createAlias();
	}

	@Override
	public boolean isOutputAliases() {
		return outputAliases;
	}

	@Override
	public void setOutputAliases(boolean outputAliases) {
		this.outputAliases = outputAliases;
	}

}