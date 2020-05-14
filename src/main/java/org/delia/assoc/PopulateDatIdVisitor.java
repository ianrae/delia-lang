package org.delia.assoc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.DBInterface;
import org.delia.db.schema.FieldInfo;
import org.delia.db.schema.SchemaMigrator;
import org.delia.db.schema.SchemaType;
import org.delia.log.Log;
import org.delia.rule.rules.RelationRuleBase;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public class PopulateDatIdVisitor implements ManyToManyVisitor {
	private FactoryService factorySvc;
	private DBInterface dbInterface;
	private DTypeRegistry registry;
	private SchemaMigrator schemaMigrator;
	private Log log;
	private DatIdMap datIdMap;
//	private Map<String,Integer> datIdMap;
	public int datIdCounter;
	public long maxIdSeen = 0L;

	public PopulateDatIdVisitor(FactoryService factorySvc, DBInterface dbInterface, DTypeRegistry registry, Log log) {
		this.factorySvc = factorySvc;
		this.dbInterface = dbInterface;
		this.registry = registry;
		this.log = log;
	}
	
	@Override
	public void visit(DStructType structType, RelationRuleBase rr) {
		if (rr.relInfo.getDatId() != null) {
			return;
		}
		
		loadSchemaFingerprintIfNeeded();
		String key = DatIdMapHelper.createKey(structType.getName(), rr.relInfo.fieldName);
		Integer datId = datIdMap.get(key);
		if (datId != null) {  //will be null for new types
			rr.relInfo.forceDatId(datId);
			rr.relInfo.otherSide.forceDatId(datId);
			datIdCounter++;
			if (datId > maxIdSeen) {
				maxIdSeen = datId;
			}
		}
	}

	private void loadSchemaFingerprintIfNeeded() {
		//only read from DB if there are MM relations.
		if (schemaMigrator == null) {
			schemaMigrator = new SchemaMigrator(factorySvc, dbInterface, registry, new DoNothingVarEvaluator());
			schemaMigrator.createSchemaTableIfNeeded();
			String fingerprint = schemaMigrator.calcDBFingerprint();
			log.log("DB fingerprint: " + fingerprint);
			this.datIdMap = buildDatIdMap(fingerprint);
		}
	}
	
	private DatIdMap buildDatIdMap(String fingerprint) {
		DatIdMap datMap = new DatIdMap();
		List<SchemaType> list = schemaMigrator.parseFingerprint(fingerprint);
		for(SchemaType sctype: list) {
			List<FieldInfo> fieldInfoL = schemaMigrator.parseFields(sctype);
			for(FieldInfo ff: fieldInfoL) {
				DType dtype = registry.getType(ff.type);
				if (dtype != null && dtype.isStructShape()) {
					String key = DatIdMapHelper.createKey(sctype.typeName, ff.name);
					int datId = ff.datId;
					if (datId != 0) {
						datMap.put(key, datId);
						log.log(String.format("DAT map: %s %d", key, datId));
					}
				}
			}
		}
		return datMap;
	}

	public SchemaMigrator getSchemaMigrator() {
		return schemaMigrator;
	}

	public DatIdMap getDatIdMap() {
		return datIdMap;
	}
}