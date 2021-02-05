package org.delia.db.schema.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.delia.core.FactoryService;
import org.delia.core.RegAwareServiceBase;
import org.delia.type.DTypeRegistry;
import org.delia.util.StringUtil;

/**
 * Compares two schemas and produces a delta object, listing all the
 * changes to fields, types, constraints, and indexes.
 * 
 * @author ian
 *
 */
public class SchemaDeltaGeneratorOther extends RegAwareServiceBase {

	public SchemaDeltaGeneratorOther(DTypeRegistry registry, FactoryService factorySvc) {
		super(registry, factorySvc);
	}

	public void handleOthers(SchemaDefinition schema1, SchemaDefinition schema2, SchemaDelta delta) {

		List<SxOtherInfo> list2 = new ArrayList<>(schema2.others);
		for(SxOtherInfo tt: schema1.others) {
			SxOtherInfo tt2 = findIn(tt, schema2);
			if (tt2 != null) {
				SxOtherDelta td = buildOtherDelta(tt);
				int n = diffFields(tt, tt2, delta, td);
				if (n > 0) {
					list2.remove(tt2);
					td.newArgs = tt2.args;
					delta.othersU.add(td);
				}
				list2.remove(tt2);
			} else {
				SxOtherDelta td = buildOtherDelta(tt);
				delta.othersD.add(td); //in list1 but not in list2
			}
		}

		for(SxOtherInfo tt: list2) {
			SxOtherDelta td = buildOtherDelta(tt);
			td.newArgs = tt.args;
			delta.othersI.add(td);
		}
	}

	private SxOtherDelta buildOtherDelta(SxOtherInfo tt) {
		SxOtherDelta otherDelta = new SxOtherDelta(tt.ct);
		otherDelta.info = tt;
		otherDelta.typeName = tt.nm;
		return otherDelta;
	}

	private SxOtherInfo findIn(SxOtherInfo tt, SchemaDefinition schema2) {
		Optional<SxOtherInfo> opt = schema2.others.stream().filter(x -> x.ct.equals(tt.ct)).findAny();
		return opt.orElse(null);
	}

	private int diffFields(SxOtherInfo tt, SxOtherInfo tt2, SchemaDelta delta, SxOtherDelta td) {
		String s1 = StringUtil.flatten(tt.args);
		String s2 = StringUtil.flatten(tt2.args);
		if (s1.equals(s2)) {
			return 0;
		}
		return 1;
	}

}