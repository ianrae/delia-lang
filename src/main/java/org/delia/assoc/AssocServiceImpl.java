package org.delia.assoc;

import org.delia.core.FactoryService;
import org.delia.db.schema.SchemaMigrator;
import org.delia.error.ErrorTracker;
import org.delia.log.Log;
import org.delia.type.DTypeRegistry;

public class AssocServiceImpl implements AssocService {

	private Log log;
	private ErrorTracker et;
	private FactoryService factorySvc;
	private DatIdMap datIdMap;
	private SchemaMigrator schemaMigrator;

	public AssocServiceImpl(SchemaMigrator schemaMigrator, FactoryService factorySvc, ErrorTracker et) {
		this.factorySvc = factorySvc;
		this.log = factorySvc.getLog();
		this.et = et;
		this.schemaMigrator = schemaMigrator;
	}
	
	@Override
	public void assignDATIds(DTypeRegistry registry) {
		PopulateDatIdVisitor visitor = new PopulateDatIdVisitor(schemaMigrator, registry, log);
		ManyToManyEnumerator enumerator = new ManyToManyEnumerator();
		enumerator.visitTypes(registry, visitor);
		int numLoaded = visitor.datIdCounter;
		datIdMap = visitor.getDatIdMap();
		if (datIdMap == null) {
			datIdMap = new DatIdMap();
		}

		visitor.loadSchemaFingerprintIfNeeded(); //force loading of fingerprint and loading datIdMap

		CreateNewDatIdVisitor newIdVisitor = new CreateNewDatIdVisitor(factorySvc, schemaMigrator.getRawExecutor(), registry, log, datIdMap);
		//since types of fields may have been deleted, we can't trust the registry
		//to visit all types needed for schema migration.
		newIdVisitor.initTableNameCreatorIfNeeded(); //explicitly load every time.

		if (schemaMigrator == null) {
			log.log("DAT ids: %d loaded, %d added", 0, 0);
			return; //there are no many-to-many types
		}
		enumerator = new ManyToManyEnumerator();
		enumerator.visitTypes(registry, newIdVisitor);
		int numAdded = newIdVisitor.datIdCounter;

		log.log("DAT ids: %d loaded, %d added", numLoaded, numAdded);
		if (datIdMap.size() != numLoaded + numAdded) {
			log.logError("DAT ERROR: datIdMap size: %d -- something's wrong", datIdMap.size());
		}
	}
	
	private void sdfsdf() {
		//read schema fingerprint
        //parse to get datIds	B
		 //build map<customer.addr, datId>
		//for each struct type
		//assign dat values from B
		  //set relinfo and relinfo.otherSide
		//for each struct type (again)
		//if dat is 0 then insert row and store returned id (serial)
		  //set relinfo and relinfo.otherSide
		
		//TODO: find a way for schema migrator to not have to re-query for fingerprint

		//are doing this every time delia.beginexecution
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

