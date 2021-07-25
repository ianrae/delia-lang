package org.delia.assoc;

import org.delia.core.FactoryService;
import org.delia.db.schema.DatMapBuilder;
import org.delia.db.schema.SchemaMigrator;
import org.delia.error.ErrorTracker;
import org.delia.log.Log;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.DBExecutor;

public class AssocServiceImpl implements AssocService {

	private final String defaultSchema;
	private Log log;
	private ErrorTracker et;
	private FactoryService factorySvc;
	private DatIdMap datIdMap;
//	private SchemaMigrator schemaMigrator;
	private DatMapBuilder datMapBuilder;
	private DBExecutor zexec;

	public AssocServiceImpl(SchemaMigrator schemaMigrator, DatMapBuilder datMapBuilder, FactoryService factorySvc, ErrorTracker et) {
		this.factorySvc = factorySvc;
		this.log = factorySvc.getLog();
		this.et = et;
//		this.schemaMigrator = schemaMigrator;
		if (schemaMigrator != null) {
			this.datMapBuilder = datMapBuilder;
			this.zexec = schemaMigrator.getZDBExecutor();
		}
		this.defaultSchema = schemaMigrator.getDefaultSchema();
	}
	
	@Override
	public void assignDATIds(DTypeRegistry registry) {
		PopulateDatIdVisitor visitor = new PopulateDatIdVisitor(datMapBuilder, registry, log, defaultSchema);
		ManyToManyEnumerator enumerator = new ManyToManyEnumerator();
		enumerator.visitTypes(registry, visitor);
		int numLoaded = visitor.datIdCounter;
		datIdMap = visitor.getDatIdMap();
		if (datIdMap == null) {
			datIdMap = new DatIdMap();
		}

		visitor.loadSchemaFingerprintIfNeeded(); //force loading of fingerprint and loading datIdMap

		CreateNewDatIdVisitor newIdVisitor = new CreateNewDatIdVisitor(factorySvc, zexec, registry, log, datIdMap);
		//since types of fields may have been deleted, we can't trust the registry
		//to visit all types needed for schema migration.
		newIdVisitor.initTableNameCreatorIfNeeded(); //explicitly load every time.

		if (datMapBuilder == null) {
			log.log("DAT ids: %d loaded, %d added", 0, 0);
			return; //there are no many-to-many types
		}
		enumerator = new ManyToManyEnumerator();
		enumerator.visitTypes(registry, newIdVisitor);
		int numAdded = newIdVisitor.datIdCounter;

		log.log("DAT ids: %d loaded, %d added", numLoaded, numAdded);
		int numActualRelations = datIdMap.getNumUniqueDatIds();
		if (numActualRelations != numLoaded + numAdded) {
			log.logError("DAT ERROR: datIdMap size: %d -- something's wrong", datIdMap.size());
		}
	}
	
	
	@Override
	public String getAssocTblName(int datId) {
		return datIdMap.getAssocTblName(datId);
	}

	@Override
	public DatIdMap getDatIdMap() {
		return datIdMap;
	}
}

