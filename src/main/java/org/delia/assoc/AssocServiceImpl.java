package org.delia.assoc;

import org.delia.db.DBInterface;
import org.delia.error.ErrorTracker;
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public class AssocServiceImpl implements AssocService {

	private Log log;
	private ErrorTracker et;
	private DBInterface dbInterface;
	
	public AssocServiceImpl(Log log, ErrorTracker et, DBInterface dbInterface) {
		this.log = log;
		this.et = et;
		this.dbInterface = dbInterface;
	}
	@Override
	public int assignDATIds(DTypeRegistry registry) {
		int numAdded = 0;
		for(String typeName: registry.getAll()) {
			DType dtype = registry.getType(typeName);
			if (! dtype.isStructShape()) {
				continue;
			}
			DStructType structType = (DStructType) dtype;
			numAdded += assignInTypeIfNeeded(structType);
		}
		return numAdded;
	}
	private int assignInTypeIfNeeded(DStructType structType) {
		int numAdded = 0;
		for(DRule rule: structType.getRawRules()) {
			if (rule instanceof RelationOneRule) {
				RelationOneRule rr = (RelationOneRule) rule;
				numAdded += assignInTypeIfNeeded(rr.relInfo);
			} else if (rule instanceof RelationManyRule) {
				RelationManyRule rr = (RelationManyRule) rule;
				numAdded += assignInTypeIfNeeded(rr.relInfo);
			}
		}
		return numAdded;
	}

	private int assignInTypeIfNeeded(RelationInfo relInfo) {
		if (relInfo.isManyToMany()) {
			if (relInfo.getDatId() == null || relInfo.getDatId() == 0) {
				int datId = 7; //assign new one
				relInfo.forceDatId(datId);
				return 1;
			}
		}
		return 0;
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
		
		//HLSQueryStatement hls = null; //build this		
		
	}
}

