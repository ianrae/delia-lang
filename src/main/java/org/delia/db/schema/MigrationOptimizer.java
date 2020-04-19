package org.delia.db.schema;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBInterface;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;

public class MigrationOptimizer extends ServiceBase {

	public static final String SCHEMA_TABLE = "DELIA_SCHEMA_VERSION";
	private DTypeRegistry registry;
	private DBExecutor dbexecutor;
	private DBAccessContext dbctx;

	public MigrationOptimizer(FactoryService factorySvc, DBInterface dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator) {
		super(factorySvc);
		this.dbctx = new DBAccessContext(registry, new DoNothingVarEvaluator());
		this.dbexecutor = dbInterface.createExector(dbctx);
		this.registry = registry;
	}
	
	public List<SchemaType> optimizeDiffs(List<SchemaType> diffL) {
		diffL = detectTableRename(diffL);
		diffL = detectFieldRename(diffL);
		
		return diffL;
	}
	
	
	private List<SchemaType> detectTableRename(List<SchemaType> diffL) {
		List<SchemaType> newlist = new ArrayList<>();
		List<SchemaType> doomedL = new ArrayList<>();
		for(SchemaType st: diffL) {
			if (st.isTblDelete()) {
				SchemaType stOther = findMatchingTableInsert(diffL, st);
				if (stOther != null) {
					st.action = "R";
					st.newName = stOther.typeName;
					doomedL.add(stOther);
					log.log("migrate: '%s' -> '%s' replace with rename.", st.typeName, stOther.typeName);
				}
				newlist.add(st);
			} else {
				newlist.add(st);
			}
		}
		
		for(SchemaType doomed: doomedL) {
			newlist.remove(doomed);
		}
		
		return newlist;
	}

	public List<SchemaType> detectFieldRename(List<SchemaType> diffL) {
		List<SchemaType> newlist = new ArrayList<>();
		List<SchemaType> doomedL = new ArrayList<>();
		for(SchemaType st: diffL) {
			if (st.isFieldDelete()) {
				SchemaType stOther = findMatchingFieldInsert(diffL, st.typeName, st.field);
				if (stOther != null) {
					FieldInfo f1 = parseFieldInfo(st);
					FieldInfo f2 = parseFieldInfo(stOther);
					if (f1.type.equals(f2.type) && f1.flagStr.equals(f2.flagStr)) {
						st.action = "R";
						st.newName = stOther.field;
						doomedL.add(stOther);
						log.log("migrate: '%s.%s' -> '%s.%s' replace with rename.", st.typeName, st.field, stOther.typeName, st.newName);
					}
				}
				newlist.add(st);
			} else {
				newlist.add(st);
			}
		}
		
		for(SchemaType doomed: doomedL) {
			newlist.remove(doomed);
		}
		
		return newlist;
	}

	private FieldInfo parseFieldInfo(SchemaType st) {
		List<FieldInfo> flist1 = parseFields(st);
		FieldInfo f1 = this.findFieldIn(st.field, flist1);
		return f1;
	}

	private SchemaType findMatchingFieldInsert(List<SchemaType> diffL, String typeName, String field) {
		int count = 0;
		SchemaType match = null;
		for(SchemaType st: diffL) {
			if (st.isFieldInsert() && st.typeName.equals(typeName)) {
				match = st;
				count++;
			}
		}
		
		if (match != null && count == 1) {
			return match;
		}
		return null;
	}
	private SchemaType findMatchingTableInsert(List<SchemaType> diffL, SchemaType st2) {
		int count = 0;
		SchemaType match = null;
		for(SchemaType st: diffL) {
			if (st.isTblInsert()) {
				String suffix = st.line.substring(st.line.indexOf(':'));
				String suffix2 = st2.line.substring(st2.line.indexOf(':'));
				if (suffix.equals(suffix2)) {
					match = st;
					count++;
				}
			}
		}
		
		if (match != null && count == 1) {
			return match;
		}
		return null;
	}

	private FieldInfo findFieldIn(String fieldName, List<FieldInfo> flist2) {
		for(FieldInfo f2: flist2) {
			if (fieldName.equals(f2.name)) {
				return f2;
			}
		}
		return null;
	}

	private List<FieldInfo> parseFields(SchemaType schema1) {
		//Customer:struct:{id:int:P,wid:int:}
		List<FieldInfo> list = new ArrayList<>();
		String s = StringUtils.substringAfter(schema1.line, "{");
		s = StringUtils.substringBeforeLast(s, "}");
		String[] ar = s.split(",");
		for(String ss: ar) {
			FieldInfo finfo = new FieldInfo();
			finfo.name = StringUtils.substringBefore(ss, ":");
			finfo.type = StringUtils.substringBetween(ss, ":", ":");
			finfo.flagStr = StringUtils.substringAfterLast(ss, ":");
			list.add(finfo);
		}
		return list;
	}

	private SchemaType findIn(SchemaType st, List<SchemaType> list2) {
		for(SchemaType schema2: list2) {
			if (st.typeName.equals(schema2.typeName)) {
				return schema2;
			}
		}
		return null;
	}

}