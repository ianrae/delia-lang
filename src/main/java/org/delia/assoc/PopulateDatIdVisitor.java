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
	private Map<String,Integer> dataIdMap;
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
		String key = createKey(structType.getName(), rr.relInfo.fieldName);
		Integer datId = dataIdMap.get(key);
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
			this.dataIdMap = buildDatIdMap(fingerprint);
		}
	}
	
	private Map<String,Integer> buildDatIdMap(String fingerprint) {
		Map<String,Integer> datMap = new HashMap<>();
		List<SchemaType> list = schemaMigrator.parseFingerprint(fingerprint);
		for(SchemaType sctype: list) {
			List<FieldInfo> fieldInfoL = schemaMigrator.parseFields(sctype);
			for(FieldInfo ff: fieldInfoL) {
				DType dtype = registry.getType(ff.type);
				if (dtype != null && dtype.isStructShape()) {
					String key = createKey(sctype.typeName, ff.name);
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
	private String createKey(String typeName, String fieldName) {
		String key = String.format("%s.%s", typeName, fieldName);
		return key;
	}

	public SchemaMigrator getSchemaMigrator() {
		return schemaMigrator;
	}
}