package org.delia.assoc;

import org.delia.db.schema.DatMapBuilder;
import org.delia.log.Log;
import org.delia.rule.rules.RelationRuleBase;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;

public class PopulateDatIdVisitor implements ManyToManyVisitor {
	private final String defaultSchema;
	private DTypeRegistry registry;
//	private SchemaMigrator schemaMigrator;
	private Log log;
	private DatIdMap datIdMap;
	public int datIdCounter;
	public long maxIdSeen = 0L;
	private boolean haveLoadedSchemaFingerprint = false;
	private DatMapBuilder datMapBuilder;

	public PopulateDatIdVisitor(DatMapBuilder datMapBuilder, DTypeRegistry registry, Log log, String defaultSchema) {
		this.registry = registry;
		this.log = log;
		this.datMapBuilder = datMapBuilder;
		this.defaultSchema = defaultSchema;
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

	public void loadSchemaFingerprintIfNeeded() {
		//only read from DB if there are MM relations.
		if (!haveLoadedSchemaFingerprint) {
			haveLoadedSchemaFingerprint = true;
//			String fingerprint = schemaMigrator.calcDBFingerprint();
//			log.log("DB fingerprint: " + fingerprint);
			this.datIdMap = datMapBuilder.buildDatIdMapFromDBFingerprint(); //buildDatIdMap(fingerprint);
		}
	}
	
//	private DatIdMap buildDatIdMap(String fingerprint) {
//		DatIdMap datMap = new DatIdMap();
//		List<SchemaType> list = schemaMigrator.parseFingerprint(fingerprint);
//		for(SchemaType sctype: list) {
//			List<FieldInfo> fieldInfoL = schemaMigrator.parseFields(sctype);
//			for(FieldInfo ff: fieldInfoL) {
//				DType dtype = registry.getType(ff.type);
//				if (dtype != null && dtype.isStructShape()) {
//					String key = DatIdMapHelper.createKey(sctype.typeName, ff.name);
//					int datId = ff.datId;
//					if (datId != 0) {
//						datMap.put(key, datId);
//						log.log(String.format("DAT map: %s %d", key, datId));
//					}
//				}
//			}
//		}
//		return datMap;
//	}

//	public SchemaMigrator getSchemaMigrator() {
//		return schemaMigrator;
//	}

	public DatIdMap getDatIdMap() {
		return datIdMap;
	}
}