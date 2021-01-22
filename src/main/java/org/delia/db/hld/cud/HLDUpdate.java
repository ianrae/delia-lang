package org.delia.db.hld.cud;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.QuerySpec;
import org.delia.db.hld.HLDField;
import org.delia.db.hld.HLDQuery;
import org.delia.runner.ConversionResult;
import org.delia.type.DValue;

public class HLDUpdate extends HLDBase {
	public HLDQuery hld;
	public ConversionResult cres;
	public QuerySpec querySpec;
    public List<HLDField> fieldL = new ArrayList<>();
    public List<DValue> valueL = new ArrayList<>();
	public boolean isMergeInto;
	public boolean isMergeAllInto;
	public String mergeKey;
	public String mergeType;
	public String mergePKField;
	public String mergeKeyOther;
	public boolean isMergeCTE;
	public DValue dvalCTE;
	public boolean isSubSelect;

	public HLDUpdate(TypeOrTable typeOrTbl, HLDQuery hld) {
		super(typeOrTbl);
		this.hld = hld;
	}
	
	/**
	 * Return true if statement will do nothing (no need to execute on DB)
	 * @return
	 */
	public boolean isEmpty() {
		return fieldL.isEmpty();
	}
	

	@Override
	public String toString() {
		return hld == null ? "" : hld.toString();
	}
}