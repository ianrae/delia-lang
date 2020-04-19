package org.delia.repl.migration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.schema.SchemaType;

public class MigrationParser extends ServiceBase {
	private int failCount;
	private String currentName;

	public MigrationParser(FactoryService factorySvc) {
		super(factorySvc);
	}

	public boolean parse(List<String> lines, List<MigrationStep> list) {
		failCount = 0;
		list.clear();
		for(String line: lines) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			if (matches(line, "create-table")) {
				String arg1 = parseArg1(line);
				failIfHasDot(arg1);
				list.add(new MigrationStep(currentName, arg1));
			} else if (matches(line, "delete-table")) {
				String arg1 = parseArg1(line);
				failIfHasDot(arg1);
				list.add(new MigrationStep(currentName, arg1));
			} else if (matches(line, "rename-table")) {
				String arg1 = parseArg1(line);
				String arg2 = parseArg2(line);
				failIfHasDot(arg1);
				failIfHasDot(arg2);
				list.add(new MigrationStep(currentName, arg1, arg2));
			} else if (matches(line, "add-field")) {
				String arg1 = parseArg1(line);
				failIfNoDot(arg1);
				list.add(new MigrationStep(currentName, arg1));
			} else if (matches(line, "delete-field")) {
				String arg1 = parseArg1(line);
				failIfNoDot(arg1);
				list.add(new MigrationStep(currentName, arg1));
			} else if (matches(line, "rename-field")) {
				String arg1 = parseArg1(line);
				String arg2 = parseArg2(line);
				failIfNoDot(arg1);
				failIfHasDot(arg2);
				list.add(new MigrationStep(currentName, arg1, arg2));
			} else if (matches(line, "alter-field")) {
				String arg1 = parseArg1(line);
				failIfNoDot(arg1);
				list.add(new MigrationStep(currentName, arg1));
			} else {
				failCount++;
			}
		}

		return failCount == 0;
	}
	
	public List<SchemaType> convertToSchemaType(List<MigrationStep> steps) {
		List<SchemaType> list = new ArrayList<>();
		for(MigrationStep step: steps) {
			SchemaType st = buildST(step);
			list.add(st);
		}
		return list;
	}
	
	private SchemaType buildST(MigrationStep step) {
		SchemaType st = new SchemaType();
		switch(step.name) {
		case "create-table":
			st.action = "I";
			st.typeName = step.arg1;
			break;
		case "delete-table":
			st.action = "D";
			st.typeName = step.arg1;
			break;
		case "rename-table":
			st.action = "U";
			st.typeName = step.arg1;
			st.newName = step.arg2;
			break;
		case "add-field":
			st.action = "I";
			st.typeName = firstPart(step.arg1);
			st.field = secondPart(step.arg1);
			break;
		case "delete-field":
			st.action = "D";
			st.typeName = firstPart(step.arg1);
			st.field = secondPart(step.arg1);
			break;
		case "rename-field":
			st.action = "U";
			st.typeName = firstPart(step.arg1);
			st.field = secondPart(step.arg1);
			st.newName = step.arg2;
		case "alter-field":
			st.action = "U";
			st.typeName = firstPart(step.arg1);
			st.field = secondPart(step.arg1);
			break;
		}
		return st;
	}

	private String firstPart(String arg1) {
		return StringUtils.substringBefore(arg1, ".");
	}
	private String secondPart(String arg1) {
		return StringUtils.substringAfter(arg1, ".");
	}


	private void failIfHasDot(String arg1) {
		if (arg1 != null && arg1.contains(".")) {
			failCount++;
		}
	}
	private void failIfNoDot(String arg1) {
		if (arg1 != null && !arg1.contains(".")) {
			failCount++;
		}
	}

	private boolean matches(String line, String name) {
		if (line.startsWith(name)) {
			this.currentName = name;
			return true;
		}
		return false;
	}

	private String parseArg1(String line) {
		int pos = line.indexOf(' ');
		if (pos < 0) {
			failCount++;
			return null;
		}
		String s = line.substring(pos);
		s = s.trim();
		pos = s.indexOf(' ');
		if (pos > 0) {
			s = s.substring(0, pos);
		}
		return s.trim();
	}
	
	private String parseArg2(String line) {
		int pos = line.indexOf(' ');
		if (pos < 0) {
			return null;
		}
		String s = line.substring(pos);
		s = s.trim();
		pos = s.indexOf(' ');
		if (pos < 0) {
			failCount++;
			return null;
		}
		if (pos > 0) {
			s = s.substring(pos);
		}
		return s.trim();
	}

	public int getFailCount() {
		return failCount;
	}

}