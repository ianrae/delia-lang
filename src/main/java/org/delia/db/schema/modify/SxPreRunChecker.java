package org.delia.db.schema.modify;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.hld.HLDSimpleQueryService;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

public class SxPreRunChecker extends ServiceBase {

	private DTypeRegistry registry;
	private DBExecutor zexec;

	public SxPreRunChecker(FactoryService factorySvc, DBInterfaceFactory dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap) {
		super(factorySvc);
		this.zexec = dbInterface.createExecutor();
		this.registry = registry;
		
		//init zdb (but datIdMap will be null in early phase of startup)
		zexec.init1(registry);
		if (datIdMap != null) {
			zexec.init2(datIdMap, varEvaluator);
		} else if (! DBType.MEM.equals(dbInterface.getDBType())){
			//hack. need some sort of DatIdMap for H2 and PG
			zexec.init2(new DatIdMap(), varEvaluator);
		}
	}
	

	public boolean preRunCheck(SchemaDelta delta, boolean doLowRiskChecks) {
		int failCount = 0;
		for(SxTypeDelta td: delta.typesI) {
			if (registry.findTypeOrSchemaVersionType(td.typeName) == null) {
				log.logError("error: create-table for unknown type '%s'. ", td.typeName);
				failCount++;
			}
		}
		
		for(SxTypeDelta td: delta.typesD) {
			if (! doSoftDeletePreRunCheck(td.typeName)) {
				failCount++;
			}
		}
		
		for(SxTypeDelta td: delta.typesU) {
			for(SxFieldDelta fd: td.fldsI) {
				//Note. we don't need this check for MEM db
				if (doLowRiskChecks && (! fd.info.flgs.contains("O") && ! isMemDB())) { //mandatory field?
					QueryBuilderService queryBuilder = this.factorySvc.getQueryBuilderService();
					QueryExp exp = queryBuilder.createCountQuery(td.typeName);
					HLDSimpleQueryService querySvc = factorySvc.createHLDSimpleQueryService(zexec.getDbInterface(), registry);
					QueryResponse qresp = querySvc.execQuery(exp, zexec);
					DValue dval = qresp.getOne();
					long numRecords = dval.asLong();
					if (numRecords > 0) {
						//records exist so we can't add new mandatory field because
						//existing records won't have a value.
						log.logError("error: adding mandatory field '%s.%s' ", td.typeName, fd.fieldName);
						failCount++;
					}
				}
			
				for(SxFieldDelta fd2: td.fldsD) {
					if (! doSoftFieldDeletePreRunCheck(td.typeName, fd2.fieldName)) {
						failCount++;
					}
				}

				for(SxFieldDelta fd2: td.fldsU) {
					if (fd2.tDelta != null) {
						//TODO: what checks here?
					} else if (fd2.flgsDelta != null) {
						if (fd2.flgsDelta.contains("P")) {
							log.logError("error: not allowed to add/remove primaryKey '%s.%s' ", td.typeName, fd2.fieldName);
							failCount++;
						}
						if (fd2.flgsDelta.contains("S")) {
							log.logError("error: not allowed to add/remove serial '%s.%s' ", td.typeName, fd2.fieldName);
							failCount++;
						}
					} else if (fd2.szDelta != null) {
						Integer old = fd2.info.sz;
						Integer newSize = fd2.szDelta;
						if (newSize < old) {
							boolean canIgnore = (newSize == 0 && old == 32);
							if (!canIgnore) {
								log.logError("error: sizeof being decreased. may cause data loss '%s.%s' ", td.typeName, fd2.fieldName);
								failCount++;
							}
						}
					}
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

}