package org.delia.repl;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.DeliaSession;
import org.delia.runner.ResultValue;
import org.delia.type.BuiltInTypes;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public class ListTypesCmd extends CmdBase {
	public ListTypesCmd() {
		super("list types", "lt");
		expectSpace = false;
	}
	public ListTypesCmd(ListTypesCmd obj) {
		super(obj);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new ListTypesCmd(this);
			cmd.cmd = name;
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		DeliaSession sess = runner.getMostRecentSess();
		if (sess == null) {
			log("no types have been defined.");
			return createEmptyRes();
		}
		DTypeRegistry registry = sess.getExecutionContext().registry;

		List<DType> list = getAllTypes(registry);
		String s = String.format("Number of types: %d", list.size());
		log(s);
		for(DType dtype: list) {
			DType baseType = dtype.getBaseType();
			String base = baseType == null ? "" : baseType.getName();
			s = String.format("  %s %s", dtype.getName(), base);
			log(s);
		}

		ResultValue res = new ResultValue();
		res.ok = true;
		return res;
	}

	private List<DType> getAllTypes(DTypeRegistry registry) {
		List<DType> list = new ArrayList<>();
		for(DType dtype: registry.getOrderedList()) {
			String typeName = BuiltInTypes.convertDTypeNameToDeliaName(dtype.getName());
			if (BuiltInTypes.isBuiltInScalarType(typeName) || typeName.equals("relation")) {
			} else {
				list.add(dtype);
			}
		}
		return list;
	}
}