package org.delia.db.newhls;

import org.delia.db.hls.AliasInfo;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

/**
 * @author ian
 *
 */
public class HLDAliasHelper {
	private HLDAliasManager aliasMgr;

	public HLDAliasHelper(HLDAliasManager aliasMgr) {
		this.aliasMgr = aliasMgr;
	}
	
	//TODO: rewrite this later
	private static class HackHack {
		public boolean isFlipped = false;
		public RelationInfo relinfo;
	}

	public String populateStructField(FilterVal val1, HLDQuery hld, DStructType fromType) {
		//when DAT actions we've already filled in structType
		HackHack hack = new HackHack();
		if (val1.structField == null) {
			String fieldName = val1.exp.strValue();
			val1.structField = buildStructType(fromType, fieldName, hack);
		} 

		String alias = hld.fromAlias;
		if (hack.isFlipped) {
			AliasInfo info = aliasMgr.createFieldAlias(hack.relinfo);
			alias = info.alias;
		}
		return alias;
	}
	private StructField buildStructType(DStructType fromType, String fieldName, HackHack hack) {
		DType fieldType = DValueHelper.findFieldType(fromType, fieldName);
		if (fieldType.isStructShape()) {
			RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fromType, new TypePair(fieldName, fieldType));
			hack.relinfo = relinfo;
			if (relinfo.isManyToMany()) {
				
			} else if (relinfo.isParent) {
				hack.isFlipped = true;
				//Customer.addr doesn't exist in db. change to Address.id
				TypePair pair = DValueHelper.findPrimaryKeyFieldPair(relinfo.farType);
				return new StructField(relinfo.farType, pair.name, pair.type);
			}
		}
		
		return new StructField(fromType, fieldName, fieldType);
	}

}