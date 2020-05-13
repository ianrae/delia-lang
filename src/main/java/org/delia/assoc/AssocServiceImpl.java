package org.delia.assoc;

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
	
	
	public AssocServiceImpl(Log log, ErrorTracker et) {
		this.log = log;
		this.et = et;
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
		}
		return 0;
	}
}

