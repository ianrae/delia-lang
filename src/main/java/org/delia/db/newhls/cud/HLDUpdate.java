package org.delia.db.newhls.cud;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.QuerySpec;
import org.delia.db.newhls.HLDField;
import org.delia.db.newhls.HLDQuery;
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

	public HLDUpdate(TypeOrTable typeOrTbl, HLDQuery hld) {
		super(typeOrTbl);
		this.hld = hld;
	}

	@Override
	public String toString() {
		return hld == null ? "" : hld.toString();
	}
}