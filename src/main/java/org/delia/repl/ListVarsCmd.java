package org.delia.repl;

import java.util.ArrayList;
import java.util.List;

import org.delia.DeliaSession;
import org.delia.runner.ResultValue;
import org.delia.type.BuiltInTypes;
import org.delia.type.DValue;

public class ListVarsCmd extends CmdBase {
	public ListVarsCmd() {
		super("list vars", "lv");
		expectSpace = false;
	}
	public ListVarsCmd(ListVarsCmd obj) {
		super(obj);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new ListVarsCmd(this);
			cmd.cmd = name;
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		DeliaSession sess = runner.getMostRecentSess();
		if (sess == null) {
			log("no vars have been defined.");
			return createEmptyRes();
		}

		List<String> list = getAllVars(sess);
		String s = String.format("Number of vars: %d", list.size());
		log(s);
		int maxLen = 5;
		for(String varName: list) {
			if (varName.length() > maxLen) {
				maxLen = varName.length();
			}
		}
		for(String varName: list) {
			ResultValue res = sess.getExecutionContext().varMap.get(varName);
			String type = "";
			String sss = "";
			List<DValue> dvals = res.getAsDValueList();
			if (dvals != null) {
				type = dvals.get(0).getType().getName();
				sss = dvals.size() > 1 ? String.format("%d values", dvals.size()) : "";
			} else if (res.shape != null) {
				Object obj = res.val;
				type = BuiltInTypes.getDeliaTypeNameFromShape(res.shape);
				if (obj instanceof DValue) {
					DValue dval = (DValue) obj;
					sss = dval.asString();
				} else {
					sss = obj == null ? "null" : obj.toString();
				}
			}
			
			String fmt = String.format("%d", maxLen);
			fmt = "  %-" + fmt + "s %-6s %s";
			s = String.format(fmt, varName, type, sss);
			log(s);
		}

		return createEmptyRes();
	}

	private List<String> getAllVars(DeliaSession sess) {
		List<String> list = new ArrayList<>();
		if (sess == null || sess.getExecutionContext() == null) {
			return list;
		}
		
		for(String var: sess.getExecutionContext().varMap.keySet()) {
			if (var.equals("$$")) {
				continue;
			}
			list.add(var);
		}
		return list;
	}
}