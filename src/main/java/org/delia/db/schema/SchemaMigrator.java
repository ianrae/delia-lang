package org.delia.db.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBHelper;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.QuerySpec;
import org.delia.hld.HLDSimpleQueryService;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.sort.topo.DeliaTypeSorter;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.typebuilder.InternalTypeCreator;
import org.delia.util.StringUtil;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

public class SchemaMigrator extends ServiceBase implements AutoCloseable {
	public static boolean useNewSchemaGen = false;
	
	public static final String SCHEMA_TABLE = "DELIA_SCHEMA_VERSION";
	public static final String DAT_TABLE = "DELIA_ASSOC"; //DAT = Delia Assoc Table

	private DTypeRegistry registry;
	private SchemaFingerprintGenerator fingerprintGenerator;
	private String currentFingerprint;
	private String dbFingerprint;
	private DBExecutor zexec;
	private MigrationRunner migrationRunner;
	private MigrationOptimizer optimizer;

	public SchemaMigrator(FactoryService factorySvc, DBInterfaceFactory dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap) {
		super(factorySvc);
		this.zexec = dbInterface.createExecutor();
		this.registry = registry;
		this.fingerprintGenerator = new SchemaFingerprintGenerator();
		
		//init zdb (but datIdMap will be null in early phase of startup)
		zexec.init1(registry);
		if (datIdMap != null) {
			zexec.init2(datIdMap, varEvaluator);
		} else if (! DBType.MEM.equals(dbInterface.getDBType())){
			//hack. need some sort of DatIdMap for H2 and PG
			zexec.init2(new DatIdMap(), varEvaluator);
		}

		InternalTypeCreator fakeCreator = new InternalTypeCreator();
		DStructType dtype = fakeCreator.createSchemaVersionType(registry, SCHEMA_TABLE);
		registry.setSchemaVersionType(dtype);
		DStructType datType = fakeCreator.createDATType(registry, DAT_TABLE);
		registry.setDATType(datType);
		this.migrationRunner = new MigrationRunner(factorySvc, registry, zexec, dbInterface);
		this.optimizer = new MigrationOptimizer(factorySvc, registry, dbInterface.getDBType());
	}
	
	@Override
	public void close() {
		try {
			zexec.close();
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
	}

	public boolean createSchemaTableIfNeeded() {
//		SchemaContext ctx = new SchemaContext();
		if (!zexec.rawTableDetect(SCHEMA_TABLE)) {
			zexec.rawCreateTable(SCHEMA_TABLE);
		}
		
		if (!zexec.rawTableDetect(DAT_TABLE)) {
			zexec.rawCreateTable(DAT_TABLE);
		}
		
		return true;
	}

	public boolean dbNeedsMigration() {
		this.currentFingerprint = fingerprintGenerator.createFingerprint(registry);
		this.dbFingerprint = calcDBFingerprint();

		return !currentFingerprint.equals(dbFingerprint);
	}
	/**
	 * dbNeedsMigration MUST have been called before this.
	 * @param doLowRiskChecks whether to do additional pre-migration checks
	 * @return true if successful
	 */
	public boolean performMigrations(boolean doLowRiskChecks) {
		//TODO add new schem here if needed
		if (useNewSchemaGen) {
			//need dbFingerprint
			//calc delta, optimize, gen ops, migrationRunner ops
			//wait: make migrationsvc into interface and make two impls
			//update associmpl, and popDatIdVisitor to use either impl too
		}
		
		List<SchemaType> list = parseFingerprint(dbFingerprint);
		List<SchemaType> list2 = parseFingerprint(currentFingerprint);
		
		List<SchemaType> diffL =  calcDiff(list, list2);
		diffL = optimizer.optimizeDiffs(diffL);
		MigrationPlan plan = new MigrationPlan();
		plan.diffL = diffL;
		boolean b = performMigrations(plan, doLowRiskChecks);
		return b;
	}
	
	private FieldInfo parseFieldInfo(SchemaType st) {
		List<FieldInfo> flist1 = parseFields(st);
		FieldInfo f1 = this.findFieldIn(st.field, flist1);
		return f1;
	}

	/**
	 * dbNeedsMigration MUST have been called before this.
	 * @return migration plan
	 */
	public MigrationPlan generateMigrationPlan() {
		List<SchemaType> list = parseFingerprint(dbFingerprint);
		List<SchemaType> list2 = parseFingerprint(currentFingerprint);
		
		MigrationPlan plan = new MigrationPlan();
		plan.diffL =  calcDiff(list, list2);
		return plan;
	}
	/**
	 * dbNeedsMigration MUST have been called before this.
	 * @param plan migration plan
	 */
	public MigrationPlan runMigrationPlan(MigrationPlan plan) {
		boolean b = performMigrations(plan, false);
		plan.runResultFlag = b;
		return plan;
	}
	

	public String calcDBFingerprint() {
		//TODO: query just single record (most recent);
		FilterExp filter = new FilterExp(99, new BooleanExp(true)); //query all
		QuerySpec spec = new QuerySpec();
		spec.queryExp = new QueryExp(99, new IdentExp(SCHEMA_TABLE), filter, null);
		HLDSimpleQueryService querySvc = factorySvc.createHLDSimpleQueryService(zexec.getDbInterface(), registry);
		QueryResponse qresp = querySvc.execQuery(spec.queryExp, zexec);
		//TODO: should specify orderby id!!
		
		if (qresp.emptyResults()) {
			return "";
		}

		//there may be multiple rows
		int n = qresp.dvalList.size();
		DValue dval = qresp.dvalList.get(n - 1); //last one
		return dval.asStruct().getField("fingerprint").asString();
	}

	//Customer:struct:{id:int::O,firstName:string::,lastName:string:O:,points:int:O:}\n
	public List<SchemaType> parseFingerprint(String fingerprint) {
		parseVersion(fingerprint);
		//Note v2 is backwards compatible with v1 so we don't need any extra code here
		
		fingerprint = StringUtils.substringAfter(fingerprint, ")");
		String ar[] = fingerprint.split("\n");
		List<SchemaType> list = new ArrayList<>();
		for(String line: ar) {
			if (line.trim().isEmpty()) {
				continue;
			}
			SchemaType schemaType = new SchemaType();
			schemaType.line = line;
			schemaType.typeName = StringUtils.substringBefore(line, ":");
			list.add(schemaType);
		}
		return list;
	}

	private int parseVersion(String fingerprint) {
		if (fingerprint.isEmpty()) {
			return 0;
		}
		String s = StringUtils.substringBefore(fingerprint, ")");
		s = StringUtils.substringAfter(s, "(v");
		Integer version = Integer.parseInt(s);
		log.log("schema-version:%d", version);
		return version;
	}

	public String getCurrentFingerprint() {
		return currentFingerprint;
	}

	public String getDbFingerprint() {
		return dbFingerprint;
	}

	public List<SchemaType> calcDiff(List<SchemaType> list1, List<SchemaType> list2Param) {
		List<SchemaType> diffList = new ArrayList<>();
		List<SchemaType> list2 = new ArrayList<>(list2Param);
		for(SchemaType schema1: list1) {
			SchemaType sc2 = findIn(schema1, list2);
			if (sc2 != null) {
				diffFields(schema1, sc2, diffList);
				list2.remove(sc2);
			} else {
				schema1.action = "D"; //in list1 but not in list2
				diffList.add(schema1);
			}
		}

		for(SchemaType schema2: list2) {
			schema2.action = "I"; //in list2 but not in list1
			diffList.add(schema2);
		}

		return diffList;
	}

	private void diffFields(SchemaType st1, SchemaType st2, List<SchemaType> diffList) {
		List<FieldInfo> flist1 = parseFields(st1);
		List<FieldInfo> flist2 = parseFields(st2);
		List<FieldInfo> list2 = new ArrayList<>(flist2);
		
		//TODO support rename
		//add fieldinfo.fieldIndex - index of field within dtype
		//if I and D have same fieldIndex and same type and options then do a rename

		//I/U/D/R/A/AT/ASZ/ASN
		for(FieldInfo finfo: flist1) {
			FieldInfo f2 = findFieldIn(finfo, flist2);
			if (f2 != null) {
				if (!finfo.type.equals(f2.type)) {
					SchemaType st = new SchemaType();
					st.typeName = st1.typeName;
					st.line = st1.line;
					st.action = "AT"; //alter type
					st.field = finfo.name;
					st.newName = f2.type; //new field type
					diffList.add(st);
				} else {
					String sizeofStr = calcSizeofString(finfo, f2);
					if (sizeofStr != null) {
						SchemaType st = new SchemaType();
						st.typeName = st1.typeName;
						st.line = st1.line;
						if (f2.type.contains(":string")) { //TODO support types derived from string such as Name
							st.action = "ASZ"; //alter size (string)
						} else {
							st.action = "ASN"; //alter size (int)
						}
						st.field = finfo.name;
						st.newName = sizeofStr;
						st.sizeof = f2.sizeof;
						diffList.add(st);
					}
				}
				
				String deltaFlags = calcDeltaFlags(finfo, f2);
				if (!StringUtil.isNullOrEmpty(deltaFlags)) {
					SchemaType st = new SchemaType();
					st.typeName = st1.typeName;
					st.line = st1.line;
					st.action = "A"; //alter
					st.field = finfo.name;
					st.newName = deltaFlags; //+O,-U,...
					diffList.add(st);
				}
				
				list2.remove(f2);
			} else {
				SchemaType st = new SchemaType();
				st.typeName = st1.typeName;
				st.line = st1.line;
				st.action = "D";
				st.field = finfo.name;
				st.datId = finfo.datId;
				diffList.add(st);
			}
		}

		for(FieldInfo f2: list2) {
			//in list2 but not in list1
			SchemaType st = new SchemaType();
			st.typeName = st2.typeName;
			st.line = st2.line;
			st.action = "I";
			st.field = f2.name;
			st.sizeof = f2.sizeof;
			diffList.add(st);
		}
	}

	private String calcSizeofString(FieldInfo finfo, FieldInfo f2) {
		if (finfo.sizeof == f2.sizeof) {
			return null;
		}
		
		return String.format("%d/%d", finfo.sizeof, f2.sizeof); // old/new size values
	}

	/** generate a string of modifier changes.
	 *  +O means adding optional
	 *  -O means removing optional
	 *  -also +U,-U,+P,-P,+S,-S
	 */
	private String calcDeltaFlags(FieldInfo finfo, FieldInfo f2) {
		StringJoiner joiner = new StringJoiner(",");
		for(int i = 0; i < finfo.flagStr.length(); i++) {
			char ch = finfo.flagStr.charAt(i);
			if (f2.flagStr.indexOf(ch) < 0) { //in f1 and not in f2?
				joiner.add(String.format("-%c", ch));
			}
		}		
		
		for(int i = 0; i < f2.flagStr.length(); i++) {
			char ch = f2.flagStr.charAt(i);
			if (finfo.flagStr.indexOf(ch) < 0) { //not in f1 and in f2?
				joiner.add(String.format("+%c", ch));
			}
		}		
		
		return joiner.toString();
	}

	private FieldInfo findFieldIn(FieldInfo target, List<FieldInfo> flist2) {
		for(FieldInfo f2: flist2) {
			if (target.name.equals(f2.name)) {
				return f2;
			}
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

	public List<FieldInfo> parseFields(SchemaType schema1) {
		//Customer:struct:{id:int:P,wid:int:}
		List<FieldInfo> list = new ArrayList<>();
		String s = StringUtils.substringAfter(schema1.line, "{");
		s = StringUtils.substringBeforeLast(s, "}");
		String strrules = StringUtils.substringAfterLast(s, "|");
		s = StringUtils.substringBeforeLast(s, "|");
		
		String[] ar = s.split(",");
		for(String ss: ar) {
			FieldInfo finfo = new FieldInfo();
			finfo.name = StringUtils.substringBefore(ss, ":");
			finfo.type = StringUtils.substringBetween(ss, ":", ":");
			finfo.type = calcSizeofIfAny(ss, finfo);
			String tmp = StringUtils.substringAfterLast(ss, ":");
			finfo.flagStr = StringUtils.substringBefore(tmp, "/");
			tmp = StringUtils.substringAfterLast(ss, "/");
			finfo.datId = Integer.parseInt(tmp);
			list.add(finfo);
		}
		
		if (! strrules.isEmpty()) {
			ar = strrules.split(";");
			for(String ss: ar) {
				FieldInfo finfo = new FieldInfo();
				finfo.isField = false;
				finfo.name = ss; //StringUtils.substringBefore(ss, "(");
				finfo.type = StringUtils.substringBefore(ss, "(");
				finfo.ruleStr = StringUtils.substringBetween(ss, "(", ")");
				list.add(finfo);
			}
		}
		
		return list;
	}

	private String calcSizeofIfAny(String ss, FieldInfo finfo) {
		String s = StringUtils.substringAfter(ss, "(");
		if (StringUtils.isEmpty(s)) {
			return finfo.type; //no sizeof
		}
		
		String s2 = StringUtils.substringBefore(s, ")");
		finfo.sizeof = Integer.parseInt(s2);
		return StringUtils.substringBefore(ss, "("); //strip of (8)
	}

	private SchemaType findIn(SchemaType st, List<SchemaType> list2) {
		for(SchemaType schema2: list2) {
			if (st.typeName.equals(schema2.typeName)) {
				return schema2;
			}
		}
		return null;
	}

	public boolean performMigrations(MigrationPlan plan, boolean doLowRiskChecks) {
		//create types in correct dependency order
		DeliaTypeSorter typeSorter = new DeliaTypeSorter();
		List<String> orderL = typeSorter.topoSort(registry);
		
		if (! preRunCheck(plan.diffL, orderL, doLowRiskChecks)) {
			log.logError("migration halted due to pre-migration check errors. No schema changes were made.");
			return false;
		}
		
		return migrationRunner.performMigrations(currentFingerprint, plan, orderL);
	}

	private boolean preRunCheck(List<SchemaType> diffL, List<String> orderL, boolean doLowRiskChecks) {
		int failCount = 0;
		for(SchemaType st: diffL) {
			if (st.isTblInsert()) {
				if (registry.findTypeOrSchemaVersionType(st.typeName) == null) {
					log.logError("error: create-table for unknown type '%s'. ", st.typeName);
					failCount++;
				}
			}
			
			if (st.isTblDelete()) {
				if (! doSoftDeletePreRunCheck(st.typeName)) {
					failCount++;
				}
			} else if (st.isFieldInsert()) {
				FieldInfo f1 = parseFieldInfo(st);
				//Note. we don't need this check for MEM db
				if (doLowRiskChecks && ! f1.flagStr.contains("O") && ! isMemDB()) { //mandatory field?
					QueryBuilderService queryBuilder = this.factorySvc.getQueryBuilderService();
					QueryExp exp = queryBuilder.createCountQuery(st.typeName);
					HLDSimpleQueryService querySvc = factorySvc.createHLDSimpleQueryService(zexec.getDbInterface(), registry);
					QueryResponse qresp = querySvc.execQuery(exp, zexec);
					DValue dval = qresp.getOne();
					long numRecords = dval.asLong();
					if (numRecords > 0) {
						//records exist so we can't add new mandatory field because
						//existing records won't have a value.
						log.logError("error: adding mandatory field '%s.%s' ", st.typeName, st.field);
						failCount++;
					}
				}
			} else if (st.isFieldDelete()) {
				if (! doSoftFieldDeletePreRunCheck(st.typeName, st.field)) {
					failCount++;
				}
			} else if (st.isFieldAlterType()) {
				//TODO: what checks here?
			} else if (st.isFieldAlter()) {
				if (st.newName.contains("P")) {
					log.logError("error: not allowed to add/remove primaryKey '%s.%s' ", st.typeName, st.field);
					failCount++;
				}
				if (st.newName.contains("S")) {
					log.logError("error: not allowed to add/remove serial '%s.%s' ", st.typeName, st.field);
					failCount++;
				}
			} else if (st.isFieldAlterSizeInt()) {
				Integer old = Integer.parseInt(StringUtils.substringBefore(st.newName, "/"));
				Integer newSize = Integer.parseInt(StringUtils.substringAfter(st.newName, "/"));
				if (newSize < old) {
					boolean canIgnore = (newSize == 0 && old == 32);
					if (!canIgnore) {
						log.logError("error: sizeof int being decreased. may cause data loss '%s.%s' ", st.typeName, st.field);
						failCount++;
					}
				}
			} else if (st.isFieldAlterSizeString()) {
				Integer old = Integer.parseInt(StringUtils.substringBefore(st.newName, "/"));
				Integer newSize = Integer.parseInt(StringUtils.substringAfter(st.newName, "/"));
				if (newSize < old) {
					log.logError("error: sizeof string being decreased. may cause data loss '%s.%s' ", st.typeName, st.field);
					failCount++;
				}
			}
		}

		return failCount == 0;
	}

	private boolean isMemDB() {
		//return rawExecutor instanceof MemRawDBExecutor;
		return DBType.MEM.equals(zexec.getDbInterface().getDBType());
	}

	private boolean doSoftDeletePreRunCheck(String typeName) {
		String backupName = String.format("%s__BAK", typeName);
		if (zexec.rawTableDetect(backupName)) {
			log.logError("Backup table '%s' already exists. You must delete this table first before running migration.", backupName);
			return false;
		}
		return true;
	}
	private boolean doSoftFieldDeletePreRunCheck(String typeName, String fieldName) {
		String backupName = String.format("%s__BAK", fieldName);
		if (zexec.rawFieldDetect(typeName, backupName)) {
			log.logError("Backup field '%s.%s' already exists. You must delete this field first before running migration.", typeName, backupName);
			return false;
		}
		return true;
	}

//	public RawDBExecutor getRawExecutor() {
//		return rawExecutor;
//	}
	public DBExecutor getZDBExecutor() {
		return zexec;
	}

}